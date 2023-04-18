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
import java.util.List;
import java.util.logging.Logger;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;


import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.ItemCollectionComparator;
import org.imixs.workflow.WorkflowKernel;
import org.imixs.workflow.engine.WorkflowService;
import org.imixs.workflow.engine.index.SchemaService;
import org.imixs.workflow.exceptions.QueryException;
import org.imixs.workflow.faces.data.WorkflowController;
import org.imixs.workflow.office.util.WorkitemHelper;

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
    public static final int MAX_SEARCH_RESULT = 20;
    public static Logger logger = Logger.getLogger(WorkitemLinkController.class.getName());

    @EJB
    protected WorkflowService workflowService;

    @EJB
    protected SchemaService schemaService;

    @Inject
    protected WorkflowController workflowController;

    private static final long serialVersionUID = 1L;
    private List<ItemCollection> searchResult = null;

    public WorkitemLinkController() {
        super();
        searchResult = new ArrayList<ItemCollection>();
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

        searchResult = new ArrayList<ItemCollection>();
        // get the param from faces context....
        FacesContext fc = FacesContext.getCurrentInstance();
        String phrase = fc.getExternalContext().getRequestParameterMap().get("phrase");
        String options = fc.getExternalContext().getRequestParameterMap().get("options");
        if (phrase == null) {
            return;
        }

        logger.finest("......workitemLink search prase '" + phrase + "'  options="+options);
        searchResult = searchWorkitems(phrase,options);

        if (searchResult != null) {
            logger.finest("found " + searchResult.size() + " user profiles...");
        }

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
        List<ItemCollection> searchResult = new ArrayList<ItemCollection>();

        logger.finest(".......search workitem links : " + phrase);
        if (phrase == null || phrase.isEmpty())
            return searchResult;

        // start lucene search
        Collection<ItemCollection> col = null;

        try {
            phrase = phrase.trim().toLowerCase();
            phrase = schemaService.escapeSearchTerm(phrase);
            // issue #170
            phrase = schemaService.normalizeSearchTerm(phrase);

            String sQuery = "";
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
            logger.warning("Lucene error error: " + e.getMessage());
        }

        return searchResult;

    }

    public List<ItemCollection> getSearchResult() {
        return searchResult;
    }

    /**
     * This method returns a list of all ItemCollections referred by the current
     * workItem (txtWorkitemRef).
     * 
     * @return - list of ItemCollection
     */
    public List<ItemCollection> getReferences() {
        return getReferences("");
    }

    /**
     * This method returns a list of ItemCollections referred by the current
     * workItem ($WorkitemRef).
     * <p>
     * The filter will be applied to the result list. So each WorkItem will be
     * tested if it matches the filter expression.
     * <p>
     * The resultset is sorted by $created
     * 
     * @return - list of ItemCollection with matches the current filter
     */
    @SuppressWarnings("unchecked")
    public List<ItemCollection> getReferences(String filter) {
        long l = System.currentTimeMillis();
        List<ItemCollection> result = new ArrayList<ItemCollection>();

        if (workflowController.getWorkitem() == null) {
            return result;
        }

        logger.finest("......lookup references for: " + filter);

        // lookup the references...
        List<String> refList = null;

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
                if (ref!=null && !ref.trim().isEmpty()) {
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

        return result;
    }

    /**
     * Returns a list of all workItems holding a reference to the current workItem.
     * 
     * @return
     * @throws NamingException
     */
    public List<ItemCollection> getExternalReferences() {
        return getExternalReferences("");
    }

    /**
     * returns a list of all workItems holding a reference to the current workItem.
     * If the filter is set the processID will be tested for the filter regex
     * 
     * @return
     * @param filter
     * @throws NamingException
     */
    public List<ItemCollection> getExternalReferences(String filter) {
        List<ItemCollection> result = new ArrayList<ItemCollection>();

        if (workflowController.getWorkitem() == null) {
            return result;
        }

        String uniqueid = workflowController.getWorkitem().getUniqueID();

        // return an empty list if still no $uniqueid is defined for the
        // current workitem
        if ("".equals(uniqueid)) {
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
