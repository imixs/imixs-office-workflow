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
package com.imixs.workflow.office.forms;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.imixs.marty.ejb.WorkitemService;
import org.imixs.marty.workflow.WorkitemLinkController;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.WorkflowKernel;
import org.imixs.workflow.faces.data.WorkflowController;

/**
 * The ChronicleController collects all chronicle data
 * 
 * history, versions, comments, references, documents
 * 
 * <p>
 * Each chronic entry for a workitme consists of the following data items:
 * <ul>
 * 
 * - type : date|history|file|version|
 * 
 * @see workitem_chronicle.xhtml
 * 
 * @author rsoika,gheinle
 * 
 */
@Named
@RequestScoped
public class ChronicleController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Inject
	protected WorkflowController workflowController;
	private static Logger logger = Logger.getLogger(ChronicleController.class.getName());

	ArrayList<ChronicleEntity> chronicleList;

	Map<Integer, Set<Integer>> yearsMonths;

	@Inject
	protected DMSController dmsController;
	
	@Inject
	protected WorkitemLinkController workitemLinkController;

	@EJB
	protected WorkitemService workitemService;

	@SuppressWarnings("unchecked")
	@PostConstruct
	public void init() {
		long l = System.currentTimeMillis();
		chronicleList = new ArrayList<ChronicleEntity>();

		yearsMonths = new HashMap<Integer, Set<Integer>>();

		/* collect history */
		List<List<Object>> history = workflowController.getWorkitem().getItemValue("txtworkflowhistory");
		// change order
		Collections.reverse(history);
		// do we have real history entries?
		if (history.size() > 0 && history.get(0) instanceof List) {
			for (List<Object> entries : history) {

				Date date = (Date) entries.get(0);
				String message = (String) entries.get(1);
				String user = (String) entries.get(2);

				ItemCollection entry = new ItemCollection();
				entry.replaceItemValue("date", date);
				entry.replaceItemValue("user", user);
				entry.replaceItemValue("message", message);
				entry.replaceItemValue("type", "history");

				addChronicleEntry(entry);
				
			}
		}

		/* collect comments */
		List<Map<String, List<Object>>> comments = workflowController.getWorkitem().getItemValue("txtCommentLog");
		for (Map<String, List<Object>> comment : comments) {

			ItemCollection itemCol = new ItemCollection(comment);
			Date date = itemCol.getItemValueDate("datcomment");
			String message = itemCol.getItemValueString("txtcomment");
			String user = itemCol.getItemValueString("nameditor");

			ItemCollection entry = new ItemCollection();
			entry.replaceItemValue("date", date);
			entry.replaceItemValue("user", user);
			entry.replaceItemValue("message", message);
			entry.replaceItemValue("type", "comment");

			addChronicleEntry(entry);
			
		}

		/* collect Attachments */
		List<ItemCollection> dmsList = dmsController.getDmsList();
		for (ItemCollection dmsEntry : dmsList) {
			ItemCollection entry = new ItemCollection(dmsEntry);
			String user = entry.getItemValueString("$creator");

			Date date = null;
			if (dmsEntry.getItemValueDate("$modified") != null) {
				date = dmsEntry.getItemValueDate("$modified");
			} else {
				date = dmsEntry.getItemValueDate("$created");
			}

			if (date == null) {
				date = new Date(); // ????
			}

			entry.replaceItemValue("date", date);
			entry.replaceItemValue("type", "dms");
			entry.replaceItemValue("user", user);

			addChronicleEntry(entry);
			
		}

		
		/* collect references */
		List<ItemCollection> references = workitemLinkController.getExternalReferences();
		references.addAll(workitemLinkController.getReferences());
		for (ItemCollection reference : references) {

			Date date = reference.getItemValueDate(WorkflowKernel.LASTEVENTDATE);
			String message = reference.getItemValueString("$WorkflowSummary");
			String user = reference.getItemValueString(WorkflowKernel.EDITOR);

			ItemCollection entry = new ItemCollection();
			entry.replaceItemValue("date", date);
			entry.replaceItemValue("user", user);
			entry.replaceItemValue("message", message);
			entry.replaceItemValue("type", "reference");
			entry.replaceItemValue(WorkflowKernel.UNIQUEID, reference.getUniqueID());

			
			addChronicleEntry(entry);
		}
		
		
		/* collect versions */
		List<ItemCollection> versions = workitemService.findAllVersions(workflowController.getWorkitem());
		for (ItemCollection version : versions) {

			Date date = version.getItemValueDate(WorkflowKernel.LASTEVENTDATE);
			String message = version.getItemValueString(WorkflowKernel.WORKFLOWSTATUS);
			String user = version.getItemValueString(WorkflowKernel.EDITOR);

			ItemCollection entry = new ItemCollection();
			entry.replaceItemValue("date", date);
			entry.replaceItemValue("user", user);
			entry.replaceItemValue("message", message);
			entry.replaceItemValue("type", "version");
			entry.replaceItemValue(WorkflowKernel.UNIQUEID, version.getUniqueID());

			
			addChronicleEntry(entry);
		}

		// sort chronicles by date.....
		Collections.sort(chronicleList, new ChronicleEntityComparator(true));

		logger.info("init in " + (System.currentTimeMillis() - l) + "ms");
	}

	
	/**
	 * This helper method adds a chronicle entry (ItemCollection) into the chronicleList.
	 * 
	 * The method updates the time data
	 * @param entry
	 */
	private void addChronicleEntry( ItemCollection entry) {
		
		String user=entry.getItemValueString("user");
		Date date=entry.getItemValueDate("date");
	
		
		ChronicleEntity chronicleEntity = new ChronicleEntity(user, date);
		int chronicleIndex = chronicleList.indexOf(chronicleEntity);
		if (chronicleIndex > -1) {
			// already created
			chronicleEntity = chronicleList.get(chronicleIndex);
		} // add history...
		chronicleEntity.getFileEntries().add(entry);

		if (chronicleIndex > -1) {
			chronicleList.set(chronicleIndex, chronicleEntity);
		} else {
			chronicleList.add(chronicleEntity);
		}

		// update years table
		addTimeData(chronicleEntity.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());

	}

	/**
	 * Adds the month and year data as a category
	 */
	private void addTimeData(LocalDate localDate) {
		int year = localDate.getYear();
		int month = localDate.getMonthValue();
		Set<Integer> mothsPerYear = yearsMonths.get(year);
		if (mothsPerYear == null) {
			mothsPerYear = new HashSet<Integer>();
		}
		mothsPerYear.add(month);
		yearsMonths.put(year, mothsPerYear);

	}

	/**
	 * Returns all chronical entries by year/month
	 * 
	 * @param year
	 * @param month
	 * @return
	 */
	public List<ChronicleEntity> getChroniclePerMonth(int year, int month) {

		ArrayList<ChronicleEntity> result = new ArrayList<ChronicleEntity>();

		for (ChronicleEntity entry : chronicleList) {
			Date date = entry.getDate();

			LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			if (month == localDate.getMonthValue() && year == localDate.getYear()) {
				result.add(entry);
			}

		}
		return result;
	}

	public List<Integer> getYears() {
		Set<Integer> result = yearsMonths.keySet();

		List<Integer> sortedList = new ArrayList<>(result);
		Collections.sort(sortedList);
		Collections.reverse(sortedList);

		return sortedList;
	}

	/**
	 * return months by year descending
	 * 
	 * @param year
	 * @return
	 */
	public List<Integer> getMonths(int year) {
		Set<Integer> result = yearsMonths.get(year);

		List<Integer> sortedList = new ArrayList<>(result);
		Collections.sort(sortedList);
		Collections.reverse(sortedList);

		return sortedList;
	}

}
