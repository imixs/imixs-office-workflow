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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.WorkflowKernel;
import org.imixs.workflow.engine.DocumentService;
import org.imixs.workflow.engine.ModelService;
import org.imixs.workflow.engine.index.SchemaService;
import org.imixs.workflow.exceptions.QueryException;
import org.imixs.workflow.faces.util.LoginController;
import org.imixs.workflow.office.config.SetupController;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 ** The BoardController provides a logic to split up the worklist by there
 * workflow groups and status. The board controller can select a worklist based
 * on a unqiueIdRef or if not defined by the current user (task list).
 * <p>
 * The controller holds a cache map storing all tasks by workflow group and
 * status.
 * <p>
 * The number of workitems loaded internally are restricted to the property
 * "boardcontroller.pagesize". The default size is 100.
 * <p>
 * The workitems displayed within ony category are restricted to the property
 * "boardcontroller.categorysize". The default size is 5.
 * 
 * @author rsoika
 * @version 2.0
 */
@Named
@ViewScoped
public class BoardController implements Serializable {

    private static final long serialVersionUID = 1L;
    private Map<BoardCategory, List<ItemCollection>> cacheTasks;

    // view params
    private String processRef;
    private ItemCollection process;
    private int pageIndex = 0;
    private int pageMax = 0;

    private boolean endOfList = false;

    private String view;
    private String title;

    @Inject
    protected LoginController loginController = null;

    @Inject
    SetupController setupController;

    private static Logger logger = Logger.getLogger(BoardController.class.getName());

    @EJB
    ModelService modelService = null;

    @EJB
    DocumentService documentService;

    @EJB
    SchemaService schemaService;

    @Inject
    @ConfigProperty(name = "boardcontroller.pagesize", defaultValue = "100")
    transient int pageSize;

    @Inject
    @ConfigProperty(name = "boardcontroller.categorysize", defaultValue = "5")
    transient int categoryPageSize;

    @Inject
    SearchController searchController;

    public BoardController() {
        super();
    }

    /**
     * Initialize default behavior initialize the processref, page index and phrase
     * 
     */
    @PostConstruct
    public void init() {
        searchController.reset();
        // extract the processref and page from the query string
        FacesContext fc = FacesContext.getCurrentInstance();
        Map<String, String> paramMap = fc.getExternalContext().getRequestParameterMap();

        setProcessRef(paramMap.get("processref"));

        String _page = paramMap.get("page");
        if (_page != null && !_page.isEmpty()) {
            setPageIndex(Integer.parseInt(_page));
        }

        String _phrase = paramMap.get("phrase");
        if (_phrase != null && !_phrase.isEmpty()) {
            searchController.getSearchFilter().setItemValue("phrase", _phrase);
        }

        view = paramMap.get("viewType");

        // initalize the task list...
        cacheTasks = new HashMap<>();
        readWorkList();
    }

    /***************************************************************************
     * Navigation
     */

    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageMax() {
        return pageMax;
    }

    public String getProcessRef() {
        if (processRef == null) {
            processRef = "";
        }
        return processRef;
    }

    public void setProcessRef(String processRef) {
        this.processRef = processRef;
        // try to load ref...
        if (processRef != null && !processRef.isEmpty()) {
            process = documentService.load(processRef);
            if (process != null) {
                String title = process.getItemValueString("name");
                setTitle(title);
            }
        }
    }

