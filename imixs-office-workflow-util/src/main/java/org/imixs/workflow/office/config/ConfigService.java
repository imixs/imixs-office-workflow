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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.Singleton;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.ItemCollectionComparator;
import org.imixs.workflow.engine.DocumentService;
import org.imixs.workflow.exceptions.AccessDeniedException;
 
/**
 * The Marty Config Service can be used to store and access configuration values
 * stored in a configuration entity (type='CONFIGURATION).
 * 
 * The ConfigService EJB provides access to named Config Entities stored in the
 * database. A configuration Entity is identified by its name (property
 * txtName). So different configuration Entities can be managed in one
 * application.
 * 
 * The ConfigService ejb is implemented as a sigelton and uses an internal cache
 * to cache config entities.
 * 
 * 
 * @author rsoika
 */

@DeclareRoles({ "org.imixs.ACCESSLEVEL.NOACCESS", "org.imixs.ACCESSLEVEL.READERACCESS",
		"org.imixs.ACCESSLEVEL.AUTHORACCESS", "org.imixs.ACCESSLEVEL.EDITORACCESS",
		"org.imixs.ACCESSLEVEL.MANAGERACCESS" })
@RolesAllowed({ "org.imixs.ACCESSLEVEL.NOACCESS", "org.imixs.ACCESSLEVEL.READERACCESS",
		"org.imixs.ACCESSLEVEL.AUTHORACCESS", "org.imixs.ACCESSLEVEL.EDITORACCESS",
		"org.imixs.ACCESSLEVEL.MANAGERACCESS" })
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class ConfigService {

	int DEFAULT_CACHE_SIZE = 30;

	@EJB
	private DocumentService documentService;

	private Cache cache = null;

	final String TYPE = "configuration";

	/**
	 * PostContruct event - loads the imixs.properties.
	 */
	@PostConstruct
	void init() {
		// initialize cache
		cache = new Cache(DEFAULT_CACHE_SIZE);
	}

	/**
	 * creates a new configuration object for a specified name
	 * 
	 * @return
	 */
	public ItemCollection createConfiguration(String name) throws Exception {
		ItemCollection configItemCollection = new ItemCollection();
		configItemCollection.replaceItemValue("type", TYPE);
		configItemCollection.replaceItemValue("txtname", name);
		configItemCollection.replaceItemValue("$writeAccess", "org.imixs.ACCESSLEVEL.MANAGERACCESS");
		configItemCollection.replaceItemValue("$readAccess", "");

		cache.put(name, configItemCollection);

		return configItemCollection;
	}

	/**
	 * This method deletes an existing Configuration.
	 * 
	 * @param aconfig
	 * @throws AccessDeniedException
	 */
	public void deleteConfiguration(ItemCollection aconfig) throws AccessDeniedException {
		cache.remove(aconfig.getItemValueString("txtName"));
		documentService.remove(aconfig);
	}

	/**
	 * This method returns a config ItemCollection for a specified name. If no
	 * configuration is found for this name the Method creates an empty
	 * configuration object. The config entity is cached internally and read
	 * from the cache
	 * 
	 * @param name
	 *            in attribute txtname
	 * 
	 */
	public ItemCollection loadConfiguration(String name) {
		return this.loadConfiguration(name, false);
	}

	/**
	 * This method returns a config ItemCollection for a specified name. If no
	 * configuration is found for this name the Method creates an empty
	 * configuration object. The config entity is cached internally. 
	 * 
	 * The method uses JPQL instead of lucene index
	 * 
	 * @see issue #172
	 * @param name
	 *            in attribute txtname
	 * 
	 * @param discardCache
	 *            - indicates if the internal cache should be discarded.
	 */
	public ItemCollection loadConfiguration(String name, boolean discardCache) {
		ItemCollection configItemCollection = null;
		// check cache...
		configItemCollection = cache.get(name);
		if (configItemCollection == null || discardCache) {
			// use JPQL here - issue #172
			Collection<ItemCollection> col = documentService.getDocumentsByType(TYPE);
			for (ItemCollection config : col) {
				if (config.getItemValueString("txtname").equals(name)) {
					configItemCollection=config;
					break;
				}
			}
			
			if (configItemCollection==null) {
				// create default values
				configItemCollection = new ItemCollection();
				configItemCollection.replaceItemValue("type", TYPE);
				configItemCollection.replaceItemValue("txtname", name);
			}
			cache.put(name, configItemCollection);
		}
		return configItemCollection;
	}

	/**
	 * save the configuration entity
	 * 
	 * @return
	 * @throws AccessDeniedException
	 */
	public ItemCollection save(ItemCollection configItemCollection) throws AccessDeniedException {
		// update write and read access
		configItemCollection.replaceItemValue("type", TYPE);

		// save entity
		configItemCollection = documentService.save(configItemCollection);

		cache.put(configItemCollection.getItemValueString("txtName"), configItemCollection);

		return configItemCollection;
	}

	/**
	 * Returns a list of all configuration entities. The method uses JQPL staements instead of lucene index. 
	 * 
	 * @see issue #172
	 * @return
	 */
	public List<ItemCollection> findAllConfigurations() {
		ArrayList<ItemCollection> configList = new ArrayList<ItemCollection>();
		Collection<ItemCollection> col = documentService.getDocumentsByType(TYPE);
		for (ItemCollection aworkitem : col) {
			configList.add(aworkitem);
		}
		
		// sort by txtname
		Collections.sort(configList, new ItemCollectionComparator("txtname", true));
		
		
		return configList;
	}

	/**
	 * Cache implementation to hold config entities
	 * 
	 * @author rsoika
	 * 
	 */
	class Cache extends ConcurrentHashMap<String, ItemCollection> implements Serializable {
		private static final long serialVersionUID = 1L;
		private final int capacity;

		public Cache(int capacity) {
			super(capacity + 1, 1.1f);
			this.capacity = capacity;
		}

		protected boolean removeEldestEntry(Entry<String, ItemCollection> eldest) {
			return size() > capacity;
		}
	}

}
