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
package org.imixs.workflow.office.ai;

import java.io.Serializable;
import java.util.logging.Logger;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.engine.DocumentService;
import org.imixs.workflow.exceptions.ModelException;
import org.imixs.workflow.faces.data.WorkflowController;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * The AIAgentStatusController provides a method to test the current status
 * ($taskid) by loading the workitem form the backend. This is for front ends to
 * monitor backend status.
 * 
 * @author rsoika
 */
@Named
@RequestScoped
public class AIAgentStatusController implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	protected WorkflowController workflowController;

	@Inject
	protected DocumentService documentService;

	private static Logger logger = Logger.getLogger(AIAgentStatusController.class.getName());

	/**
	 * This method loads the current workitem and returns the task id.
	 * 
	 * @return
	 * @throws ModelException
	 */
	public String getStatus() {

		logger.fine("├── Load AI Workitem Status");

		ItemCollection _tempWorkitem = documentService.load(workflowController.getWorkitem().getUniqueID());
		if (_tempWorkitem != null) {
			logger.fine("└── TaskID=" + _tempWorkitem.getTaskID());

			String result = _tempWorkitem.getItemValueString("$workflowstatus") + " (" + _tempWorkitem.getTaskID()
					+ ") ...";

			return result;
		} else {
			return "Thinking....";
		}

	}
}
