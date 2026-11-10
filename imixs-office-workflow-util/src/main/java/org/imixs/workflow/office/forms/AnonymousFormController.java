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

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.WorkflowKernel;
import org.imixs.workflow.exceptions.AccessDeniedException;
import org.imixs.workflow.exceptions.ModelException;
import org.imixs.workflow.exceptions.PluginException;
import org.imixs.workflow.exceptions.ProcessingErrorException;
import org.imixs.workflow.faces.util.ErrorHandler;

import jakarta.annotation.security.RunAs;
import jakarta.enterprise.context.Conversation;
import jakarta.enterprise.context.ConversationScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * The AnonymousFormController is to initialize a new workitem for an
 * unauthorized user. This is typically useful for Contact forms in a web site.
 * 
 * The feature need to be activated by the env variable
 * ANONYMOUS_FORMS_ENABLED=true
 * 
 * The method process validates that only a new created workitem within the same
 * conversation is allowed to be processed. This avoids situations where other
 * data can be manipulated.
 * 
 * @author rsoika
 *
 */
@Named
@ConversationScoped
@RunAs("org.imixs.ACCESSLEVEL.MANAGERACCESS")
public class AnonymousFormController implements Serializable {

    private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(AnonymousFormController.class.getName());

    public static final String ITEM_CUSTOM_FORM = "txtWorkflowEditorCustomForm";

    List<ItemCollection> events = null;
    ItemCollection workitem = null;
    String currentUniqueID = null;

    @Inject
    @ConfigProperty(name = "anonymous.forms.enabled", defaultValue = "false")
    boolean enabled;

    @Inject
    @ConfigProperty(name = "anonymous.forms.models", defaultValue = "- no models -")
    String models;

    @Inject
    SystemWorkitemService systemWorkitemService;

    @Inject
    Conversation conversation;

    public AnonymousFormController() {
        super();
    }

    /**
     * This method can be used to initalize a workitem by query params
     * 
     * model=xxx&task=xxx
     * 
     * @throws ModelException
     * 
     **/

    public void init() throws ModelException {

        if (enabled) {
            // start a long-running conversation so that this bean (and the
            // WorkflowController) survive the postback of the form
            if (conversation.isTransient()) {
                conversation.setTimeout(30 * 60 * 1000); // 30 minutes
                conversation.begin();
                logger.fine("conversation started - cid=" + conversation.getId());
            }

            // extract the id from the query string
            FacesContext fc = FacesContext.getCurrentInstance();
            Map<String, String> paramMap = fc.getExternalContext().getRequestParameterMap();

            // extract Model and Task
            String model = paramMap.get("model");
            String task = paramMap.get("task");

            // if the model is not listed in modelversions we throw an exception
            if (models == null || models.isBlank()) {
                logger.warning("ANONYMOUS_FORMS_MODELS not defined!");
                workitem = null;
                return;
            }
            if (!models.contains(model)) {
                logger.warning("Model '" + model + "' not defined by ANONYMOUS_FORMS_MODELS - verify configuration!");
                workitem = null;
                return;
            }

            workitem = systemWorkitemService.init(model, Integer.parseInt(task));
            // set initial uniqueid
            currentUniqueID = WorkflowKernel.generateUniqueID();
            workitem.setItemValue(WorkflowKernel.UNIQUEID, currentUniqueID);
            events = systemWorkitemService.getEvents();
        } else {
            logger.warning("ANONYMOUS_FORMS_ENABLED=false!");
        }
    }

    /**
     * Wrapper method
     * 
     * @return
     * @throws PluginException
     * @throws ProcessingErrorException
     * @throws AccessDeniedException
     * @throws ModelException
     */
    public String process() {
        String actionResult = "completed";
        if (enabled) {

            // verify if we are still in same workitem
            if (currentUniqueID == null || currentUniqueID.isBlank()
                    || !currentUniqueID.equals(workitem.getUniqueID())) {
                logger.warning("Failed to process - unknown uniqueid!");
                return "";
            }

            try {
                workitem = systemWorkitemService.processWorkItem(workitem);
                actionResult = workitem.getItemValueString("action");

                // If no action was defined computed it from the current viewID...
                if (actionResult == null || actionResult.isEmpty()) {
                    FacesContext facesContext = FacesContext.getCurrentInstance();
                    actionResult = facesContext.getViewRoot().getViewId();
                    actionResult = actionResult + "?id=" + getWorkitem().getUniqueID() + "&faces-redirect=true";
                }

            } catch (PluginException pe) {
                actionResult = null;
                workitem.removeItem("action");
                // add a new FacesMessage into the FacesContext
                ErrorHandler.handlePluginException(pe);
            } catch (ModelException me) {
                actionResult = null;
                workitem.removeItem("action");
                // add a new FacesMessage into the FacesContext
                ErrorHandler.handleModelException(me);
            }
        }

        // test if the action result is an external URL. In this case initiate a
        // redirect
        if (actionResult != null && actionResult.startsWith("http")) {
            // clear action result and start redirect
            String externalURL = actionResult;
            actionResult = "";
            try {
                ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
                externalContext.redirect(externalURL);
            } catch (IOException e) {
                logger.warning(
                        "Failed to redirect action result: " + externalURL + "  - Error: " + e.getMessage());
                e.printStackTrace();
            }
        }

        logger.info("Final Result = " + actionResult);
        return actionResult;
    }

    public ItemCollection getWorkitem() {
        return workitem;
    }

    public void setWorkitem(ItemCollection workitem) {
        this.workitem = workitem;
    }

    /**
     * Wrapper method
     * 
     * @return
     * @throws ModelException
     */
    public List<ItemCollection> getEvents() throws ModelException {
        return events;
    }

}
