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

package org.imixs.workflow.office.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import org.imixs.marty.security.UserGroupService;
import org.imixs.workflow.exceptions.AccessDeniedException;
import org.imixs.workflow.exceptions.ModelException;
import org.imixs.workflow.faces.data.WorkflowController;
import org.imixs.workflow.faces.data.WorkflowEvent;

/**
 * This AccessRoleController supports the migration from the old txtgroups property in a
 * userprofile to the new access roles.
 * <p>
 * Since version 4.5.1  a list of fixed UserGroups is supported only.
 * 
 * @author rsoika
 * 
 */
@Named
@SessionScoped
public class AccessRoleController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    UserGroupService userGroupService;
 
    @Inject
    WorkflowController workflowController;
    
    private String role;
    
    public final static String[] ACCESS_ROLES = { "org.imixs.ACCESSLEVEL.MANAGERACCESS",
            "org.imixs.ACCESSLEVEL.EDITORACCESS", "org.imixs.ACCESSLEVEL.AUTHORACCESS",
            "org.imixs.ACCESSLEVEL.READERACCESS", "org.imixs.ACCESSLEVEL.NOACCESS"
    };

    
    
  

    private static Logger logger = Logger.getLogger(AccessRoleController.class.getName());

    public AccessRoleController() {
        super();
       
    }

  
    
    /**
     * WorkflowEvent listener to update the current FormDefinition.
     * 
     * @param workflowEvent
     * @throws AccessDeniedException
     * @throws ModelException
     */
    @SuppressWarnings("unchecked")
    public void onWorkflowEvent(@Observes WorkflowEvent workflowEvent) {
        if (workflowEvent == null || workflowEvent.getWorkitem() == null) {
            return;
        }

        // skip if not a profile...
        if (!workflowEvent.getWorkitem().getItemValueString("type").startsWith("profile")) {
            return;
        }

        int eventType = workflowEvent.getEventType();
        if (WorkflowEvent.WORKITEM_CHANGED == eventType ) {
            // extract the core role (migration from old usergroup names)
            List<String> groups = workflowEvent.getWorkitem().getItemValue("txtgroups");
            List<String> accessGroupList = new ArrayList<String>(Arrays.asList(ACCESS_ROLES)); 
            // test if we already have new role name.
            if (groups.size()==1 && accessGroupList.contains(groups.get(0))) {
                setUserRole(groups.get(0));
                return;
            } else {
                // we need to extract the role. Frist try to find a core role
                for (String coreRole: accessGroupList) {
                    if (groups.contains(coreRole)) {
                        setUserRole(coreRole);
                        return;
                    }
                }
                logger.warning("profile does not contain new core role! - trying migration....");
                for (String group: groups) {
                    // get the new core role.
                    String newrole=userGroupService.getCoreGroupName(group);
                    if (newrole!=null) {
                        setUserRole(newrole);
                        return;
                    }
                }
                logger.severe("unable to detect a valid group!");
            }
        }
        
        if (WorkflowEvent.WORKITEM_BEFORE_PROCESS == eventType ) {
            // add the new core gorup to txtgroups
            workflowEvent.getWorkitem().setItemValue("txtGroups", getUserRole());
        }

    }
    
    public void setUserRole(String role) {
        this.role=role;
    }
    public String getUserRole() {
        return this.role;
    }
    

    public String[] getAccessRoles() {
        return ACCESS_ROLES;
    }
    
  

}
