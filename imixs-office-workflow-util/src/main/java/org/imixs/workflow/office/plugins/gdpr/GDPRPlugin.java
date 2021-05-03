package org.imixs.workflow.office.plugins.gdpr;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.mail.internet.AddressException;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.WorkflowKernel;
import org.imixs.workflow.engine.WorkflowService;
import org.imixs.workflow.engine.plugins.AbstractPlugin;
import org.imixs.workflow.engine.plugins.SplitAndJoinPlugin;
import org.imixs.workflow.exceptions.AccessDeniedException;
import org.imixs.workflow.exceptions.InvalidAccessException;
import org.imixs.workflow.exceptions.ModelException;
import org.imixs.workflow.exceptions.PluginException;
import org.imixs.workflow.exceptions.ProcessingErrorException;
import org.imixs.workflow.exceptions.QueryException;
import org.imixs.workflow.util.XMLParser;

/**
 * This Plugin is used for GDPR Workflow.
 * 
 * 
 * <h1>GDPR Request</h1>
 * The plugin controls GDPR Request and creates sub tasks. The plugin is triggered by the GDPR
 * process 'Request' and creates a subtask for the Workfow 'Process Request' for
 * each available request type 'System'.
 * 
 * To trigger the creation process the following item definition is used:
 * 
 * <pre>
 * {@code
 * 
 *     <item name="gdpr">
 *     	  <mode>create|update</mode>
 *        <processid>100</processid>
 *        <activityid>10</activityid>
 *        <items>namTeam,txtname</items>
 *     </item>
 * 
 * }
 * </pre>
 * 
 * A subprocess can either be created or updated. The mode can be set by the tag
 * <mode>
 * 
 * 
 * <h1>DSFA</h1>
 * The plugin copies each ID form _affected_systems into txtworkitemref
 * 
 * @author rsoika
 * @version 2.0
 * 
 */
public class GDPRPlugin extends AbstractPlugin {
	private static Logger logger = Logger.getLogger(GDPRPlugin.class.getName());

	public static String PROCESSING_ERROR = "PROCESSING_ERROR";

	/**
	 * The method verifies if a sequencenumber is set. If not a new sequencenumber
	 * is computed.
	 * <p>
	 * If a version was created, all workitems from the master with the types
	 * 'workitem' or 'childworkitem' are copied into the new version. . In this case
	 * the minute items are renumbered. *
	 * 
	 * @return
	 * @throws PluginException
	 * @throws AddressException
	 */
	@Override
	public ItemCollection run(ItemCollection documentContext, ItemCollection documentActivity) throws PluginException {

		// do we have a gdpr case?
		ItemCollection evalItemCollection = this.getWorkflowService().evalWorkflowResult(documentActivity,"item",
				documentContext);

		// find the data object
		if (evalItemCollection != null && evalItemCollection.hasItem("gdpr")) {

			String processDefinition = evalItemCollection.getItemValueString("gdpr");
			// evaluate the item content (XML format expected here!)
			ItemCollection processingData = XMLParser.parseItemStructure(processDefinition);

			// create subsystems?
			if ("create".equalsIgnoreCase(processingData.getItemValueString("mode"))) {
				createGdprSubTasks(documentContext, processingData);
			}

			// ubdate exsisting sub tasks?
			if ("update".equalsIgnoreCase(processingData.getItemValueString("mode"))) {
				updateGdprSubTasks(documentContext, processingData);
			}

		}

		// do we have a _affected_systems
		// is used by workflow 'DSFA'
		if (documentContext.hasItem("_affected_systems")) {
			// we copy all uniqueids into the txtworkitemref
			String saffectedSystems = documentContext.getItemValueString("_affected_systems");

			String[] list = saffectedSystems.split(",");
			List<String> newList = new ArrayList<String>();
			for (String id : list) {
				newList.add(id);
			}
			documentContext.replaceItemValue(SplitAndJoinPlugin.LINK_PROPERTY, newList);
		}

		return documentContext;
	}

