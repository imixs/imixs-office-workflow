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
 *      Imixs Software Solutions GmbH - initial API and implementation
 *      Ralph Soika
 *  
 *******************************************************************************/
package org.imixs.workflow.office.forms;

import java.io.Serializable;
import java.util.Map;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.engine.WorkflowService;
import org.imixs.workflow.exceptions.PluginException;
import org.imixs.workflow.faces.data.WorkflowController;

/**
 * The ValidationController evaluates the BPMN event validation rules.
 * <p>
 * A bpmn event object can be provided with the optional validation rules in the
 * workflow result
 * <p>
 * 
 * <pre>
 * {@code
 *  <validation name="required">false</validation>
  <validation name="confirm">Are you sure?</validation>
 * }
 * </pre>
 * <p>
 * These flags can be used to dynamically evaluate the required field of a
 * h:input ui component:
 * <p>
 * <code>required="#{requiredController.required}"</code>
 * <p>
 * The method getConfirmMessage evaluates the text of the validation tag named
 * 'confirm'. This message text can be used for java script validation
 * 
 * @author rsoika
 * @version 1.0
 */

@Named
@RequestScoped
public class ValidationController implements Serializable {

    private static final long serialVersionUID = 1L;

    private static Logger logger = Logger.getLogger(ValidationController.class.getName());

    private boolean required = true;

    @Inject
    WorkflowController workflowController;

    @Inject
    WorkflowService workflowService;

    public boolean isRequired() throws PluginException {
        required = false;
        if (workflowController != null && workflowController.getWorkitem() != null) {

            FacesContext context = FacesContext.getCurrentInstance();

            ExternalContext ec = context.getExternalContext();
            Map<String, String[]> paramValues = ec.getRequestParameterValuesMap();

            // test if we find a imixs_workflow_eventid_
            for (String id : paramValues.keySet()) {
                int eventPos = id.indexOf(":imixs_workflow_eventid_");
                if (eventPos > -1) {
                    // we have an event - so the required default is now true
                    required = true;
                    // extract the event id from the h:commandButton id
                    String eventid = id.substring(eventPos + 24);
                    if (eventid != null && !eventid.isEmpty()) {
                        // fetch model event.....
                        for (ItemCollection event : workflowController.getEvents()) {

                            if (eventid.equals(event.getItemValueString("numactivityid"))) {
                                logger.finest("......evaluate validation rule for event " + eventid);
                                // we found the corresponding event.
                                // evaluate the workflow result....
                                ItemCollection evalItemCollection = workflowService.evalWorkflowResult(event,
                                        "validation", workflowController.getWorkitem());
                                if (evalItemCollection != null) {
                                    // evaluate the validation rules...
                                    if (!evalItemCollection.getItemValueBoolean("required")) {
                                        logger.finest("validation: event=" + eventid + " required=false");
                                        required = false;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    /**
     * This method evaluates the validation confirm message from a given BPMN event
     * object.
     * 
     * @param event
     * @return - the confirm message or null if not defined.
     */
    public String getConfirmMessage(ItemCollection event) {
        ItemCollection evalItemCollection;
        try {
            evalItemCollection = workflowService.evalWorkflowResult(event, "validation",
                    workflowController.getWorkitem());
            if (evalItemCollection != null) {
                logger.finest("......evaluate validation confirm message");
                // evaluate the validation confirm message...
                String message = evalItemCollection.getItemValueString("confirm");
                return message;
            }
        } catch (PluginException e) {
            logger.warning("Failed to evaluate the event confirm message: " + e.getMessage());
        }
        return null;
    }

}
