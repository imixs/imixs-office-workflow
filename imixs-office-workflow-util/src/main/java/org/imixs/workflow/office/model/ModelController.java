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

package org.imixs.workflow.office.model;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.imixs.marty.team.TeamController;
import org.imixs.workflow.FileData;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.ModelManager;
import org.imixs.workflow.WorkflowKernel;
import org.imixs.workflow.bpmn.BPMNUtil;
import org.imixs.workflow.engine.ModelService;
import org.imixs.workflow.engine.WorkflowService;
import org.imixs.workflow.exceptions.AccessDeniedException;
import org.imixs.workflow.exceptions.InvalidAccessException;
import org.imixs.workflow.exceptions.ModelException;
import org.imixs.workflow.exceptions.PluginException;
import org.imixs.workflow.faces.data.WorkflowController;
import org.imixs.workflow.faces.fileupload.FileUploadController;
import org.openbpmn.bpmn.BPMNModel;
import org.openbpmn.bpmn.exceptions.BPMNModelException;
import org.openbpmn.bpmn.util.BPMNModelFactory;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.event.ActionEvent;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * The ModelController provides information about workflow models. It is the
 * main controller used in the UI to display model related data.
 * <p>
 * A ModelVersion is always expected in the format
 * 'DOMAIN-LANGUAGE-VERSIONNUMBER', e.g. office-de-0.1, support-en-2.0,
 * system-de-0.0.1
 * <p>
 * Model data is accessed via the {@link SharedModelManager} singleton, which
 * holds a shared {@link ModelManager} instance for all active user sessions.
 * This ensures that after a model upload or deletion all sessions immediately
 * see the updated model data.
 *
 * @see SharedModelManager
 * @author rsoika
 */
@Named
@SessionScoped
public class ModelController implements Serializable {

	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(ModelController.class.getName());

	protected ItemCollection modelUploads = null;

	@Inject
	protected ModelService modelService;

	@Inject
	protected WorkflowService workflowService;

	@Inject
	protected TeamController teamController;

	@Inject
	protected SharedModelManager sharedModelManager;

	@Inject
	FileUploadController fileUploadController;

	/**
	 * PostConstruct event - init model uploads
	 */
	@PostConstruct
	void init() {
		modelUploads = new ItemCollection();
	}

	/**
	 * Returns the shared ModelManager instance provided by the
	 * {@link SharedModelManager} singleton.
	 *
	 * @return shared {@link ModelManager} instance
	 */
	public ModelManager getModelManager() {
		return sharedModelManager.getModelManager();
	}

	public ItemCollection getModelUploads() {
		return modelUploads;
	}

	public void setModelUploads(ItemCollection modelUploads) {
		this.modelUploads = modelUploads;
	}

	/**
	 * Returns all groups for a given model version.
	 *
	 * @param version - the model version
	 * @return list of group names
	 */
	public List<String> getGroups(String version) {
		try {
			BPMNModel model = getModelManager().getModel(version);
			Set<String> groups = getModelManager().findAllGroupsByModel(model);
			return new ArrayList<>(groups);
		} catch (ModelException | InvalidAccessException e) {
			logger.severe("Unable to load groups:" + e.getMessage());
		}
		return new ArrayList<String>();
	}

	/**
	 * Returns a String list of all WorkflowGroup names.
	 * <p>
	 * Workflow groups of the system model will be skipped. The workflow group list
	 * is used to assign a workflow group to a core process.
	 *
	 * @return list of workflow groups
	 */
	public List<String> getWorkflowGroups() {
		List<String> result = new ArrayList<String>();
		List<String> allGroups = modelService.findAllWorkflowGroups();
		for (String group : allGroups) {
			String version = getVersionByGroup(group);
			if (version.startsWith("system-")) {
				continue;
			}
			result.add(group);
		}
		Collections.sort(result);
		return result;
	}

	/**
	 * Returns the model version for a given workflow group name.
	 *
	 * @param group - the workflow group name
	 * @return model version string or null if not found
	 */
	public String getVersionByGroup(String group) {
		try {
			return modelService.findVersionByGroup(group);
		} catch (ModelException e) {
			logger.severe(e.getMessage());
		}
		return null;
	}

