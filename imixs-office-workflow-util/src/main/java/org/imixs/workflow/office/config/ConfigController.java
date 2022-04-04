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

package org.imixs.workflow.office.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.model.SelectItem;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.exceptions.AccessDeniedException;

/**
 * This ConfigController acts as a frontend controller for a Config Entity. The
 * entity (itemCollection) holds config params. The entity is stored with the
 * type "configuration" and a configurable name (txtname). The property
 * 'txtname' is used to select the config entity by a query.
 * 
 * The bean interacts with the marty ConfigService EJB which is responsible for
 * creation, loading and saving the entity. This singleton ejb can manage
 * multiple config entities. The ConfigController bean is also
 * ApplicationScoped, so it can be shared in one application. From the backend
 * it is possible to use the ConfigControler or also directly the ConfigService
 * EJB.
 * 
 * The Bean can be overwritten to add additional busines logic (e.g. converting
 * params or providing additional custom getter methods).
 * 
 * 
 * Use multiple instances in one application, bean can be decleared in the
 * faces-config.xml file. The managed-ban-name as the manged property 'name' can
 * be set to custom values:
 * 
 * <code> 
  	<managed-bean>
		<managed-bean-name>myConfigController</managed-bean-name>
		<managed-bean-class>org.imixs.marty.config.ConfigController</managed-bean-class>
		<managed-property>
			<property-name>name</property-name>
			<value>REPORT_CONFIGURATION</value>
		</managed-property>
	</managed-bean>
 * </code>
 * 
 * The Bean provides easy access to the config params from a JSF Page. Example:
 * 
 * <code>
 * <h:inputText value="#{configController.workitem.item['myParam1']}" >
						</h:inputText>
 * </code>
 * 
 * @author rsoika
 * 
 */
@ApplicationScoped
public class ConfigController implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name = "CONFIGURATION";

	private ItemCollection configItemCollection = null;

	@EJB
	ConfigService configService;

	public ConfigController() {
		super();
	}

	/**
	 * This method load the config entity after postContstruct. If no Entity
	 * exists than the ConfigService EJB creates a new config entity.
	 * 
	 */
	@PostConstruct
	public void init() {
		configItemCollection = configService.loadConfiguration(getName());
	}

	/**
	 * Refresh the configItemCollection. The method can be called by a client to get
	 * an updated version of the config entity. The method discards the internal
	 * cache!
	 */
	public void loadConfiguration() {
		configItemCollection = configService.loadConfiguration(getName(), true);
	}

	/**
	 * Returns the name of the configuration entity
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the configuration entity
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	public ItemCollection getWorkitem() {
		return this.configItemCollection;
	}

	/**
	 * SelectItem getter Method provides a getter method to an ArrayList of
	 * <SelectItem> objects for a specific param stored in the configuration
	 * entity. A param entry can be devided by a | into a label and a value
	 * component. Example:
	 * 
	 * <code>
	 *   Important | 1 
	 *   Unimportant | 0 
	 * </code>
	 * 
	 * <code>
	 * <f:selectItems value="#{configMB.selectItems['txtMyParam2']}" />
	 * </code>
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<SelectItem> getSelectItems(String param) throws Exception {

		ArrayList<SelectItem> selection;
		selection = new ArrayList<SelectItem>();

		// check if a value for this param is available...
		// if not return an empty list
		if (!this.configItemCollection.hasItem(param)) {
			return selection;
		}

		// get value list first value from vector if size >0
		List<?> valueList = this.configItemCollection.getItemValue(param);
		for (Object aValue : valueList) {
			// test if aValue has a | as an delimiter
			String sValue = aValue.toString();
			String sName = sValue;

			if (sValue.indexOf("|") > -1) {
				sValue = sValue.substring(0, sValue.indexOf("|"));
				sName = sName.substring(sName.indexOf("|") + 1);
			}
			selection.add(new SelectItem(sName.trim(), sValue.trim()));

		}
		return selection;
	}

	/**
	 * save method updates the txtname property and save the config entity
	 * 
	 * @throws AccessDeniedException
	 */
	public void save() throws AccessDeniedException {
		// update name
		configItemCollection.replaceItemValue("txtname", this.getName());
		// save entity
		try {
			configItemCollection = configService.save(configItemCollection);
			System.out.println("...configuration " + this.getName() + " updated!");
		} catch (Exception e) {
			System.out.println("...Failed to save configuration " + this.getName() + " : " + e.getMessage() + " - reloading config!");
			configItemCollection = configService.loadConfiguration(getName(), true);
		}
	}

}
