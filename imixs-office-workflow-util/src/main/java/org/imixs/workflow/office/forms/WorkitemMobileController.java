/*******************************************************************************
 *  Imixs Workflow Technology
 *  Copyright (C) 2003, 2008 Imixs Software Solutions GmbH,  
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
 *  Contributors:  
 *  	Imixs Software Solutions GmbH - initial API and implementation
 *  	Ralph Soika
 *  
 *******************************************************************************/
package org.imixs.workflow.office.forms;

import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Logger;

import org.imixs.workflow.exceptions.ModelException;
import org.imixs.workflow.faces.data.WorkflowController;

import jakarta.enterprise.context.Conversation;
import jakarta.enterprise.context.ConversationScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.Path;

/**
 * The WorkitemMobileController can be used to create a new workitem and switch
 * into the workitem-m form.
 * The controller is used by the page /forms/create.jsf
 * 
 * @author rsoika
 * @version 1.0
 */
@Named
@ConversationScoped
@Path("/travel")
public class WorkitemMobileController implements Serializable {

    private static final long serialVersionUID = 1L;

    protected String version;
    protected String task;
    protected String ref;

    protected boolean redirect = false;

    @Inject
    private Conversation conversation;

    @Inject
    WorkflowController workflowController;

    private static Logger logger = Logger.getLogger(WorkitemMobileController.class.getName());

    public void init() {
        logger.info("-- Init new beleg...");
        logger.info("----- version=" + getVersion());
        logger.info("----- task=" + getTask());
        logger.info("----- ref=" + getRef());

        try {
            if (version != null && !version.isEmpty()) {
                logger.info("Create mobile workitem...");
                // formController.setFormDefinition(null);
                if (task == null || task.isEmpty()) {
                    task = "0";
                }

                workflowController.create(version, Integer.parseInt(task), null);
                workflowController.getWorkitem().setItemValue("$workitemref", ref);
                workflowController.getWorkitem().setItemValue("$uniqueidref", ref);
                // Deine Initialisierungslogik
                // Nur beim ersten Aufruf weiterleiten
                if (!redirect && !FacesContext.getCurrentInstance().isPostback()) {
                    try {
                        String url = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath()
                                + "/pages/workitems/mobile.xhtml?cid=" + conversation.getId();
                        logger.info("redirect to: " + url);
                        redirect = true;
                        FacesContext.getCurrentInstance().getExternalContext().redirect(url);
                    } catch (IOException e) {
                        // Exception handling
                    }
                }
            }
        } catch (NumberFormatException | ModelException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

}
