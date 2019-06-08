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

package com.imixs.workflow.office.views;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.imixs.marty.config.SetupController;
import org.imixs.marty.model.ProcessController;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.engine.lucene.LuceneSearchService;
import org.imixs.workflow.exceptions.QueryException;
import org.imixs.workflow.faces.data.ViewController;

/**
 * The SearchController provides methods for a convenient search experience.
 * <p>
 * The conntroller extends the Imixs ViewController and provides custom filter
 * and search queries to request a individual WorkList result. The
 * ItemCollection search filter holds filter criteria for a customized search
 * query.
 * <p>
 * The bean is session scoped to hold the search filter over a page journy.
 * <p>
 * The SearchController defines alos a set of predefined filter properties:
 * 
 * <ul>
 * <li>_processRef = holds a reference to a core process entity</li>
 * <li>_spaceRef = holds a reference to a core space entity</li>
 * <li>_phrase = holds the serach phrase</li>
 * </ul>
 * 
 * 
 * @author rsoika
 * @version 2.2.0
 */
@Named
@SessionScoped
public class SearchController extends ViewController implements Serializable {

	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(SearchController.class.getName());

	ItemCollection searchFilter = null;

	@Inject
	SetupController setupController;

	@Inject
	ProcessController processController;

	@EJB
	LuceneSearchService luceneSearchService;

	ItemCollection process;
	ItemCollection space;

	/**
	 * This method set the sort order and sort criteria
	 * 
	 **/
	public void init() {
		this.setSortBy(setupController.getSortBy());
		this.setSortReverse(setupController.getSortReverse());
		searchFilter = new ItemCollection();

		// extract the id from the query string
		FacesContext fc = FacesContext.getCurrentInstance();
		Map<String, String> paramMap = fc.getExternalContext().getRequestParameterMap();
		searchFilter.replaceItemValue("processref", paramMap.get("processref"));
		searchFilter.replaceItemValue("spaceref", paramMap.get("spaceref"));
		searchFilter.replaceItemValue("phrase", paramMap.get("phrase"));

		// try to load process/space objects
		process = processController.getEntityById(searchFilter.getItemValueString("processref"));
		space = processController.getEntityById(searchFilter.getItemValueString("spaceref"));

	}

	public ItemCollection getProcess() {
		return process;
	}

	public ItemCollection getSpace() {
		return space;
	}

	/**
	 * Resets the search filter and the current result.
	 * 
	 * @param event
	 */
	@Override
	public void reset() {
		searchFilter = new ItemCollection();
		searchFilter.replaceItemValue("type", "workitem");
		super.reset();
	}

	/**
	 * Resets the search filter but not the search phrase (txtSearch) The method
	 * reset the current result.
	 * 
	 * @param event
	 */
	public void resetFilter() {
		String searchPhrase = searchFilter.getItemValueString("txtSearch");
		searchFilter = new ItemCollection();
		searchFilter.replaceItemValue("type", "workitem");
		super.reset();
		// restore search phrase
		searchFilter.replaceItemValue("txtSearch", searchPhrase);
	}

	/**
	 * Creates a bookmarkable search link to the worklist including the current search phrase. 
	 * 
	 * @param action
	 *            - post-redirect link to worklist
	 */
	public String doSearch() {
		String action = "/pages/workitems/worklist.xhtml?faces-redirect=true&phrase="
				+ searchFilter.getItemValueString("phrase");
		return action;
	}

	public ItemCollection getSearchFilter() {
		if (searchFilter == null)
			searchFilter = new ItemCollection();
		return searchFilter;
	}

	public void setSearchFilter(ItemCollection searchFilter) {
		this.searchFilter = searchFilter;
	}

	/**
	 * Can be set to true to restrict the result to workitems containing
	 * attachments.
	 * 
	 * @param dms
	 */
	public void setDMSMode(boolean dms) {
		this.searchFilter.replaceItemValue("dms_search", dms);
	}

