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
 * <skill>material.plastic-polymere</skill>
 * Resolves a single skill node by its exact computed name.
 *
 * <skill separator="\n\n">material.plastic-polymere.*</skill>
 * Resolves all skills beneath the given path (recursive, whole subtree).
 *
 * <skill children="true" />material.plastic-polymere</skill>
 * Resolves only the direct children of the given node via $uniqueidref.
 *
 * An optional separator attribute controls the block header format.
 * Defaults to "## Skill: " if not provided.
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
                String skillPattern = xmlTag.getContent();
                // String name = xmlTag.getAttribute("name");
                String separator = xmlTag.getAttribute("separator");
                String skillContent = null;

                if (skillPattern != null && !skillPattern.isBlank()) {
                    if (skillPattern.endsWith(".*")) {
                        // Recursive subtree lookup
                        String basePath = skillPattern.substring(0, skillPattern.length() - 2);
                        skillContent = resolveBySubtree(basePath, separator);
                        if (skillContent == null) {
                            logger.warning("Failed to resolve skill subtree name='" + skillPattern + "'");
                        }
                    } else {
                        // Exact node lookup
                        skillContent = resolveByName(skillPattern, separator);
                        if (skillContent == null) {
                            logger.warning("Failed to resolve skill name='" + skillPattern + "'");
                        }
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
     * Resolves a single skill node by its exact computed name.
     * Returns the formatted skill block or null if not found.
     */
    private String resolveByName(String name, String separator) throws QueryException {
        String query = "(type:skill) AND (name:\"" + name + "\")";
        List<ItemCollection> result = documentService.find(query, 1, 0);
        if (result.isEmpty()) {
            return null;
        }
        return buildSkillBlock(result.get(0), separator);
    }

    /**
     * Resolves all skill nodes beneath a given path using a Lucene wildcard query.
     * Returns concatenated skill blocks or null if no skills were found.
     *
     * Example: basePath="material.plastic-polymere"
     * Query: (type:skill) AND (name:material.plastic-polymere.*)
     */
    private String resolveBySubtree(String basePath, String separator) throws QueryException {
        String query = "(type:skill) AND (name:" + basePath + ".*)";
        List<ItemCollection> result = documentService.find(query, 100, 0);
        if (result.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (ItemCollection skill : result) {
            sb.append(buildSkillBlock(skill, separator));
        }
        return sb.toString();
    }

    /**
     * Builds a formatted skill block for inclusion in the prompt.
     * Uses "## Skill: " as default separator if none is provided.
     */
    private String buildSkillBlock(ItemCollection skill, String separator) {
        if (separator == null || separator.isBlank()) {
            separator = "## Skill: ";
        }
        String topic = skill.getItemValueString("topic");
        String description = skill.getItemValueString("description");
        String content = skill.getItemValueString("content");

        StringBuilder sb = new StringBuilder();
        sb.append(separator).append(topic);
        if (description != null && !description.isBlank()) {
            sb.append(" — ").append(description);
        }
        sb.append("\n\n");
        if (content != null && !content.isBlank()) {
            sb.append(content);
            sb.append("\n\n");
        }
        return sb.toString();
    }
}