	/**
	 * Returns a list of all Imixs Tasks for a given workflow group.
	 *
	 * @param group - the workflow group name
	 * @return list of task entities
	 */
	public List<ItemCollection> findAllTasksByGroup(String group) {
		List<ItemCollection> result = new ArrayList<>();
		if (group == null || group.isEmpty()) {
			return result;
		}
		try {
			String version = modelService.findVersionByGroup(group);
			BPMNModel model = getModelManager().getModel(version);
			result = getModelManager().findTasks(model, group);
		} catch (ModelException e) {
			logger.severe("Failed to call findAllTasksByGroup for '" + group + "'");
		}
		return result;
	}

	/**
	 * Returns a list of all valid Imixs Start Tasks for a given workflow group.
	 * <p>
	 * The method validates the structure of each start task. A task with an
	 * unexpected type is logged as a warning and excluded from the result.
	 *
	 * @param version - the model version
	 * @param group   - the workflow group name
	 * @return list of valid start task entities
	 */
	public List<ItemCollection> findAllStartTasksByGroup(String version, String group) {
		List<ItemCollection> result = new ArrayList<>();
		try {
			BPMNModel model = getModelManager().getModel(version);
			List<ItemCollection> _result = getModelManager().findStartTasks(model, group);

			// Validate each start task - type is a mandatory field
			for (ItemCollection task : _result) {
				String type = task.getItemValueString("txttype");
				if (!type.isEmpty() && !WorkflowController.DEFAULT_TYPE.equals(type)) {
					String msg = "Invalid initial task in model='" + version + "' workflowGroup='"
							+ group + "' task=" + task.getItemValueString("numProcessID")
							+ " wrong type='" + type + "' -> expected: '" + WorkflowController.DEFAULT_TYPE + "'";
					logger.warning(msg);
					sharedModelManager.addModelWarning(msg);
					continue;
				}
				result.add(task);
			}
		} catch (ModelException e) {
			logger.severe(
					"Failed to find start tasks for workflow group '" + group + "' : " + e.getMessage());
		}
		return result;
	}

	/**
	 * Finds the process entity containing a specific workflow group in the item
	 * 'txtworkflowlist'. Returns null if no matching workflow group was found.
	 *
	 * @param workflowgroup - the workflow group name to search for
	 * @return matching process entity or null
	 */
	public ItemCollection findProcessByWorkflowGroup(String workflowgroup) {
		if (workflowgroup != null) {
			// Use the TeamController to support caching
			List<ItemCollection> processList = teamController.getProcessList();
			for (ItemCollection process : processList) {
				List<String> workflowList = process.getItemValueList("txtworkflowlist", String.class);
				if (workflowList != null && workflowList.contains(workflowgroup)) {
					return process;
				}
			}
		}
		return null;
	}

	/**
	 * Returns a sorted list of all model versions.
	 * <p>
	 * Used by the Model View.
	 *
	 * @return sorted list of model version strings
	 */
	public List<String> getVersions() {
		return modelService.getVersions();
	}

	/**
	 * Returns a Model metadata entity for a given version.
	 * <p>
	 * Used by the Model View.
	 *
	 * @param version - the model version
	 * @return model metadata as ItemCollection or null if not found
	 */
	public ItemCollection getModelEntity(String version) {
		try {
			return modelService.loadModelMetaData(version);
		} catch (ModelException e) {
			logger.severe("Failed to find model version '" + version + "' : " + e.getMessage());
		}
		return null;
	}

	/**
	 * Handles uploaded BPMN model files. Only .bpmn files are supported.
	 * <p>
	 * After a successful upload the {@link SharedModelManager} is reset to clear
	 * all internal caches. This ensures that all active user sessions immediately
	 * see the updated model data.
	 *
	 * @param event - the JSF ActionEvent
	 * @throws ModelException if the model file cannot be read or saved
	 */
	public void doUploadModel(ActionEvent event) throws ModelException {

		try {
			fileUploadController.attacheFiles(modelUploads);
		} catch (PluginException e) {
			e.printStackTrace();
		}

		List<FileData> fileList = getModelUploads().getFileData();
		if (fileList == null) {
			return;
		}

		for (FileData file : fileList) {
			if (file.getName().endsWith(".bpmn")) {
				InputStream inputStream = new ByteArrayInputStream(file.getContent());
				try {
					BPMNModel model = BPMNModelFactory.read(inputStream);
					modelService.saveModel(model);
				} catch (BPMNModelException | InvalidAccessException e) {
					throw new ModelException(ModelException.INVALID_MODEL,
							"Unable to read model file: " + file.getName(), e);
				}
			} else {
				logger.log(Level.WARNING, "Invalid model type - file {0} cannot be imported!", file.getName());
			}
		}

		// Reset the shared ModelManager to clear all caches - affects all active
		// sessions
		sharedModelManager.reset();
		modelUploads = new ItemCollection();
	}

