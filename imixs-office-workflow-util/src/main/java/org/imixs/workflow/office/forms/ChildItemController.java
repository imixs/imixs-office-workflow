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
package org.imixs.workflow.office.forms;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import jakarta.enterprise.context.ConversationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Named;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.exceptions.AccessDeniedException;
import org.imixs.workflow.faces.data.WorkflowEvent;

/**
 * The CDI ChildItemController provides methods to display and edit sub items in
 * a sub-form. The controller provides methods to add or remove a childItem.
 * 
 * Each childItem is represented internally as a HashMap. To the front-end the
 * ChildItemController provides a list of ItemCollections
 * 
 * This CDI Controller can be used to provide different sub-views. The property
 * '_ChildItems' of the current WorkItem hold the items.
 * 
 *
 * 
 * @author rsoika
 * @version 1.0
 */
@Named
@ConversationScoped
public class ChildItemController implements Serializable {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(ChildItemController.class.getName());

	protected List<ItemCollection> childItems = null;

	public static final String CHILD_ITEM_PROPERTY = "_ChildItems";

	public void setChildItems(List<ItemCollection> childItems) {
		this.childItems = childItems;
	}

	/**
	 * This methd returns a ItemCollections for each orderItem stored in the
	 * property txtOrderItems.
	 * 
	 * Example: <code>
	 *   #{childController.childItems}
	 * </code>
	 * 
	 * @return
	 */
	public List<ItemCollection> getChildItems() {
		return childItems;
	}

	/**
	 * WorkflowEvent listener to convert embeded HashMaps into ItemCollections and
	 * reconvert them before processing
	 * 
	 * @param workflowEvent
	 * @throws AccessDeniedException
	 */
	public void onWorkflowEvent(@Observes WorkflowEvent workflowEvent) throws AccessDeniedException {

		int eventType = workflowEvent.getEventType();
		ItemCollection workitem = workflowEvent.getWorkitem();
		if (workitem == null) {
			return;
		}

		// reset orderItems if workItem has changed
		if (WorkflowEvent.WORKITEM_CHANGED == eventType || WorkflowEvent.WORKITEM_CREATED == eventType) {
			// reset state
			explodeChildList(workitem);
		}

		// before the workitem is saved we update the field txtOrderItems
		if (WorkflowEvent.WORKITEM_BEFORE_PROCESS == eventType ) {
			implodeChildList(workitem);
		}

		if (WorkflowEvent.WORKITEM_AFTER_PROCESS == eventType) {
			// reset state
			explodeChildList(workitem);
		}

	}

	/**
	 * Adds a new order item
	 */
	public void add() {
		if (childItems != null) {
			ItemCollection itemCol = new ItemCollection();
			itemCol.replaceItemValue("numPos", childItems.size() + 1);
			childItems.add(itemCol);
		}
	}

	public void remove(int pos) {
		if (childItems != null) {
			childItems.remove(pos - 1);
		}

		// now we need to reorder the numPos attribute for all existing childs..
		int iPos = 1;
		for (ItemCollection item : childItems) {
			item.replaceItemValue("numPos", iPos);
			iPos++;
		}

	}

	public double convertDouble(String aValue) {
		if (aValue == null || aValue.isEmpty())
			return 0;

		return new Double(aValue);
	}

	/**
	 * Convert the List of ItemCollections back into a List of Map elements
	 * 
	 * @param workitem
	 */
	@SuppressWarnings({ "rawtypes" })
	protected void implodeChildList(ItemCollection workitem) {
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
	protected void explodeChildList(ItemCollection workitem) {
		// convert current list of childItems into ItemCollection elements
		childItems = new ArrayList<ItemCollection>();

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
	}

}
