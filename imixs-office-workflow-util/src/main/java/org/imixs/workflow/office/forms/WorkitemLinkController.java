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
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.NamingException;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.ItemCollectionComparator;
import org.imixs.workflow.WorkflowKernel;
import org.imixs.workflow.engine.WorkflowService;
import org.imixs.workflow.engine.index.SchemaService;
import org.imixs.workflow.exceptions.QueryException;
import org.imixs.workflow.faces.data.WorkflowController;
import org.imixs.workflow.office.util.WorkitemHelper;

/**
 * The WorkitemLinkController provides suggest-box behavior based on the JSF 2.0
 * ajax capability to add WorkItem references to the current WorkItem.
 * 
 * All WorkItem references will be stored in the property 'txtworkitemref'
 * Note: @RequestScoped did not work because the ajax request will reset the
 * result during submit
 * 
 * 
 * 
 * @author rsoika
 * @version 1.0
 */

@Named
@ViewScoped
public class WorkitemLinkController implements Serializable {

    public static final String LINK_PROPERTY = "$workitemref";
	public static final String LINK_PROPERTY_DEPRECATED = "txtworkitemref";
	public static final int MAX_SEARCH_RESULT = 20;
	public static Logger logger = Logger.getLogger(WorkitemLinkController.class.getName());

	@EJB
	protected WorkflowService workflowService;

	@EJB
	protected SchemaService schemaService;

	@Inject
	protected WorkflowController workflowController;

	private static final long serialVersionUID = 1L;
	private List<ItemCollection> searchResult = null;
	// private Map<String, List<ItemCollection>> externalReferences = null;
	// private Map<String, List<ItemCollection>> references = null;

	private String input = null;
	private int minimumChars = 3; // minimum input required for a suggestion

	public WorkitemLinkController() {
		super();
		searchResult = new ArrayList<ItemCollection>();
	}

