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
package org.imixs.workflow.office.config;

import java.io.Serializable;
import java.util.logging.Logger;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.engine.WorkflowService;
import org.imixs.workflow.exceptions.ModelException;
import org.imixs.workflow.faces.data.WorkflowController;
import org.imixs.workflow.faces.data.WorkflowEvent;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * The SpaceController provides methods to create spaces within a JSF view.
 * 
 * @author rsoika,gheinle
 */
@Named
@RequestScoped
public class SpaceController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Inject
	protected WorkflowController workflowController;

	@Inject
	WorkflowService workflowService;

	@Inject
	protected PropertyController propertyController;

	private static Logger logger = Logger.getLogger(SpaceController.class.getName());

	/**
	 * This method creates a space. The param 'parentID' is options. In case a
	 * parentID is provided, the method creates a sub-space. If no 'parentID' is
	 * provided or the id is empty than a root space is created.
	 * 
	 * @return
	 * @throws ModelException
	 */
	public void create(String parentID) throws ModelException {

		if (parentID != null && !parentID.isEmpty()) {
			// create sub space
			ItemCollection parentSpace = workflowService.getWorkItem(parentID);
			if (parentSpace != null) {
				workflowController.create(propertyController.getProperty("setup.system.model"), 100, parentID);
				workflowController.getWorkitem().setItemValue("space.parent.name",
						parentSpace.getItemValueString("name"));
			} else {
				logger.warning("Can not create subspace - space " + parentID + " not found!");
			}

		} else {
			// create root space
			workflowController.create(propertyController.getProperty("setup.system.model"), 100, null);
		}
	}

	/**
	 * On space save we verify if a parent space is still provided
	 * 
	 * @param documentEvent
	 */
	public void onWorkflowEvent(@Observes WorkflowEvent documentEvent) {
		if (documentEvent == null)
			return;

		if (WorkflowEvent.WORKITEM_BEFORE_PROCESS == documentEvent.getEventType()) {
			if (documentEvent.getWorkitem().getType().startsWith("space")) {
				String idRef = workflowController.getWorkitem().getItemValueString(WorkflowService.UNIQUEIDREF);
				logger.info("space parent ref=" + idRef);
				if (idRef.isEmpty() || "[ROOT]".equals(idRef)) {
					logger.fine("remove parent");
					documentEvent.getWorkitem().setItemValue(WorkflowService.UNIQUEIDREF, "");
					documentEvent.getWorkitem().setItemValue("space.parent.name", "");
					documentEvent.getWorkitem().setItemValue("txtparentname", "");

				}
			}
		}

	}
}
