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
 *  	Imixs Software Solutions GmbH - initial API and implementation
 *  	Ralph Soika
 *  
 *******************************************************************************/
package org.imixs.marty.web.office;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import org.imixs.marty.web.workitem.WorkitemListener;
import org.imixs.marty.web.workitem.WorkitemMB;
import org.imixs.workflow.ItemCollection;

/**
 * This bean provides functionallity to add time-sheet entries to a workitem.
 * The Bean works inside the Parent workitem 6000-6099 and the child process
 * 6100-5199
 * 
 * The bean supports only workitems in the 61xxx range - this is currently
 * hard-coded! (see method getProcessFitlerSelection)
 * 
 * The property myTimeSheetSummary holds an itemCollection with the summary for
 * all attributes with number values
 * 
 * @author rsoika
 */
public class TimeSheetMB implements WorkitemListener {
	private WorkitemMB workitemMB = null;
	private ItemCollection filter = null; // search filter
	private ArrayList<SelectItem> processSelection = null;

	private List<ItemCollection> myTimeSheet = null;
	private ItemCollection myTimeSheetSummary = null;
	private ItemCollection filterTimeSheetSummary = null;
	private List<ItemCollection> filterTimeSheet = null;

	private static Logger logger = Logger.getLogger("org.imixs.workflow");

	/**
	 * This method register the bean as an workitemListener
	 */
	@PostConstruct
	public void init() {
		// register this Bean as a workitemListener to the current WorktieMB
		this.getWorkitemBean().addWorkitemListener(this);
	}

	/**
	 * creates a new child with default values
	 * 
	 * @param event
	 * @throws Exception
	 */
	public void doCreate(ActionEvent event) throws Exception {

		this.getWorkitemBean().doCreateChildWorkitem(event);

	}

	public void doEdit(ActionEvent event) throws Exception {
		this.getWorkitemBean().doEditChild(event);
	}

	public void doReset(ActionEvent event) throws Exception {
		myTimeSheet = null;
		filterTimeSheet = null;
	}

	/**
	 * creates test data....
	 * 
	 * @param event
	 * @throws Exception
	 */
	public void doSimulate(ActionEvent event) throws Exception {
		int totalcount = 0;
		// read projects to get refs
		String sQuery = "SELECT DISTINCT wi FROM Entity AS wi ";
		sQuery += " JOIN wi.textItems as tref ";
		sQuery += " WHERE wi.type = 'workitem' ";
		sQuery += " AND tref.itemName = 'txtworkflowgroup' and tref.itemValue = 'Projekt' ";

		Collection<ItemCollection> col = this.getWorkitemBean()
				.getEntityService().findAllEntities(sQuery, 0, -1);

		logger.info(col.size() + " Projects found");

		for (ItemCollection aworkitem : col) {
			String sIDRef = aworkitem.getItemValueString("$uniqueid");
			// generate 3 Demo Entries
			for (int i = 0; i < 500; i++) {

				// create test data
				Random r = new Random();
				int iUser = r.nextInt(4);
				String sUser = null;

				switch (iUser) {

				case 1:
					sUser = "Ronny";
					break;
				case 2:
					sUser = "Anna";
					break;
				case 3:
					sUser = "Eddy";
					break;

				default:
					sUser = "rsoika";
					break;
				}

				// create random date
				Calendar calendar = Calendar.getInstance();
				calendar.set(2011, r.nextInt(11), r.nextInt(30));

				// now create the test dataItemcollection
				ItemCollection itemColDemo = new ItemCollection();
				itemColDemo.replaceItemValue("type", "childworkitem");
				itemColDemo.replaceItemValue("$UniqueIDRef", sIDRef);

				itemColDemo.replaceItemValue(
						"$modelversion",
						this.getWorkitemBean().getWorkitem()
								.getItemValue("$modelversion"));

				itemColDemo.replaceItemValue("$processid", 6100);
				itemColDemo.replaceItemValue("$activityid", 10);
				itemColDemo.replaceItemValue("_duration",
						(r.nextInt(6) + 1) * 10);
				itemColDemo.replaceItemValue("datdate", calendar.getTime());
				itemColDemo.replaceItemValue("namcreator", sUser);
				itemColDemo.replaceItemValue("_subject", sUser + "'s job...");
				itemColDemo.replaceItemValue("_category", "C" + r.nextInt(4));

				// process demo data....
				this.getWorkitemBean().getWorkflowService()
						.processWorkItem(itemColDemo);

				totalcount++;
				logger.info(" ************ " + totalcount
						+ " workitems process ************");
			}
		}
	}

