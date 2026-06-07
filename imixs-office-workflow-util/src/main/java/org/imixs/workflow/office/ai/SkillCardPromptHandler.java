/****************************************************************************
 * Copyright (c) 2022-2025 Imixs Software Solutions GmbH and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * This Source Code may also be made available under the terms of the
 * GNU General Public License, version 2 or later (GPL-2.0-or-later),
 * which is available at https://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0-or-later
 ****************************************************************************/

package org.imixs.workflow.office.ai;

import java.util.List;
import java.util.logging.Logger;

import org.imixs.ai.workflow.ImixsAIPromptEvent;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.engine.DocumentService;
import org.imixs.workflow.exceptions.AdapterException;
import org.imixs.workflow.exceptions.QueryException;
import org.imixs.workflow.util.XMLParser;
import org.imixs.workflow.util.XMLTag;

import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

/**
 * The SkillCardPromptHandler adds a Skill Card by its ID into the prompt
 * template. The template must provide a <skillcard id="...."/> place holder
 * 
 * @author rsoika
 *
 */
public class SkillCardPromptHandler {

    public static final String PROMPT_ERROR = "PROMPT_ERROR";

    private static final Logger logger = Logger.getLogger(SkillCardPromptHandler.class.getName());

    @Inject
    DocumentService documentService;

    public void onEvent(@Observes ImixsAIPromptEvent event) throws AdapterException {
        if (event == null || event.getWorkitem() == null) {
            return;
        }

        String prompt = event.getPromptTemplate();

        if (prompt == null || prompt.isBlank()) {
            return;
        }
        try {
            List<XMLTag> xmlTagList = XMLParser.parseTagMatches(prompt, "skillcard");
            for (XMLTag xmlTag : xmlTagList) {

                String id = xmlTag.getAttribute("id");
                String query = "(type:skill) AND (name:" + id + ")";
                List<ItemCollection> result;

                result = documentService.find(query, 1, 0);

                if (result.size() == 1) {
                    ItemCollection skill = result.get(0);
                    String skillContent = skill.getItemValueString("content");

                    // replace XML Tag with skill description in prompt
                    prompt = prompt.replace(xmlTag.getOuterXML(), skillContent);
                } else {
                    logger.warning("Failed to resolve skillCard id='" + id + "'");
                }
            }
            // finally update prompt
            event.setPromptTemplate(prompt);
        } catch (QueryException e) {
            logger.warning("Failed to resolve skill in prompt definition: " + e.getMessage());
        }

    }

}
