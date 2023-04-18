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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.ItemCollectionComparator;
import org.imixs.workflow.WorkflowKernel;
import org.imixs.workflow.engine.ModelService;
import org.imixs.workflow.engine.WorkflowService;
import org.imixs.workflow.exceptions.AccessDeniedException;
import org.imixs.workflow.exceptions.ModelException;
import org.imixs.workflow.exceptions.PluginException;
import org.imixs.workflow.exceptions.ProcessingErrorException;
import org.imixs.workflow.exceptions.QueryException;

/**
 * The WorkitemService provides methods to create, process, update and remove a
 * workItem. The service can be used to all types of workitems (e.g. workitem,
 * process, space)
 * 
 * @author rsoika
 */
@DeclareRoles({ "org.imixs.ACCESSLEVEL.NOACCESS",
		"org.imixs.ACCESSLEVEL.READERACCESS",
		"org.imixs.ACCESSLEVEL.AUTHORACCESS",
		"org.imixs.ACCESSLEVEL.EDITORACCESS",
		"org.imixs.ACCESSLEVEL.MANAGERACCESS" })
@RolesAllowed({ "org.imixs.ACCESSLEVEL.NOACCESS",
		"org.imixs.ACCESSLEVEL.READERACCESS",
		"org.imixs.ACCESSLEVEL.AUTHORACCESS",
		"org.imixs.ACCESSLEVEL.EDITORACCESS",
		"org.imixs.ACCESSLEVEL.MANAGERACCESS" })
@Stateless
@LocalBean
public class WorkitemService {

	@EJB
	WorkflowService workflowService;

	@EJB
	ModelService modelService;
	
	private static Logger logger = Logger.getLogger(WorkitemService.class
			.getName());

	/**
	 * This method creates a new workItem. The workItem becomes a response
	 * object to the provided parent ItemCollection. The workItem will also be
	 * assigned to the ProcessEntity specified by the provided modelVersion and
	 * ProcessID. The method throws an exception if the ProcessEntity did not
	 * exist in the model.
	 * 
	 * The Method set the property '$WriteAccess' to the default value of the
	 * current Principal name. This allows initial updates of BlobWorkitems
	 * 
	 * The Attributes txtWorkflowEditor, numProcessID, $modelVersion and
	 * txtWrofklwoGroup will be set to the corresponding values of processEntity
	 * 
	 * @param parent
	 *            ItemCollection representing the parent where the workItem is
	 *            assigned to. This is typical a project entity but can also be
	 *            an other workItem
	 * @param processEntity
	 *            ItemCollection representing the ProcessEntity where the
	 *            workItem is assigned to
	 */
	public ItemCollection createWorkItem(ItemCollection parent,
			String sProcessModelVersion, int aProcessID) throws Exception {
		logger.fine("create workitem...");
		// lookup ProcessEntiy from the model
		ItemCollection processEntity = modelService.getModel(sProcessModelVersion).getTask(
				aProcessID);
		if (processEntity == null)
			throw new Exception(
					"error createWorkItem: Process Entity can not be found ("
							+ sProcessModelVersion + "|" + aProcessID + ")");

		String sEditorID = processEntity.getItemValueString("txteditorid");
		if ("".equals(sEditorID))
			sEditorID = "default";
		int processID = processEntity.getItemValueInteger("numProcessID");
		String sModelVersion = processEntity
				.getItemValueString("$modelversion");
		String sWorkflowGroup = processEntity
				.getItemValueString("txtworkflowgroup");

		// create empty workitem
		ItemCollection workItem = new ItemCollection();
		workItem.replaceItemValue("type", "workitem");
		workItem.replaceItemValue("$processID", processID);

		// set default writeAccess
		workItem.replaceItemValue("$writeAccess", workflowService.getUserName());

		// assign project name and reference
		workItem.replaceItemValue("$uniqueidRef",
				parent.getItemValueString("$uniqueid"));

		// assign ModelVersion, group and editor
		workItem.replaceItemValue("$modelversion", sModelVersion);
		workItem.replaceItemValue("$workflowgroup", sWorkflowGroup);
		workItem.replaceItemValue("txtworkfloweditorid", sEditorID);

		return workItem;

	}

	/**
	 * Processes a WorkItem.
	 * 
	 * @throws ProcessingErrorException
	 * @throws AccessDeniedException
	 * @throws PluginException
	 * @throws ModelException 
	 * 
	 */
	public ItemCollection processWorkItem(ItemCollection aworkitem)
			throws AccessDeniedException, ProcessingErrorException,
			PluginException, ModelException {
		// Process workitem...
		return workflowService.processWorkItem(aworkitem);
	}

	
	
	
	/**
	 * This method finds all versions of a given workitem.
	 * 
	 */
	public List<ItemCollection> findAllVersions(ItemCollection workitem) {
		List<ItemCollection> versions = new ArrayList<ItemCollection>();
		if (null == workitem)
			return versions;
		List<ItemCollection> col = null;
		String sRefID = workitem.getItemValueString("$workitemId");
		String refQuery = "( (type:\"workitem\" OR type:\"workitemarchive\" OR type:\"workitemversion\") AND $workitemid:\""
				+ sRefID + "\")";
		try {
			col = workflowService.getDocumentService().findStubs(refQuery, 999, 0,WorkflowKernel.LASTEVENTDATE,true);
			// sort by $modified
			Collections.sort(col, new ItemCollectionComparator("$modified", true));
			// Only return version list if more than one version was found!
			if (col.size() > 1) {
				for (ItemCollection aworkitem : col) {
					versions.add(aworkitem);
				}
			}
	
		} catch (QueryException e) {
			logger.warning("findAllVersions - invalid query: " + e.getMessage());
		}
		return versions;
	}

	/**
	 * This method finds all ancestors versions to a given workitem. The method open
	 * versions recursive back to the first source workitem. The method returns an
	 * empty list if no version exist (only the main version)
	 * 
	 */
	public List<ItemCollection> findAncestorsVersions(ItemCollection workitem) {
		List<ItemCollection> versions = new ArrayList<ItemCollection>();
		if (null != workitem) {
			String sourceVersion = workitem.getItemValueString(WorkflowKernel.UNIQUEIDSOURCE);
			while (!sourceVersion.isEmpty()) {
				ItemCollection version = workflowService.getWorkItem(sourceVersion);
				versions.add(version);
				sourceVersion = version.getItemValueString(WorkflowKernel.UNIQUEIDSOURCE);
			}
		}
		return versions;
	}
	
	
}
