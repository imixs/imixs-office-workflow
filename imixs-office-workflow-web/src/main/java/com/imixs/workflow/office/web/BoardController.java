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
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Observes;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.inject.Inject;
import javax.inject.Named;

import org.imixs.marty.config.SetupController;
import org.imixs.marty.workflow.WorkflowEvent;
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
@SessionScoped
public class BoardController implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final int DEFAULT_PAGE_SIZE = 100;
	private static final int DEFAULT_CATEGORY_PAGE_SIZE = 5;

	private Map<BoardCategory, List<ItemCollection>> cacheTasks;

	private int categoryPageSize;
	private int pageSize;
	private int pageIndex = 0;
	private boolean endOfList = false;
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

	
	
	
	/***************************************************************************
	 * Navigation
	 */

	public int getPageIndex() {
		return pageIndex;
	}

	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
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

	/**
	 * Get the board title. The default board title is message['worklist.owner']
	 * 
	 * @return
	 */
	public String getTitle() {
		if (title == null || title.isEmpty()) {
			// get default title...
			// #{message['worklist.owner']}
			try {
				Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
				ResourceBundle rb = null;
				if (locale != null)
					rb = ResourceBundle.getBundle("bundle.messages", locale);
				else
					rb = ResourceBundle.getBundle("bundle.messages");
				title = rb.getString("worklist.owner");
			} catch (java.util.MissingResourceException eb) {
				title = "";
			}
		}

		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * This method computes the search query based on the property ref. If no ref is
	 * defined, the default query selects the task list for the current user.
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
	 * This method discards the cache an reset the current ref.
	 */
	public void reset() {
		cacheTasks = null;
		ref = null;
		pageIndex = 0;
		endOfList = false;
	}

	/**
	 * This method discards the cache.
	 */
	public void refresh() {
		cacheTasks = null;
		pageIndex = 0;
		endOfList = false;
	}

	/**
	 * Returns a list of all workflow groups out of the current worklist. If no
	 * worklist is yet selected, the method triggers the method readWorkList();
	 * 
	 * @return
	 */
	public List<BoardCategory> getCategories() {
		if (cacheTasks == null) {
			// initalize the task list...
			readWorkList();
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

	public void doLoadNext() {
		pageIndex++;
		cacheTasks = null;
	}

	public void doLoadNext(ActionEvent event) {
		doLoadNext();
	}

	public void doLoadNext(AjaxBehaviorEvent event) {
		doLoadNext();
	}

	public void doLoadPrev() {
		pageIndex--;
		if (pageIndex < 0) {
			pageIndex = 0;
		}
		cacheTasks = null;
	}

	public void doLoadPrev(ActionEvent event) {
		doLoadPrev();
	}

	public void doLoadPrev(AjaxBehaviorEvent event) {
		doLoadPrev();
	}

	public boolean isEndOfList() {
		if (cacheTasks == null) {
			// initalize the task list...
			readWorkList();
		}
		return endOfList;
	}

	public void setEndOfList(boolean endOfList) {
		this.endOfList = endOfList;
	}

	/**
	 * WorkflowEvent listener listens to WORKITEM events and reset the result list
	 * after changing a workitem.
	 * 
	 * @param workflowEvent
	 **/
	public void onWorkflowEvent(@Observes WorkflowEvent workflowEvent) {
		if (workflowEvent == null || workflowEvent.getWorkitem() == null) {
			return;
		}
		if (WorkflowEvent.WORKITEM_AFTER_PROCESS == workflowEvent.getEventType()) {
			refresh();
		}
	}

	public int getCategoryPageSize() {
		if (categoryPageSize <= 0) {
			categoryPageSize=DEFAULT_CATEGORY_PAGE_SIZE;
		}
		return categoryPageSize;
	}

	public void setCategoryPageSize(int categoryPageSize) {
		this.categoryPageSize = categoryPageSize;
	}

	/**
	 * This method returns the current page of workitems for the given category.
	 * 
	 * @param category
	 * @return
	 */
	public List<ItemCollection> getWorkitems(BoardCategory category) {
		List<ItemCollection> temp = cacheTasks.get(category);
	
		int iStart = category.getPageIndex() * category.getPageSize();
		int iEnd = iStart + (category.getPageSize());
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
		int i = (category.getPageIndex() + 1) * category.getPageSize();
	
		if (i >= temp.size()) {
			return true;
		}
	
		return false;
	
	}

	/**
	 * This method reads the first 100 elements form the task list and puts them
	 * into a cache.
	 * 
	 * @throws QueryException
	 * 
	 */
	private void readWorkList()  {
		long l=System.currentTimeMillis();
		cacheTasks = new HashMap<>();
		
		String sortBy = setupController.getSortBy();
		boolean bReverse = setupController.getSortReverse();
		List<ItemCollection> taskList;
		try {
			taskList = documentService.find(getQuery(), getPageSize(), getPageIndex(), sortBy,
					bReverse);
		} catch (QueryException e) {
			logger.warning("failed to read task list: " + e.getMessage());
			taskList=new ArrayList<>();
		}

		endOfList = (taskList.size() < pageSize);

		// now split up the result into groups and tasks....

		for (ItemCollection workitem : taskList) {

			BoardCategory tmpCat = new BoardCategory(workitem.getItemValueString(WorkflowKernel.WORKFLOWGROUP),
					workitem.getItemValueString(WorkflowKernel.WORKFLOWSTATUS), workitem.getProcessID(),getCategoryPageSize());

			// did we already have a cached list?
			List<ItemCollection> tasksByCategory = cacheTasks.get(tmpCat);
			if (tasksByCategory == null) {
				tasksByCategory = new ArrayList<>();
			}
			// add task....
			tasksByCategory.add(workitem);

			cacheTasks.put(tmpCat, tasksByCategory);
		}

		logger.fine("read worklist in " + (System.currentTimeMillis()-l) +"ms");

	}

}
