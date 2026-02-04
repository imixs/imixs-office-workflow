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

package org.imixs.workflow.office.views;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.imixs.marty.team.TeamController;
import org.imixs.marty.team.TeamService;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.engine.DocumentService;
import org.imixs.workflow.engine.ModelService;
import org.imixs.workflow.engine.index.SchemaService;
import org.imixs.workflow.exceptions.InvalidAccessException;
import org.imixs.workflow.exceptions.ModelException;
import org.imixs.workflow.faces.data.ViewController;
import org.imixs.workflow.faces.util.LoginController;
import org.imixs.workflow.office.config.SetupController;
import org.imixs.workflow.office.model.ModelController;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.event.Event;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * The SearchController provides methods for a convenient search experience.
 * <p>
 * The controller extends the Imixs ViewController and provides custom filter
 * and search queries to request a individual WorkList result. The
 * ItemCollection search filter holds filter criteria for a customized search
 * query.
 * <p>
 * The bean is session scoped to hold the search filter over a page journey.
 * <p>
 * The SearchController defines also a set of predefined filter properties:
 * 
 * <ul>
 * <li>processRef = holds a reference to a core process entity</li>
 * <li>spaceRef = holds a reference to a core space entity</li>
 * <li>phrase = holds the serach phrase</li>
 * </ul>
 * 
 * 
 * @author rsoika
 * @version 2.2.0
 */
@Named
@SessionScoped
public class SearchController extends ViewController implements Serializable {

    private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(SearchController.class.getName());

    ItemCollection searchFilter = null;
    String defaultQuery = null;
    String title = null;

    @Inject
    SetupController setupController;

    @Inject
    TeamController processController;

    @Inject
    LoginController loginController;

    @Inject
    ModelController modelController;

    @Inject
    ModelService modelService;

    @EJB
    SchemaService schemaService;

    @Inject
    DocumentService documentService;

    @Inject
    TeamService teamService;

    @Inject
    protected Event<SearchEvent> searchEvents;

    @Inject
    @ConfigProperty(name = "office.search.noanalyze", defaultValue = "undefined")
    transient String officeSearchNoanalyse;

    @Inject
    @ConfigProperty(name = "office.search.pagesize", defaultValue = "10")
    transient int officeSearchSize;

    ItemCollection process;
    ItemCollection space;

