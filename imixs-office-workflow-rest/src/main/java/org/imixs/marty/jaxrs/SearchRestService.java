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

package org.imixs.marty.jaxrs;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.imixs.marty.ejb.ProcessService;
import org.imixs.marty.util.WorkitemHelper;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.jee.ejb.WorkflowService;
import org.imixs.workflow.jee.util.PropertyService;
import org.imixs.workflow.plugins.jee.extended.LucenePlugin;
import org.imixs.workflow.xml.EntityCollection;
import org.imixs.workflow.xml.XMLItemCollectionAdapter;

/**
 * The ProcessRestService provides methods to access the marty process and space
 * entities. The Service extends the imixs-workflow-jaxrs api.
 * 
 * Additional the service provides a list of all workflow groups
 * 
 * 
 * @author rsoika
 * 
 */
@Named("searchService")
@RequestScoped
@Path("/search")
@Produces({ "text/html", "application/xml", "application/json" })
public class SearchRestService implements Serializable {

	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(SearchRestService.class
			.getSimpleName());

	@EJB
	private PropertyService propertyService;

	@EJB
	private WorkflowService workflowService;

	/**
	 * Returns the result of a lucene search. If the query contains () AND OR we
	 * think its a valid lucene search term. Otherwise the query will be
	 * transformed into a search term
	 * 
	 * 
	 * @param query
	 *            - lucene search phrase
	 * @return
	 */
	@SuppressWarnings("static-access")
	@GET
	@Path("/{query}")
	public EntityCollection getSearchResult(@PathParam("query") String query,
			@DefaultValue("30") @QueryParam("count") int count,
			@QueryParam("items") String items) {

		if (query != null) {

			// start lucene search
			Collection<ItemCollection> col = null;
			try {
				logger.fine("SearchQuery=" + query);
				LucenePlugin lucenePlugin = new LucenePlugin();
				lucenePlugin.setMaxResult(count);

				// if the query contains () AND OR we think its a valid lucen
				// search therm
				col = lucenePlugin.search(generateSearchTerm(query), workflowService);

				return XMLItemCollectionAdapter.putCollection(col,
						getItemList(items));
			} catch (Exception e) {
				logger.warning("  lucene error!");
				e.printStackTrace();
			}

		}

		return new EntityCollection();
	}

	@GET
	@Path("/{query}.xml")
	@Produces(MediaType.TEXT_XML)
	public EntityCollection getSearchResultXML(
			@PathParam("query") String query,
			@DefaultValue("30") @QueryParam("count") int count,
			@QueryParam("items") String items) {

		return getSearchResult(query, count, items);

	}

	@GET
	@Path("/{query}.json")
	@Produces(MediaType.APPLICATION_JSON)
	public EntityCollection getSearchResultJSON(
			@PathParam("query") String query,
			@DefaultValue("30") @QueryParam("count") int count,
			@QueryParam("items") String items) {
		return getSearchResult(query, count, items);
	}

	private String generateSearchTerm(String searchphrase) {

		// skip if searchphrase contains () or AND OR
		if (searchphrase.contains("(") 
				|| searchphrase.contains(")")
				|| searchphrase.contains("AND ")
				|| searchphrase.contains("OR ")
					
				)
			return searchphrase;
		
		
		
		String sSearchTerm = "";

		List<String> typeList = Arrays.asList(new String[] { "workitem",
				"workitemarchive" });

		// convert type list into comma separated list
		String sTypeQuery = "";
		Iterator<String> iterator = typeList.iterator();
		while (iterator.hasNext()) {
			sTypeQuery += "type:\"" + iterator.next() + "\"";
			if (iterator.hasNext())
				sTypeQuery += " OR ";
		}
		sSearchTerm += "(" + sTypeQuery + ") AND";

		if (!"".equals(searchphrase)) {
			// trim
			searchphrase = searchphrase.trim();
			// lower case....
			searchphrase = searchphrase.toLowerCase();
			// check the default operator
			String defaultOperator = propertyService.getProperties()
					.getProperty("lucence.defaultOperator");
			if (defaultOperator != null
					&& "AND".equals(defaultOperator.toUpperCase())) {
				String[] segs = searchphrase.split(Pattern.quote(" "));

				sSearchTerm += " (";
				for (String seg : segs) {
					sSearchTerm += " *" + seg + "* AND";
				}
				if (sSearchTerm.endsWith("AND"))
					sSearchTerm = sSearchTerm.substring(0,
							sSearchTerm.length() - 3);

				sSearchTerm += ") ";

			} else {
				// because lucene parser default to OR operator no Operator is
				// used here
				String[] segs = searchphrase.split(Pattern.quote(" "));
				sSearchTerm += " (";
				for (String seg : segs) {
					sSearchTerm += " *" + seg + "* ";
				}

				sSearchTerm += ") ";

			}
		} else
		// cut last AND
		if (sSearchTerm.endsWith("AND"))
			sSearchTerm = sSearchTerm.substring(0, sSearchTerm.length() - 3);

		return sSearchTerm;
	}

	/**
	 * This method returns a List object from a given comma separated string.
	 * The method returns null if no elements are found. The provided parameter
	 * looks typical like this: <code>
	 *   txtWorkflowStatus,numProcessID,txtName
	 * </code>
	 * 
	 * @param items
	 * @return
	 */
	private List<String> getItemList(String items) {
		if (items == null || "".equals(items))
			return null;
		Vector<String> v = new Vector<String>();
		StringTokenizer st = new StringTokenizer(items, ",");
		while (st.hasMoreTokens())
			v.add(st.nextToken());
		return v;
	}
}
