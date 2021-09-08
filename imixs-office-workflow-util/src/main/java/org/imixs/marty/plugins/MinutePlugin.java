package org.imixs.marty.plugins;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.mail.internet.AddressException;


import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.ItemCollectionComparator;
import org.imixs.workflow.PluginDependency;
import org.imixs.workflow.WorkflowKernel;
import org.imixs.workflow.engine.WorkflowService;
import org.imixs.workflow.engine.plugins.AbstractPlugin;
import org.imixs.workflow.exceptions.PluginException;

/**
 * This Plugin controls MinuteItems of a parent workflow.
 * <p>
 * A MinuteItem can be of any type (e.g. 'workitem' or 'childworkitem'). The
 * plugin number all MinuteItems automatically with a continuing
 * numSequenceNumber. The attribute 'minutetype' indicates if a workitem is a
 * minuteparent or a minuteitem.
 * <p>
 * When a new MinuteItem is created or has no sequencenumber, the plugin
 * computes the next sequencenumber automatically.
 * <p>
 * In case the minute parent is a version (UNIQUEIDSOURCE), than the plugin
 * copies all MinuteItems from the master and renumbers the MinuteItems
 * (sequencenumber).
 * 
 * <p>
 * The Plugin manges the items 'minuteparent' and 'minuteitem'. These items hold
 * a $uniqueID for the corresponding parent or minute entity.
 * 
 * <p>
 * If the Event sets the item 'resetminuteversionhistory' to the boolean value
 * 'true', the plugin will reset the version history.
 * 
 * @author rsoika
 * @version 2.0
 * 
 */
public class MinutePlugin extends AbstractPlugin  {
	private static Logger logger = Logger.getLogger(MinutePlugin.class.getName());

	public final static String MINUTE_TYPE_PARENT = "minuteparent";
	public final static String MINUTE_TYPE_ITEM = "minuteitem";
	public final static String SEQUENCENUMBER = "numsequencenumber";
	public final static String MINUTETYPE = "minutetype";

	public final static String RESET_MINUTE_VERSION_HISTORY = "resetminuteversionhistory";

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

		// update the attribute minuteType)
		String minuteType = updateMinuteType(documentContext);

		if (MINUTE_TYPE_ITEM.equals(minuteType)) {
			// Compute a sequencenumber for new child workitems
			if (documentContext.getItemValueInteger(SEQUENCENUMBER) <= 0
					&& documentContext.getType().contains("workitem")) {
				computeNextSequenceNumber(documentContext);
			}
			// Update the parent fields _team and _present
			updateHeaderItems(documentContext);
		}

		// Parent
		if (MINUTE_TYPE_PARENT.equals(minuteType)) {

			// test if we need to overtake the minute workitems from the master and reset
			// the $created....
			if (documentContext.hasItem(WorkflowKernel.UNIQUEIDSOURCE)
					&& documentContext.getItemValueBoolean(WorkflowKernel.ISVERSION)) {

				String masterUniqueID = documentContext.getItemValueString(WorkflowKernel.UNIQUEIDSOURCE);
				ItemCollection master = this.getWorkflowService().getWorkItem(masterUniqueID);
				if (master != null) {
					// take all childs which are still active (type=workitem or childworkitem)
					List<ItemCollection> childs = this.getWorkflowService().getWorkListByRef(master.getUniqueID());
					List<ItemCollection> newMinuteList = new ArrayList<ItemCollection>();
					for (ItemCollection minute : childs) {
						String stype = minute.getType();
						if (minute != null && MINUTE_TYPE_ITEM.equals(minute.getItemValueString(MINUTETYPE))
								&& ("workitem".equals(stype) || "childworkitem".equals(stype))) {
							newMinuteList.add(minute);
						}
					}
					// sort new minutes by old sequence number...
					Collections.sort(newMinuteList, new ItemCollectionComparator("numsequencenumber", true));

					// renumber all minutes and set the new WORKITEMIDREF....
					for (int i = 0; i < newMinuteList.size(); i++) {
						ItemCollection minute = newMinuteList.get(i);
						minute.removeItem(WorkflowKernel.UNIQUEID);
						minute.replaceItemValue(WorkflowService.UNIQUEIDREF, documentContext.getUniqueID());
						minute.replaceItemValue(SEQUENCENUMBER, i + 1);
						// save it....
						this.getWorkflowService().getDocumentService().save(minute);
					}
					logger.fine("Copied " + newMinuteList.size() + " sucessfull");

				}

				// now we reset the creation date
				logger.fine("reset itemvalue $CREATED for new version...");
				documentContext.removeItem("$created");

				ItemCollection evalResult = this.getWorkflowService().evalWorkflowResult(documentActivity,"item",
						documentContext);
				if (evalResult != null && evalResult.getItemValueBoolean(RESET_MINUTE_VERSION_HISTORY)) {
					logger.fine("reset version history....");
					documentContext.replaceItemValue("$WorkItemID", WorkflowKernel.generateUniqueID());
					documentContext.removeItem(RESET_MINUTE_VERSION_HISTORY);
				}

			}
		}

