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

    @Inject
    @ConfigProperty(name = "anonymous.forms.enabled", defaultValue = "false")
    boolean enabled;

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

            workitem = systemWorkitemService.init(model, Integer.parseInt(task));
            // workflowController.setWorkitem(workitem);
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
                // add a new FacesMessage into the FacesContext
                ErrorHandler.handlePluginException(pe);
            } catch (ModelException me) {
                actionResult = null;
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

        logger.info("Result = " + actionResult);
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
