/*******************************************************************************
 *  Imixs Workflow 
 *  Copyright (C) 2001, 2011 Imixs Software Solutions GmbH,  
 *  http://www.imixs.com
 *  
 *  This program is free software; you can redistribute it and/or 
 *  modify it under the terms of the GNU General Public License 
 *  as published by the Free Software Foundation; either version 2 
 *  of the License, or (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful, 
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 *  General Public License for more details.
 *  
 *  You can receive a copy of the GNU General Public
 *  License at http://www.gnu.org/licenses/gpl.html
 *  
 *  Project: 
 *  	http://www.imixs.org
 *  	http://java.net/projects/imixs-workflow
 *  
 *  Contributors:  
 *  	Imixs Software Solutions GmbH - initial API and implementation
 *  	Ralph Soika - Software Developer
 *******************************************************************************/

package org.imixs.workflow.office.ai;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.imixs.ai.workflow.ImixsAIPromptEvent;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.ItemCollectionComparator;
import org.imixs.workflow.engine.DocumentEvent;
import org.imixs.workflow.engine.DocumentService;
import org.imixs.workflow.engine.WorkflowService;
import org.imixs.workflow.exceptions.AdapterException;
import org.imixs.workflow.exceptions.InvalidAccessException;
import org.imixs.workflow.exceptions.PluginException;
import org.imixs.workflow.exceptions.QueryException;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.ConcurrencyManagement;
import jakarta.ejb.ConcurrencyManagementType;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.enterprise.event.Observes;

/**
 * The SkillService is an EJB handling kill documents containing markup text for
 * AI prompt definitions.
 * <p>
 * A skill is identified by its 'name' attribute.
 * 
 * 
 * @author rsoika
 */

@DeclareRoles({ "org.imixs.ACCESSLEVEL.NOACCESS", "org.imixs.ACCESSLEVEL.READERACCESS",
        "org.imixs.ACCESSLEVEL.AUTHORACCESS", "org.imixs.ACCESSLEVEL.EDITORACCESS",
        "org.imixs.ACCESSLEVEL.MANAGERACCESS" })
