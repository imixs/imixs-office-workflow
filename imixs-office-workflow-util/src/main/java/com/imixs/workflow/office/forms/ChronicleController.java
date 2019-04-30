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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.imixs.workflow.ItemCollection;
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
	
	
	@PostConstruct
	public void init() {
		chronicle= new ArrayList<ItemCollection>();
		
		yearsMonths = new HashMap<Integer,Set<Integer>>();
		
		// collect history
		List<List<Object>> history = workflowController.getWorkitem().getItemValue("txtworkflowhistory");
		
		long l=System.currentTimeMillis();
		for (List<Object> entries: history) {
			
			Date date=(Date) entries.get(0);
			String message=(String) entries.get(1);
			String user=(String) entries.get(2);
			
			
			
			ItemCollection entry=new ItemCollection();
			entry.replaceItemValue("date",date);
			entry.replaceItemValue("user",user);
			entry.replaceItemValue("message",message);
			
			chronicle.add(entry);
			
			
			// update years table
			LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			int year=localDate.getYear();
			int month=localDate.getMonthValue();
			Set<Integer> mothsPerYear = yearsMonths.get(year);
			if (mothsPerYear==null) {
				mothsPerYear=new HashSet<Integer>();
			}
			mothsPerYear.add(month);
			yearsMonths.put(year, mothsPerYear);
			
			
			
		}
		
		
		
		logger.info("init in " + (	System.currentTimeMillis()-l) + "ms");
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
	
	
	
	
	public Set<Integer> getYears() {
		return yearsMonths.keySet();
	}
	
	public Set<Integer> getMonths(int year) {
		return yearsMonths.get(year);
	}
	
}