	/**
	 * returns the current search filter
	 * 
	 * @return
	 */
	public ItemCollection getFilter() {
		if (filter == null) {
			filter = new ItemCollection();

			// set date
			try {
				filter.replaceItemValue("datfrom", new Date());
				filter.replaceItemValue("datto", new Date());
			} catch (Exception e) {

				e.printStackTrace();
			}

		}
		return filter;
	}

	public void setFilter(ItemCollection filter) {
		this.filter = filter;
	}

	/**
	 * returns a SelctItem Array containing all Process Ids for the current
	 * workflowGroupFilter. The workflowGroupFilter property has the format:
	 * 'offic-de-1.0.0|Purchase'
	 * 
	 * the method searches only for processIDs in the same ModelVerson and Group
	 * 
	 * 
	 * @return a SelectItem array list with the corresponding ProcessIDs (int)
	 */
	public ArrayList<SelectItem> getProcessFilterSelection() {

		if (processSelection != null)
			return processSelection;

		// build new processSelection
		processSelection = new ArrayList<SelectItem>();

		try {
			// find first process entity

			ItemCollection startProcess;
			// current model Version
			String sModelVersion = this.getWorkitemBean().getWorkitem()
					.getItemValueString("$modelversion");
			startProcess = this.getWorkitemBean().getModelService()
					.getProcessEntityByVersion(6100, sModelVersion);

			if (startProcess == null) {
				logger.warning("TimeSheetMB unable to find start ProcessEntity - ID=6100 !");
				return processSelection;
			}

			String sWorkflowGroup = startProcess
					.getItemValueString("txtWorkflowGroup");
			List<ItemCollection> processList = this
					.getWorkitemBean()
					.getModelService()
					.getAllProcessEntitiesByGroupByVersion(sWorkflowGroup,
							sModelVersion);
			for (ItemCollection process : processList) {
				processSelection.add(new SelectItem(process
						.getItemValueInteger("numprocessid"), process
						.getItemValueString("txtname")));

			}
		} catch (Exception e) {
			logger.severe("TimeSheetMB unable - error computing process list");
			e.printStackTrace();
		}
		return processSelection;

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
		try {

			String sRefUniqueID = this.getWorkitemBean().getWorkitem()
					.getItemValueString("$uniqueid");
			if ("".equals(sRefUniqueID))
				return;

			FacesContext context = FacesContext.getCurrentInstance();
			ExternalContext externalContext = context.getExternalContext();
			String sUser = externalContext.getUserPrincipal().getName();

			// construct query
			String sQuery = "SELECT DISTINCT wi FROM Entity AS wi ";
			sQuery += " JOIN wi.textItems as tref ";
			sQuery += " JOIN wi.integerItems as tp ";
			sQuery += " JOIN wi.textItems as tc ";
			sQuery += " JOIN wi.calendarItems as td ";

			// restrict type depending of a supporte ref id
			sQuery += " WHERE wi.type = 'childworkitem' ";

			sQuery += " AND tref.itemName = '$uniqueidref' and tref.itemValue = '"
					+ sRefUniqueID + "' ";

			sQuery += " AND tc.itemName = 'namcreator' and tc.itemValue = '"
					+ sUser + "' ";
			// Process ID
			sQuery += " AND tp.itemName = '$processid' AND tp.itemValue >=6100 AND tp.itemValue<=6999";

			sQuery += " AND td.itemName = 'datdate' ";

			sQuery += " ORDER BY td.itemValue DESC";

			logger.fine("TimeSheetMB loadMyTimeSheet - query=" + sQuery);
			Collection<ItemCollection> col = this.getWorkitemBean()
					.getEntityService().findAllEntities(sQuery, 0, -1);

			for (ItemCollection aworkitem : col) {
				myTimeSheet.add((aworkitem));
				computeSummaryOfNumberValues(aworkitem,myTimeSheetSummary);
			}
		} catch (Exception ee) {
			myTimeSheet = null;
			ee.printStackTrace();
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

		try {
			if (filter == null)
				return;

			String sRefUniqueID = this.getWorkitemBean().getWorkitem()
					.getItemValueString("$uniqueid");
			if ("".equals(sRefUniqueID))
				return;

			String sUser = filter.getItemValueString("namCreator");

			Date datFrom = filter.getItemValueDate("datFrom"); // format
																// '2008-09-15'
			Date datTo = filter.getItemValueDate("datTo");
			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

			int iProcessID = filter.getItemValueInteger("$processid");

			// construct query
			String sQuery = "SELECT DISTINCT wi FROM Entity AS wi ";
			sQuery += " JOIN wi.textItems as tref ";

			if (iProcessID > 0)
				sQuery += " JOIN wi.integerItems as tp ";

			if (!"".equals(sUser))
				sQuery += " JOIN wi.textItems as tc ";

			if (datFrom != null || datTo != null)
				sQuery += " JOIN wi.calendarItems as td ";

			// restrict type depending of a supporte ref id
			sQuery += " WHERE wi.type = 'childworkitem' ";

			sQuery += " AND tref.itemName = '$uniqueidref' and tref.itemValue = '"
					+ sRefUniqueID + "' ";

			if (!"".equals(sUser))
				sQuery += " AND tc.itemName = 'namcreator' and tc.itemValue = '"
						+ sUser + "' ";
			// Process ID
			if (iProcessID > 0)
				sQuery += " AND tp.itemName = '$processid' AND tp.itemValue ="
						+ iProcessID;

			sQuery += " AND td.itemName = 'datdate' ";

			if (datFrom != null)

				sQuery += " AND td.itemValue >= '" + formatter.format(datFrom)
						+ "' ";

			if (datTo != null) {
				// format '2008-09-15'
				// we need to adjust the day for 1 because time is set to
				// 0:00:00 per default
				Calendar calTo = Calendar.getInstance();
				calTo.setTime(datTo);
				calTo.add(Calendar.DAY_OF_MONTH, 1);
				sQuery += " AND td.itemValue < '"
						+ formatter.format(calTo.getTime()) + "' ";
			}

			sQuery += " ORDER BY td.itemValue ASC";

			logger.fine("TimeSheetMB loadFilterTimeSheet - query=" + sQuery);
			Collection<ItemCollection> col = this.getWorkitemBean()
					.getEntityService().findAllEntities(sQuery, 0, -1);

			for (ItemCollection aworkitem : col) {
				filterTimeSheet.add((aworkitem));
				computeSummaryOfNumberValues(aworkitem,filterTimeSheetSummary);
			}
		} catch (Exception ee) {
			filterTimeSheet = null;
			ee.printStackTrace();
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
	private void computeSummaryOfNumberValues(ItemCollection aworkitem,
			ItemCollection summaryWorkitem) {
		// test if attributes can be summaries into the
		// myTimeSheetSummary.
		// iterate over all items
		for (Object key : aworkitem.getAllItems().keySet()) {
			// test the object type
			try {
				Vector v = aworkitem.getItemValue(key.toString());
				if (v.size() > 0) {
					Object o = v.firstElement();
					// test if the object value is a number....
					Double ff = 0.0;
					try {
						ff = Double.valueOf(o.toString());
					} catch (NumberFormatException def) {
						// not a number
					}
					if (ff != 0) {
						logger.fine("compute summary " + ff);
						double dSummary = summaryWorkitem
								.getItemValueDouble(key.toString());
						for (Object d : v) {
							dSummary += Double.valueOf(d.toString());
						}
						summaryWorkitem.replaceItemValue(key.toString(),
								dSummary);
					}
				}
	
			} catch (Exception e) {
				logger.warning("error computing Summary: " + e.getMessage());
			}
		}
	}

	/**
	 * helper method to get the current instance of the WorkitemMB
	 * 
	 * @return
	 */
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

	public void onWorkitemChanged(ItemCollection arg0) {
		// reset timesheet
		myTimeSheet = null;
	}

	/**
	 * updates the comment list Therefor the method copies the txtComment into
	 * the txtCommentList and clears the txtComment field
	 */
	public void onWorkitemProcess(ItemCollection workitem) {

	}

	public void onChildProcess(ItemCollection arg0) {
		// nothing
	}

	public void onChildCreated(ItemCollection arg0) {
		try {
			arg0.replaceItemValue("datdate", new Date());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onChildProcessCompleted(ItemCollection arg0) {
		myTimeSheet = null;
		filterTimeSheet=null;
	}

	public void onWorkitemCreated(ItemCollection arg0) {
		// nothing
	}

	public void onWorkitemProcessCompleted(ItemCollection arg0) {

	}

	public void onChildDelete(ItemCollection e) {
		// nothing
	}

	public void onChildDeleteCompleted() {
		myTimeSheet = null;
	}

	public void onChildSoftDelete(ItemCollection e) {
		// nothing
	}

	public void onChildSoftDeleteCompleted(ItemCollection e) {
		myTimeSheet = null;
	}

	public void onWorkitemDelete(ItemCollection e) {
		// nothing
	}

	public void onWorkitemDeleteCompleted() {
		// nothing
	}

	public void onWorkitemSoftDelete(ItemCollection e) {

	}

	public void onWorkitemSoftDeleteCompleted(ItemCollection e) {
		// nothing
	}

}