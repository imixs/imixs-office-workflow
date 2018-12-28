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

package com.imixs.workflow.office.forms;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.WorkflowKernel;
import org.imixs.workflow.engine.DocumentService;
import org.imixs.workflow.engine.ModelService;
import org.imixs.workflow.exceptions.AccessDeniedException;
import org.imixs.workflow.exceptions.QueryException;
import org.imixs.workflow.faces.data.WorkflowEvent;
import org.imixs.workflow.faces.util.LoginController;


/**
 ** This Bean acts a a front controller for the TimeSheet sub forms.
 * 
 * @author rsoika
 * 
 */
@Named
@ConversationScoped
public class TimesheetController extends ChildWorkitemController implements Serializable {

	private static final long serialVersionUID = 1L;

	private ItemCollection filter = null; // search filter
	private List<ItemCollection> myTimeSheet = null;
	private ItemCollection myTimeSheetSummary = null;
	private ItemCollection filterTimeSheetSummary = null;
	private List<ItemCollection> filterTimeSheet = null;
	private List<ItemCollection> processSelection = null;

	@Inject
	protected LoginController loginController = null;

	@EJB
	ModelService modelService = null;
	
	@EJB 
	DocumentService documentService;

	/**
	 * Initializes the search filter
	 * 
	 * @return
	 */
	public void initFilter() {
		filter = new ItemCollection();
		// set date
		try {
			filter.replaceItemValue("datfrom", new Date());
			filter.replaceItemValue("datto", new Date());
		} catch (Exception e) {

			e.printStackTrace();
		}

	}

	/**
	 * returns the current search filter
	 * 
	 * @return
	 */
	public ItemCollection getFilter() {
		if (filter == null)
			initFilter();
		return filter;
	}

	public void setFilter(ItemCollection filter) {
		this.filter = filter;
	}

	/**
	 * Reset the current users timeSheet selection
	 */
	@Override
	public void reset() {
		super.reset();
		myTimeSheet = null;
		filterTimeSheet = null;
	}

	/**
	 * reset the current child workItem
	 */
	public void clear() {
		//this.setWorkitem(null);
	}

	/**
	 * retuns a List with all timesheet entries for the current user
	 * 
	 * @return
	 */
	public List<ItemCollection> getMyTimeSheet() {
		if (myTimeSheet == null)
			loadMyTimeSheet();
		return myTimeSheet;
	}

	/**
	 * Returns the myTimeSheetSummary Itemcollection. The values are computed
	 * during the method call loadMyTimeSheet()
	 * 
	 * @return
	 */
	public ItemCollection getMyTimeSheetSummary() {
		if (myTimeSheetSummary == null)
			myTimeSheetSummary = new ItemCollection();
		return myTimeSheetSummary;
	}

	/**
	 * retuns a List with all timesheet entries filtered by the filter setting
	 * 
	 * @return
	 */
	public List<ItemCollection> getFilterTimeSheet() {
		if (filterTimeSheet == null)
			loadFilterTimeSheet();
		return filterTimeSheet;
	}

	/**
	 * Returns the myTimeSheetSummary Itemcollection. The values are computed
	 * during the method call loadMyTimeSheet()
	 * 
	 * @return
	 */
	public ItemCollection getFilterTimeSheetSummary() {
		if (filterTimeSheetSummary == null)
			filterTimeSheetSummary = new ItemCollection();
		return filterTimeSheetSummary;
	}

	/**
	 * returns an Array containing all Process Entities from the TimeSheet sub
	 * process (6100-6199)
	 * 
	 * @return an array list with the corresponding ProcessEntities
	 */
	public List<ItemCollection> getProcessSelection() {

		if (processSelection != null)
			return processSelection;

		// build new processSelection
		processSelection = new ArrayList<ItemCollection>();

		try {
			// find first process entity

			ItemCollection startProcess;
			// current model Version
			String sModelVersion = this.getParentWorkitem().getItemValueString("$modelversion");
			startProcess = modelService.getModel(sModelVersion).getTask(6100);

			if (startProcess == null) {
				logger.warning("TimeSheetMB unable to find start ProcessEntity - ID=6100 !");
				return processSelection;
			}

			String sWorkflowGroup = startProcess.getItemValueString("txtWorkflowGroup");
			List<ItemCollection> processList = modelService.getModel(sModelVersion).findTasksByGroup(sWorkflowGroup);
			for (ItemCollection process : processList) {
				processSelection.add(process);

			}
		} catch (Exception e) {
			logger.severe("TimeSheetMB unable - error computing process list");
			e.printStackTrace();
		}
		return processSelection;

	}

