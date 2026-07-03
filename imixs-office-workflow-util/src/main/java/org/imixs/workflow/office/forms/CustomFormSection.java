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

import java.util.List;

import org.imixs.workflow.ItemCollection;

/**
 * This CustomFormSection provides the information from a custom form
 * definition
 * 
 * 
 * @author rsoika
 * @version 1.0
 */
public class CustomFormSection {

	String label;
	String columns;
	String path;
	String options;
	boolean readonly;
	List<CustomFormItem> items;
	String layout;
	String childItemName;
	List<ItemCollection> childItems = null;
	ItemCollection workitem;

	public CustomFormSection(ItemCollection workitem, String label, String columns, String path, boolean readonly,
			String options) {
		super();
		this.workitem = workitem;
		this.label = label;
		this.readonly = readonly;
		this.columns = columns;
		this.options = options;
		if (path != null) {
			this.path = path.trim();
		}

		// Default Group layout
		layout = "group";
		// test if we have a 'childItem' option
		childItemName = getOptionValue("childitem");
		if (childItemName != null && !childItemName.isEmpty()) {
			childItems = ChildItemController.explodeChildList(workitem, childItemName);
			if ("table".equals(getOptionValue("layout"))) {
				layout = "table";
			}
		}
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getColumns() {
		return columns;
	}

	public void setColumns(String columns) {
		this.columns = columns;
	}

	/**
	 * Section layout - default 'group'
	 * Optional 'table'
	 * 
	 * @return
	 */
	public String getLayout() {
		return layout;
	}

	public void setLayout(String layout) {
		this.layout = layout;
	}

	public List<CustomFormItem> getItems() {
		return items;
	}

	public void setItems(List<CustomFormItem> items) {
		this.items = items;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public boolean isReadonly() {
		return readonly;
	}

	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
	}

	public String getOptions() {
		return options;
	}

	public void setOptions(String options) {
		this.options = options;
	}

	/**
	 * Returns a single value from the Option key/value list
	 * 
	 * @param options - options
	 * @param key     - option key
	 * @return option value
	 */
	public String getOptionValue(String key) {
		// Null checks
		if (options == null || key == null || options.trim().isEmpty() || key.trim().isEmpty()) {
			return null;
		}

		// Split options into key/value pairs (separated by semicolon)
		String[] pairs = options.split(";");

		for (String pair : pairs) {
			// Split each pair into key and value (separated by equals sign)
			String[] keyValue = pair.split("=", 2); // Limit to 2 in case value contains "="

			if (keyValue.length == 2) {
				String currentKey = keyValue[0].trim();
				String currentValue = keyValue[1].trim();

				// Check if the searched key was found
				if (key.equals(currentKey)) {
					return currentValue;
				}
			}
		}

		// Key not found
		return null;
	}

	public ItemCollection getWorkitem() {
		return workitem;
	}

	public void setWorkitem(ItemCollection workitem) {
		this.workitem = workitem;
	}

	/**
	 * Returns the optional childItem name if defined in the options (e.g.
	 * options="childitem=bill")
	 * 
	 * @return
	 */
	public String getChildItemName() {
		return childItemName;
	}

	public void setChildItemName(String childItemName) {
		this.childItemName = childItemName;
	}

	public List<ItemCollection> getChildItems() {
		return childItems;
	}

	public void setChildItems(List<ItemCollection> childItems) {
		this.childItems = childItems;
	}

}
