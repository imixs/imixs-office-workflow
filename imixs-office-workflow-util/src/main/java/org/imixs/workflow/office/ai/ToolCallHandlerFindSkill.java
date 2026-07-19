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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.imixs.ai.ImixsAIContextHandler;
import org.imixs.ai.tools.ImixsAIToolCallEvent;
import org.imixs.ai.tools.ToolCallHandler;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.engine.DocumentService;
import org.imixs.workflow.exceptions.QueryException;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;

/**
 * Handles the "find_skill" tool call.
 * 
 * Searches for skills by category and returns a list of matching skills with
 * their name, description and $uniqueid. The agent uses name and description to
 * select the most appropriate skill, and passes the $uniqueid to get_skill to
 * retrieve the full content.
 */
@Named
public class ToolCallHandlerFindSkill implements ToolCallHandler, Serializable {

    public static final String TOOL_FIND_SKILL = "find_skill";

    private static final Logger logger = Logger.getLogger(ToolCallHandlerFindSkill.class.getName());

    @Inject
    DocumentService documentService;

    @Override
    public String getToolName() {
        return TOOL_FIND_SKILL;
    }

    /**
     * Registers the find_skill function definition during the agent tool
     * registration phase.
     */
    @Override
    public void register(ImixsAIContextHandler contextHandler) {
        contextHandler.addFunction(
                TOOL_FIND_SKILL,
                "Searches for available skills by category. Returns a list of skills with their "
                        + "name, description and uniqueid. Use the name and description to select the most "
                        + "appropriate skill for the current task. Then call get_skill with the uniqueid "
                        + "to retrieve the full skill content.",
                """
                        {
                            "type": "object",
                            "properties": {
                                "category": {
                                    "type": "string",
                                    "description": "The skill category to search for"
                                }
                            },
                            "required": ["category"]
                        }
                        """);
    }

    /**
     * Handles the "find_skill" tool call. Queries all skills of the given category
     * and returns their name, description and $uniqueid as a JSON array.
     */
    @Override
    public void handle(ImixsAIToolCallEvent event) {
        if (!TOOL_FIND_SKILL.equals(event.getToolName())) {
            return;
        }
        String category = event.getArguments().getString("category");
        logger.info("├── ToolCallHandlerFindSkill: find_skill category='" + category + "'");

        try {
            String query = "(type:skill) AND (category:" + category + ")";
            List<ItemCollection> result = documentService.find(query, 100, 0);

            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            for (ItemCollection skill : result) {
                JsonObjectBuilder entry = Json.createObjectBuilder()
                        .add("uniqueid", skill.getUniqueID())
                        .add("name", skill.getItemValueString("name"))
                        .add("description", skill.getItemValueString("description"));
                arrayBuilder.add(entry);
            }
            String resultJson = arrayBuilder.build().toString();

            logger.info("│   └── ✅ find_skill returned " + result.size()
                    + " skill(s) for category='" + category + "'");
            event.setResultValue(resultJson);
            event.setToolMessage(resultJson);

        } catch (QueryException e) {
            logger.log(Level.WARNING, "│   └── ⚠️ find_skill failed: " + e.getMessage());
            event.setError(e.getMessage());
        }
    }
}