	/**
	 * Creats a new system subtask. The model version is taken form the
	 * documentContext
	 * 
	 * @param system
	 * @param documentContext
	 * @throws PluginException
	 */
	private void createGdprSubTasks(ItemCollection workItem, ItemCollection processingData) throws PluginException {
		logger.finest("......creating gdpr sub tasks...");

		int processID = processingData.getItemValueInteger("processid");
		int activityID = processingData.getItemValueInteger("activityid");

		List<ItemCollection> systems = findActiveSystems();
		if (systems != null) {
			for (ItemCollection system : systems) {
				// create new Workitem.....
				ItemCollection subWorkitem = new ItemCollection();

				subWorkitem.replaceItemValue(WorkflowKernel.MODELVERSION, workItem.getModelVersion());
				subWorkitem.replaceItemValue(WorkflowKernel.TASKID, processID);
				subWorkitem.setEventID(activityID);

				subWorkitem.replaceItemValue("$uniqueidref", workItem.getUniqueID());
				subWorkitem.replaceItemValue("txtprocessref", workItem.getItemValueString("txtprocessRef"));
				subWorkitem.replaceItemValue("txtspaceref", system.getUniqueID());
				subWorkitem.replaceItemValue("_gdpr_parent_uniqueid", workItem.getUniqueID());

				// add the origin reference
				subWorkitem.replaceItemValue(SplitAndJoinPlugin.LINK_PROPERTY, workItem.getUniqueID());

				// now clone the field list...
				copyItemList(processingData.getItemValueString("items"), workItem, subWorkitem);

				// copy from system subject and team information
				subWorkitem.replaceItemValue("_system_subject", system.getItemValueString("_subject"));
				subWorkitem.replaceItemValue("namteam", system.getItemValueString("namteam"));

				try {
					this.getWorkflowService().processWorkItem(subWorkitem);
				} catch (AccessDeniedException | ProcessingErrorException | ModelException e) {
					throw new PluginException(GDPRPlugin.class.getName(), PROCESSING_ERROR, e.getMessage(), e);
				}
			}
		}
	}

	/**
	 * Update running subtasks...
	 * 
	 * @param workItem
	 * @param processingData
	 * @throws PluginException
	 */
	private void updateGdprSubTasks(ItemCollection workItem, ItemCollection processingData) throws PluginException {
		logger.finest("......updateing gdpr sub tasks...");

		int processID = processingData.getItemValueInteger("processid");
		int activityID = processingData.getItemValueInteger("activityid");

		List<ItemCollection> subTasks = findSubTasks(workItem.getItemValueString(WorkflowService.UNIQUEIDREF));

		for (ItemCollection subtask : subTasks) {
			if (processID == subtask.getTaskID()) {

				subtask.setEventID(activityID);
				// now clone the field list...
				copyItemList(processingData.getItemValueString("items"), workItem, subtask);
				try {
					this.getWorkflowService().processWorkItem(subtask);
				} catch (AccessDeniedException | ProcessingErrorException | ModelException e) {
					throw new PluginException(GDPRPlugin.class.getName(), PROCESSING_ERROR, e.getMessage(), e);
				}
			}
		}

	}

	/**
	 * This method returns all active GDPR systems (processid=1100 - 1899)
	 * 
	 * @return
	 */
	private List<ItemCollection> findActiveSystems() {

		String sQuery = "(type:\"workitem\" AND $processid:[1100 TO 1899] )";

		List<ItemCollection> systemList;
		try {
			systemList = getWorkflowService().getDocumentService().find(sQuery, 999, 0);
		} catch (QueryException e) {
			throw new InvalidAccessException(InvalidAccessException.INVALID_ID, e.getMessage(), e);
		}

		return systemList;

	}

	/**
	 * This method returns all active GDPR systems (processid=1100 - 1899)
	 * 
	 * @return
	 */
	private List<ItemCollection> findSubTasks(String parentRef) {

		String sQuery = "(type:\"workitem\" AND $uniqueidref:\"" + parentRef + "\")";

		List<ItemCollection> systemList;
		try {
			systemList = getWorkflowService().getDocumentService().find(sQuery, 999, 0);
		} catch (QueryException e) {
			throw new InvalidAccessException(InvalidAccessException.INVALID_ID, e.getMessage(), e);
		}

		return systemList;

	}

	/**
	 * This Method copies the fields defined in 'items' into the targetWorkitem.
	 * Multiple values are separated with comma ','.
	 * 
	 * In case a item name contains '|' the target field name will become the right
	 * part of the item name.
	 */
	private void copyItemList(String items, ItemCollection source, ItemCollection target) {
		// clone the field list...
		StringTokenizer st = new StringTokenizer(items, ",");
		while (st.hasMoreTokens()) {
			String field = st.nextToken().trim();

			int pos = field.indexOf('|');
			if (pos > -1) {
				target.replaceItemValue(field.substring(pos + 1).trim(),
						source.getItemValue(field.substring(0, pos).trim()));
			} else {
				target.replaceItemValue(field, source.getItemValue(field));
			}
		}
	}

}