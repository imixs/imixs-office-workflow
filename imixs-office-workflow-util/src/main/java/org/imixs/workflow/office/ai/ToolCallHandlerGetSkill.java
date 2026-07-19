/****************************************************************************
 * Copyright (c) 2022-2025 Imixs Software Solutions GmbH and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0-or-later
 ****************************************************************************/
package org.imixs.workflow.office.ai;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.imixs.ai.tools.ImixsAIToolCallEvent;
import org.imixs.ai.tools.ImixsAIToolRegistrationEvent;
import org.imixs.ai.tools.ToolCallHandler;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.engine.DocumentService;

import jakarta.annotation.Priority;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.interceptor.Interceptor;

/**
 * Handles the "get_skill" tool call.
 * 
 * Loads the full content of a skill directly by its $uniqueid. The $uniqueid is
 * obtained from a prior find_skill call.
 */
@Named
public class ToolCallHandlerGetSkill implements ToolCallHandler, Serializable {

    public static final String TOOL_GET_SKILL = "get_skill";

    private static final Logger logger = Logger.getLogger(ToolCallHandlerGetSkill.class.getName());

    @Inject
    DocumentService documentService;

    @Override
    public String getToolName() {
        return TOOL_GET_SKILL;
    }

    /**
     * Registers the get_skill function definition during the agent tool
     * registration phase.
     */
    public void onToolRegistration(
            @Observes @Priority(Interceptor.Priority.LIBRARY_BEFORE) ImixsAIToolRegistrationEvent event) {
        event.addFunction(
                TOOL_GET_SKILL,
                "Retrieves the full content of a skill by its uniqueid. "
                        + "The uniqueid is provided by a prior find_skill call. "
                        + "The returned content contains the full instructions for the current task.",
                """
                        {
                            "type": "object",
                            "properties": {
                                "uniqueid": {
                                    "type": "string",
                                    "description": "The uniqueid of the skill as returned by find_skill"
                                }
                            },
                            "required": ["uniqueid"]
                        }
                        """);
    }

    /**
     * Handles the "get_skill" tool call. Loads the skill directly by its $uniqueid
     * and returns the content field.
     */
    @Override
    public void handle(ImixsAIToolCallEvent event) {
        if (!TOOL_GET_SKILL.equals(event.getToolName())) {
            return;
        }
        String uniqueid = event.getArguments().getString("uniqueid");
        logger.info("├── ToolCallHandlerGetSkill: get_skill uniqueid='" + uniqueid + "'");

        try {
            ItemCollection skill = documentService.load(uniqueid);
            if (skill != null) {
                String content = skill.getItemValueString("content");
                logger.info("│   └── ✅ get_skill loaded skill '"
                        + skill.getItemValueString("name")
                        + "' for uniqueid='" + uniqueid + "'");
                event.setResultValue(content);
                event.setToolMessage(content);
            } else {
                String msg = "No skill found for uniqueid='" + uniqueid + "'";
                logger.warning("│   └── ⚠️ " + msg);
                event.setError(msg);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "│   └── ⚠️ get_skill failed: " + e.getMessage());
            event.setError(e.getMessage());
        }
    }
}