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

import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Named;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.exceptions.AccessDeniedException;
import org.imixs.workflow.faces.data.WorkflowEvent;

/**
 * The marty VersionController provides the list of versions to the current
 * loaded workitem.
 * 
 * @author rsoika
 * @version 2.0
 */
@Named
@RequestScoped
public class VersionController implements Serializable {

	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(VersionController.class.getName());

	private List<ItemCollection> versions = null;

	@EJB
	protected WorkitemService workitemService;

	public VersionController() {
		super();
	}

	/**
	 * WorkflowEvent listener
	 * 
	 * @param workflowEvent
	 * @throws AccessDeniedException
	 */
	public void onWorkflowEvent(@Observes WorkflowEvent workflowEvent) throws AccessDeniedException {

		if (workflowEvent == null)
			return;

		// skip if not a workItem...
		if (workflowEvent.getWorkitem() != null
				&& !workflowEvent.getWorkitem().getItemValueString("type").startsWith("workitem"))
			return;

		if (WorkflowEvent.WORKITEM_CHANGED == workflowEvent.getEventType()
				|| WorkflowEvent.WORKITEM_AFTER_PROCESS == workflowEvent.getEventType()) {
			logger.finest("......find all versions...");
			versions = workitemService.findAllVersions(workflowEvent.getWorkitem());
		}

	}

	/**
	 * returns a List with all Versions of the current Workitem
	 * 
	 * @return
	 */
	public List<ItemCollection> getVersions() {
		return versions;
	}

}