	/**
	 * Deletes the given model from the database and resets the shared ModelManager
	 * cache so all active user sessions see the change immediately.
	 *
	 * @param modelversion - the model version to delete
	 * @throws AccessDeniedException if the current user lacks permission
	 * @throws ModelException        if the model version is invalid
	 */
	public void deleteModel(String modelversion) throws AccessDeniedException, ModelException {
		modelService.deleteModelData(modelversion);
		// Reset the shared ModelManager to clear all caches - affects all active
		// sessions
		sharedModelManager.reset();
	}

	/**
	 * Returns a process entity for a given model version and process ID, or null if
	 * no matching entity exists.
	 *
	 * @param processid    - the process ID to look up
	 * @param modelversion - the model version to search in
	 * @return matching process entity or null
	 */
	public ItemCollection getProcessEntity(int processid, String modelversion) {
		try {
			BPMNModel model = getModelManager().getModel(modelversion);
			return getModelManager().findTaskByID(model, processid);
		} catch (ModelException | InvalidAccessException e) {
			logger.warning("Unable to load task " + processid + " in model version '" + modelversion + "' - "
					+ e.getMessage());
		}
		return null;
	}

	/**
	 * Returns the documentation of a process entity identified by its process ID
	 * and model version. Dynamic text replacement is applied using the given
	 * document context.
	 *
	 * @param processid       - the process ID
	 * @param modelversion    - the model version
	 * @param documentContext - the workitem used for text replacement
	 * @return resolved description string, or empty string if not found
	 */
	public String getProcessDescription(int processid, String modelversion, ItemCollection documentContext) {
		ItemCollection pe = null;
		try {
			BPMNModel model = getModelManager().getModel(modelversion);
			pe = getModelManager().findTaskByID(model, processid);
		} catch (ModelException | InvalidAccessException e1) {
			logger.warning("Unable to load task " + processid + " in model version '" + modelversion + "' - "
					+ e1.getMessage());
		}
		if (pe == null) {
			return "";
		}
		String desc = pe.getItemValueString(BPMNUtil.TASK_ITEM_DOCUMENTATION);
		try {
			desc = workflowService.adaptText(desc, documentContext);
		} catch (PluginException e) {
			logger.warning("Unable to update processDescription: " + e.getMessage());
		}
		return desc;
	}

	/**
	 * Returns the documentation of the initial task for a given workflow group,
	 * with textblock entries resolved.
	 * <p>
	 * A dummy workitem is created to resolve the correct textblock context.
	 *
	 * @param initialTask   - the initial task entity
	 * @param modelVersion  - the model version
	 * @param workflowGroup - the workflow group name
	 * @return resolved description string, or empty string if not found
	 */
	public String getProcessDescriptionByInitialTask(ItemCollection initialTask, String modelVersion,
			String workflowGroup) {
		String result = "";
		if (initialTask != null) {
			// Create a dummy workitem to resolve the correct textblock context
			ItemCollection dummy = new ItemCollection();
			dummy.setItemValue(WorkflowKernel.WORKFLOWSTATUS, initialTask.getItemValueString("name"));
			dummy.setItemValue(WorkflowKernel.WORKFLOWGROUP, workflowGroup);
			result = getProcessDescription(initialTask.getItemValueInteger("taskid"), modelVersion, dummy);
		}
		return result;
	}

	/**
	 * Returns the list of model warnings collected during model validation.
	 * Delegates to the SharedModelManager.
	 *
	 * @return unmodifiable list of warning messages
	 */
	public List<String> getModelWarnings() {
		return new ArrayList<>(sharedModelManager.getModelWarnings());
	}
}