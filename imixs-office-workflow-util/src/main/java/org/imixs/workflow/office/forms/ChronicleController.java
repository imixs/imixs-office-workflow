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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Logger;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.WorkflowKernel;
import org.imixs.workflow.engine.DocumentService;
import org.imixs.workflow.engine.plugins.HistoryPlugin;
import org.imixs.workflow.faces.data.WorkflowController;
import org.imixs.workflow.faces.data.WorkflowEvent;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.ConversationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * The ChronicleController collects all chronicle data
 * 
 * history, versions, comments, references, documents
 * 
 * <p>
 * Each chronic entry for a workitem consists of the following data items:
 * <ul>
 * 
 * - type : date|history|file|version|
 * 
 * 
 * <p>
 * With the method 'toggleFilter' the chronicle list can be filter to a specific
 * type. This method call recalculates also the time data.
 * 
 * @see workitem_chronicle.xhtml
 * @author rsoika,gheinle
 */
@Named
@ConversationScoped
public class ChronicleController implements Serializable {

	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(ChronicleController.class.getName());

	List<ChronicleEntity> originChronicleList;
	List<ChronicleEntity> filteredChronicleList;
	String filter = null;
	Map<Integer, Set<Integer>> yearsMonths;

	@Inject
	protected WorkflowController workflowController;

	@Inject
	protected DMSController dmsController;

	@Inject
	protected WorkitemLinkController workitemLinkController;

	@EJB
	protected WorkitemService workitemService;

	@EJB
	protected DocumentService documentService;

	private DateFormat dateFormat = null;

