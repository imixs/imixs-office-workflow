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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

import javax.naming.NamingException;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.ItemCollectionComparator;
import org.imixs.workflow.WorkflowKernel;
import org.imixs.workflow.engine.WorkflowService;
import org.imixs.workflow.engine.index.SchemaService;
import org.imixs.workflow.exceptions.QueryException;
import org.imixs.workflow.faces.data.WorkflowController;
import org.imixs.workflow.office.util.WorkitemHelper;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * The WorkitemLinkController provides suggest-box behavior based on the JSF 2.0
 * ajax capability to add WorkItem references to the current WorkItem.
 * 
 * All WorkItem references will be stored in the property 'txtworkitemref'
 * Note: @RequestScoped did not work because the ajax request will reset the
 * result during submit
 * 
 * 
 * 
 * @author rsoika
 * @version 1.0
 */

@Named
@RequestScoped
public class WorkitemLinkController implements Serializable {

    public static final String LINK_PROPERTY = "$workitemref";
    public static final String LINK_PROPERTY_DEPRECATED = "txtworkitemref";
    public static final int MAX_SEARCH_RESULT = 1000;
    public static Logger logger = Logger.getLogger(WorkitemLinkController.class.getName());

    // search and lookups
    private Map<Integer, List<ItemCollection>> searchCache = null;
    private Map<Integer, List<ItemCollection>> referencesCache = null;
    private Map<Integer, List<ItemCollection>> externalReferencesCache = null;
    private List<ItemCollection> searchResult = null;

    @EJB
    protected WorkflowService workflowService;

    @EJB
    protected SchemaService schemaService;

    @Inject
    protected WorkflowController workflowController;

    private static final long serialVersionUID = 1L;

    public WorkitemLinkController() {
        super();
        searchResult = new ArrayList<ItemCollection>();
        searchCache = new HashMap<Integer, List<ItemCollection>>();
        referencesCache = new HashMap<Integer, List<ItemCollection>>();
        externalReferencesCache = new HashMap<Integer, List<ItemCollection>>();
    }

    /**
     * This method searches a text phrase within the user profile objects
     * (type=profile).
     * <p>
     * JSF Integration:
     * 
     * {@code 
     * 
     * <h:commandScript name="imixsOfficeWorkflow.mlSearch" action=
     * "#{cargosoftController.search()}" rendered="#{cargosoftController!=null}"
     * render= "cargosoft-results" /> }
     * 
     * <p>
     * JavaScript Example:
     * 
     * <pre>
     * {@code
     *  imixsOfficeWorkflow.cargosoftSearch({ item: '_invoicenumber' })
     *  }
     * </pre>
     * 
     */
    public void searchWorkitems() {
        // get the param from faces context....
        FacesContext fc = FacesContext.getCurrentInstance();
        String _phrase = fc.getExternalContext().getRequestParameterMap().get("phrase");
        String options = fc.getExternalContext().getRequestParameterMap().get("options");
        if (_phrase == null) {
            return;
        }

        logger.finest("......workitemLink search prase '" + _phrase + "'  options=" + options);
        searchWorkitems(_phrase, options);

    }

    /**
     * This method returns a list of profile ItemCollections matching the search
     * phrase. The search statement includes the items 'txtName', 'txtEmail' and
     * 'txtUserName'. The result list is sorted by txtUserName
     * 
     * @param phrase - search phrase
     * @return - list of matching profiles
     */
    public List<ItemCollection> searchWorkitems(String phrase, String filter) {
        int searchHash = computeSearchHash(phrase, filter);
        searchResult = searchCache.get(searchHash);
        if (searchResult != null) {
            return searchResult;
        }

        logger.finest(".......search workitem links : " + phrase);
        if (phrase == null || phrase.isEmpty()) {
            searchResult = new ArrayList<ItemCollection>();
            searchCache.put(searchHash, searchResult);
            return searchResult;
        }

        // start lucene search
        searchResult = new ArrayList<ItemCollection>();
        Collection<ItemCollection> col = null;
        String sQuery = "";
        try {
            phrase = phrase.trim().toLowerCase();
            phrase = schemaService.escapeSearchTerm(phrase);
            // issue #170
            phrase = schemaService.normalizeSearchTerm(phrase);

            // search only type workitem and workitemsarchive
            sQuery += "((type:workitem) OR (type:workitemarchive)) AND  (*" + phrase + "*)";

            if (filter != null && !"".equals(filter.trim())) {
                String sNewFilter = filter.trim().replace(".", "?");
                sQuery += " AND (" + sNewFilter + ") ";
            }
            logger.finest("......query=" + sQuery);

            col = workflowService.getDocumentService().find(sQuery, MAX_SEARCH_RESULT, 0);

            if (col != null) {
                for (ItemCollection aWorkitem : col) {
                    searchResult.add(WorkitemHelper.clone(aWorkitem));
                }
                // sort by $workflowabstract..
                Collections.sort(searchResult, new ItemCollectionComparator("$workflowabstract", true));
            }

        } catch (Exception e) {
            logger.warning("Search error query = '" + sQuery + "'  - " + e.getMessage());
        }

        searchCache.put(searchHash, searchResult);
        return searchResult;

    }

