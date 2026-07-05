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

import java.util.List;
import java.util.logging.Logger;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.engine.WorkflowService;
import org.imixs.workflow.engine.plugins.AbstractPlugin;
import org.imixs.workflow.exceptions.AccessDeniedException;
import org.imixs.workflow.exceptions.InvalidAccessException;
import org.imixs.workflow.exceptions.ModelException;
import org.imixs.workflow.exceptions.PluginException;
import org.imixs.workflow.exceptions.ProcessingErrorException;
import org.imixs.workflow.exceptions.QueryException;

import jakarta.inject.Inject;

/**
 * This system model plug-in supports additional business logic for skill
 * entities. The plugin verifies the uniqueness of a skill name.
 * 
 * The plug-in updates also the properties 'skill.name' and 'skill.parent.name'.
 * 
 * The method also tries to process the sub skills with the same event.
 * 
 * Model: system
 * 
 * @author rsoika
 * 
 */
public class SkillPlugin extends AbstractPlugin {

    public static String SKILL_DELETE_ERROR = "SKILL_DELETE_ERROR";
    public static String SKILL_ARCHIVE_ERROR = "SKILL_ARCHIVE_ERROR";
    public static String SKILL_NAME_ERROR = "SKILL_NAME_ERROR";

    private static Logger logger = Logger.getLogger(SkillPlugin.class.getName());

    @Inject
    SkillService skillService;

    @Inject
    WorkflowService workflowService;

    /**
     * The method verifies if the skill Information need
     * to be updated to the parent and sub skills.
     *
     * The method also tries to update the workflow status of all sub skill
     **/
    @Override
    public ItemCollection run(ItemCollection documentContext, ItemCollection event) throws PluginException {
        String type = documentContext.getType();

        // verify skill and sub skills...
        if (type.startsWith("skill")) {

            if (documentContext.getItemValueBoolean("$$ignoreNameUpdate")) {
                documentContext.removeItem("$$ignoreNameUpdate");
            } else {
                inheritParentSkillProperties(documentContext);
                // verify name if still unique....
                validateUniqueSkillName(documentContext);
            }

            // try to process all sub skills....
            List<ItemCollection> subSkills = skillService.findAllSubSkills(documentContext.getUniqueID(), type);
            // now we can trigger the same workflow event for all sub skills
            for (ItemCollection subSkill : subSkills) {
                try {
                    // Update parent name
                    String sParentSkillName = documentContext.getItemValueString("name");
                    subSkill.replaceItemValue("skill.parent.name", sParentSkillName);
                    subSkill.replaceItemValue("name",
                            sParentSkillName + "." + subSkill.getItemValueString("skill.name"));

                    // Process sub skill
                    subSkill.event(documentContext.getEventID());
                    subSkill.setItemValue("$$ignoreNameUpdate", true);
                    workflowService.processWorkItem(subSkill);
                } catch (PluginException | AccessDeniedException | ProcessingErrorException | ModelException e) {
                    logger.warning("Unable to process sub skill '" +
                            subSkill.getItemValueString("name") + "' : "
                            + e.getMessage());
                }
            }

        }

        return documentContext;
    }

    /**
     * This method inherits the skill Name and team lists from a parent
     * Skill. A parent skill is referenced by the $UniqueIDRef.
     * 
     * If the parent is archived or deleted, the method throws a pluginExcepiton
     * 
     * @throws PluginException
     * 
     */
    private void inheritParentSkillProperties(ItemCollection skill) throws PluginException {
        // test if the skill has a parent..
        ItemCollection parentSkill = skillService.loadParentSkill(skill);
        if (parentSkill != null) {
            logger.fine("Updating Parent skill Information for '" + skill.getUniqueID() + "'");
            String sParentName = parentSkill.getItemValueString("name");

            skill.replaceItemValue("name", sParentName + "." + skill.getItemValueString("skill.name"));
            skill.replaceItemValue("skill.parent.name", sParentName);
        } else {
            // root skill - update name
            skill.replaceItemValue("name", skill.getItemValueString("skill.name"));
        }

    }

    /**
     * This method validates the uniqueness of the item name of a skill
     * 
     * @param skill
     * 
     * @throws PluginException if name is already taken
     */
    private void validateUniqueSkillName(ItemCollection orgunit) throws PluginException {
        String name = orgunit.getItemValueString("name");
        String unqiueid = orgunit.getUniqueID();

        // support deprecated item name 'txtname
        String sQuery = "((type:\"skill\" OR type:\"skillarchive\") AND (name:\"" + name + "\"))";

        List<ItemCollection> skillList;
        try {
            skillList = getWorkflowService().getDocumentService().find(sQuery, 9999, 0);

            for (ItemCollection askill : skillList) {
                if (!askill.getUniqueID().equals(unqiueid)) {
                    throw new PluginException(SkillPlugin.class.getName(), SKILL_NAME_ERROR,
                            "A skill with this name already exists!");
                }
            }

        } catch (QueryException e) {
            throw new InvalidAccessException(InvalidAccessException.INVALID_ID, e.getMessage(), e);
        }

    }
}