	@Override
	public void onWorkflowEvent(@Observes WorkflowEvent workflowEvent) throws AccessDeniedException {
		super.onWorkflowEvent(workflowEvent);
		// if workItem has changed, then reset the child list
		if (workflowEvent != null && WorkflowEvent.WORKITEM_CHANGED == workflowEvent.getEventType()) {
			initFilter();
		}

//		if (workflowEvent != null && WorkflowEvent.WORKITEM_BEFORE_PROCESS == workflowEvent.getEventType()) {
//			// test if datdate is set
//			if (workflowEvent.getWorkitem().getItemValueDate("datdate") == null)
//				workflowEvent.getWorkitem().replaceItemValue("datdate", new Date());
//
//			// test if _duration is set
//			if ("".equals(workflowEvent.getWorkitem().getItemValueString("_duration")))
//				workflowEvent.getWorkitem().replaceItemValue("_duration", 0);
//		}
	}

	/**
	 * this method loads the child workitems to the current workitem.
	 * 
	 * In Addition the method creates the ItemCollection myTimeSheetSummary.
	 * This itemColletion contains the summary of all attributes which are
	 * number values.
	 * 
	 * @see org.imixs.WorkitemService.business.WorkitemServiceBean
	 */
	private void loadMyTimeSheet() {
		myTimeSheet = new ArrayList<ItemCollection>();
		myTimeSheetSummary = new ItemCollection();
		if (getParentWorkitem() != null) {
			String uniqueIdRef = getParentWorkitem().getItemValueString(WorkflowKernel.UNIQUEID);

			String sUser = loginController.getUserPrincipal();

//			// construct query
//			String sQuery = "SELECT DISTINCT wi FROM Entity AS wi ";
//			sQuery += " JOIN wi.textItems as tref ";
//			sQuery += " JOIN wi.integerItems as tp ";
//			sQuery += " JOIN wi.textItems as tc ";
//			sQuery += " JOIN wi.calendarItems as td ";
//
//			// restrict type depending of a supporte ref id
//			sQuery += " WHERE wi.type = 'childworkitem' ";
//
//			sQuery += " AND tref.itemName = '$uniqueidref' and tref.itemValue = '" + uniqueIdRef + "' ";
//
//			sQuery += " AND tc.itemName = 'namcreator' and tc.itemValue = '" + sUser + "' ";
//			// Process ID
//			sQuery += " AND tp.itemName = '$processid' AND tp.itemValue >=6100 AND tp.itemValue<=6999";
//
//			sQuery += " AND td.itemName = 'datdate' ";
//
//			sQuery += " ORDER BY td.itemValue DESC";

			
			
			String sQuery="(type:\"childworkitem\") AND ($uniqueidref:\"" + uniqueIdRef + "\")"
					+ " AND (namcreator:\"" + sUser + "\")"
							+ " AND ($processid:[6100 TO 6999])";
			
			
			
			
			logger.fine("TimeSheetMB loadMyTimeSheet - query=" + sQuery);
			Collection<ItemCollection> col;
			try {
				col = documentService.find(sQuery, 999, 0,"datdate",true);

				for (ItemCollection aworkitem : col) {
					myTimeSheet.add((this.cloneWorkitem(aworkitem)));
					computeSummaryOfNumberValues(aworkitem, myTimeSheetSummary);
				}
			} catch (QueryException e) {
				logger.warning("loadMyTimeSheet - invalid query: " + e.getMessage());
			}

		}

	}