@RolesAllowed({ "org.imixs.ACCESSLEVEL.NOACCESS", "org.imixs.ACCESSLEVEL.READERACCESS",
        "org.imixs.ACCESSLEVEL.AUTHORACCESS", "org.imixs.ACCESSLEVEL.EDITORACCESS",
        "org.imixs.ACCESSLEVEL.MANAGERACCESS" })
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class SkillService {

    int DEFAULT_CACHE_SIZE = 30;
    public static final String PROMPT_ERROR = "PROMPT_ERROR";
    public static final String SKILL_CONTENT_REGEX = "(?i)<skill>(.*?)</skill>";

    @EJB
    private DocumentService documentService;

    private Cache cache = null;

    final String TYPE = "skill";

    private static Logger logger = Logger.getLogger(SkillService.class.getName());

    /**
     * PostConstruct event - loads the imixs.properties.
     */
    @PostConstruct
    void init() {
        // initialize cache
        cache = new Cache(DEFAULT_CACHE_SIZE);
    }

    /**
     * This method returns a skill ItemCollection for a specified ID. If no skill is
     * found for this id the method returns null.
     * <p>
     * The skill entity is cached internally and read from the cache
     * 
     * @param name - id of the skill (name)
     * 
     */
    public ItemCollection load(String name) {
        return this.load(name, false);
    }

    /**
     * This method returns a skill ItemCollection for a specified name or id. If no
     * skill is found for this name the Method returns null.
     * 
     * 
     * @param name         - id of the skill
     * @param discardCache - indicates if the internal cache should be discarded.
     */
    public ItemCollection load(String name, boolean discardCache) {
        ItemCollection skillItemCollection = null;
        // check cache...
        skillItemCollection = cache.get(name);
        if (skillItemCollection == null || discardCache) {

            // try to load by ID....

            String sQuery = "(type:\"" + TYPE + "\" AND name:\"" + name + "\")";
            Collection<ItemCollection> col;
            try {
                col = documentService.find(sQuery, 1, 0);
                if (col.size() > 0) {
                    skillItemCollection = col.iterator().next();
                } else {
                    logger.fine("Missing skill : '" + name + "'");
                }
            } catch (QueryException e) {
                logger.warning("getTextBlock - invalid query: " + e.getMessage());
            }

            if (skillItemCollection != null) {
                // cache...
                cache.put(name, skillItemCollection);
            }

        }
        return skillItemCollection;
    }

    /**
     * This method reacts on CDI events of the type ImixsAIPromptEvent and parses a
     * string for xml tag <skill>. Those tags will be replaced with the content of
     * teh corresponding skill.
     * <p>
     * 
     * @throws PluginException
     * 
     */
    public void onEvent(@Observes ImixsAIPromptEvent event) throws AdapterException {
        if (event.getWorkitem() == null) {
            return;
        }
        String prompt = event.getPromptTemplate();
        // Resolve skill tags recursively, tracking visited skills to detect recursion
        prompt = resolveSkillTags(prompt, new HashSet<>());
        event.setPromptTemplate(prompt);
    }

    /**
     * Recursively resolves <skill> tags in a prompt string. Tracks visited skill
     * names to detect and prevent infinite recursion.
     *
     * @param prompt        - the prompt string to process
     * @param visitedSkills - set of skill names already visited in this resolution
     *                      chain
     * @return the resolved prompt string
     * @throws PluginException if a skill is not found or a recursive reference is
     *                         detected
     */
    private String resolveSkillTags(String prompt, Set<String> visitedSkills) throws AdapterException {
        Pattern pattern = Pattern.compile(SKILL_CONTENT_REGEX);
        Matcher matcher = pattern.matcher(prompt);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String skillName = matcher.group(1).trim();

            // Detect recursive reference
            if (visitedSkills.contains(skillName)) {
                throw new AdapterException(SkillService.class.getSimpleName(), PROMPT_ERROR,
                        "Recursive skill reference detected: '" + skillName + "'");
            }

            ItemCollection skill = this.load(skillName);
            if (skill == null) {
                throw new AdapterException(SkillService.class.getSimpleName(), PROMPT_ERROR,
                        "Skill '" + skillName + "' not found!");
            }

            String skillContent = skill.getItemValueString("content");

            // Track this skill before resolving its content
            visitedSkills.add(skillName);
            // Recursively resolve nested skill tags within this skill's content
            skillContent = resolveSkillTags(skillContent, visitedSkills);
            // Remove from visited after resolution to allow reuse in sibling branches
            visitedSkills.remove(skillName);

            matcher.appendReplacement(result, Matcher.quoteReplacement(skillContent));
            logger.fine("Skill '" + skillName + "' resolved and inserted into prompt.");
        }

        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * This method refreshes the cache if a textblock was saved.
     * 
     * @param event
     */
    public void onDocumentEvent(@Observes DocumentEvent event) {

        if (TYPE.equals(event.getDocument().getType()) && (event.getEventType() == DocumentEvent.ON_DOCUMENT_SAVE
                || event.getEventType() == DocumentEvent.ON_DOCUMENT_DELETE)) {
            cache = new Cache(DEFAULT_CACHE_SIZE);
        }

    }

    /**
     * Cache implementation to hold config entities
     * 
     * @author rsoika
     * 
     */
    class Cache extends ConcurrentHashMap<String, ItemCollection> {
        private static final long serialVersionUID = 1L;
        private final int capacity;

        public Cache(int capacity) {
            super(capacity + 1, 1.1f);
            this.capacity = capacity;
        }

        protected boolean removeEldestEntry(Entry<String, ItemCollection> eldest) {
            return size() > capacity;
        }
    }

    /**
     * Load the parent skill for a given space.
     * If the space is a root space, the method returns null
     * 
     * @param skill
     * @return parent space or null if the given space is a root space
     */
    public ItemCollection loadParentSkill(ItemCollection skill) {
        String ref = skill.getItemValueString(WorkflowService.UNIQUEIDREF);
        if (!ref.isEmpty()) {
            // lookup parent...
            return documentService.load(ref);
        }
        return null;
    }

    /**
     * Helper method to find all sub-skills for a given uniqueID.
     * 
     * @param sIDRef
     * @return
     */
    public List<ItemCollection> findAllSubSkills(String sIDRef, String... types) {
        if (sIDRef == null) {
            return null;
        }
        String sQuery = "(";
        // query type...
        if (types != null && types.length > 0) {
            sQuery += "(";
            for (int i = 0; i < types.length; i++) {
                sQuery += " type:\"" + types[i] + "\"";
                if ((i + 1) < types.length) {
                    sQuery += " OR ";
                }
            }
            sQuery += ") ";
        }
        sQuery += " AND $uniqueidref:\"" + sIDRef + "\")";

        List<ItemCollection> subSkillList;
        try {
            subSkillList = documentService.find(sQuery, 9999, 0);
        } catch (QueryException e) {
            throw new InvalidAccessException(InvalidAccessException.INVALID_ID, e.getMessage(), e);
        }

        // sort by txtname
        Collections.sort(subSkillList, new ItemCollectionComparator("name", true));
        return subSkillList;
    }
}