	public String getInput() {
		if (input==null) {
			input="";
		}
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

	/**
	 * minimum input required for a suggestion Default is 3
	 * 
	 * @return
	 */
	public int getMinimumChars() {
		return minimumChars;
	}

	public void setMinimumChars(int minimumChars) {
		this.minimumChars = minimumChars;
	}

	/**
	 * This method reset the search and input state.
	 */
	public void reset() {
		searchResult = new ArrayList<ItemCollection>();
		input = "";
		logger.fine("workitemLinkController reset");
	}

	/**
	 * This ajax event method reset the search and input state.
	 * 
	 * @param event
	 */
	public void reset(AjaxBehaviorEvent event) {
		reset();
	}

	/**
	 * Starts a lucene search to provide searchResult for suggest list
	 * 
	 * @param filter
	 *            - search filter
	 * @param minchars
	 *            - the minimum length for the filter string
	 */
	public void search(String filter, int minchars) {

		if (input == null || input.isEmpty() || input.length() < minchars) {
			return;
		}
		
			
		logger.fine("search: filter=" + filter);
		searchResult = new ArrayList<ItemCollection>();

		try {
			String sSearchTerm = "";
			// search only type workitem and workitemsarchive
			sSearchTerm += "((type:workitem) OR (type:workitemarchive)) AND ";

			if (filter != null && !"".equals(filter)) {
				String sNewFilter = filter;
				sNewFilter = sNewFilter.replace(".", "?");
				sSearchTerm += "(" + sNewFilter + ") AND ";
			}
			if (!"".equals(input)) {
				// escape input..
				input = schemaService.escapeSearchTerm(input);
				sSearchTerm += " (*" + input.toLowerCase() + "*)";
			}

			searchResult = workflowService.getDocumentService().findStubs(sSearchTerm, MAX_SEARCH_RESULT, 0,
					WorkflowKernel.LASTEVENTDATE, true);


		} catch (Exception e) {
			logger.warning("  lucene error!");
			e.printStackTrace();
		}

	}

	/*
	 * Starts a lucene search to provide searchResult for suggest list
	 */
	public void search(String filter) {
		search(filter, minimumChars);
	}

	public List<ItemCollection> getSearchResult() {
		return searchResult;
	}

	/**
	 * This methods adds a new workItem reference
	 */
	@SuppressWarnings("unchecked")
    public void add(String aUniqueID) {
		logger.fine("LinkController add workitem reference: " + aUniqueID);
		List<String> refList=null;
		//  support deprecated ref field
		if (!workflowController.getWorkitem().hasItem(LINK_PROPERTY) 
		         && workflowController.getWorkitem().hasItem(LINK_PROPERTY_DEPRECATED)) {
		    refList = workflowController.getWorkitem().getItemValue(LINK_PROPERTY_DEPRECATED);
		} else {
		    refList = workflowController.getWorkitem().getItemValue(LINK_PROPERTY);
		}
		
		// clear empty entry if set
		if (refList.size() == 1 && "".equals(refList.get(0)))
			refList.remove(0);

		// test if not yet a member of
		if (refList.indexOf(aUniqueID) == -1) {
			refList.add(aUniqueID);
			workflowController.getWorkitem().replaceItemValue(LINK_PROPERTY, refList);
		}

		// reset
		reset(null);
	}

	/**
	 * This methods removes a workItem reference
	 */
	@SuppressWarnings("unchecked")
    public void remove(String aUniqueID) {

		logger.fine("LinkController remove workitem reference: " + aUniqueID);
		List<String> refList =null;
		//  support deprecated ref field
        if (!workflowController.getWorkitem().hasItem(LINK_PROPERTY) 
                 && workflowController.getWorkitem().hasItem(LINK_PROPERTY_DEPRECATED)) {
            refList = workflowController.getWorkitem().getItemValue(LINK_PROPERTY_DEPRECATED);
        } else {
            refList = workflowController.getWorkitem().getItemValue(LINK_PROPERTY);
        }
        
		// test if a member of
		if (refList.indexOf(aUniqueID) > -1) {
			refList.remove(aUniqueID);
			workflowController.getWorkitem().replaceItemValue(LINK_PROPERTY, refList);
		}
		// reset
		reset(null);
		// references = null;
	}

	/**
	 * This method returns a list of all ItemCollections referred by the current
	 * workItem (txtWorkitemRef).
	 * 
	 * @return - list of ItemCollection
	 */
	public List<ItemCollection> getReferences() {
		return getReferences("");
	}

	/**
	 * This method returns a list of ItemCollections referred by the current
	 * workItem ($WorkitemRef).
	 * <p>
	 * The filter will be applied to the result list. So each WorkItem will be
	 * tested if it matches the filter expression.
	 * <p>
	 * The resultset is sorted by $created
	 * 
	 * @return - list of ItemCollection with matches the current filter
	 */
	@SuppressWarnings("unchecked")
	public List<ItemCollection> getReferences(String filter) {
		long l=System.currentTimeMillis();
		List<ItemCollection> result = new ArrayList<ItemCollection>();

		if (workflowController.getWorkitem() == null) {
			return result;
		}

		logger.finest("......lookup references for: " + filter);

		// lookup the references...
		List<String> refList = null;
		
		//  support deprecated ref field
        if (!workflowController.getWorkitem().hasItem(LINK_PROPERTY) 
                 && workflowController.getWorkitem().hasItem(LINK_PROPERTY_DEPRECATED)) {
            refList = workflowController.getWorkitem().getItemValue(LINK_PROPERTY_DEPRECATED);
        } else {
            refList = workflowController.getWorkitem().getItemValue(LINK_PROPERTY);
        }
        
		
		if (refList != null && !refList.isEmpty()) {
			// select all references.....
			String sQuery = "(";
			for (String ref : refList) {
				sQuery = sQuery + "$uniqueid:\"" + ref + "\" OR ";
			}

			// cust last OR
			if (sQuery.endsWith("OR ")) {
				sQuery = sQuery.substring(0, sQuery.lastIndexOf("OR "));
			}

			sQuery = sQuery + ")";

			List<ItemCollection> workitems = null;

			try {
				workitems = workflowService.getDocumentService().findStubs(sQuery, MAX_SEARCH_RESULT, 0,
						WorkflowKernel.CREATED, true);
			} catch (QueryException e) {

				e.printStackTrace();
			}
			// do we have a result?
			if (workitems != null) {
				// now test if filter matches if defined....
				if (filter != null && !filter.isEmpty()) {
					for (ItemCollection itemcol : workitems) {
						// test
						if (WorkitemHelper.matches(itemcol, filter)) {
							result.add(itemcol);
						}
					}
				} else {
					// no filter - add all
					result.addAll(workitems);
				}
			}
		}

		logger.fine("...lookup references for: " + filter + " in " + (System.currentTimeMillis()-l) + "ms");
		
		return result;
	}

	/**
	 * Returns a list of all workItems holding a reference to the current workItem.
	 * 
	 * @return
	 * @throws NamingException
	 */
	public List<ItemCollection> getExternalReferences() {
		return getExternalReferences("");
	}

	/**
	 * returns a list of all workItems holding a reference to the current workItem.
	 * If the filter is set the processID will be tested for the filter regex
	 * 
	 * @return
	 * @param filter
	 * @throws NamingException
	 */
	public List<ItemCollection> getExternalReferences(String filter) {
		List<ItemCollection> result = new ArrayList<ItemCollection>();

		if (workflowController.getWorkitem() == null) {
			return result;
		}

		String uniqueid = workflowController.getWorkitem().getUniqueID();

		// return an empty list if still no $uniqueid is defined for the
		// current workitem
		if ("".equals(uniqueid)) {
			return result;
		}

		// select all references.....
		String sQuery = "(";
		sQuery = " (type:\"workitem\" OR type:\"workitemarchive\") AND (" + LINK_PROPERTY + ":\"" + uniqueid + "\"  OR " + LINK_PROPERTY_DEPRECATED + ":\"" + uniqueid + "\")";

		List<ItemCollection> workitems = null;

		try {
			workitems = workflowService.getDocumentService().findStubs(sQuery, MAX_SEARCH_RESULT, 0,
					WorkflowKernel.LASTEVENTDATE, true);
		} catch (QueryException e) {

			e.printStackTrace();
		}
		// sort by modified
		Collections.sort(workitems, new ItemCollectionComparator("$created", true));

		// now test if filter matches, and clone the workItem
		if (filter != null && !filter.isEmpty()) {
			for (ItemCollection itemcol : workitems) {
				// test
				if (WorkitemHelper.matches(itemcol, filter)) {
					result.add(itemcol);
				}
			}
		} else {
			result.addAll(workitems);
		}
		return result;

	}

}