    public List<ItemCollection> getSearchResult() {
        return searchResult;
    }

    /**
     * Helper method compute a hash from a phrase and a filter rule
     */
    public static int computeSearchHash(String _phrase, String _filter) {
        int hash = 0;

        if (_phrase != null && !_phrase.isEmpty()) {
            hash = Objects.hash(hash, _phrase);
        }
        if (_filter != null && !_filter.isEmpty()) {
            hash = Objects.hash(hash, _filter);
        }

        return hash;
    }

    /**
     * Deprecated - use getReferencesInbound
     * 
     * @return - list of ItemCollection
     */
    @Deprecated
    public List<ItemCollection> getReferences() {
        return getReferences("");
    }

    /**
     * Deprecated - use getReferencesInbound
     * 
     * @return - list of ItemCollection with matches the current filter
     */
    @Deprecated
    public List<ItemCollection> getReferences(String filter) {
        return getReferencesInbound(filter);
    }

    /**
     * This method returns a list of ItemCollections referred by the item
     * '$WorkitemRef'.
     *
     * 
     * @return
     * @throws NamingException
     */
    public List<ItemCollection> getReferencesOutbound() {
        return getReferencesOutbound("");
    }

    /**
     * This method returns a list of ItemCollections referred by the item
     * '$WorkitemRef'.
     * <p>
     * The filter will be applied to the result list. So each WorkItem will be
     * tested if it matches the filter expression.
     * <p>
     * The resultset is sorted by $created
     * 
     * @return - list of ItemCollection with matches the current filter
     */
    @SuppressWarnings("unchecked")
    public List<ItemCollection> getReferencesOutbound(String filter) {

        long l = System.currentTimeMillis();
        List<ItemCollection> result = new ArrayList<ItemCollection>();

        if (workflowController.getWorkitem() == null) {
            return result;
        }

        logger.finest("......lookup references for: " + filter);
        int searchHash = computeSearchHash(workflowController.getWorkitem().getUniqueID(), filter);
        result = referencesCache.get(searchHash);
        if (result != null) {
            return result;
        }

        // lookup the references...
        List<String> refList = null;
        result = new ArrayList<ItemCollection>();
        // support deprecated ref field
        if (!workflowController.getWorkitem().hasItem(LINK_PROPERTY)
                && workflowController.getWorkitem().hasItem(LINK_PROPERTY_DEPRECATED)) {
            refList = workflowController.getWorkitem().getItemValue(LINK_PROPERTY_DEPRECATED);
        } else {
            refList = workflowController.getWorkitem().getItemValue(LINK_PROPERTY);
        }

        if (refList != null && !refList.isEmpty()) {

            logger.finest("... we have " + refList.size() + " references stored");

            // select all references.....
            String sQuery = "(";
            for (String ref : refList) {
                if (ref != null && !ref.trim().isEmpty()) {
                    sQuery = sQuery + "$uniqueid:\"" + ref + "\" OR ";
                }
            }
            // in case we do not have any ids we can skip
            // this can happen in some rare cases
            if (!sQuery.contains("$uniqueid")) {
                // no valid references found
                return result;
            }

            // cust last OR
            if (sQuery.endsWith("OR ")) {
                sQuery = sQuery.substring(0, sQuery.lastIndexOf("OR "));
            }

            sQuery = sQuery + ")";

            if (filter != null && !"".equals(filter.trim())) {
                String sNewFilter = filter.trim().replace(".", "?");
                sQuery += " AND (" + sNewFilter + ") ";
            }
            logger.finest("......query=" + sQuery);

            List<ItemCollection> workitems = null;

            try {
                workitems = workflowService.getDocumentService().findStubs(sQuery, MAX_SEARCH_RESULT, 0,
                        WorkflowKernel.CREATED, true);
            } catch (QueryException e) {

                e.printStackTrace();
            }
            // do we have a result?
            if (workitems != null) {
                result.addAll(workitems);
            }
        }

        logger.fine("...lookup references for: " + filter + " in " + (System.currentTimeMillis() - l) + "ms");

        referencesCache.put(searchHash, result);
        return result;
    }

