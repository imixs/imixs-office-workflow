package org.imixs.marty.web.office.deprecated;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.component.UIParameter;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import org.imixs.marty.web.workitem.WorkitemListener;
import org.imixs.marty.web.workitem.WorkitemMB;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.plugins.jee.extended.LucenePlugin;

/**
 * This ManagedBean supports Process lookups.
 * 
 * 
 * This in an example how to bind this feature into a form:
 * 
 * <code>
       <ui:include src="/pages/workitems/forms/process_suggest_input.xhtml">
			<ui:param name="item_value" value="#{workitemMB.workitem.item['_processRef1']}" />
			<ui:param name="item_label" value="Prozess Auswahl" />
			<ui:param name="tooltip" value="Suchen Sie einen anderen Prozess" />
		</ui:include>	
 * </code>
 * 
 * @see process_suggest_input.xhtml
 * 
 * @author rsoika
 * 
 */
public class WorkitemLookupMB implements WorkitemListener {

	private static Logger logger = Logger.getLogger("org.imixs.workflow");

	// Workflow Manager
	@EJB
	org.imixs.workflow.jee.ejb.WorkflowService workflowService;

//	private ProcessDataAdapter processDataAdapter = null;
	final int MAX_CACHE_SIZE = 10;
	private Cache cache; // cache holds workitems
	private WorkitemMB workitemMB = null;
	private String filter; // defines a regex process filter 
	private List<ItemCollection> references=null;;

	public WorkitemLookupMB() {
		super();

		cache = new Cache(MAX_CACHE_SIZE);
	//	processDataAdapter = new ProcessDataAdapter();

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
	 * Diese Methode ist wird als suggestionAction für eine rich:suggestionbox
	 * verwendet, um User-Daten via LDAP zu suchen.
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

			if (!"".equals(searchphrase)) {
				sSearchTerm += " (*" + searchphrase.toLowerCase() + "*)";

			}
			
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
					"processReference");
			// clear empty entry if set
			if (list.size()==1 && "".equals(list.elementAt(0)))
				list.remove(0);
			
			if (list.indexOf(aRef) == -1) {
				list.add(aRef);
	
				getWorkitemBean().getWorkitem().replaceItemValue(
						"processReference", list);
	
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * This methods adds a new process reference to the property
	 * 'processReference'. The reference is expected in the param 'id'
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
		
		
		try {
			// get current list
			Vector list = getWorkitemBean().getWorkitem().getItemValue(
					"processReference");
			// clear empty entry if set
			if (list.size()==1 && "".equals(list.elementAt(0)))
				list.remove(0);
			
			if (list.indexOf(processEntityIdentifier) == -1) {
				list.add(processEntityIdentifier);

				getWorkitemBean().getWorkitem().replaceItemValue(
						"processReference", list);

			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	/**
	 * This method deletes a reference from the property processReference.
	 * The reference is expected in the param 'id'
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
						"processReference");
				// clear empty entry if set
				if (list.size()==1 && "".equals(list.elementAt(0)))
					list.remove(0);
				
				list.remove(processEntityIdentifier);
					
				getWorkitemBean().getWorkitem().replaceItemValue(
							"processReference", list);

				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}



	
	
	/**
	 * This method uses the Map Interface as a return value to allow the
	 * parameterized access to process reference.
	 * 
	 * 
	 * in a jsf page using a expression language like this:
	 * 
	 * #{processLookupMB.item[accountname]}
	 * 
	 * @see The inner class ProcessDataAdapter
	 * 
	 * @return
	 */
	/*
	public Map getProcessData() {
		return processDataAdapter;

	}
	*/
	
	/**
	 * returns a ItemCollection list of References
	 * @return
	 * @throws NamingException
	 */
	public List<ItemCollection> getReferences() throws NamingException {
		
		if (references!=null)
			return references;
		
		
		references = new ArrayList<ItemCollection>();
		
		// lookup the references...
		Vector<String> list = getWorkitemBean().getWorkitem().getItemValue(
				"processReference");
		// empty list?
		
		if (list.size()==0 || (list.size()==1 && "".equals(list.elementAt(0))))
			return references;
		
		String sQuery="select entity from Entity where entity.id IN (";
		for (String aID: list) {
			sQuery+="'"+aID + "',";
		}
		// cut last ,
		sQuery=sQuery.substring(0,sQuery.length()-1);
		sQuery+=")";

		Collection<ItemCollection> col=null;
		try {
			col = workflowService.getEntityService().findAllEntities(sQuery, 0,-1);
			for (ItemCollection itemcol:col) {
				references.add(itemcol);
			}
		} catch (Exception e) {
		
			e.printStackTrace();
		}
		
		
		

		return references;
	}

	
	
	

	/**
	 * This method returns the workitem to a $unqiueID. The method uses the
	 * internal cache object to avoid repeating lookups
	 * 
	 * 
	 */
	public ItemCollection lookupProcessData(String aUID) throws NamingException {
		ItemCollection workitem = null;

		// try to get workitem out from cache
		workitem = (ItemCollection) cache.get(aUID);
		if (workitem == null) {
			// lookup workitem
			try {
				workitem = workflowService.getWorkItem(aUID);

				if (workitem == null)
					// no workitem found - create empty workitem
					workitem = new ItemCollection();
				else
					// put data in cache
					cache.put(aUID, workitem);

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		return workitem;
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

	public WorkitemMB getWorkitemBean() {
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
	 * This class helps to addapt the behavior of a processDataLookup to a
	 * MapObject The Class overwrites the get Method and calls
	 * lookupProcessData() to get an ItemCollection for a specific $uniqueid
	 * 
	 * in a jsf page using a expression language like this:
	 * 
	 * #{mybean.processData[record].item['txtWorkflowStatus']}
	 * 
	 * 
	 * @author rsoika
	 * 
	 */
	
	class XXXProcessDataAdapter implements Map {

		public XXXProcessDataAdapter() {

		}

		/**
		 * returns an ItemCollectionAdapter for a UID
		 */
		@SuppressWarnings("unchecked")
		public Object get(Object key) {
			try {
				return lookupProcessData(key.toString());
			} catch (NamingException e) {
				e.printStackTrace();
				return new ItemCollection();
			}
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

	/**
	 * Cache implementation to hold userData objects (ItemCollections) Used by
	 * lookupUserData()
	 * 
	 * @author rsoika
	 * 
	 */
	class Cache extends LinkedHashMap {
		private final int capacity;

		public Cache(int capacity) {
			super(capacity + 1, 1.1f, true);
			this.capacity = capacity;
		}

		protected boolean removeEldestEntry(Entry eldest) {
			return size() > capacity;
		}
	}

	public void onWorkitemCreated(ItemCollection e) {
	}

	public void onWorkitemChanged(ItemCollection e) {
	}

	public void onWorkitemProcess(ItemCollection e) {
	}

	public void onWorkitemProcessCompleted(ItemCollection e) {
		cache.clear();
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

}
