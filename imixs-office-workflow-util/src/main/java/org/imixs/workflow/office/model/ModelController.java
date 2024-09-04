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
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.imixs.workflow.FileData;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.bpmn.BPMNUtil;
import org.imixs.workflow.engine.ModelService;
import org.imixs.workflow.engine.WorkflowService;
import org.imixs.workflow.exceptions.AccessDeniedException;
import org.imixs.workflow.exceptions.ModelException;
import org.imixs.workflow.exceptions.PluginException;
import org.openbpmn.bpmn.BPMNModel;
import org.openbpmn.bpmn.exceptions.BPMNModelException;
import org.openbpmn.bpmn.util.BPMNModelFactory;
import org.xml.sax.SAXException;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.event.ActionEvent;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * The ModelController provides informations about workflow models.
 * 
 * A ModelVersion is always expected in the format
 * 
 * 'DOMAIN-LANGUAGE-VERSIONNUMBER'
 * 
 * e.g. office-de-0.1, support-en-2.0, system-de-0.0.1
 * 
 * 
 * @author rsoika
 * 
 */
@Named
@RequestScoped
public class ModelController implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	protected ModelUploadHandler modelUploadHandler;

	@EJB
	protected ModelService modelService;

	@EJB
	protected WorkflowService workflowService;

	private static Logger logger = Logger.getLogger(ModelController.class.getName());

	/**
	 * returns all groups for a version
	 * 
	 * @param version
	 * @return
	 */
	public List<String> getGroups(String version) {
		try {
			BPMNModel model = modelService.getModelManager().getModel(version);
			Set<String> groups = modelService.getModelManager().findAllGroupsByModel(model);
			// Convert the Set to a List
			return new ArrayList<>(groups);
		} catch (ModelException e) {
			logger.warning("Unable to load groups:" + e.getMessage());
		}
		// return empty result
		return new ArrayList<String>();
	}

	/**
	 * Returns a String list of all WorkflowGroup names.
	 * 
	 * Workflow groups of the system model will be skipped.
	 * 
	 * A workflowGroup with a '~' in its name will be skipped. This indicates a
	 * child process.
	 * 
	 * The worflowGroup list is used to assign a workflow Group to a core process.
	 * 
	 * @return list of workflow groups
	 * @throws ModelException
	 */
	public List<String> getWorkflowGroups() throws ModelException {
		List<String> result = new ArrayList<>();
		for (BPMNModel model : modelService.getModelManager().getAllModels()) {
			String version = BPMNUtil.getVersion(model);
			// Skip system model..
			if (version.startsWith("system-")) {
				continue;
			}
			Set<String> groups = modelService.getModelManager().findAllGroupsByModel(model);
			for (String groupName : groups) {
				if (result.contains(groupName))
					continue;
				if (groupName.contains("~"))
					continue;
				result.add(groupName);
			}
		}
		Collections.sort(result);
		return result;
	}

	/**
	 * Returns the ModelVersion of a given group name
	 * 
	 * @param group
	 * @return
	 * @throws ModelException
	 */
	public String getVersionByGroup(String group) throws ModelException {
		return modelService.getModelManager().findVersionByGroup(group);
	}

	/**
	 * Returns a list of all Imixs Tasks for a given group
	 * 
	 * @return
	 */
	public List<ItemCollection> findAllTasksByGroup(String group) {
		List<ItemCollection> result = new ArrayList<>();
		try {

			BPMNModel model = modelService.getModelManager().getModel(group);
		} catch (ModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * Returns a list of all Imixs Start Tasks for a given group
	 * 
	 * @return
	 */
	public List<ItemCollection> findAllStartTasksByGroup(String version, String group) {
		List<ItemCollection> result = new ArrayList<>();
		try {
			BPMNModel model = modelService.getModelManager().getModel(version);
			return modelService.getModelManager().findStartTasks(model, group);
		} catch (ModelException e) {
			logger.severe(
					"Failed to find start tasks for workflow group '" + group + "' : " + e.getMessage());
		}
		return result;
	}

	/**
	 * Returns a sorted set of all model versions.
	 * <p>
	 * Used by the Model View.
	 * 
	 * @return
	 */
	public Set<String> getVersions() {
		return modelService.getModelEntityStore().keySet();
	}

	/**
	 * Returns a Model Entity. Used by the Model View
	 * 
	 * @param version
	 * @return
	 */
	public ItemCollection getModelEntity(String version) {
		return modelService.loadModel(version);
	}

	/**
	 * This method adds all uploaded model files. The method tests the model type
	 * (.bmpm, .ixm). BPMN Model will be handled by the ImixsBPMNParser. A .ixm file
	 * will be imported using the default import mechanism.
	 * 
	 * @param event
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws ParseException
	 * @throws ModelException
	 * 
	 */
	public void doUploadModel(ActionEvent event)
			throws ModelException {
		List<FileData> fileList = modelUploadHandler.getModelUploads().getFileData();

		if (fileList == null) {
			return;
		}
		for (FileData file : fileList) {

			// test if bpmn model?
			if (file.getName().endsWith(".bpmn")) {
				InputStream inputStream = new ByteArrayInputStream(file.getContent());
				BPMNModel model;
				try {
					model = BPMNModelFactory.read(inputStream);
					modelService.saveModel(model);
					continue;
				} catch (BPMNModelException e) {
					throw new ModelException(ModelException.INVALID_MODEL,
							"Unable to read model file: " + file.getName(), e);
				}
			} else {
				// model type not supported!
				logger.log(Level.WARNING, "Invalid Model Type. Model {0} can't be imported!", file.getName());
			}
		}
		modelUploadHandler.reset();
	}

	/**
	 * This Method deletes the given model from the database and the internal model
	 * cache.
	 * 
	 * @throws AccessDeniedException
	 * @throws ModelException
	 */
	public void deleteModel(String modelversion) throws AccessDeniedException, ModelException {
		modelService.deleteModel(modelversion);
	}

	/**
	 * This method returns a process entity for a given ModelVersion or null if no
	 * entity exists.
	 * 
	 * 
	 * @param modelVersion
	 *                     - version for the model to search the process entity
	 * @param processid
	 *                     - id of the process entity
	 * @return an instance of the matching process entity
	 * @throws ModelException
	 */
	public ItemCollection getProcessEntity(int processid, String modelversion) {
		try {
			BPMNModel model = modelService.getModelManager().getModel(modelversion);
			return modelService.getModelManager().findTaskByID(model, processid);
		} catch (ModelException e) {
			logger.warning("Unable to load task " + processid + " in model version '" + modelversion + "' - "
					+ e.getMessage());
		}
		return null;
	}

	/**
	 * This method return the 'rtfdescription' of a processentity and applies the
	 * dynamic Text replacement function from the jee plugin
	 * 
	 * @param processid
	 * @param modelversion
	 * @return
	 * @throws ModelException
	 */
	public String getProcessDescription(int processid, String modelversion, ItemCollection documentContext) {
		ItemCollection pe = null;
		try {
			BPMNModel model = modelService.getModelManager().getModel(modelversion);
			pe = modelService.getModelManager().findTaskByID(model, processid);
		} catch (ModelException e1) {
			logger.warning("Unable to load task " + processid + " in model version '" + modelversion + "' - "
					+ e1.getMessage());
		}
		if (pe == null) {
			return "";
		}
		// String desc = pe.getItemValueString("rtfdescription");
		String desc = pe.getItemValueString(BPMNUtil.TASK_ITEM_DOCUMENTATION);
		try {
			desc = workflowService.adaptText(desc, documentContext);
		} catch (PluginException e) {
			logger.warning("Unable to update processDescription: " + e.getMessage());
		}
		return desc;
	}
}