	/**
	 * Returns a Lucene search query based on the define searchFilter parameter set
	 * 
	 * Depending on the view type the method restricts the result set by namcreator
	 * or namowner
	 * 
	 * 
	 * If a search phrase is given, sortBy and sortReverse are reset. In case of a
	 * general search in a specific contest the sortBy and sortReverse are set the
	 * settings provided by the setupController
	 * 
	 * @param searchFilter
	 *            - ItemCollection with filter criteria
	 * @param view
	 *            - WorkList View type - @see WorklistController
	 * 
	 * @return - a lucene search query
	 */
	@SuppressWarnings({ "unchecked", "unlikely-arg-type" })
	@Override
	public String getQuery() {
		String sSearchTerm = "";
		String emptySearchTerm = ""; // indicates that no query as the default type-query was defined.

		// read the filter parameters and removes duplicates
		List<Integer> processIDs = searchFilter.getItemValue("$taskID");
		List<String> processRefList = searchFilter.getItemValue("ProcessRef");
		List<String> spacesRefList = searchFilter.getItemValue("SpaceRef");
		List<String> workflowGroups = searchFilter.getItemValue("$WorkflowGroup");
		// trim lists
		while (processIDs.contains(""))
			processIDs.remove("");
		while (processRefList.contains(""))
			processRefList.remove("");
		while (spacesRefList.contains(""))
			spacesRefList.remove("");
		while (workflowGroups.contains(""))
			workflowGroups.remove("");
		while (processRefList.contains("-"))
			processRefList.remove("-");
		while (spacesRefList.contains("-"))
			spacesRefList.remove("-");

		List<String> typeList = searchFilter.getItemValue("Type");
		if (typeList.isEmpty() || "".equals(typeList.get(0))) {
			// typeList = Arrays.asList(new String[] { "workitem", "workitemarchive" });
			// default restrict to workitem
			typeList = Arrays.asList(new String[] { "workitem" });
		}

		// convert type list into comma separated list
		String sTypeQuery = "";
		Iterator<String> iterator = typeList.iterator();
		while (iterator.hasNext()) {
			sTypeQuery += "type:\"" + iterator.next() + "\"";
			if (iterator.hasNext())
				sTypeQuery += " OR ";
		}
		sSearchTerm += "(" + sTypeQuery + ") AND";
		emptySearchTerm = sSearchTerm; // define empty serach query

		// test if dms_search==true
		if ("true".equals(searchFilter.getItemValueString("dms_search"))) {
			sSearchTerm += " (dms_count:[1 TO 99]) AND";
		}

		// test if result should be restricted to creator?
		String sCreator = searchFilter.getItemValueString("Creator");

		// test if result should be restricted to owner?
		String sOwner = searchFilter.getItemValueString("namOwner");

		Date datFrom = searchFilter.getItemValueDate("date.From");
		Date datTo = searchFilter.getItemValueDate("date.To");

		// process ref
		if (!processRefList.isEmpty()) {
			sSearchTerm += " (";
			iterator = processRefList.iterator();
			while (iterator.hasNext()) {
				sSearchTerm += "$uniqueidref:\"" + iterator.next() + "\"";
				if (iterator.hasNext())
					sSearchTerm += " OR ";
			}
			sSearchTerm += " ) AND";
		}

		// Space ref
		if (!spacesRefList.isEmpty()) {
			sSearchTerm += " (";
			iterator = spacesRefList.iterator();
			while (iterator.hasNext()) {
				sSearchTerm += "$uniqueidref:\"" + iterator.next() + "\"";
				if (iterator.hasNext())
					sSearchTerm += " OR ";
			}
			sSearchTerm += " ) AND";
		}

		// Workflow Group...
		if (!workflowGroups.isEmpty()) {
			sSearchTerm += " (";
			iterator = workflowGroups.iterator();
			while (iterator.hasNext()) {
				sSearchTerm += "txtworkflowgroup:\"" + iterator.next() + "\"";
				if (iterator.hasNext())
					sSearchTerm += " OR ";
			}
			sSearchTerm += " ) AND";

		}

		// serach date range?
		String sDateFrom = "191401070000"; // because * did not work here
		String sDateTo = "211401070000";
		SimpleDateFormat dateformat = new SimpleDateFormat("yyyyMMddHHmm");

		if (datFrom != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(datFrom);
			sDateFrom = dateformat.format(cal.getTime());
		}
		if (datTo != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(datTo);
			cal.add(Calendar.DATE, 1);
			sDateTo = dateformat.format(cal.getTime());
		}

		if (datFrom != null || datTo != null) {
			// expected format $created:[20020101 TO 20030101]
			sSearchTerm += " ($created:[" + sDateFrom + " TO " + sDateTo + "]) AND";
		}

		// creator
		if (!"".equals(sCreator)) {
			sSearchTerm += " ($creator:\"" + sCreator.toLowerCase() + "\") AND";
		}

		// owner
		if (!"".equals(sCreator)) {
			sSearchTerm += " (namowner:\"" + sOwner.toLowerCase() + "\") AND";
		}

		if (!processIDs.isEmpty()) {
			sSearchTerm += " (";
			Iterator<Integer> iteratorID = processIDs.iterator();
			while (iteratorID.hasNext()) {
				sSearchTerm += "$taskid:\"" + iteratorID.next() + "\"";
				if (iteratorID.hasNext())
					sSearchTerm += " OR ";
			}
			sSearchTerm += " ) AND";
		}

		// Search phrase....
		String searchphrase = searchFilter.getItemValueString("phrase");
		// escape search phrase
		try {
			searchphrase = luceneSearchService.normalizeSearchTerm(searchphrase);
		} catch (QueryException e) {
			// add a new FacesMessage into the FacesContext
			FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_WARN, e.getLocalizedMessage(), null);
			FacesContext.getCurrentInstance().addMessage(null, facesMessage);
			return null;
		}

		if (searchphrase != null && !"".equals(searchphrase)) {
			// we do not sort the result other then by relevance. Sorting by date does not
			// make sense.
			setSortBy(null);
			this.setSortReverse(false);
			// trim
			searchphrase = searchphrase.trim();
			// lower case....
			searchphrase = searchphrase.toLowerCase();
			sSearchTerm += " (" + searchphrase + ") ";
		} else {
			// set sortBy and sortReverse
			this.setSortBy(setupController.getSortBy());
			this.setSortReverse(setupController.getSortReverse());
		}

		// test if a seach query was finally defined....
		if (sSearchTerm.equals(emptySearchTerm)) {
			// no query defined
			sSearchTerm = null;
		}

		// cut last AND
		if (sSearchTerm != null && sSearchTerm.endsWith("AND")) {
			sSearchTerm = sSearchTerm.substring(0, sSearchTerm.length() - 3);
		}

		logger.fine("Query=" + sSearchTerm);
		return sSearchTerm;
	}

}
