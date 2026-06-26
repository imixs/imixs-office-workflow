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
 * The SkillPromptHandler resolves <skill> tags in a prompt template.
 * 
 * Supported tag variants:
 * 
 * <skill id="SKILL_ID" /> Resolves a single skill by its exact name/ID.
 * 
 * <skill category="material" /> Resolves all skills matching the given category
 * and concatenates their content into labeled blocks.
 * 
 * <skill id="SKILL_ID" category="material" /> ID takes precedence. Category is
 * used as fallback if the ID is not found.
 * 
 * <skill category="material" separator="---" /> Optional separator attribute
 * controls the block header format. Defaults to "## Skill: " if not provided.
 * 
 * @author rsoika
 */
public class SkillPromptHandler {

    public static final String PROMPT_ERROR = "PROMPT_ERROR";
    private static final Logger logger = Logger.getLogger(SkillPromptHandler.class.getName());

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
            List<XMLTag> xmlTagList = XMLParser.parseTagMatches(prompt, "skill");
            for (XMLTag xmlTag : xmlTagList) {
                String id = xmlTag.getAttribute("id");
                String category = xmlTag.getAttribute("category");
                String separator = xmlTag.getAttribute("separator");
                String skillContent = null;

                // Case 1: ID is provided - exact match lookup (existing behavior)
                if (id != null && !id.isBlank()) {
                    skillContent = resolveById(id);
                    if (skillContent == null) {
                        logger.warning("Failed to resolve skill id='" + id + "'");
                    }
                }

                // Case 2: No ID or ID not found - fallback to category lookup
                if (skillContent == null && category != null && !category.isBlank()) {
                    skillContent = resolveByCategory(category, separator);
                    if (skillContent == null) {
                        logger.warning("Failed to resolve skill category='" + category + "'");
                    }
                }

                // Replace tag in prompt if content was found
                if (skillContent != null) {
                    prompt = prompt.replace(xmlTag.getOuterXML(), skillContent);
                }
            }
            event.setPromptTemplate(prompt);
        } catch (QueryException e) {
            logger.warning("Failed to resolve skill in prompt definition: " + e.getMessage());
        }
    }

    /**
     * Resolves a single skill by its exact name/ID. Returns the skill content or
     * null if not found.
     */
    private String resolveById(String id) throws QueryException {
        String query = "(type:skill) AND (name:" + id + ")";
        List<ItemCollection> result = documentService.find(query, 1, 0);
        if (result.size() == 1) {
            return result.get(0).getItemValueString("content");
        }
        return null;
    }

    /**
     * Resolves all skills matching a given category and concatenates their content.
     * Each skill block is prefixed with a separator line containing the skill ID
     * and description. Uses "## Skill: " as default separator if none is provided.
     * Returns null if no skills were found.
     */
    private String resolveByCategory(String category, String separator) throws QueryException {
        String query = "(type:skill) AND (category:" + category + ")";
        List<ItemCollection> result = documentService.find(query, 100, 0);
        if (result.isEmpty()) {
            return null;
        }
        if (separator == null || separator.isBlank()) {
            separator = "## Skill: ";
        }
        StringBuilder sb = new StringBuilder();
        for (ItemCollection skill : result) {
            String skillId = skill.getItemValueString("name");
            String skillDescription = skill.getItemValueString("description");
            String skillContent = skill.getItemValueString("content");

            sb.append(separator).append(skillId);
            if (skillDescription != null && !skillDescription.isBlank()) {
                sb.append(" — ").append(skillDescription);
            }
            sb.append("\n\n");
            sb.append(skillContent);
            sb.append("\n\n");
        }
        return sb.toString();
    }
}