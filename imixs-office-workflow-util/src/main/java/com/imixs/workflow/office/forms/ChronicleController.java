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
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.ItemCollectionComparator;
import org.imixs.workflow.faces.data.WorkflowController;

/**
 * The ChronicleController collects all chronicle data 
 * 
 *  history, versions, comments, references, documents
 *  
 *  <p>
 *  Each chronic entry for a workitme consists of the following data items:
 *  <ul>
 *  
 *  - type : date|history|file|version|
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

	ArrayList<ItemCollection> chronicle;
	
	
	Map<Integer,Set<Integer>> yearsMonths;
	
	@Inject
	DMSController dmsController;
	
	@PostConstruct
	public void init() {
		long l=System.currentTimeMillis();
		chronicle= new ArrayList<ItemCollection>();
		
		yearsMonths = new HashMap<Integer,Set<Integer>>();
		
		// collect history
		
		List<List<Object>> history = workflowController.getWorkitem().getItemValue("txtworkflowhistory");
		// do we have real history entries?
		if (history.size()>0 && history.get(0) instanceof List) {
			for (List<Object> entries: history) {
				
				Date date=(Date) entries.get(0);
				String message=(String) entries.get(1);
				String user=(String) entries.get(2);
				
				ItemCollection entry=new ItemCollection();
				entry.replaceItemValue("date",date);
				entry.replaceItemValue("user",user);
				entry.replaceItemValue("message",message);
				entry.replaceItemValue("type","history");
				chronicle.add(entry);
				
				// update years table
				addTimeData(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
			}
		}
		
		
		
		// collect comments
		List<Map<String,List<Object>>> comments = workflowController.getWorkitem().getItemValue("txtCommentLog");
		for (Map<String,List<Object>> comment: comments) {
			
			ItemCollection itemCol=new ItemCollection(comment);
			Date date= itemCol.getItemValueDate("datcomment");
			String message=itemCol.getItemValueString("txtcomment");
			String user=itemCol.getItemValueString("nameditor");
			
			ItemCollection entry=new ItemCollection();
			entry.replaceItemValue("date",date);
			entry.replaceItemValue("user",user);
			entry.replaceItemValue("message",message);
			entry.replaceItemValue("type","comment");

			chronicle.add(entry);
			
			// update years table
			addTimeData(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
		}
		
		// Attachments
		List<ItemCollection> dmsList=dmsController.getDmsList();
		for (ItemCollection dmsEntry: dmsList) {
			ItemCollection entry=new ItemCollection(dmsEntry);
			String user=entry.getItemValueString("$editor");
			
			Date date=null;
			if (dmsEntry.getItemValueDate("$modified")!=null) {
				 date=dmsEntry.getItemValueDate("$modified");
			} else {
				 date=dmsEntry.getItemValueDate("$created");
			}
			
			if (date==null) {
				date=new Date(); //????
			}
			
			entry.replaceItemValue("date",date);
			entry.replaceItemValue("type", "dms");
			entry.replaceItemValue("user",user);
			
			chronicle.add(entry);
			// update years table
			addTimeData(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
		}
		
		
		// sort chronicles by date.....
		Collections.sort(chronicle, new ItemCollectionComparator("date", true));
		
		logger.info("init in " + (	System.currentTimeMillis()-l) + "ms");
	}
	
	/**
	 * Adds the month and year data as a category
	 */
	private void addTimeData(LocalDate localDate) {
		int year=localDate.getYear();
		int month=localDate.getMonthValue();
		Set<Integer> mothsPerYear = yearsMonths.get(year);
		if (mothsPerYear==null) {
			mothsPerYear=new HashSet<Integer>();
		}
		mothsPerYear.add(month);
		yearsMonths.put(year, mothsPerYear);
		
		
	}

	public List<ItemCollection> getChroniclePerMonth(int year,int month) {
		
		
		ArrayList<ItemCollection> result = new ArrayList<ItemCollection>();
		//int month = localDate.getMonthValue();
		
		
		for (ItemCollection entry : chronicle) {
			Date date=entry.getItemValueDate("date");
			LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			if (month==localDate.getMonthValue() && year==localDate.getYear()) {
				result.add(entry);
			}
			
		}
		return result;
	}
	
	
	
	
	public List<Integer> getYears() {
		Set<Integer> result= yearsMonths.keySet();
		
		List<Integer> sortedList = new ArrayList<>(result);
		Collections.sort(sortedList);
		Collections.reverse(sortedList);
		
		return sortedList;
	}
	
	/**
	 * return months by year descending
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