    /**
     * This method set the sort order and sort criteria
     * 
     **/
    @Override
    public void init() {
        this.setSortBy(setupController.getSortBy());
        this.setSortReverse(setupController.getSortReverse());
        this.setPageSize(officeSearchSize);
        this.setPageIndex(getPageIndex());

        if (searchFilter == null) {
            reset();
        }
        // extract the id from the query string
        FacesContext fc = FacesContext.getCurrentInstance();
        Map<String, String> paramMap = fc.getExternalContext().getRequestParameterMap();

        // default query?
        defaultQuery = paramMap.get("query");
        title = paramMap.get("title");

        // process Ref?
        String processRef = paramMap.get("processref");
        if (processRef != null && !processRef.isEmpty()) {
            ItemCollection _process = documentService.load(processRef);
            // test if the processRef is a uniqueid or if we have a name of a process
            if (_process == null) {
                // try to resolve the processRef by name...
                _process = teamService.getProcessByName(processRef);
                if (_process != null) {
                    processRef = _process.getUniqueID();
                }
            }
            searchFilter.replaceItemValue("processref", processRef);
        }

        // archive mode?
        if ("true".equals(paramMap.get("archive"))) {
            searchFilter.replaceItemValue("type", "workitemarchive");
        }

        String spaceRef = paramMap.get("spaceref");
        if (spaceRef != null && !spaceRef.isEmpty()) {
            searchFilter.replaceItemValue("spaceref", spaceRef);
        }

        String owner = paramMap.get("owner");
        if (owner != null && !owner.isEmpty()) {
            searchFilter.replaceItemValue("user", owner);
            searchFilter.replaceItemValue("usermode", "owner");
        }
        String creator = paramMap.get("creator");
        if (creator != null && !creator.isEmpty()) {
            searchFilter.replaceItemValue("user", creator);
            searchFilter.replaceItemValue("usermode", "creator");
        }
        String participant = paramMap.get("participant");
        if (participant != null && !participant.isEmpty()) {
            searchFilter.replaceItemValue("user", participant);
            searchFilter.replaceItemValue("usermode", "participant");
        }

        // extract optional workflowgorup and task
        String workflowgroup = paramMap.get("workflowgroup");
        if (workflowgroup != null && !workflowgroup.isEmpty()) {
            searchFilter.replaceItemValue("$WorkflowGroup", workflowgroup);

            // do we also have a task?
            String task = paramMap.get("task");
            if (task != null && !task.isEmpty()) {
                // if task is a number take it as is
                int taskID = 0;
                try {
                    taskID = Integer.parseInt(task);
                } catch (NumberFormatException nfe) {
                    // try to find the task based on the name of the given task....

                    try {
                        String version = modelService.findVersionByGroup(workflowgroup);
                    } catch (ModelException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    // Model model = modelController.getModelByGroup(workflowgroup);
                    List<ItemCollection> tasks = modelController.findAllTasksByGroup(workflowgroup); // model.findTasksByGroup(workflowgroup);
                    for (ItemCollection taskElement : tasks) {
                        if (task.equals(taskElement.getItemValueString("txtname"))) {
                            taskID = taskElement.getItemValueInteger("numProcessID");
                            break;
                        }
                    }
                }
                if (taskID > 0) {
                    searchFilter.replaceItemValue("$taskid", taskID);
                }
            }
        }

        String phrase = paramMap.get("phrase");
        if (phrase != null && !phrase.isEmpty()) {
            searchFilter.replaceItemValue("phrase", phrase);
        }

        // try to load process/space objects
        process = processController.getEntityById(searchFilter.getItemValueString("processref"));
        space = processController.getEntityById(searchFilter.getItemValueString("spaceref"));

    }

    /**
     * 3 = by last update 1 = by created 2 = by created
     */
    @Override
    public String getSortBy() {

        if ("1".equals(this.getSearchFilter().getItemValueString("sortorder"))
                || "2".equals(this.getSearchFilter().getItemValueString("sortorder"))) {
            return "$created";
        }
        if ("3".equals(this.getSearchFilter().getItemValueString("sortorder"))
                || "4".equals(this.getSearchFilter().getItemValueString("sortorder"))) {
            return "$lasteventdate";
        }
        return super.getSortBy();

    }

    public String getDefaultQuery() {
        return defaultQuery;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public boolean isSortReverse() {

        if ("2".equals(this.getSearchFilter().getItemValueString("sortorder"))
                || "4".equals(this.getSearchFilter().getItemValueString("sortorder"))) {
            return false;
        }
        if ("1".equals(this.getSearchFilter().getItemValueString("sortorder"))
                || "3".equals(this.getSearchFilter().getItemValueString("sortorder"))) {
            return true;
        }

        return super.isSortReverse();
    }

    public ItemCollection getProcess() {
        return process;
    }

    public ItemCollection getSpace() {
        return space;
    }

    /**
     * Resets the search filter and the current result.
     * 
     * @param event
     */
    @Override
    public void reset() {
        defaultQuery = null;
        title = null;
        searchFilter = new ItemCollection();
        searchFilter.replaceItemValue("type", "workitem");
        searchFilter.replaceItemValue("usermode", "owner");
        this.setPageIndex(0);
        super.reset();
    }

    /**
     * Resets the search filter but not the current search phrase (phrase) and the
     * process reference. The method refreshes also the current result.
     * 
     * @param event
     */
    public String resetFilter() {
        String processref = searchFilter.getItemValueString("processref");
        String searchPhrase = searchFilter.getItemValueString("phrase");
        reset();
        // restore the old phrase and processref
        searchFilter.replaceItemValue("phrase", searchPhrase);
        searchFilter.replaceItemValue("processref", processref);
        return refreshSearch();
    }

    /**
     * This method reset the search filter but not the phrase. The search pageIndex
     * is set to 0 and a new bookmarkable search link to the worklist including the
     * current search phrase is returned.
     * 
     */
    public String resetSearch() {
        String searchPhrase = searchFilter.getItemValueString("phrase");
        reset();
        searchFilter.replaceItemValue("phrase", searchPhrase);
        return refreshSearch();
    }

    /**
     * This method resets the search PageIndex to 0 and updates the bookmarkable
     * search link to the worklist including the current search phrase.
     * 
     */
    public String refreshSearch() {
        String phrase = searchFilter.getItemValueString("phrase");
        String processref = searchFilter.getItemValueString("processref");

        try {
            phrase = URLEncoder.encode(phrase, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.severe("unable to encode search phrase!");
            e.printStackTrace();
        }
        String action = "/pages/workitems/worklist.xhtml?faces-redirect=true&phrase=" + phrase;
        if (!processref.isEmpty()) {
            action = action + "&processref=" + processref;
        }
        this.setPageIndex(0);
        return action;
    }

    public String refreshSearch(AjaxBehaviorEvent event) {
        return refreshSearch();
    }

    public ItemCollection getSearchFilter() {
        if (searchFilter == null) {
            reset();
        }
        // searchFilter = new ItemCollection();
        return searchFilter;
    }

    public void setSearchFilter(ItemCollection searchFilter) {
        this.searchFilter = searchFilter;
    }

    /**
     * Can be set to true to restrict the result to workitems containing
     * attachments.
     * 
     * @param dms
     */
    public void setDMSMode(boolean dms) {
        this.searchFilter.replaceItemValue("dms_search", dms);
    }

    /**
     * Returns a Lucene search query based on the define searchFilter parameter set
     * <p>
     * If a search phrase is given, sortBy and sortReverse are reset. In case of a
     * general search in a specific contest the sortBy and sortReverse are set the
     * settings provided by the setupController
     * 
     * @param searchFilter - ItemCollection with filter criteria
     * @param view         - WorkList View type - @see WorklistController
     * 
     * @return - a lucene search query
     */
    @SuppressWarnings({ "unchecked", "unlikely-arg-type" })
    @Override
    public String getQuery() {

        if (defaultQuery != null && !defaultQuery.isEmpty()) {
            return defaultQuery;
        }

        String query = "";
        String emptySearchTerm = ""; // indicates that no query as the default type-query was defined.

        // read the filter parameters and removes duplicates
        List<Integer> processIDs = searchFilter.getItemValue("$taskID");
        List<String> processRefList = searchFilter.getItemValue("ProcessRef");
        List<String> spacesRefList = searchFilter.getItemValue("SpaceRef");
        List<String> workflowGroups = searchFilter.getItemValue("$WorkflowGroup");
        // trim lists
        while (processIDs.contains(""))
            processIDs.remove("");
        while (processRefList.contains(""))
            processRefList.remove("");
        while (spacesRefList.contains(""))
            spacesRefList.remove("");
        while (workflowGroups.contains(""))
            workflowGroups.remove("");
        while (processRefList.contains("-"))
            processRefList.remove("-");
        while (spacesRefList.contains("-"))
            spacesRefList.remove("-");

        List<String> typeList = searchFilter.getItemValue("Type");
        if (typeList.isEmpty() || "".equals(typeList.get(0))) {
            // default restrict to workitem
            typeList = Arrays.asList(new String[] { "workitem", "workitemarchive" });
        }

        // convert type list into comma separated list
        String sTypeQuery = "";
        Iterator<String> iterator = typeList.iterator();
        while (iterator.hasNext()) {
            sTypeQuery += "type:\"" + iterator.next() + "\"";
            if (iterator.hasNext())
                sTypeQuery += " OR ";
        }
        query += "(" + sTypeQuery + ") AND";
        emptySearchTerm = query; // define empty serach query

        // test if dms_search==true
        if ("true".equals(searchFilter.getItemValueString("dms_search"))) {
            query += " ($file.count:[1 TO 999]) AND";
        }

        Date datFrom = searchFilter.getItemValueDate("date.From");
        Date datTo = searchFilter.getItemValueDate("date.To");

        // process ref
        if (!processRefList.isEmpty()) {
            query += " (";
            iterator = processRefList.iterator();
            while (iterator.hasNext()) {
                query += "$uniqueidref:\"" + iterator.next() + "\"";
                if (iterator.hasNext())
                    query += " OR ";
            }
            query += " ) AND";
        }

        // Space ref
        if (!spacesRefList.isEmpty()) {
            query += " (";
            iterator = spacesRefList.iterator();
            while (iterator.hasNext()) {
                query += "$uniqueidref:\"" + iterator.next() + "\"";
                if (iterator.hasNext())
                    query += " OR ";
            }
            query += " ) AND";
        }

        // Workflow Group...
        if (!workflowGroups.isEmpty()) {
            query += " (";
            iterator = workflowGroups.iterator();
            while (iterator.hasNext()) {
                query += "$workflowgroup:\"" + iterator.next() + "\"";
                if (iterator.hasNext())
                    query += " OR ";
            }
            query += " ) AND";

        }

        // serach date range?
        String sDateFrom = "191401070000"; // because * did not work here
        String sDateTo = "211401070000";
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyyMMddHHmm");

        if (datFrom != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(datFrom);
            sDateFrom = dateformat.format(cal.getTime());
        }
        if (datTo != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(datTo);
            cal.add(Calendar.DATE, 1);
            sDateTo = dateformat.format(cal.getTime());
        }

        if (datFrom != null || datTo != null) {
            // expected format $created:[20020101 TO 20030101]
            query += " ($created:[" + sDateFrom + " TO " + sDateTo + "]) AND";
        }

        // filter requests by $create or $owner
        String sUser = this.getSearchFilter().getItemValueString("user");
        String sUserMode = this.getSearchFilter().getItemValueString("usermode");
        if (!"".equals(sUser)) {
            if ("creator".equals(sUserMode)) {
                query += " ($creator:\"" + sUser.toLowerCase() + "\") AND";
            } else if ("participant".equals(sUserMode)) {
                query += " ($participants:\"" + sUser.toLowerCase() + "\") AND";
            } else {
                query += " ($owner:\"" + sUser.toLowerCase() + "\") AND";
            }
        }

        if (!processIDs.isEmpty()) {
            query += " (";
            Iterator<Integer> iteratorID = processIDs.iterator();
            while (iteratorID.hasNext()) {
                query += "$taskid:\"" + iteratorID.next() + "\"";
                if (iteratorID.hasNext())
                    query += " OR ";
            }
            query += " ) AND";
        }

        // extend custom by search phrase....
        if (searchEvents != null) {
            SearchEvent event = new SearchEvent(searchFilter, SearchEvent.ON_QUERY);
            searchEvents.fire(event); // extend query
            String customQuery = event.getQuery();
            if (customQuery != null && !customQuery.isEmpty()) {
                if (!customQuery.trim().endsWith("AND")) {
                    customQuery = customQuery + " AND";
                }
                query += customQuery;
            }
        }

        // Search phrase....
        String searchphrase = searchFilter.getItemValueString("phrase");

        if (searchphrase != null && !"".equals(searchphrase)) {

            query += " (";

            // test if search phrase contains special characters
            String normalizedSearchphrase = schemaService.normalizeSearchTerm(searchphrase);
            if (!"undefined".equals(officeSearchNoanalyse) && !searchphrase.equalsIgnoreCase(normalizedSearchphrase)) {
                String[] officeFields = officeSearchNoanalyse.split(",");

                query += " (";
                Iterator<String> itemIterator = Arrays.asList(officeFields).iterator();
                while (itemIterator.hasNext()) {
                    String itemName = itemIterator.next();
                    if (!schemaService.getFieldListNoAnalyze().contains(itemName)) {
                        throw new InvalidAccessException("SEARCH_ERROR", "office.search.noanalyze contains the item '"
                                + itemName + "' which is not listed in 'index.fields.noanalyze'!");
                    }

                    query += itemName + ":\"" + searchphrase.trim() + "\" ";
                    if (itemIterator.hasNext())
                        query += " OR ";
                }
                query += ") OR ";
            }

            // standard search phrase
            query += "(" + normalizedSearchphrase.trim() + ") ";

            query += ")";
            // we do not sort the result other then by relevance. Sorting by date does not
            // make sense.
            setSortBy(null);
            this.setSortReverse(false);

        } else {
            // set sortBy and sortReverse
            this.setSortBy(setupController.getSortBy());
            this.setSortReverse(setupController.getSortReverse());
        }

        // test if a seach query was finally defined....
        if (query.equals(emptySearchTerm)) {
            // no query defined
            query = null;
        }

        // cut last AND
        if (query != null && query.endsWith("AND")) {
            query = query.substring(0, query.length() - 3);
        }

        logger.fine("Search Query=" + query);

        return query;
    }

}
