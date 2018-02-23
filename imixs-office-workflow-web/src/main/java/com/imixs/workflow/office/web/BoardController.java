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

package com.imixs.workflow.office.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.imixs.marty.config.SetupController;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.WorkflowKernel;
import org.imixs.workflow.engine.DocumentService;
import org.imixs.workflow.engine.ModelService;
import org.imixs.workflow.exceptions.QueryException;
import org.imixs.workflow.faces.util.LoginController;

/**
 ** The BoardController provides a logic to split up the worklist by there
 * workflow groups and status. The board contoller can select a worklist based
 * on a unqiueIdRef or if not defined by the current user (task list).
 * 
 * The controller holds a cache map storing all tasks by workflow group and
 * status.
 * 
 * @author rsoika
 * 
 */
@Named
//@ConversationScoped
@SessionScoped
public class BoardController implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final int DEFAULT_MAX_RESULT = 100;
	private static final int DEFAULT_PAGE_SIZE = 5;

	private Map<BoardCategory, List<ItemCollection>> cacheTasks;

	private int maxResult;
	private int pageSize;
	private String query;
	private String ref;
	private String title;

	@Inject
	protected LoginController loginController = null;

	@Inject
	SetupController setupController;

	private static Logger logger = Logger.getLogger(BoardController.class.getName());

	@EJB
	ModelService modelService = null;

	@EJB
	DocumentService documentService;

	public int getMaxResult() {
		if (maxResult <= 0) {
			maxResult = DEFAULT_MAX_RESULT;
		}
		return maxResult;
	}

	public void setMaxResult(int maxResult) {
		this.maxResult = maxResult;
	}

	public int getPageSize() {
		if (pageSize <= 0) {
			pageSize = DEFAULT_PAGE_SIZE;
		}
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public String getRef() {
		return ref;
	}

	public void setRef(String ref) {
		this.ref = ref;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * This method computes the search query based on the property ref. If no ref is
	 * defined, the search query selects the task list for the current user.
	 * 
	 * @return
	 */
	public String getQuery() {
		// set default query
		if (ref == null || ref.isEmpty()) {
			query = "(type:\"workitem\" AND namowner:\"" + loginController.getRemoteUser() + "\")";
		} else {
			query = "(type:\"workitem\" AND $uniqueidref:\"" + ref + "\")";
		}
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	/**
	 * This method returns the current page of workitems for the given category.
	 * 
	 * @param category
	 * @return
	 */
	public List<ItemCollection> getWorkitems(BoardCategory category) {
		List<ItemCollection> temp = cacheTasks.get(category);

		int iStart = category.getPageIndex() * getPageSize();
		int iEnd = iStart + (getPageSize());
		if (iEnd > temp.size()) {
			iEnd = temp.size();
		}

		// return sublist
		return temp.subList(iStart, iEnd);
	}

	/**
	 * Loads the next page for a category
	 */
	public void doLoadNext(BoardCategory category) {
		if (!isEndOfList(category)) {
			category.setPageIndex(category.pageIndex + 1);
		}
	}

	/**
	 * Loads the prev page for a category
	 */
	public void doLoadPrev(BoardCategory category) {
		if (category.pageIndex > 0) {
			category.setPageIndex(category.pageIndex - 1);
		}

	}

	public boolean isEndOfList(BoardCategory category) {
		List<ItemCollection> temp = cacheTasks.get(category);
		int i = (category.getPageIndex() + 1) * getPageSize();

		if (i >= temp.size()) {
			return true;
		}

		return false;

	}

	/**
	 * Returns a list of all workflow groups out of the current worklist. 
	 * If no worklist is yet selected, the method triggers the method readWorkList();
	 * 
	 * @return
	 */
	public List<BoardCategory> getCategories() {
		if (cacheTasks == null) {
			// initalize the task list...
			try {
				readWorkList();
			} catch (QueryException e) {
				logger.warning("failed to read task list: " + e.getMessage());
			}

		}
		List<BoardCategory> result = new ArrayList<BoardCategory>();
		result.addAll(cacheTasks.keySet());

		// sort result

		Collections.sort(result, new Comparator<BoardCategory>() {
			@Override
			public int compare(BoardCategory p1, BoardCategory p2) {
				return p1.toString().compareTo(p2.toString());
			}
		});

		return result;
	}

	/**
	 * This method discards the cache an reset the current ref.
	 */
	public void reset() {
		cacheTasks = null;
		ref = null;
	}

	/**
	 * This method reads the first 100 elements form the task list and puts them
	 * into a cache.
	 * 
	 * @throws QueryException
	 * 
	 */
	private void readWorkList() throws QueryException {

		cacheTasks = new HashMap<>();

		String sortBy = setupController.getSortBy();
		boolean bReverse = setupController.getSortReverse();
		List<ItemCollection> taskList = documentService.find(getQuery(), getMaxResult(), 0, sortBy, bReverse);

		// now split up the result into groups and tasks....

		for (ItemCollection workitem : taskList) {

			BoardCategory tmpCat = new BoardCategory(workitem.getItemValueString(WorkflowKernel.WORKFLOWGROUP),
					workitem.getItemValueString(WorkflowKernel.WORKFLOWSTATUS), workitem.getProcessID());

			// did we already have a cached list?
			List<ItemCollection> tasksByCategory = cacheTasks.get(tmpCat);
			if (tasksByCategory == null) {
				tasksByCategory = new ArrayList<>();
			}
			// add task....
			tasksByCategory.add(workitem);

			cacheTasks.put(tmpCat, tasksByCategory);
		}

	}

}
