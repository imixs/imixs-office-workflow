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

package org.imixs.workflow.office.forms;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.Model;
import org.imixs.workflow.engine.ModelService;
import org.imixs.workflow.engine.ProcessingEvent;
import org.imixs.workflow.exceptions.AccessDeniedException;
import org.imixs.workflow.exceptions.ModelException;

import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.enterprise.event.Observes;

/**
 * The CustomFormController computes a set of fields based on a data object
 * provided by the model. This data is used by the {@link CustomFormController}
 * to display sections and fields.
 * 
 * 
 * @author rsoika
 * 
 */
@DeclareRoles({ "org.imixs.ACCESSLEVEL.NOACCESS",
        "org.imixs.ACCESSLEVEL.READERACCESS",
        "org.imixs.ACCESSLEVEL.AUTHORACCESS",
        "org.imixs.ACCESSLEVEL.EDITORACCESS",
        "org.imixs.ACCESSLEVEL.MANAGERACCESS" })
@RolesAllowed({ "org.imixs.ACCESSLEVEL.NOACCESS",
        "org.imixs.ACCESSLEVEL.READERACCESS",
        "org.imixs.ACCESSLEVEL.AUTHORACCESS",
        "org.imixs.ACCESSLEVEL.EDITORACCESS",
        "org.imixs.ACCESSLEVEL.MANAGERACCESS" })
@Stateless
@LocalBean
public class CustomFormService implements Serializable {

    @EJB
    ModelService modelService;

    private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(CustomFormService.class.getName());

    /**
     * WorkflowEvent listener to update the current FormDefinition.
     * 
     * @param processingEvent
     * @throws AccessDeniedException
     * @throws ModelException
     */
    public void onWorkflowEvent(@Observes ProcessingEvent processingEvent) throws ModelException {
        if (processingEvent == null)
            return;
        // skip if not a workItem...
        if (processingEvent.getDocument() != null
                && !processingEvent.getDocument().getItemValueString("type").startsWith("workitem")) {
            return;
        }

        int eventType = processingEvent.getEventType();
        if (ProcessingEvent.BEFORE_PROCESS == eventType || ProcessingEvent.AFTER_PROCESS == eventType) {
            // update the custom form definition
            updateCustomFieldDefinition(processingEvent.getDocument());
        }
    }

    /**
     * This method updates the custom Field Definition based on a given workitem.
     * The method first looks if the model contains a custom definition and stores
     * the data into the field txtWorkflowEditorCustomForm
     * 
     * @return
     * @throws ModelException
     */
    public String updateCustomFieldDefinition(ItemCollection workitem) throws ModelException {
        logger.fine("---> updateCustomFieldDefinition");
        String content = fetchFormDefinitionFromModel(workitem);
        if (!content.isEmpty()) {
            // store the new content
            workitem.replaceItemValue("txtWorkflowEditorCustomForm", content);
        }
        return content;
    }

    /**
     * read the form definition from a dataObject and search for a dataobject with a
     * imixs-form tag. If not matching dataobject is defined then return an empty
     * string.
     * 
     * @param workitem
     * @return
     */
    @SuppressWarnings("unchecked")
    private String fetchFormDefinitionFromModel(ItemCollection workitem) {
        Model model;
        ItemCollection task;
        try {
            model = modelService.getModelByWorkitem(workitem);
            task = model.getTask(workitem.getTaskID());
        } catch (ModelException e) {
            logger.warning("unable to parse data object in model: " + e.getMessage());
            return "";
        }

        List<List<String>> dataObjects = task.getItemValue("dataObjects");
        for (List<String> dataObject : dataObjects) {
            // there can be more than one dataOjects be attached.
            // We need the one with the tag <imixs-form>
            String templateName = dataObject.get(0);
            String content = dataObject.get(1);
            // we expect that the content contains at least one occurrence of <imixs-form>
            if (content.contains("<imixs-form>")) {
                logger.finest("......DataObject name=" + templateName);
                logger.finest("......DataObject content=" + content);
                return content;
            } else {
                // seems not to be a imixs-form definition!
            }
        }
        // nothing found!
        return "";
    }

}
