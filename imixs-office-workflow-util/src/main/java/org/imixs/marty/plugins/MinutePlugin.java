package org.imixs.marty.plugins;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.mail.internet.AddressException;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.WorkflowKernel;
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
public class MinutePlugin extends AbstractPlugin {
    private static Logger logger = Logger.getLogger(MinutePlugin.class.getName());

    public static final String CHILD_ITEM_PROPERTY = "_ChildItems";

    public final static String RESET_MINUTE_VERSION_HISTORY = "resetminuteversionhistory";

    /**
     * If a version was created, all workitems from the master with the types
     * 'workitem' or 'childworkitem' are copied into the new version.
     * 
     * @return
     * @throws PluginException
     * @throws AddressException
     */
    @Override
    public ItemCollection run(ItemCollection documentContext, ItemCollection documentActivity) throws PluginException {

        // test if we need to overtake the minute workitems from the master and reset
        // the $created....
        if (documentContext.hasItem(WorkflowKernel.UNIQUEIDSOURCE)
                && documentContext.getItemValueBoolean(WorkflowKernel.ISVERSION)) {

            String masterUniqueID = documentContext.getItemValueString(WorkflowKernel.UNIQUEIDSOURCE);
            ItemCollection master = this.getWorkflowService().getWorkItem(masterUniqueID);
            if (master != null) {
                // take all childs which are still active (type=workitem or childworkitem)
                List<ItemCollection> childs = explodeChildList(documentContext);
                List<ItemCollection> newMinuteList = new ArrayList<ItemCollection>();
                for (ItemCollection minute : childs) {
                    String stype = minute.getItemValueString("minute.type");
                    if (minute != null && stype.equals("task")) {
                        newMinuteList.add(minute);
                    }
                }
                implodeChildList(documentContext, newMinuteList);
                logger.fine("Copied " + newMinuteList.size() + " sucessfull");

            }

            // now we reset the creation date
            logger.fine("reset itemvalue $CREATED for new version...");
            documentContext.removeItem("$created");

            ItemCollection evalResult = this.getWorkflowService().evalWorkflowResult(documentActivity, "item",
                    documentContext);
            if (evalResult != null && evalResult.getItemValueBoolean(RESET_MINUTE_VERSION_HISTORY)) {
                logger.fine("reset version history....");
                documentContext.replaceItemValue("$WorkItemID", WorkflowKernel.generateUniqueID());
                documentContext.removeItem(RESET_MINUTE_VERSION_HISTORY);
            }

        }

        return documentContext;
    }

    /**
     * Convert the List of ItemCollections back into a List of Map elements
     * 
     * @param workitem
     */
    @SuppressWarnings({ "rawtypes" })
    protected void implodeChildList(ItemCollection workitem, List<ItemCollection> childItems) {
        List<Map> mapOrderItems = new ArrayList<Map>();
        // convert the child ItemCollection elements into a List of Map
        if (childItems != null) {
            logger.fine("Convert child items into Map...");
            // iterate over all order items..
            for (ItemCollection orderItem : childItems) {
                mapOrderItems.add(orderItem.getAllItems());
            }
            workitem.replaceItemValue(CHILD_ITEM_PROPERTY, mapOrderItems);
        }
    }

    /**
     * converts the Map List of a workitem into a List of ItemCollectons
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected List<ItemCollection> explodeChildList(ItemCollection workitem) {
        // convert current list of childItems into ItemCollection elements
        ArrayList<ItemCollection> childItems = new ArrayList<ItemCollection>();

        List<Object> mapOrderItems = workitem.getItemValue(CHILD_ITEM_PROPERTY);
        int pos = 1;
        for (Object mapOderItem : mapOrderItems) {

            if (mapOderItem instanceof Map) {
                ItemCollection itemCol = new ItemCollection((Map) mapOderItem);
                itemCol.replaceItemValue("numPos", pos);
                childItems.add(itemCol);
                pos++;
            }
        }
        return childItems;
    }

}