/*******************************************************************************
 *  Imixs Workflow Technology
 *  Copyright (C) 2011 Imixs Software Solutions GmbH,  
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
 *  	Imixs Software Solutions GmbH - Ralph Soika
 *  
 *******************************************************************************/
package org.imixs.marty.web.office;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.component.UIParameter;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import org.imixs.marty.util.Cache;
import org.imixs.marty.web.workitem.WorkitemListener;
import org.imixs.marty.web.workitem.WorkitemMB;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.plugins.jee.extended.LucenePlugin;

/**
 * This ManagedBean supports Process lookups. The Method setNewProcessReference
 * adds a new process reference which will be stored into the property
 * 'txtworkitemref'
 * 
 * The property 'filter' defines a regex to filter specific processids to be
 * lookuped by the method suggestProcess()
 * 
 * The following example shows how to integrate the bean into a jsf page for a
 * process lookup
 * 
 * <code>
     	<ui:include src="/pages/workitems/forms/suggest_reference.xhtml">
					<ui:param name="tooltip" value="#{form_messages.sub_basic_lookup_help}" />
					<ui:param name="handler" value="#{contactLookupMB}" />
		</ui:include>		
 * </code>
 * 
 * The WorkitemReferenceMB can be configured to filter specific process ids
 * throgh the faces-config.xml. See the following example:
 * 
 * <code>
   <!-- Contract Lookup - 4000 -->
	<managed-bean>
		<managed-bean-name>contractLookupMB</managed-bean-name>
		<managed-bean-class>
			org.imixs.marty.web.office.WorkitemReferenceMB
		</managed-bean-class>
		<managed-bean-scope>session</managed-bean-scope>
		<!-- Filter -->
		<managed-property>
			<property-name>filter</property-name>
			<property-class>java.lang.String</property-class>
			<value>4...</value>
		</managed-property>
	</managed-bean>
 * </code>
 * 
 * @see process_suggest_input.xhtml
 * 
 * @author rsoika
 * 
 */
public class WorkitemReferenceMB implements WorkitemListener {

	public static String FIELD_NAME = "txtworkitemref";

	private static Logger logger = Logger.getLogger("org.imixs.workflow");

	// Workflow Manager
	@EJB
	org.imixs.workflow.jee.ejb.WorkflowService workflowService;

	private WorkitemMB workitemMB = null;
	private String filter; // defines a regex process filter
	private List<ItemCollection> referencesTo = null; // outgoing references
	private List<ItemCollection> referencesFrom = null; // incomming references

	private WorkItemAdapter workItemAdapter = null;

	public WorkitemReferenceMB() {
		super();
	}

	/**
	 * This method register the bean as an workitemListener
	 */
	@PostConstruct
	public void init() {
		// register this Bean as a workitemListener to the current WorktieMB
		this.getWorkitemBean().addWorkitemListener(this);
	}