    public String getView() {
        if (view == null || view.isEmpty()) {
            view = "worklist.owner";
        }
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    public ItemCollection getProcess() {
        return process;
    }

    /**
     * Get the board title. The default board title is message['worklist.owner']
     * 
     * @return
     */
    public String getTitle() {
        if (title == null || title.isEmpty()) {
            // get default title...
            try {
                Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
                ResourceBundle rb = null;
                if (locale != null)
                    rb = ResourceBundle.getBundle("bundle.messages", locale);
                else
                    rb = ResourceBundle.getBundle("bundle.messages");
                title = rb.getString(getView());
            } catch (java.util.MissingResourceException eb) {
                title = "";
            }
        }

        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPhraseEncoded() {
        if (searchController.getSearchFilter() == null) {
            return null;
        }
        try {
            return URLEncoder.encode(searchController.getSearchFilter().getItemValueString("phrase"), "UTF-8");
        } catch (UnsupportedEncodingException e) {

            e.printStackTrace();
            return searchController.getSearchFilter().getItemValueString("phrase");
        }
    }

    /**
     * This method discards the cache an reset the current ref.
     */
    public void reset() {
        cacheTasks = null;
        pageIndex = 0;
        endOfList = false;
    }

    /**
     * This method discards the cache.
     */
    public void refresh() {
        cacheTasks = null;
        pageIndex = 0;
        endOfList = false;
    }

    /**
     * This method resets the search PageIndex to 0 and updates the bookmarkable
     * search link to the worklist including the current search phrase.
     * 
     */
    public String refreshSearch() {
        this.setPageIndex(0);
        return getBookmark();
    }

    private String getBookmark() {
        String phrase = searchController.getSearchFilter().getItemValueString("phrase");

        try {
            phrase = URLEncoder.encode(phrase, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.severe("unable to encode search phrase!");
            e.printStackTrace();
        }
        String action = "/pages/notes_board.xhtml?faces-redirect=true&page=" + getPageIndex() + "&processref="
                + getProcessRef() + "&phrase=" + phrase;

        return action;
    }

    /**
     * This method increases the search PageIndex and updates the bookmarkable
     * search link to the worklist including the current search phrase.
     * 
     */
    public String getNext() {
        this.setPageIndex(this.getPageIndex() + 1);
        return getBookmark();
    }

    /**
     * This method increases the search PageIndex and updates the bookmarkable
     * search link to the worklist including the current search phrase.
     * 
     */
    public String getPrev() {
        this.setPageIndex(this.getPageIndex() - 1);
        return getBookmark();
    }

    public String getPage(int index) {
        this.setPageIndex(index);
        return getBookmark();
    }

    /**
     * Returns a list of all workflow groups out of the current worklist. If no
     * worklist is yet selected, the method triggers the method readWorkList();
     * 
     * @return
     */
    public List<BoardCategory> getCategories() {

        List<BoardCategory> result = new ArrayList<BoardCategory>();
        result.addAll(cacheTasks.keySet());

        // sort result
        Collections.sort(result, new Comparator<BoardCategory>() {
            @Override
            public int compare(BoardCategory p1, BoardCategory p2) {
                return p1.toString().compareTo(p2.toString());
            }
        });

        return result;
    }

    /**
     * Loads the next page for a category
     */
    public void doLoadNext(BoardCategory category) {
        if (!isEndOfList(category)) {
            category.setPageIndex(category.pageIndex + 1);
        }
    }

    /**
     * Loads the prev page for a category
     */
    public void doLoadPrev(BoardCategory category) {
        if (category.pageIndex > 0) {
            category.setPageIndex(category.pageIndex - 1);
        }

    }

    public boolean isEndOfList() {
        return endOfList;
    }

    public void setEndOfList(boolean endOfList) {
        this.endOfList = endOfList;
    }

    public int getCategoryPageSize() {
        return categoryPageSize;
    }

    public void setCategoryPageSize(int categoryPageSize) {
        this.categoryPageSize = categoryPageSize;
    }

    /**
     * This method returns the current page of workitems for the given category.
     * 
     * @param category
     * @return
     */
    public List<ItemCollection> getWorkitems(BoardCategory category) {
        List<ItemCollection> temp = cacheTasks.get(category);

        int iStart = category.getPageIndex() * category.getPageSize();
        int iEnd = iStart + (category.getPageSize());
        if (iEnd > temp.size()) {
            iEnd = temp.size();
        }

        // return sublist
        return temp.subList(iStart, iEnd);
    }

    public boolean isEndOfList(BoardCategory category) {
        List<ItemCollection> temp = cacheTasks.get(category);
        int i = (category.getPageIndex() + 1) * category.getPageSize();
        if (i >= temp.size()) {
            return true;
        }
        return false;
    }

    /**
     * This method reads the first 100 elements form the task list and puts them
     * into a cache.
     * 
     * @throws QueryException
     * 
     */
    private void readWorkList() {
        long l = System.currentTimeMillis();
        cacheTasks = new HashMap<>();

        String sortBy = setupController.getSortBy();
        boolean bReverse = setupController.getSortReverse();
        List<ItemCollection> taskList;
        try {
            if (getProcessRef().isEmpty()) {
                if ("worklist.creator".equals(view)) {
                    searchController.getSearchFilter().setItemValue("usermode", "creator");
                } else {
                    searchController.getSearchFilter().setItemValue("usermode", "owner");
                }
                searchController.getSearchFilter().setItemValue("user", loginController.getUserPrincipal());
            } else {
                searchController.getSearchFilter().setItemValue("ProcessRef", getProcessRef());
            }

            String query = searchController.getQuery();
            taskList = documentService.findStubs(query, getPageSize(), getPageIndex(), sortBy, bReverse);
            // count pages only once...
            if (pageMax == 0) {
                pageMax = documentService.countPages(query, getPageSize());
            }
        } catch (QueryException e) {
            logger.warning("failed to read task list: " + e.getMessage());
            taskList = new ArrayList<>();
        }

        endOfList = (taskList.size() < pageSize);

        // now split up the result into groups and tasks....
        for (ItemCollection workitem : taskList) {
            BoardCategory tmpCat = new BoardCategory(workitem.getItemValueString(WorkflowKernel.WORKFLOWGROUP),
                    workitem.getItemValueString(WorkflowKernel.WORKFLOWSTATUS), workitem.getTaskID(),
                    getCategoryPageSize());

            // did we already have a cached list?
            List<ItemCollection> tasksByCategory = cacheTasks.get(tmpCat);
            if (tasksByCategory == null) {
                tasksByCategory = new ArrayList<>();
            }
            // add task....
            tasksByCategory.add(workitem);

            cacheTasks.put(tmpCat, tasksByCategory);
        }

        logger.fine("read worklist in " + (System.currentTimeMillis() - l) + "ms");

    }

}
