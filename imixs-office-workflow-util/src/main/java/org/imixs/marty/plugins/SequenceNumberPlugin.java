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

package org.imixs.marty.plugins;

import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.mail.internet.AddressException;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.WorkflowKernel;
import org.imixs.workflow.engine.plugins.AbstractPlugin;
import org.imixs.workflow.exceptions.AccessDeniedException;
import org.imixs.workflow.exceptions.ModelException;
import org.imixs.workflow.exceptions.PluginException;
import org.imixs.workflow.office.util.SequenceService;

/**
 * This Plugin handles a unique sequence number for a workItem. The current
 * sequenceNumber for a workitem is based on the workflowGroup. The next free
 * sequence number will be stored in the configuration Entity 'BASIC'. The
 * configuration entity provides a property named 'sequencenumbers' with the
 * current number range for each workflowGroup.
 * <p>
 * If a WorkItem is assigned to a WorkflowGroup with no corresponding entry in
 * the BASIC configuration, the Plugin will not compute a new number for the
 * workitem.
 * <p>
 * If the Workitem already have a sequence number, the plugin will not run.
 * <p>
 * The new computed SequenceNumer will be stored into the property
 * 'numsequencenumber'. To compute the sequence Number the plugin uses the
 * SequeceService .
 * <p>
 * <strong>Optimistic Locking Problem</strong>
 * <p>
 * In earlier versions, the method runs in a OptimisticLockException in case
 * that multiple processes run in parallel. To fix this the SequenceService was
 * changed into a singleton. See issue #290.
 * <p>
 * 
 * @see SequenceService
 * @author rsoika
 * @version 2.0
 * 
 */
public class SequenceNumberPlugin extends AbstractPlugin {

	private static Logger logger = Logger.getLogger(SequenceNumberPlugin.class.getName());
	public static String NO_SEQUENCE_SERVICE_FOUND = "NO_SEQUENCE_SERVICE_FOUND";

	@EJB
	SequenceService sequenceService = null;

	/**
	 * This method loads a sequence number object and increases the sequence number
	 * in the workItem. If the workItem is form type'workitem' then the next
	 * sequecnenumer is computed based on the workflowGroup. If the workItem is from
	 * type='childworkitem' then the next sequencenumber is computed on the parent
	 * workItem.
	 * 
	 * @return
	 * @throws AddressException
	 */
	@Override
	public ItemCollection run(ItemCollection documentContext, ItemCollection event) throws PluginException {

		/*
		 * The plugin will only run for type='worktiem'.
		 */
		String sType = documentContext.getItemValueString("Type");
		// also give a squence number for archived workitems
		if (!sType.equals("workitem")) {
			return documentContext;
		}
		/* check if worktitem still have a sequence number? */
		if (sequenceService.hasSequenceNumber(documentContext)) {
			return documentContext;
		}

		logger.fine("...calculating next sequencenumber: '" + documentContext.getUniqueID() + "'");
		try {
			// test if $WorkflowGorup is already available.....
			if (documentContext.getItemValueString(WorkflowKernel.WORKFLOWGROUP).isEmpty()) {
				// set temporary workflow group, will be updated by the WorkflowKernel later...
				ItemCollection itemColNextTask = this.getWorkflowService().evalNextTask(documentContext);
				if (itemColNextTask != null) {
					documentContext.replaceItemValue(WorkflowKernel.WORKFLOWGROUP,
							itemColNextTask.getItemValueString("txtworkflowgroup"));
				}
			}
			sequenceService.computeSequenceNumber(documentContext);
		} catch (AccessDeniedException e) {
			throw new PluginException(e.getErrorContext(), e.getErrorCode(), "calculating next sequencenumber failed: ", e);
		} catch (ModelException e) {
			throw new PluginException(e.getErrorContext(), e.getErrorCode(), "calculating next sequencenumber failed: ", e);
		}

		return documentContext;
	}

}
