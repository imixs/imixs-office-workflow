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

package org.imixs.workflow.office.util;

import java.util.List;
import java.util.logging.Logger;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.annotation.security.RunAs;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.WorkflowKernel;
import org.imixs.workflow.engine.DocumentService;
import org.imixs.workflow.exceptions.AccessDeniedException;
import org.imixs.workflow.office.config.ConfigService;

/**
 * The SequcneceService is a singleton EJB which handles continuous
 * sequenceNumbers for a workitem separated for each workflowGroup.
 * <p>
 * The sequence numbers are stored in the item 'sequencenumbers' of the
 * configuration entity with the name "BASIC" in in the following format
 * <p>
 * <code>[GROUP]=123</code>
 * <p>
 * <strong>Optimistic Locking Problem</strong>
 * <p>
 * In earlier versions, the method runs in a OptimisticLockException in case
 * that multiple processes run in parallel. To fix this the service is changed
 * into a singleton. See issue #290.
 * <p>
 *
 * @author rsoika
 * 
 */

@DeclareRoles({ "org.imixs.ACCESSLEVEL.NOACCESS", "org.imixs.ACCESSLEVEL.READERACCESS",
		"org.imixs.ACCESSLEVEL.AUTHORACCESS", "org.imixs.ACCESSLEVEL.EDITORACCESS",
		"org.imixs.ACCESSLEVEL.MANAGERACCESS" })
@RolesAllowed({ "org.imixs.ACCESSLEVEL.NOACCESS", "org.imixs.ACCESSLEVEL.READERACCESS",
		"org.imixs.ACCESSLEVEL.AUTHORACCESS", "org.imixs.ACCESSLEVEL.EDITORACCESS",
		"org.imixs.ACCESSLEVEL.MANAGERACCESS" })
@Singleton
@RunAs("org.imixs.ACCESSLEVEL.MANAGERACCESS")
public class SequenceService {

	private static Logger logger = Logger.getLogger(SequenceService.class.getName());

	@EJB
	ConfigService configService;

	@EJB
	DocumentService documentService;

	/**
	 * This method computes the sequence number based on a configuration entity with
	 * the name "BASIC". The configuration provides a property 'sequencenumbers'
	 * with the current number range for each workflowGroup. If a Workitem have a
	 * WorkflowGroup with no corresponding entry the method will not compute a new
	 * number.
	 * <p>
	 * This method loads and updates the configuration entity in a new transaction.
	 * In combination with the
	 * 
	 * @Singleton pattern a conflict of multipl running processing steps is no
	 *            longer possible. See issue #290.
	 * 
	 * @throws InvalidWorkitemException
	 * @throws AccessDeniedException
	 */
	@TransactionAttribute(value = TransactionAttributeType.REQUIRES_NEW)
	public void computeSequenceNumber(ItemCollection documentContext) throws AccessDeniedException {

		// if the worktitem already have a sequence number than skip!
		if (documentContext.getItemValueInteger("numsequencenumber") > 0) {
			return;
		}

		ItemCollection configItemCollection = configService.loadConfiguration("BASIC", true);
		if (configItemCollection != null) {
			// read configuration and test if a corresponding configuration
			// exists
			String sWorkflowGroup = documentContext.getItemValueString(WorkflowKernel.WORKFLOWGROUP);
			@SuppressWarnings("unchecked")
			List<String> vNumbers = configItemCollection.getItemValue("sequencenumbers");

			// find a matching identifier
			String groupIdentifier = null;
			String generalIdentifier = null;
			int identifierPosition = -1;
			// test if we have a group identifier...
			for (String aIdentifier : vNumbers) {
				// test for group identifier...
				if (aIdentifier.startsWith(sWorkflowGroup + "=")) {
					groupIdentifier = aIdentifier;
				}
				// test for general identifier...
				if (aIdentifier.startsWith("[GENERAL]=")) {
					generalIdentifier = aIdentifier;
				}
			}

			// if we did not find a group identifier we choose the GroupIdentifier if
			// available...
			if (groupIdentifier == null && generalIdentifier != null) {
				// select the general identifier...
				groupIdentifier = generalIdentifier;
				sWorkflowGroup = "[GENERAL]";
			}

			// did we found an identifier?
			if (groupIdentifier != null) {
				// we got the next number....
				String sequcenceNumber = groupIdentifier.substring(groupIdentifier.indexOf('=') + 1);
				long currentSequenceNumber = Long.parseLong(sequcenceNumber);
				documentContext.replaceItemValue("numsequencenumber",currentSequenceNumber); 
				
				long newSequenceNumber = currentSequenceNumber + 1;
				// Save the new Number back into the config entity
				groupIdentifier = sWorkflowGroup + "=" + newSequenceNumber;

				// update identifier....
				for (int i = 0; i < vNumbers.size(); i++) {
					if (vNumbers.get(i).startsWith(sWorkflowGroup + "=")) {
						identifierPosition = i;
						break;
					}
				}
				if (identifierPosition > -1) {
					vNumbers.set(identifierPosition, sWorkflowGroup + "=" + newSequenceNumber);
					configItemCollection.replaceItemValue("sequencenumbers", vNumbers);
					// do not use documentService here - cache need to be
					// updated!
					configService.save(configItemCollection);
				}
			} else {
				// to avoid problems with incorrect data values we remove the
				// property numsequencenumber in this case
				documentContext.removeItem("numsequencenumber");
			}

		} else {
			logger.warning("No BASIC configuration found!");
		}

	}

}
