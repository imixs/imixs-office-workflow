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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.ItemCollectionComparator;
import org.imixs.workflow.engine.DocumentService;
import org.imixs.workflow.engine.ModelService;
import org.imixs.workflow.engine.WorkflowService;
import org.imixs.workflow.exceptions.ModelException;
import org.imixs.workflow.faces.data.WorkflowController;
import org.imixs.workflow.faces.data.WorkflowEvent;

import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;

/**
 * The SkillController provides methods to create skill documents within a JSF
 * view.
 * 
 * The controller expects a BPMN Model with the workflow group 'skill'
 * 
 * @author rsoika
 */
@Named
@SessionScoped
public class SkillController implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	protected WorkflowController workflowController;

	@Inject
	WorkflowService workflowService;

	@Inject
	protected DocumentService documentService;

	@Inject
	protected ModelService modelService;

	private List<ItemCollection> skills = null;

	private static Logger logger = Logger.getLogger(SkillController.class.getName());

	/**
	 * This method creates a skill with in a tree structure. The param 'parentID' is
	 * optional. In case a
	 * parentID is provided, the method creates a sub-skill. If no 'parentID' is
	 * provided or the id is empty than a root skill is created.
	 * 
	 * @return
	 * @throws ModelException
	 */
	public void create(String parentID) throws ModelException {

		logger.info("├── Create Skill");
		logger.info("│   ├── ParentID: " + parentID);
		String modelVersion = modelService.findVersionByGroup("Skill");
		logger.info("│   ├── modelVersion: " + modelVersion);

		if (parentID != null && !parentID.isEmpty()) {
			// create sub space
			ItemCollection parentSkill = workflowService.getWorkItem(parentID);
			if (parentSkill != null) {
				workflowController.create(modelVersion, 100, parentID);
				workflowController.getWorkitem().setItemValue("skill.parent.name",
						parentSkill.getItemValueString("name"));
			} else {
				logger.warning("Can not create sub skill - skill " + parentID + " not found!");
			}

		} else {
			// create root space
			workflowController.create(modelVersion, 100, null);
		}
	}

	/**
	 * On space save we verify if a parent skill is still provided
	 * 
	 * @param documentEvent
	 */
	public void onWorkflowEvent(@Observes WorkflowEvent documentEvent) {
		if (documentEvent == null) {
			return;
		}
		if (WorkflowEvent.WORKITEM_BEFORE_PROCESS == documentEvent.getEventType()) {
			if (documentEvent.getWorkitem().getType().startsWith("skill")) {
				String idRef = workflowController.getWorkitem().getItemValueString(WorkflowService.UNIQUEIDREF);
				logger.info("├── skill parent ref=" + idRef);
				if (idRef.isEmpty() || "[ROOT]".equals(idRef)) {
					logger.info("│   ├── remove parent");
					documentEvent.getWorkitem().setItemValue(WorkflowService.UNIQUEIDREF, "");
					documentEvent.getWorkitem().setItemValue("skill.parent.name", "");

				}
			}
		}
		if (WorkflowEvent.WORKITEM_AFTER_PROCESS == documentEvent.getEventType()) {
			skills = null;
		}
	}

	/**
	 * This method returns a cached list of skills
	 * 
	 * @return
	 */
	public List<ItemCollection> getSkills() {
		if (skills != null) {
			return skills;
		}

		logger.fine("lookup skills by type...");
		skills = documentService.getDocumentsByType("skill");

		// sort by name
		Collections.sort(skills, new ItemCollectionComparator("name", true));

		return skills;
	}

	/**
	 * Returns the skill list as a nested JSON tree structure, suitable for
	 * the skill-bubble visualization. The tree hierarchy is built using the
	 * full path field 'name', since 'skill.parent.name' also stores the
	 * parent's full path (see SkillController.create()). The short display
	 * name 'skill.name' is included separately, only used for rendering labels.
	 */
	public String getSkillTreeJson() {
		List<ItemCollection> skillList = getSkills();
		Map<String, JsonObjectBuilder> nodesByPath = new HashMap<>();
		Map<String, List<String>> childrenByParentPath = new HashMap<>();
		List<String> rootPaths = new ArrayList<>();

		for (ItemCollection skill : skillList) {
			// 'name' holds the full path and is guaranteed unique - used as the map key
			String path = skill.getItemValueString("name");
			// 'skill.name' holds the short display name - used only as a JSON field
			String shortName = skill.getItemValueString("skill.name");
			String parentPath = skill.getItemValueString("skill.parent.name");
			String topic = skill.getItemValueString("skill.topic");
			String description = skill.getItemValueString("skill.description");
			String id = skill.getUniqueID();

			nodesByPath.put(path, Json.createObjectBuilder()
					.add("name", shortName)
					.add("path", path)
					.add("topic", topic)
					.add("description", description)
					.add("id", id));

			if (parentPath == null || parentPath.isEmpty()) {
				rootPaths.add(path);
			} else {
				childrenByParentPath.computeIfAbsent(parentPath, k -> new ArrayList<>()).add(path);
			}
		}

		JsonArrayBuilder rootArray = Json.createArrayBuilder();
		for (String rootPath : rootPaths) {
			rootArray.add(buildTreeNode(rootPath, nodesByPath, childrenByParentPath));
		}
		return rootArray.build().toString();
	}

	/**
	 * Recursively attaches child nodes to build the nested tree structure.
	 */
	private JsonObjectBuilder buildTreeNode(String name, Map<String, JsonObjectBuilder> nodesByName,
			Map<String, List<String>> childrenByParent) {
		JsonObjectBuilder node = nodesByName.get(name);
		List<String> childNames = childrenByParent.get(name);

		if (childNames != null && !childNames.isEmpty()) {
			JsonArrayBuilder childArray = Json.createArrayBuilder();
			for (String childName : childNames) {
				childArray.add(buildTreeNode(childName, nodesByName, childrenByParent));
			}
			node.add("children", childArray);
		}
		return node;
	}
}