	/**
	 * this method loads the child workitems to the current workitem
	 * 
	 * @see org.imixs.WorkitemService.business.WorkitemServiceBean
	 */
	private void loadFilterTimeSheet() {
		filterTimeSheet = new ArrayList<ItemCollection>();
		filterTimeSheetSummary = new ItemCollection();

		if (filter == null)
			return;
		if (getParentWorkitem() != null) {
			String uniqueIdRef = getParentWorkitem().getItemValueString(WorkflowKernel.UNIQUEID);

			String sUser = filter.getItemValueString("namCreator");

			Date datFrom = filter.getItemValueDate("datFrom"); // format
																// '20080915'
			if (datFrom==null) {
				datFrom=new Date();
			}
			Date datTo = filter.getItemValueDate("datTo");
			if (datTo==null) {
				datTo=new Date();
			}
			DateFormat formatter = new SimpleDateFormat("yyyyMMdd");

			int iProcessID = filter.getItemValueInteger("$processid");

			// construct query
			String sQuery="";
			//= "SELECT DISTINCT wi FROM Entity AS wi ";
			//sQuery += " JOIN wi.textItems as tref ";

//			if (iProcessID > 0)
//				sQuery += " JOIN wi.integerItems as tp ";
//
//			if (!"".equals(sUser))
//				sQuery += " JOIN wi.textItems as tc ";

//			if (datFrom != null || datTo != null)
//				sQuery += " JOIN wi.calendarItems as td ";

			// restrict type depending of a supporte ref id
			//sQuery += " WHERE wi.type IN ('childworkitem','childworkitemarchive') ";
			
			sQuery += "(type:\"childworkitem\" OR type:\"childworkitemarchive\")";

			//sQuery += " AND tref.itemName = '$uniqueidref' and tref.itemValue = '" + uniqueIdRef + "' ";

			sQuery += " AND ($uniqueidref:\"" + uniqueIdRef +"\")"; 
			
			if (!"".equals(sUser)) {
				//sQuery += " AND tc.itemName = 'namcreator' and tc.itemValue = '" + sUser + "' ";
				sQuery += " AND (namcreator:\"" + sUser + "\")";
			}
			// Process ID
			if (iProcessID > 0) {
				//sQuery += " AND tp.itemName = '$processid' AND tp.itemValue =" + iProcessID;
				sQuery += " AND ($processid:" + iProcessID + ")";
			}
			
			//sQuery += " AND td.itemName = 'datdate' ";
			// format '2008-09-15'
			// we need to adjust the day for 1 because time is set to
			// 0:00:00 per default
			Calendar calTo = Calendar.getInstance();
			calTo.setTime(datTo);
			calTo.add(Calendar.DAY_OF_MONTH, 1);
		
			sQuery += " AND (datdate:["+formatter.format(datFrom) + " TO " + formatter.format(calTo.getTime()) + "])";
			

			logger.fine("loadFilterTimeSheet - query=" + sQuery);
			Collection<ItemCollection> col;
			try {
				col = documentService.find(sQuery, 999,0,"datdate", true);
				for (ItemCollection aworkitem : col) {
					filterTimeSheet.add((this.cloneWorkitem(aworkitem)));
					computeSummaryOfNumberValues(aworkitem, filterTimeSheetSummary);
				}
			} catch (QueryException e) {
				logger.warning("loadFilterTimeSheet - invalid query: " + e.getMessage());
			}

			
		}

	}

	/**
	 * This method updates the ItemCollection summaryWorkitem with the number
	 * veralues of the provided workitem. So the summaryWorkitem contains the
	 * summary of all attributes which are number values.
	 * 
	 * 
	 * @param aworkitem
	 * @param summaryWorkitem
	 */
	private void computeSummaryOfNumberValues(ItemCollection aworkitem, ItemCollection summaryWorkitem) {
		// test if attributes can be summaries into the
		// myTimeSheetSummary.
		// iterate over all items
		for (Object key : aworkitem.getAllItems().keySet()) {
			// test the object type
			try {
				List<?> v = aworkitem.getItemValue(key.toString());
				if (v.size() > 0) {
					Object o = v.get(0);
					// test if the object value is a number....
					Double ff = 0.0;
					try {
						ff = Double.valueOf(o.toString());
					} catch (NumberFormatException def) {
						// not a number
					}
					if (ff != 0) {
						logger.fine("compute summary " + ff);
						double dSummary = summaryWorkitem.getItemValueDouble(key.toString());
						for (Object d : v) {
							dSummary += Double.valueOf(d.toString());
						}
						summaryWorkitem.replaceItemValue(key.toString(), dSummary);
					}
				}

			} catch (Exception e) {
				logger.warning("error computing Summary: " + e.getMessage());
			}
		}
	}

	@Override
	public ItemCollection cloneWorkitem(ItemCollection aWorkitem) {
		ItemCollection clone = super.cloneWorkitem(aWorkitem);
		clone.replaceItemValue("_duration", aWorkitem.getItemValue("_duration"));
		clone.replaceItemValue("_subject", aWorkitem.getItemValue("_subject"));
		clone.replaceItemValue("_category", aWorkitem.getItemValue("_category"));
		return clone;
	}

}
