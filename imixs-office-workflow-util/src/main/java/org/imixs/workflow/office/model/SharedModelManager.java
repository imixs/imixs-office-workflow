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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.ModelManager;
import org.imixs.workflow.WorkflowKernel;
import org.imixs.workflow.bpmn.BPMNUtil;
import org.imixs.workflow.engine.WorkflowService;
import org.imixs.workflow.exceptions.InvalidAccessException;
import org.imixs.workflow.exceptions.ModelException;
import org.imixs.workflow.exceptions.PluginException;
import org.imixs.workflow.faces.data.WorkflowController;
import org.openbpmn.bpmn.BPMNModel;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;

/**
 * The SharedModelManager is a application-scoped singleton EJB that holds a
 * shared instance of the {@link ModelManager} POJO for use in the UI layer
 * (e.g. ModelController). Used instead of creating a local ModelManager
 * instance.
 * <p>
 * In contrast to the ModelManager instances used by the WorkflowKernel during
 * processing - which are always session or request-local - this shared instance
 * is intentionally global and serves as a cache for model meta data in the
 * frontend.
 * <p>
 * The internal ModelManager can be reset at any time (e.g. after a model upload
 * or deletion) without affecting any ongoing workflow processing, since the
 * WorkflowKernel always holds its own local ModelManager instance during a
 * transaction.
 *
 * @see ModelController
 * @see ModelManager
 * @author rsoika
 */
@Singleton
public class SharedModelManager {

    private static final Logger logger = Logger.getLogger(SharedModelManager.class.getName());

    @Inject
    WorkflowService workflowService;

    // The shared ModelManager instance - volatile to ensure visibility across
    // threads
    private volatile ModelManager modelManager;

    // Collect model warnings for frontend display
    private final Set<String> modelWarnings = Collections.synchronizedSet(new LinkedHashSet<>());

    /**
     * Initializes the shared ModelManager instance on startup.
     */
    @PostConstruct
    public void init() {
        modelManager = new ModelManager(workflowService);
        logger.info("├── SharedModelManager initialized.");
    }

    /**
     * Returns the shared ModelManager instance.
     * <p>
     * Note: this instance is shared across all active user sessions. It is suitable
     * for read-only UI operations (e.g. resolving model groups, start tasks,
     * process descriptions). It must not be used inside workflow processing
     * transactions.
     *
     * @return the shared {@link ModelManager} instance
     */
    public ModelManager getModelManager() {
        return modelManager;
    }

    /**
     * Resets the shared ModelManager by creating a new instance.
     * <p>
     * This clears all internal caches (modelStore, bpmnEntityCache,
     * bpmnElementCache, groupCache) and forces a fresh reload of model data on the
     * next access.
     * <p>
     * This method should be called after a model upload or deletion to ensure all
     * active user sessions see the updated model data immediately.
     * <p>
     * Ongoing workflow processing is not affected since the WorkflowKernel holds
     * its own local ModelManager instance.
     */
    public synchronized void reset() {
        modelManager = new ModelManager(workflowService);
        modelWarnings.clear();
        logger.info("├── SharedModelManager reset - all model caches cleared.");
    }

    public void addModelWarning(String message) {
        modelWarnings.add(message);
    }

    public Set<String> getModelWarnings() {
        return modelWarnings;
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
            BPMNModel model = modelManager.getModel(modelversion);
            pe = modelManager.findTaskByID(model, processid);
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
            BPMNModel model = modelManager.getModel(version);
            List<ItemCollection> _result = modelManager.findStartTasks(model, group);

            // Validate each start task - type is a mandatory field
            for (ItemCollection task : _result) {
                String type = task.getItemValueString("txttype");
                if (!type.isEmpty() && !WorkflowController.DEFAULT_TYPE.equals(type)) {
                    String msg = "Invalid initial task in model='" + version + "' workflowGroup='"
                            + group + "' task=" + task.getItemValueString("numProcessID")
                            + " wrong type='" + type + "' -> expected: '" + WorkflowController.DEFAULT_TYPE + "'";
                    logger.warning(msg);
                    addModelWarning(msg);
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
     * Returns a BPMN form definition associated with a given task ItemCollection.
     * 
     * The form definition is read from an optional <code>bpmn:DataObject</code>
     * associated with the current task element. A <code>bpmn:DataObject</code> must
     * contain a `form-tag` containing the form definition. If not matching
     * <code>bpmn:DataObject</code> is defined the method returns an empty string.
     * 
     * @param workitem
     * @return
     */
    @SuppressWarnings("unchecked")
    public String fetchFormDefinitionByTask(ItemCollection task) {

        // return if no modelversion is defined
        if (task == null) {
            return "";
        }

        List<List<String>> dataObjects = task.getItemValue("dataObjects");
        for (List<String> dataObject : dataObjects) {
            // there can be more than one dataOjects be attached.
            // We need the one with the tag <imixs-form>
            String templateName = dataObject.get(0);
            String content = dataObject.get(1);
            // we expect that the content contains at least one occurrence of <imixs-form>
            if (content.contains("<imixs-form>")) {
                logger.finest("......DataObject name=" + templateName);
                logger.finest("......DataObject content=" + content);
                return content;
            } else {
                // seems not to be a imixs-form definition!
            }
        }
        // nothing found!
        return "";
    }
}