    /**
     * Deprecated - use getReferencesOutbound instead
     * 
     * @return
     */
    @Deprecated
    public List<ItemCollection> getExternalReference() {
        return getReferencesOutbound("");
    }

    /**
     * Deprecated - use getReferencesOutbound
     * 
     * @return
     * @param filter
     * @throws NamingException
     */
    @Deprecated
    public List<ItemCollection> getExternalReferences(String filter) {
        return getReferencesOutbound(filter);
    }

    /**
     * This method returns a list of all workItems holding a reference to the
     * current workItem.
     *
     * @return - list of ItemCollection
     */
    public List<ItemCollection> getReferencesInbound() {
        return getReferencesInbound("");
    }

    /**
     * This method returns a list of all workItems holding a reference to the
     * current workItem.
     * If the filter is set the processID will be tested for the filter regex
     * 
     * @return
     * @param filter
     * @throws NamingException
     */
    public List<ItemCollection> getReferencesInbound(String filter) {
        List<ItemCollection> result = new ArrayList<ItemCollection>();

        if (workflowController.getWorkitem() == null) {
            return result;
        }

        String uniqueid = workflowController.getWorkitem().getUniqueID();

        // return an empty list if still no $uniqueId is defined for the
        // current workitem
        if ("".equals(uniqueid)) {
            return result;
        }

        int searchHash = computeSearchHash(uniqueid, filter);
        result = externalReferencesCache.get(searchHash);
        if (result != null) {
            return result;
        }

        // select all references.....
        String sQuery = "(";
        sQuery = " (type:\"workitem\" OR type:\"workitemarchive\") AND (" + LINK_PROPERTY + ":\"" + uniqueid + "\"  OR "
                + LINK_PROPERTY_DEPRECATED + ":\"" + uniqueid + "\")";

        List<ItemCollection> workitems = null;

        try {
            workitems = workflowService.getDocumentService().findStubs(sQuery, MAX_SEARCH_RESULT, 0,
                    WorkflowKernel.LASTEVENTDATE, true);
        } catch (QueryException e) {

            e.printStackTrace();
        }
        // sort by modified
        Collections.sort(workitems, new ItemCollectionComparator("$created", true));
        result = new ArrayList<ItemCollection>();
        // now test if filter matches, and clone the workItem
        if (filter != null && !filter.isEmpty()) {
            for (ItemCollection itemcol : workitems) {
                // test
                if (WorkitemHelper.matches(itemcol, filter)) {
                    result.add(itemcol);
                }
            }
        } else {
            result.addAll(workitems);
        }

        externalReferencesCache.put(searchHash, result);
        return result;

    }

    /**
     * This method returns a list of ItemCollections referred by list of IDs.
     * 
     * @return - list of ItemCollection with matches the given ids
     */
    public List<ItemCollection> getReferencesOutboundByIds(List<String> ids) {
        List<ItemCollection> result = new ArrayList<ItemCollection>();
        for (String id : ids) {
            ItemCollection refItemCol = workflowService.getWorkItem(id);
            if (refItemCol != null) {
                result.add(refItemCol);
            }
        }
        return result;
    }

    /**
     * Helper method to load a full workitem from the frontend
     * 
     * @param id
     * @return
     */
    public ItemCollection getWorkitem(String id) {
        return workflowService.getWorkItem(id);
    }

}
