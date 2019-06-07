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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.event.AjaxBehaviorEvent;
import javax.inject.Named;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.ItemCollectionComparator;
import org.imixs.workflow.engine.DocumentService;
import org.imixs.workflow.engine.lucene.LuceneSearchService;

/**
 * The SuggestInputController can be used to suggest inputs from earlier
 * requests within the same workflowGroup.
 * 
 * @author rsoika
 * @version 1.0
 */

@Named
@SessionScoped
public class SuggestImportController implements Serializable {

	public static final int MAX_RESULT = 30;

	static Logger logger = Logger.getLogger(SuggestImportController.class.getName());

	@EJB
	DocumentService documentService = null;
	
	@EJB
	LuceneSearchService luceneSearchService;

	private static final long serialVersionUID = 1L;
	private List<ItemCollection> searchResult = null;

	public SuggestImportController() {
		super();

	}

	public List<ItemCollection> getSearchResult() {
		return searchResult;
	}

	/**
	 * This method reset the search and input state.
	 */
	public void reset() {
		searchResult = new ArrayList<ItemCollection>();
		logger.fine("reset");
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
	 * This method updates the current workitem with the values defined by teh
	 * itemList from the given suggest workitem.
	 * 
	 * @param suggest  - ItemColleciton with data to suggest
	 * @param itemList - item names to be updated.
	 */
	public void update(ItemCollection workitem, ItemCollection suggest, String itemList) {
		logger.finest("......update " + itemList + "...");
		String[] itemNames = itemList.split("[\\s,;]+");
		for (String itemName : itemNames) {
			logger.finest("......update item " + itemName);
			workitem.replaceItemValue(itemName, suggest.getItemValue(itemName));

			logger.finest("......new value=" + suggest.getItemValue(itemName));
		}
	}

	/**
	 * This method initializes a lucene search. The method is triggered by ajax
	 * events from the userInput.xhtml page. The minimum length of a given input
	 * search phrase have to be at least 3 characters
	 * 
	 * @param keyItemName    - itemName to identify the unique itemCollection
	 * @param input          - search phrase
	 * @param searchItemList - itemName list to serach for
	 * 
	 */
	public void search(String keyItemName, String input, String searchItemList, String workflowGroup) {
		if (input == null || input.length() < 3)
			return;
		logger.finest("......search for=" + input);
		searchResult = searchEntity(keyItemName, searchItemList, workflowGroup, input);

	}

	/**
	 * This method returns a list of ItemCollections matching the search phrase and
	 * workflowgroup.
	 * <p>
	 * The type is restrcited to "workitem" and "workitemarchive"
	 * <p>
	 * The result is filtered for unique entries and is used to display the
	 * suggestion list
	 * 
	 * @param phrase - search phrase
	 * @return - list of matching requests
	 */
	private List<ItemCollection> searchEntity(String keyItemName, String searccItemList, String workflowGroup,
			String phrase) {
		long l = System.currentTimeMillis();
		List<ItemCollection> searchResult = new ArrayList<ItemCollection>();

		if (phrase == null || phrase.isEmpty())
			return searchResult;

		List<ItemCollection> col = null;
		try {
			phrase = phrase.trim();
			// phrase = LuceneSearchService.escapeSearchTerm(phrase);
			phrase = luceneSearchService.normalizeSearchTerm(phrase);
			String sQuery = "(type:\"workitem\" OR type:\"workitemarchive\") AND ($workflowgroup:\"" + workflowGroup
					+ "\") ";

			// build query for each search item...
			String[] itemNames = searccItemList.split("[\\s,;]+");
			sQuery += " AND (";

			for (String itemName : itemNames) {
				sQuery += "(" + itemName + ":(" + phrase + "*)) OR ";
			}
			// cut last or...
			sQuery = sQuery.substring(0, sQuery.length() - 3);
			sQuery += ")";

			logger.finest("......search: " + sQuery);

			col = documentService.find(sQuery, MAX_RESULT, 0, "$modified", true);
			logger.finest("......found: " + col.size());
		} catch (Exception e) {
			logger.warning("  lucene error - " + e.getMessage());
		}

		// Removing the Elements by assigning list to TreeSet
		long l1 = System.currentTimeMillis();

		Set<ItemCollection> uniqueResultList = col.stream().collect(
				Collectors.toCollection(() -> new TreeSet<>(new SuggestItemCollectionComparator(keyItemName))));
		logger.finest("...filtert result list in " + (System.currentTimeMillis() - l1) + "ms");

		searchResult.addAll(uniqueResultList);

		// sort by txtname..
		Collections.sort(searchResult, new ItemCollectionComparator(keyItemName, true));

		logger.info("...computed suggestion result in " + (System.currentTimeMillis() - l) + "ms");

		return searchResult;

	}

	class SuggestItemCollectionComparator implements Comparator<ItemCollection> {
		String itemName;

		public SuggestItemCollectionComparator(String aItemName) {
			this.itemName = aItemName;
		}

		@Override
		public int compare(ItemCollection e1, ItemCollection e2) {
			return e1.getItemValueString(itemName).compareTo(e2.getItemValueString(itemName));
		}

	}

}