	/**
	 * Diese Methode wird als suggestionAction für eine rich:suggestionbox
	 * verwendet, um Daten zu suchen.
	 * 
	 * Dazu wird der Parameter event ausgewertet
	 * 
	 * @param event
	 * @return
	 * @throws NamingException
	 * @throws SQLException
	 */
	public List<ItemCollection> suggestProcess(Object event)
			throws NamingException {
		DirContext ldapCtx = null;
		List<ItemCollection> workitems = new ArrayList<ItemCollection>();

		if (event == null || event.toString() == null
				|| "".equals(event.toString()))
			return workitems;

		// eingabe abfragen
		String searchphrase = event.toString();
		if ("null".equals(searchphrase.toLowerCase()))
			return workitems;

		try {

			String sSearchTerm = "";

			if (filter != null && !"".equals(filter)) {
				String sNewFilter = filter;

				sNewFilter = sNewFilter.replace(".", "?");
				sSearchTerm = "($processid:" + sNewFilter + ") AND ";

			}
			if (!"".equals(searchphrase)) {
				sSearchTerm += " (*" + searchphrase.toLowerCase() + "*)";

			}

			System.out.println("Suggest: " + sSearchTerm);
			workitems = LucenePlugin.search(sSearchTerm, workflowService);

		} catch (Exception e) {
			logger.warning("  lucene error!");
			e.printStackTrace();
		}

		return workitems;

	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	/**
	 * This methods adds a new process reference to the property
	 * 'processReference'
	 * 
	 * This method is used by the richfaces suggest widget
	 */
	public void setNewProcessReference(String aRef) {
		try {
			// get current list
			Vector list = getWorkitemBean().getWorkitem().getItemValue(
					FIELD_NAME);
			// clear empty entry if set
			if (list.size() == 1 && "".equals(list.elementAt(0)))
				list.remove(0);

			if (list.indexOf(aRef) == -1) {
				list.add(aRef);

				getWorkitemBean().getWorkitem().replaceItemValue(FIELD_NAME,
						list);
				referencesTo = null;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * This methods adds a new process reference to the property
	 * 'txtWorkitemRef'. The reference is expected in the param 'id'
	 */
	public void doAddReference(ActionEvent event) throws Exception {
		// get Process ID out from the ActionEvent Object....
		List children = event.getComponent().getChildren();
		String processEntityIdentifier = "";

		for (int i = 0; i < children.size(); i++) {
			if (children.get(i) instanceof UIParameter) {
				UIParameter currentParam = (UIParameter) children.get(i);
				if (currentParam.getName().equals("id")
						&& currentParam.getValue() != null) {
					processEntityIdentifier = (String) currentParam.getValue();
					break;
				}

			}
		}
		if ("".equals(processEntityIdentifier))
			return;

		this.setNewProcessReference(processEntityIdentifier);
	}

	/**
	 * This method deletes a reference from the property txtWorkitemRef. The
	 * reference is expected in the param 'id'
	 * 
	 * @param event
	 * @throws Exception
	 */
	public void doRemoveReference(ActionEvent event) throws Exception {
		// get Process ID out from the ActionEvent Object....
		List children = event.getComponent().getChildren();
		String processEntityIdentifier = "";

		for (int i = 0; i < children.size(); i++) {
			if (children.get(i) instanceof UIParameter) {
				UIParameter currentParam = (UIParameter) children.get(i);
				if (currentParam.getName().equals("id")
						&& currentParam.getValue() != null) {
					processEntityIdentifier = (String) currentParam.getValue();
					break;
				}

			}
		}

		// remove the reference
		if (!"".equals(processEntityIdentifier)) {
			try {
				// get current list
				Vector list = getWorkitemBean().getWorkitem().getItemValue(
						FIELD_NAME);
				// clear empty entry if set
				if (list.size() == 1 && "".equals(list.elementAt(0)))
					list.remove(0);

				list.remove(processEntityIdentifier);

				getWorkitemBean().getWorkitem().replaceItemValue(FIELD_NAME,
						list);
				referencesTo = null;

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

	/**
	 * returns a ItemCollection list of References stored in the current
	 * workitem (outgoing references). If the filter is set the processID will
	 * be tested for the filter regex
	 * 
	 * @return
	 * @throws NamingException
	 */
	public List<ItemCollection> getReferences() {

		if (referencesTo != null)
			return referencesTo;

		referencesTo = new ArrayList<ItemCollection>();

		long lTime = System.currentTimeMillis();

		// lookup the references...
		Vector<String> list = getWorkitemBean().getWorkitem().getItemValue(
				FIELD_NAME);
		// empty list?

		if (list.size() == 0
				|| (list.size() == 1 && "".equals(list.elementAt(0))))
			return referencesTo;

		String sQuery = "SELECT entity FROM Entity entity "
				+ " WHERE entity.type = 'workitem' AND entity.id IN (";
		for (String aID : list) {
			sQuery += "'" + aID + "',";
		}
		// cut last ,
		sQuery = sQuery.substring(0, sQuery.length() - 1);
		sQuery += ")";

		Collection<ItemCollection> col = null;
		try {
			col = workflowService.getEntityService().findAllEntities(sQuery, 0,
					-1);
			for (ItemCollection itemcol : col) {

				if (filter != null && !"".equals(filter)) {
					String sProcessID = ""
							+ itemcol.getItemValueInteger("$ProcessID");
					if (!sProcessID.matches(filter))
						continue;
				}

				referencesTo.add(itemcol);
			}
		} catch (Exception e) {

			e.printStackTrace();
		}

		logger.fine("  WorkitemRef Lookup:  "
				+ (System.currentTimeMillis() - lTime) + " ms");

		return referencesTo;
	}

	/**
	 * returns a ItemCollection list of all workitems holding a reference to the
	 * current workitem. If the filter is set the processID will be tested for
	 * the filter regex
	 * 
	 * 
	 * @return
	 * @throws NamingException
	 */
	public List<ItemCollection> getIncommingReferences() throws NamingException {
		if (referencesFrom != null)
			return referencesFrom;

		referencesFrom = new ArrayList<ItemCollection>();

		long lTime = System.currentTimeMillis();

		String uniqueid = getWorkitemBean().getWorkitem().getItemValueString(
				"$uniqueid");

		// return an empty list if still no $uniqueid is defined for the current
		// workitem
		if ("".equals(uniqueid))
			return referencesFrom;

		// select all references.....
		String sQuery = "SELECT workitem FROM Entity AS workitem"
				+ " JOIN workitem.textItems AS rnr"
				+ " WHERE workitem.type = 'workitem' "
				+ " AND rnr.itemName = '" + FIELD_NAME + "'"
				+ " AND rnr.itemValue='" + uniqueid + "'"
				+ " ORDER BY workitem.created DESC";

		logger.fine("  Incomming Referece Lookup - query:  " + sQuery);
		Collection<ItemCollection> col = null;
		try {
			col = workflowService.getEntityService().findAllEntities(sQuery, 0,
					-1);
			for (ItemCollection itemcol : col) {

				if (filter != null && !"".equals(filter)) {
					String sProcessID = ""
							+ itemcol.getItemValueInteger("$ProcessID");
					if (!sProcessID.matches(filter))
						continue;
				}

				referencesFrom.add(itemcol);
			}
		} catch (Exception e) {

			e.printStackTrace();
		}

		logger.fine("  Incomming Referece Lookup:  "
				+ (System.currentTimeMillis() - lTime) + " ms");

		return referencesFrom;

	}

	/**
	 * selects the workitem to the corresponding process. A parameter 'id' with
	 * the $uniqueid is expected
	 * 
	 * @param event
	 * @return
	 * @throws Exception
	 */
	public void doSwitchToWorkitem(ActionEvent event) throws Exception {
		// Activity ID raussuchen und in activityID speichern
		List children = event.getComponent().getChildren();
		String aID = null;

		for (int i = 0; i < children.size(); i++) {
			if (children.get(i) instanceof UIParameter) {
				UIParameter currentParam = (UIParameter) children.get(i);
				if (currentParam.getName().equals("id")
						&& currentParam.getValue() != null) {
					aID = currentParam.getValue().toString();
					break;
				}
			}
		}

		if (aID != null) {
			ItemCollection itemCol = workflowService.getWorkItem(aID);
			getWorkitemBean().setWorkitem(itemCol);
		}

	}

	private WorkitemMB getWorkitemBean() {
		if (workitemMB == null)
			workitemMB = (WorkitemMB) FacesContext
					.getCurrentInstance()
					.getApplication()
					.getELResolver()
					.getValue(FacesContext.getCurrentInstance().getELContext(),
							null, "workitemMB");
		return workitemMB;
	}

	/**
	 * This method uses the Map Interface as a returnvalue to allow the
	 * parameterized access to a woritem references.
	 * 
	 * 
	 * in a jsf page using a expression language like this:
	 * 
	 * #{workitemReferenceMB.referencesFor[$uniqueid]}
	 * 
	 * @see The inner class UserNameAdapter
	 * 
	 * @return
	 */
	public Map getReferencesFor() {
		if (workItemAdapter == null)
			workItemAdapter = new WorkItemAdapter();
		return workItemAdapter;

	}

	public void onWorkitemCreated(ItemCollection e) {
	}

	public void onWorkitemChanged(ItemCollection e) {
		referencesTo = null;
		referencesFrom = null;
	}

	public void onWorkitemProcess(ItemCollection e) {
	}

	public void onWorkitemProcessCompleted(ItemCollection e) {
		referencesTo = null;
		referencesFrom = null;
	}

	public void onWorkitemDelete(ItemCollection e) {
	}

	public void onWorkitemDeleteCompleted() {
	}

	public void onWorkitemSoftDelete(ItemCollection e) {
	}

	public void onWorkitemSoftDeleteCompleted(ItemCollection e) {
	}

	public void onChildProcess(ItemCollection e) {
	}

	public void onChildProcessCompleted(ItemCollection e) {
	}

	public void onChildCreated(ItemCollection e) {
	}

	public void onChildDelete(ItemCollection e) {
	}

	public void onChildDeleteCompleted() {
	}

	public void onChildSoftDelete(ItemCollection e) {
	}

	public void onChildSoftDeleteCompleted(ItemCollection e) {
	}

	/**
	 * This class helps to addapt the behavior of a workitemLookup to a
	 * MapObject The Class overwrites the get Method and returns a collection of
	 * workitems
	 * 
	 * in a jsf page using a expression language like this:
	 * 
	 * #{woritemReferenceMB.workitem['.......']}
	 * 
	 * 
	 * @author rsoika
	 * 
	 */
	class WorkItemAdapter implements Map {
		final int MAX_CACHE_SIZE = 20;
		private Cache cache;

		public WorkItemAdapter() {
			cache = new Cache(MAX_CACHE_SIZE);
		}

		/**
		 * returns a colection of ItemCollection for all refreences to the
		 * requested workitem
		 */
		@SuppressWarnings("unchecked")
		public Object get(Object key) {

			List<ItemCollection> aworkitemRefList = null;
			// test if allredy cached....
			aworkitemRefList = (List<ItemCollection>) cache.get(key);
			if (aworkitemRefList != null) {
				logger.fine(" workitemAdapter Lookup allready cached");
				return aworkitemRefList;
			}

			aworkitemRefList = new ArrayList<ItemCollection>();

			// test if UniqueID is allready listed
			ItemCollection aworkitem = null;
			try {
				aworkitem = workflowService.getWorkItem(key.toString());
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			if (aworkitem == null)
				return aworkitemRefList;
			// lookup the references for this workItem...
			Vector<String> list = aworkitem.getItemValue(FIELD_NAME);
			// empty list?

			if (list.size() == 0
					|| (list.size() == 1 && "".equals(list.elementAt(0))))
				return aworkitemRefList;
			long lTime = System.currentTimeMillis();
			String sQuery = "select entity from Entity entity where entity.id IN (";
			for (String aID : list) {
				sQuery += "'" + aID + "',";
			}
			// cut last ,
			sQuery = sQuery.substring(0, sQuery.length() - 1);
			sQuery += ")";

			Collection<ItemCollection> col = null;
			try {
				col = workflowService.getEntityService().findAllEntities(
						sQuery, 0, -1);
				for (ItemCollection itemcol : col) {

					if (filter != null && !"".equals(filter)) {
						String sProcessID = ""
								+ itemcol.getItemValueInteger("$ProcessID");
						if (!sProcessID.matches(filter))
							continue;
					}

					aworkitemRefList.add(itemcol);
				}
			} catch (Exception e) {

				e.printStackTrace();
			}

			// now put the list into the cache....
			cache.put(key, aworkitemRefList);
			logger.info("  WorkitemRef Lookup:  "
					+ (System.currentTimeMillis() - lTime) + " ms");

			return aworkitemRefList;

		}

		public Object put(Object key, Object value) {
			return null;
		}

		/* ############### Default methods ################# */

		public void clear() {
		}

		public boolean containsKey(Object key) {
			return false;
		}

		public boolean containsValue(Object value) {
			return false;
		}

		public Set entrySet() {
			return null;
		}

		public boolean isEmpty() {
			return false;
		}

		public Set keySet() {
			return null;
		}

		public void putAll(Map m) {
		}

		public Object remove(Object key) {
			return null;
		}

		public int size() {
			return 0;
		}

		public Collection values() {
			return null;
		}

	}

}
