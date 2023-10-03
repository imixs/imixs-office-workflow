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

import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.imixs.workflow.FileData;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.Model;
import org.imixs.workflow.bpmn.BPMNModel;
import org.imixs.workflow.bpmn.BPMNParser;
import org.imixs.workflow.engine.ModelService;
import org.imixs.workflow.engine.SetupService;
import org.imixs.workflow.engine.WorkflowService;
import org.imixs.workflow.exceptions.AccessDeniedException;
import org.imixs.workflow.exceptions.ModelException;
import org.imixs.workflow.exceptions.PluginException;
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

	@EJB
	protected SetupService setupService;

	private Map<String, ItemCollection> modelEntityCache = new HashMap<String, ItemCollection>();

	private static Logger logger = Logger.getLogger(ModelController.class.getName());


	/**
	 * returns all groups for a version
	 * 
	 * @param version
	 * @return
	 */
	public List<String> getGroups(String version) {
		try {
			return modelService.getModel(version).getGroups();
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
	 */
	public List<String> getWorkflowGroups() {

		Set<String> set = new HashSet<String>();
		List<String> versions = modelService.getVersions();
		for (String version : versions) {
			try {
				if (version.startsWith("system-"))
					continue;
				set.addAll(modelService.getModel(version).getGroups());
			} catch (ModelException e) {
				e.printStackTrace();
			}
		}
		List<String> result = new ArrayList<>();

		// add all groups without '~'
		for (String agroup : set) {
			if (result.contains(agroup))
				continue;
			if (agroup.contains("~"))
				continue;
			result.add(agroup);
		}

		Collections.sort(result);
		return result;

	}

	/**
	 * Returns a String list of all Sub-WorkflowGroup names for a specified
	 * WorkflowGroup.
	 * 
	 * 
	 * A SubWorkflowGroup contains a '~' in its name.
	 * 
	 * The SubWorflowGroup list is used to assign sub workflow Group to a workitem
	 * 
	 * @see getWorkflowGroups()
	 * 
	 * @param parentWorkflowGroup
	 *            - the parent workflow group name
	 * @return list of all sub workflow groups for the given parent group name
	 */
	public List<String> getSubWorkflowGroups(String parentWorkflowGroup) {

		Set<String> set = new HashSet<String>();
		List<String> versions = modelService.getVersions();
		for (String version : versions) {
			try {
				if (version.startsWith("system-"))
					continue;
				set.addAll(modelService.getModel(version).getGroups());
			} catch (ModelException e) {
				e.printStackTrace();
			}
		}
		List<String> result = new ArrayList<>();

		// add all groups starting with GROUP~
		for (String agroup : set) {
			if (result.contains(agroup))
				continue;
			if (!agroup.startsWith(parentWorkflowGroup + "~"))
				continue;
			result.add(agroup);
		}

		Collections.sort(result);
		return result;

	}

	public Model getModelByWorkitem(ItemCollection workitem) {
		try {
			return modelService.getModelByWorkitem(workitem);
		} catch (ModelException e) {
			return null;
		}
	}

	/**
	 * Returns a model object for corresponding workflow group.
	 * 
	 * @param group
	 * @return
	 */
	public Model getModelByGroup(String group) {
		List<String> versions = modelService.findVersionsByGroup(group);
		if (!versions.isEmpty()) {
			String version = versions.get(0);
			try {
				return modelService.getModel(version);
			} catch (ModelException e) {
				logger.warning(e.getMessage());
				return null;
			}
		}
		return null;
	}

	/**
	 * returns all model versions
	 * 
	 * @return
	 */
	public List<String> getVersions() {
	    List<String> list=modelService.getVersions();
	    Collections.sort(list);
		return list;
	}

	public ItemCollection getModelEntity(String version) {

		ItemCollection result = modelEntityCache.get(version);
		if (result == null) {
			result = modelService.loadModelEntity(version);
			modelEntityCache.put(version, result);
		}

		return result;
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
		try {
			for (FileData file : fileList) {
				logger.info("Import bpmn-model: " + file.getName());

				// test if bpmn model?
				if (file.getName().endsWith(".bpmn")) {
					BPMNModel model = BPMNParser.parseModel(file.getContent(), "UTF-8");
					modelService.saveModel(model);
					continue;
				}
				// model type not supported!
				throw new ModelException(ModelException.INVALID_MODEL, "Invalid Model Type. Model can't be imported!");
			}

		} catch (ParseException | ParserConfigurationException | SAXException | IOException e) {
			// internal parsing error - convert to model exception
			throw new ModelException(ModelException.INVALID_MODEL, e.getMessage(), e);
		} finally {
			// Reset the ModelUpload Cache!
			modelUploadHandler.reset();
		}
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
	 *            - version for the model to search the process entity
	 * @param processid
	 *            - id of the process entity
	 * @return an instance of the matching process entity
	 * @throws ModelException
	 */
	public ItemCollection getProcessEntity(int processid, String modelversion) {
		try {
			return modelService.getModel(modelversion).getTask(processid);
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
			pe = modelService.getModel(modelversion).getTask(processid);
		} catch (ModelException e1) {
			logger.warning("Unable to load task " + processid + " in model version '" + modelversion + "' - "
					+ e1.getMessage());

		}
		if (pe == null) {
			return "";
		}
		//String desc = pe.getItemValueString("rtfdescription");
		String desc = pe.getItemValueString(BPMNModel.TASK_ITEM_DOCUMENTATION);
      

		try {
			desc = workflowService.adaptText(desc, documentContext);
		} catch (PluginException e) {
			logger.warning("Unable to update processDescription: " + e.getMessage());
		}

		return desc;

	}
}
