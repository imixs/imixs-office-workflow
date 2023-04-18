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
import java.util.StringTokenizer;
import java.util.logging.Logger;

import jakarta.enterprise.context.ConversationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.imixs.marty.profile.UserController;
import org.imixs.marty.team.TeamController;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.exceptions.AccessDeniedException;
import org.imixs.workflow.faces.data.WorkflowEvent;
import org.imixs.workflow.faces.util.ResourceBundleHandler;

/**
 * The FormController provides information about the Form, FormSections and
 * FormParts defined by a Workitem.
 * 
 * A form definition must be provided by the workitem property
 * 'txtWorkflowEditorid' marked with the '#' character and separated with
 * charater '|'.
 * 
 * e.g.: form_tab#basic_project|sub_timesheet[owner,manager]
 * 
 * 
 * @author rsoika
 * 
 */
@Named
@ConversationScoped
public class FormController implements Serializable {

    //public static final String DEFAULT_EDITOR_ID = "form_panel_simple#basic";
    public static final String DEFAULT_EDITOR_ID = "form_basic";

    @Inject
    protected TeamController processController;

    @Inject
    protected UserController userController;

    @Inject
    protected ResourceBundleHandler resourceBundleHandler;

    private static final long serialVersionUID = 1L;

    private FormDefinition formDefinition = null;

    private static Logger logger = Logger.getLogger(FormController.class.getName());

    public FormController() {
        super();
    }

    /**
     * Retuns the FormDefinition for the current workitem
     * 
     * @return
     */
    public FormDefinition getFormDefinition() {
        if (formDefinition == null) {
            logger.warning("formDefinition is null - verify model definition and the workitem 'type' attribute'.");
            formDefinition = new FormDefinition();
            computeFormDefinition(new ItemCollection());

        }
        return formDefinition;
    }

    public void setFormDefinition(FormDefinition formDefinition) {
        this.formDefinition = formDefinition;
    }

    /**
     * WorkflowEvent listener to update the current FormDefinition.
     * 
     * @param workflowEvent
     * @throws AccessDeniedException
     */
    public void onWorkflowEvent(@Observes WorkflowEvent workflowEvent) throws AccessDeniedException {
        if (workflowEvent == null)
            return;

        // skip if no item 'txtWorkflowEditorid' exists
        if (!workflowEvent.getWorkitem().hasItem("txtWorkflowEditorid")) {
            return;
        }
        int eventType = workflowEvent.getEventType();

        // if workItem has changed, then update the form definition
        if (WorkflowEvent.WORKITEM_CHANGED == eventType || WorkflowEvent.WORKITEM_CREATED == eventType
                || WorkflowEvent.WORKITEM_AFTER_PROCESS == eventType) {
            computeFormDefinition(workflowEvent.getWorkitem());
        }

    }

    /**
     * Computes an new FormDefinition based on a given workitem.
     * 
     * array list with EditorSection Objects defined by a given workitem. Each
     * EditorSection object contains the url and the name of one section.
     * EditorSections must be provided by the workitem property
     * 'txtWorkflowEditorid' marked with the '#' character and separated with
     * charater '|'.
     * 
     * e.g.: form_tab#basic_project|sub_timesheet[owner,manager]
     * 
     * This example provides the editor sections 'basic_project' and
     * 'sub_timesheet'. The optional marker after the second section in [] defines
     * the user membership to access this action. In this example the second section
     * is only visible if the current user is member of the project owner or manager
     * list.
     * 
     * The following example illustrates how to iterate over the section array from
     * a JSF fragment:
     * 
     * <code>
     * <ui:repeat value="#{workitemMB.editorSections}" var="section">
     *   ....
     *      <ui:include src="/pages/workitems/forms/#{section.url}.xhtml" />
     * </code>
     * 
     * 
     * The array of EditorSections also contains information about the name for a
     * section. This name is read from the resouce bundle 'bundle.forms'. The '/'
     * character will be replaced with '_'. So for example the section url
     * myforms/sub_timesheet will result in resoure bundle lookup for the name
     * 'myforms_sub_timersheet'
     * 
     * @return
     */
    public FormDefinition computeFormDefinition(ItemCollection aworkitem) {

        formDefinition = new FormDefinition();

        String editorID = DEFAULT_EDITOR_ID;

        if (aworkitem == null) {
            // return empty array
            return formDefinition;
        }

        // test for txtWorkflowEditorid
        String currentEditor = aworkitem.getItemValueString("txtWorkflowEditorid");
        if (!currentEditor.isEmpty()) {
            editorID = currentEditor;
        }

        if (editorID.indexOf('#') == -1) {
            // set form as direct path
            formDefinition.setPath(editorID);
            return formDefinition;
        }

        // Set the main part
        String sMainForm = editorID.substring(0, editorID.indexOf('#'));
        formDefinition.setPath(sMainForm);
        formDefinition.setName(findResourceNameByPath(sMainForm));

        // now get the sections....

        StringTokenizer subSections = new StringTokenizer(editorID.substring(editorID.indexOf('#') + 1), "|");
        while (subSections.hasMoreTokens()) {
            try {
                String path = subSections.nextToken();

                // if the URL contains a [] section test the defined
                // user
                // permissions
                if (path.indexOf('[') > -1 || path.indexOf(']') > -1) {
                    boolean bPermissionGranted = false;
                    // yes - cut the permissions
                    String sPermissions = path.substring(path.indexOf('[') + 1, path.indexOf(']'));

                    // cut the permissions from the URL
                    path = path.substring(0, path.indexOf('['));
                    StringTokenizer stPermission = new StringTokenizer(sPermissions, ",");
                    while (stPermission.hasMoreTokens()) {
                        String aPermission = stPermission.nextToken();
                        // test for user role
                        ExternalContext ectx = FacesContext.getCurrentInstance().getExternalContext();
                        if (ectx.isUserInRole(aPermission)) {
                            bPermissionGranted = true;
                            break;
                        }
                        // test if user is project member
                        String sProjectUniqueID = aworkitem.getItemValueString("txtProcessRef");

                        if ("manager".equalsIgnoreCase(aPermission)
                                && processController.isManagerOf(sProjectUniqueID)) {
                            bPermissionGranted = true;
                            break;
                        }
                        if ("team".equalsIgnoreCase(aPermission)
                                && this.processController.isTeamMemberOf(sProjectUniqueID)) {
                            bPermissionGranted = true;
                            break;
                        }

                    }

                    // if not permission is granted - skip this section
                    if (!bPermissionGranted)
                        continue;

                }

                String sName = findResourceNameByPath(path);

                FormSection aSection = new FormSection(path, sName);
                formDefinition.getSections().add(aSection);

            } catch (Exception est) {
                logger.severe("[getEditorSections] can not parse EditorSections : '" + editorID + "'");
                logger.severe(est.getMessage());
            }

        }

        return formDefinition;

    }

    /**
     * Finds in the resource bundles a name for a URL/Path
     * 
     * @param path
     * @return
     */
    private String findResourceNameByPath(String path) {
        String sResouceURL = path.replace('/', '_');

        // compute name from ressource Bundle....
        return resourceBundleHandler.get(sResouceURL);
    }
}