		return documentContext;
	}

	/**
	 * This method verifies if the current workitem has already a minutetype. If not
	 * the minutetype will be updated and set to 'minuteitem' or 'minuteparent'. If
	 * the workitem is a minute item, the minute parent uniqueid will be stored into
	 * the filed 'minuteParentRef'.
	 * 
	 * 
	 */
	@SuppressWarnings("unchecked")
	private String updateMinuteType(ItemCollection documentContext) {
		ItemCollection parent = null;
		String minutetype = documentContext.getItemValueString(MINUTETYPE);

		if (!minutetype.isEmpty()) {
			// we already identified this type..
			return minutetype;
		}

		// we have not yet computed, so we need to verify it.
		boolean foundMinuteParent = false;
		List<String> uniqueIdRefList = documentContext.getItemValue(WorkflowService.UNIQUEIDREF);
		for (String id : uniqueIdRefList) {
			parent = this.getWorkflowService().getWorkItem(id);
			if (parent != null) {
				// test if a parent workitem exits
				if (MINUTE_TYPE_PARENT.equals(parent.getItemValueString(MINUTETYPE))) {
					foundMinuteParent = true;
					break;
				}
			}
		}

		if (foundMinuteParent) {
			// it is a minute item!
			documentContext.replaceItemValue(MINUTETYPE, MINUTE_TYPE_ITEM);
			documentContext.replaceItemValue("minuteParentRef", parent.getUniqueID());
			return MINUTE_TYPE_ITEM;
		} else {
			// Mark this workitem as a MINUTE_TYPE_PARENT
			logger.fine("mark workitem as minute parent");
			documentContext.replaceItemValue(MINUTETYPE, MINUTE_TYPE_PARENT);
			// Temporally store ....
			// This is because in the case that a minute-item was created (splitPlugin)
			// before the parent was saved the first time (first process step), a lookup for
			// this parent later did not return the parent.
			// The DocumentService is clever enough to handle this case :-)
			// But we need to avoid the creation of a snapshot as we did not know the new
			// ACL yet. For this reason we set the $nosnapshot flag temporally to true
			documentContext.setItemValue("$nosnapshot", true);
			this.getWorkflowService().getDocumentService().save(documentContext);
			// remove the nosnapshot flag!
			documentContext.removeItem("$nosnapshot");
			return MINUTE_TYPE_PARENT;
		}
	}

	/**
	 * This method compute a new numsequencenumber for a minute item. The next
	 * number is computed based on the highest available number.
	 * 
	 * See issue #234
	 * 
	 */
	private void computeNextSequenceNumber(ItemCollection documentContext) {
		// test if minute parent workitem exits
		ItemCollection parent = this.getWorkflowService()
				.getWorkItem(documentContext.getItemValueString("minuteParentRef"));
		int bestNumber = 1;
		if (parent != null) {
			// find all minute itmes and comptue the next Number...
			List<ItemCollection> childs = this.getWorkflowService().getWorkListByRef(parent.getUniqueID(), null, 999, 0,
					null, false);
			for (ItemCollection minute : childs) {
				if (minute != null && MINUTE_TYPE_ITEM.equals(minute.getItemValueString(MINUTETYPE))
						&& minute.getItemValueInteger(SEQUENCENUMBER) > 0) {
					int currentNumber = minute.getItemValueInteger(SEQUENCENUMBER);
					if (currentNumber >= bestNumber) {
						bestNumber = currentNumber + 1;
					}
				}
			}
			documentContext.replaceItemValue(SEQUENCENUMBER, bestNumber);
		}

	}

	/**
	 * This method updtes the header items _team and _present form the parent
	 * workitem.
	 * 
	 */
	private void updateHeaderItems(ItemCollection documentContext) {
		// test if minute parent workitem exits
		ItemCollection parent = this.getWorkflowService()
				.getWorkItem(documentContext.getItemValueString("minuteParentRef"));
		if (parent != null) {
			documentContext.replaceItemValue("_team", parent.getItemValue("_team"));
			documentContext.replaceItemValue("_present", parent.getItemValue("_present"));

		}

	}

	
}