	/**
	 * This helper method is called during the WorkflowEvent.WORKITEM_CHANGED to
	 * update the chronicle view for the current workitem.
	 */
	@PostConstruct
	@SuppressWarnings("unchecked")
	public void init() {
		long l = System.currentTimeMillis();
		originChronicleList = new ArrayList<ChronicleEntity>();

		yearsMonths = new HashMap<Integer, Set<Integer>>();

		if (workflowController.getWorkitem() == null || FacesContext.getCurrentInstance() == null) {
			return; // no op
		}
		try {
			Locale browserLocale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
			ResourceBundle rb = ResourceBundle.getBundle("bundle.messages", browserLocale);
			String sDatePattern = rb.getString("dateTimePattern");
			dateFormat = new SimpleDateFormat(sDatePattern);
		} catch (MissingResourceException mre) {
			dateFormat = null;
		}

		/* collect history */
		List<List<?>> history = null;
		// test deprecated field 'txtworkflowhistory'
		if (!workflowController.getWorkitem().hasItem(HistoryPlugin.ITEM_HISTORY_LOG)
				&& workflowController.getWorkitem().hasItem("txtworkflowhistory")) {
			// migrate deprecated field name
			history = workflowController.getWorkitem().getItemValue("txtworkflowhistory");
			// in old workitems, the workflow history may not be ordered chronological.
			// We re-sort the order here
			// Sort the list by date in descending order
			Collections.sort(history, new Comparator<List<?>>() {
				@Override
				public int compare(List<?> entry1, List<?> entry2) {
					Date date1 = (Date) entry1.get(0);
					Date date2 = (Date) entry2.get(0);
					// Compare in descending order
					return date1.compareTo(date2);
				}
			});
		} else {
			history = workflowController.getWorkitem().getItemValue(HistoryPlugin.ITEM_HISTORY_LOG);
		}

		// change order
		Collections.reverse(history);
		// do we have real history entries?
		if (history.size() > 0 && history.get(0) instanceof List) {
			for (List<?> entries : history) {
				Date date = (Date) entries.get(0);
				String message = (String) entries.get(1);
				String user = (String) entries.get(2);
				ItemCollection entry = new ItemCollection();
				entry.replaceItemValue("date", date);
				entry.replaceItemValue("user", user);
				entry.replaceItemValue("message", message);
				entry.replaceItemValue("type", "history");
				addChronicleEntry(originChronicleList, entry);
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

			addChronicleEntry(originChronicleList, entry);
		}

		/* collect Imixs-AI chat history */
		if (workflowController.getWorkitem().hasItem(AIController.AI_CHAT_HISTORY)) {
			List<ItemCollection> aiChatHistory = ChildItemController.explodeChildList(workflowController.getWorkitem(),
					AIController.AI_CHAT_HISTORY);
			// change order
			Collections.reverse(aiChatHistory);
			for (ItemCollection aiEntry : aiChatHistory) {
				ItemCollection entry = new ItemCollection();
				entry.replaceItemValue("date", aiEntry.getItemValueDate("date"));
				entry.replaceItemValue("user", aiEntry.getItemValueString("user"));
				String message = "Question: \n" + aiEntry.getItemValueString("question");
				message += "\nAnswer: \n" + aiEntry.getItemValueString("answer");
				entry.replaceItemValue("message", message);
				entry.replaceItemValue("type", "imixs-ai");
				addChronicleEntry(originChronicleList, entry);
			}
		}

		/* collect Attachments */
		List<ItemCollection> dmsList = dmsController.getDmsList();
		for (ItemCollection dmsEntry : dmsList) {
			ItemCollection entry = new ItemCollection(dmsEntry);
			String user = entry.getItemValueString("$creator");

			Date date = null;
			if (dmsEntry.getItemValueDate("$modified") != null) {
				date = dmsEntry.getItemValueDate("$modified");
				user = entry.getItemValueString("$editor");
			} else {
				date = dmsEntry.getItemValueDate("$created");
			}
			if (date == null) {
				date = new Date(); // ????
			}

			entry.replaceItemValue("date", date);
			entry.replaceItemValue("type", "dms");
			entry.replaceItemValue("user", user);

			addChronicleEntry(originChronicleList, entry);
		}

		/*
		 * Collect all references.
		 * We look for direct references and external references. The method uniques the
		 * list so that a reference can only occur once.
		 */
		List<ItemCollection> references = workitemLinkController.getReferences();

		// Disabled External References - Issue #653
		// List<ItemCollection> externalReferences =
		// workitemLinkController.getExternalReferences();
		// // unique list... (references can be occur twice)
		// for (ItemCollection _workitem : externalReferences) {
		// if (!containsUniqueID(references, _workitem.getUniqueID())) {
		// references.add(_workitem);
		// }
		// }

		for (ItemCollection reference : references) {
			Date date = reference.getItemValueDate(WorkflowKernel.CREATED);
			String message = reference.getItemValueString("$WorkflowSummary");
			// test if no summary....
			if (message.isEmpty()) {
				// print the date....
				if (dateFormat == null) {
					message = message + date.toString();
				} else {
					message = message + dateFormat.format(date);
				}
			}
			// String user = reference.getItemValueString(WorkflowKernel.EDITOR);
			String user = reference.getItemValueString(WorkflowKernel.CREATOR);

			ItemCollection entry = new ItemCollection();
			entry.replaceItemValue("$WorkflowGroup", reference.getItemValue("$WorkflowGroup"));
			entry.replaceItemValue("$WorkflowStatus", reference.getItemValue("$WorkflowStatus"));
			entry.replaceItemValue(WorkflowKernel.LASTEVENTDATE, reference.getItemValue(WorkflowKernel.LASTEVENTDATE));
			entry.replaceItemValue("date", date);
			entry.replaceItemValue("user", user);
			entry.replaceItemValue("$lasteditor", reference.getItemValueString(WorkflowKernel.EDITOR));
			entry.replaceItemValue("message", message);
			entry.replaceItemValue("type", "reference");
			entry.replaceItemValue(WorkflowKernel.UNIQUEID, reference.getUniqueID());

			addChronicleEntry(originChronicleList, entry);
		}

		/* collect versions */
		List<ItemCollection> versions = workitemService.findAllVersions(workflowController.getWorkitem());
		for (ItemCollection version : versions) {

			if (workflowController.getWorkitem().getUniqueID().equals(version.getUniqueID())) {
				// skipp current workitem
				continue;
			}

			ItemCollection versionFull = documentService.load(version.getUniqueID());
			// Date date = version.getItemValueDate(WorkflowKernel.LASTEVENTDATE);
			Date date = version.getItemValueDate(WorkflowKernel.CREATED);
			String message = version.getItemValueString("$WorkflowSummary");
			String user = version.getItemValueString(WorkflowKernel.EDITOR);

			ItemCollection entry = new ItemCollection();
			entry.replaceItemValue("$WorkflowGroup", version.getItemValue("$WorkflowGroup"));
			entry.replaceItemValue("$WorkflowStatus", version.getItemValue("$WorkflowStatus"));
			entry.replaceItemValue(WorkflowKernel.LASTEVENTDATE, version.getItemValue(WorkflowKernel.LASTEVENTDATE));

			entry.replaceItemValue("user", user);
			if (!versionFull.getItemValueString("$uniqueIdSource").isEmpty()) {
				entry.replaceItemValue("message", message);
				entry.replaceItemValue("icon", "typcn-starburst-outline");
				if (versionFull.hasItem(WorkflowKernel.CREATED + ".version")) {
					date = versionFull.getItemValueDate(WorkflowKernel.CREATED + ".version");
				}
			} else {
				entry.replaceItemValue("message", message);
				entry.replaceItemValue("icon", "typcn-starburst");
			}
			entry.replaceItemValue("date", date);
			entry.replaceItemValue("type", "version");
			entry.replaceItemValue(WorkflowKernel.UNIQUEID, version.getUniqueID());

			addChronicleEntry(originChronicleList, entry);
		}

		// sort chronicles by date.....
		Collections.sort(originChronicleList, new ChronicleEntityComparator(true));

		computeTimeData(originChronicleList);

		// set full filtered list
		filteredChronicleList = new ArrayList<ChronicleEntity>();
		filteredChronicleList.addAll(originChronicleList);

		logger.fine("...init in " + (System.currentTimeMillis() - l) + "ms");
	}

	/*
	 * Helper Method checks if a given UniqueID is part of a list of Workitems
	 * 
	 * @param list
	 * 
	 * @param uniqueID
	 * 
	 * @return
	 */
	private boolean containsUniqueID(List<ItemCollection> list, String uniqueID) {
		for (ItemCollection workitem : list) {
			if (workitem.getUniqueID().equals(uniqueID)) {
				return true;
			}
		}
		// no match
		return false;
	}

	/**
	 * WorkflowEvent listener
	 * 
	 * If a new WorkItem was created or changed, the chronicle view will be
	 * initialized.
	 * 
	 * @param workflowEvent
	 */
	public void onWorkflowEvent(@Observes WorkflowEvent workflowEvent) {
		if (workflowEvent == null) {
			return;
		}
		if (WorkflowEvent.WORKITEM_CREATED == workflowEvent.getEventType()
				|| WorkflowEvent.WORKITEM_CHANGED == workflowEvent.getEventType()) {
			// reset chronicle data...
			init();
		}
	}

	/**
	 * Returns the current active filter or null if no filter is active.
	 * 
	 * @return
	 */
	public String getFilter() {
		return filter;
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

		if (result == null) {
			// no entries
			return new ArrayList<>();
		}

		List<Integer> sortedList = new ArrayList<>(result);
		Collections.sort(sortedList);
		Collections.reverse(sortedList);

		return sortedList;
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
		for (ChronicleEntity entry : filteredChronicleList) {
			Date date = entry.getDate();

			LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			if (month == localDate.getMonthValue() && year == localDate.getYear()) {
				result.add(entry);
			}
		}

		logger.finest("......getChroniclePerMonth - found " + result.size() + " chronicle entities");
		return result;
	}

	/**
	 * This method call creates a new filtered list of the originChronicl List. and
	 * recalculates also the time data.
	 * 
	 * @param category
	 */
	public void toggleFilter(String category) {
		long l = System.currentTimeMillis();
		logger.finest("......toggleFilter : " + category);

		if (category != null && !category.isEmpty() && category.equals(filter)) {
			// toggle existing category
			filter = null;
		} else {
			filter = category;
		}

		if (filter == null || filter.isEmpty()) {
			filteredChronicleList = new ArrayList<ChronicleEntity>();
			filteredChronicleList.addAll(originChronicleList);
		} else {
			filteredChronicleList = new ArrayList<ChronicleEntity>();
			// build a new filtered list with only data of the given category
			for (ChronicleEntity chronicleEntry : originChronicleList) {
				List<ItemCollection> entries = chronicleEntry.getEntries();
				// test each entry for the given category
				for (ItemCollection entry : entries) {
					if (filter.equals(entry.getType())) {
						// match!
						addChronicleEntry(filteredChronicleList, entry);
					}
				}
			}
			// sort chronicles by date.....
			Collections.sort(filteredChronicleList, new ChronicleEntityComparator(true));
		}

		computeTimeData(filteredChronicleList);

		logger.finest("......filter=" + filter + " size= " + filteredChronicleList.size());
		logger.fine("...computed filter in " + (System.currentTimeMillis() - l) + "ms");
	}

	/**
	 * This helper method adds a chronicle entry (ItemCollection) into the
	 * chronicleList.
	 * 
	 * The method updates the time data
	 * 
	 * @param entry
	 */
	private void addChronicleEntry(List<ChronicleEntity> chronicleList, ItemCollection entry) {
		String user = entry.getItemValueString("user");
		Date date = entry.getItemValueDate("date");
		if (date == null) {
			return;
		}
		LocalDateTime localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
		// set second to 00
		localDate = localDate.truncatedTo(ChronoUnit.MINUTES);
		date = java.util.Date.from(localDate.atZone(ZoneId.systemDefault()).toInstant());
		ChronicleEntity chronicleEntity = new ChronicleEntity(user, date);
		int chronicleIndex = chronicleList.indexOf(chronicleEntity);
		if (chronicleIndex > -1) {
			// already created
			chronicleEntity = chronicleList.get(chronicleIndex);
		} // add history...
		chronicleEntity.addEntry(entry);

		if (chronicleIndex > -1) {
			chronicleList.set(chronicleIndex, chronicleEntity);
		} else {
			chronicleList.add(chronicleEntity);
		}
	}

	/**
	 * This method recalculates the yeas/months for the current entry list
	 */
	private void computeTimeData(List<ChronicleEntity> chronicleList) {
		yearsMonths = new HashMap<Integer, Set<Integer>>();
		for (ChronicleEntity chronicleEntity : chronicleList) {
			// update years table
			addTimeData(chronicleEntity.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
		}

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

}
