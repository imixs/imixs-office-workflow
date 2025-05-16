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
package org.imixs.workflow.office.dataview;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.imixs.archive.core.SnapshotService;
import org.imixs.workflow.FileData;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.engine.DocumentService;
import org.imixs.workflow.engine.WorkflowService;
import org.imixs.workflow.exceptions.ModelException;
import org.imixs.workflow.exceptions.PluginException;
import org.imixs.workflow.exceptions.QueryException;
import org.imixs.workflow.faces.data.ViewController;
import org.imixs.workflow.faces.data.ViewHandler;
import org.imixs.workflow.office.forms.CustomFormController;
import org.imixs.workflow.office.forms.CustomFormSection;
import org.imixs.workflow.poi.XSSFUtil;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.Conversation;
import jakarta.enterprise.context.ConversationScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;

/**
 * The DataViewController is used to display a data view
 * <p>
 * The controller uses the uniqueId from the URL to load the definition.
 * The bean reads optional cached query data form
 * a session scoped cache EJB and reloads the last state. This is useful for
 * situations where the user navigates to a new page (e.g. open a workitem) and
 * late uses browser history back.
 * <p>
 * Note: This bean is ConversationScoped, because it uses the
 * CustomFormController which expects conversion scope!
 * 
 * 
 * @author rsoika
 * @version 1.0
 */
@Named
@ConversationScoped
public class DataViewController extends ViewController {

    private static final long serialVersionUID = 1L;

    public static final String ERROR_CONFIG = "CONFIG_ERROR";
    public static final int MAX_ROWS = 3000;

    private List<CustomFormSection> sections = null;
    private List<ItemCollection> viewItemDefinitions = null;
    protected ItemCollection dataDefinition = null;
    private ItemCollection filter;
    private String query;
    private String errorMessage;

    @Inject
    DataViewCache dataViewCache;

    @Inject
    private Conversation conversation;

    @Inject
    private DocumentService documentService;

    @Inject
    private WorkflowService workflowService;

    @Inject
    SnapshotService snapshotService;

    @Inject
    CustomFormController customFormController;

    @Inject
    ViewHandler viewHandler;

    @Inject
    DataViewDefinitionController dataViewDefinitionController;

    private static Logger logger = Logger.getLogger(DataViewController.class.getName());

    @Override
    @PostConstruct
    public void init() {
        super.init();
        // this.setQuery(dataViewController.getQuery());
        this.setSortBy("$modified");
        this.setSortReverse(false);
        this.setPageSize(100);
        this.setLoadStubs(false);
    }

    /**
     * This method loads the custom form sections
     */
    public void onLoad() {
        String uniqueid = null;

        // Important: start a new conversation beause of the usage of the
        // CustomFormController!
        startConversation();

        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (!facesContext.isPostback() && !facesContext.isValidationFailed()) {
            // ...
            FacesContext fc = FacesContext.getCurrentInstance();
            Map<String, String> paramMap = fc.getExternalContext().getRequestParameterMap();
            // try to extract the uniqueid form the query string...
            uniqueid = paramMap.get("id");
            if (uniqueid == null || uniqueid.isEmpty()) {
                // alternative 'workitem=...'
                uniqueid = paramMap.get("workitem");
            }
            dataDefinition = documentService.load(uniqueid);
        }

        if (uniqueid != null && !uniqueid.isEmpty()) {
            filter = dataViewCache.get(uniqueid);
        } else {
            filter = new ItemCollection();
        }

        try {
            // Init new Filter....
            if (dataDefinition != null) {
                filter.setItemValue("txtWorkflowEditorCustomForm", dataDefinition.getItemValue("form"));
                filter.setItemValue("name", dataDefinition.getItemValueString("name"));
                filter.setItemValue("description", dataDefinition.getItemValueString("description"));
                viewItemDefinitions = DataViewDefinitionController
                        .computeDataViewItemDefinitions(dataDefinition);

                customFormController.computeFieldDefinition(filter);
                sections = customFormController.getSections();

                // Update View Handler settings
                String sortBy = dataDefinition.getItemValueString("sort.by");
                if (sortBy.isEmpty()) {
                    sortBy = "$modified";
                }
                this.setSortBy(sortBy);
                this.setSortReverse(dataDefinition.getItemValueBoolean("sort.reverse"));
                this.setPageIndex(filter.getItemValueInteger("pageIndex"));
                if (!filter.getItemValueString("query").isEmpty()) {
                    query = filter.getItemValueString("query");
                    // Prefetch data to update total count and page count
                    viewHandler.getData(this);
                }
            }
        } catch (ModelException | QueryException e) {
            logger.warning("Failed to load dataview definition: " + e.getMessage());
        }

    }

    public ItemCollection getDefinition() {
        return dataDefinition;
    }

    public List<CustomFormSection> getSections() {
        return sections;
    }

    public List<ItemCollection> getViewItemDefinitions() {
        return viewItemDefinitions;
    }

    public ItemCollection getFilter() {
        return filter;
    }

    public void setFilter(ItemCollection filter) {
        this.filter = filter;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * This helper method builds a query from the query definition and the current
     * filter criteria.
     * 
     * The method loads the query form the definition and replaces all {<itemname>}
     * elements with the values from the filter
     * 
     *
     * @throws QueryException
     */
    public void run() throws QueryException {

        reset();

        // build new query from template
        query = dataDefinition.getItemValueString("query");

        List<String> filterItems = filter.getItemNames();
        for (String itemName : filterItems) {
            String itemValue = filter.getItemValueString(itemName);

            // is date?
            if (filter.getItemValueDate(itemName) != null) {
                String sDateFrom = "191401070000"; // because * did not work here
                String sDateTo = "211401070000";
                SimpleDateFormat dateformat = new SimpleDateFormat("yyyyMMddHHmm");

                itemValue = dateformat.format(filter.getItemValueDate(itemName));

            }

            // Create regex pattern to match {itemName} (case-sensitive)
            // The Pattern.quote is used to escape any special regex characters in the
            // itemName
            // Replace all occurrences in the query case-insensitive.
            query = query.replaceAll("(?i)\\{" + Pattern.quote(itemName) + "\\}", itemValue);
        }
        logger.finest("query=" + query);
        filter.setItemValue("query", query);

        // Prefetch data to update total count and page count
        viewHandler.getData(this);
        // cache filter
        dataViewCache.put(dataDefinition.getUniqueID(), filter);

    }

    /**
     * Returns the current query
     * 
     * @return
     */
    public String getQuery() {
        return query;
    }

    /**
     * This method navigates back in the page index and caches the current page
     * index
     */
    public void back() {
        viewHandler.back(this);
        filter.setItemValue("pageIndex", this.getPageIndex());
    }

    /**
     * This method navigates forward in the page index and caches the current page
     * index
     */
    public void forward() {
        viewHandler.forward(this);
        filter.setItemValue("pageIndex", this.getPageIndex());
    }

    /**
     * Starts a new conversation
     */
    protected void startConversation() {
        if (conversation.isTransient()) {
            conversation.setTimeout(
                    ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest())
                            .getSession().getMaxInactiveInterval() * 1000);
            conversation.begin();
            logger.log(Level.FINEST, "......start new conversation, id={0}",
                    conversation.getId());

        }
    }

    /**
     * Exports data into a excel template processed by apache-poi
     *
     * @throws PluginException
     * @throws QueryException
     */
    public String export() throws PluginException, QueryException {

        // build query and prepare dataset
        run();

        SimpleDateFormat dateformat = new SimpleDateFormat("yyyyMMddHHmm");
        String targetFileName = dataDefinition.getItemValueString("poi.targetFilename");
        if (targetFileName.isEmpty()) {
            throw new PluginException(DataViewController.class.getSimpleName(), ERROR_CONFIG,
                    "Missing Excel Export definition - check configuration!");
        }
        logger.info("start export : " + targetFileName + "...");
        logger.fine(query);

        // load template
        FileData fileData = loadTemplate();

        if (fileData == null) {
            // we did not found the template!
            throw new PluginException(DataViewController.class.getSimpleName(), ERROR_CONFIG,
                    "Missing Excel Export Template - check DataView definition!");
        }
        targetFileName = targetFileName + "_" + dateformat.format(new Date()) + ".xlsx";
        try {
            // test if query exceeds max count
            int totalCount = documentService.count(query);
            if (totalCount > MAX_ROWS) {
                throw new PluginException(DataViewController.class.getSimpleName(), ERROR_CONFIG,
                        "Data can not be exported into Excel because dataset exceeds " + MAX_ROWS + " rows!");
            }
            String sortBy = dataDefinition.getItemValueString("sort.by");
            if (sortBy.isEmpty()) {
                sortBy = "$modified"; // default
            }
            List<ItemCollection> invoices = documentService.find(query, MAX_ROWS, 0, sortBy,
                    dataDefinition.getItemValueBoolean("sort.reverse"));
            if (invoices.size() > 0) {
                String referenceCell = dataDefinition.getItemValueString("poi.referenceCell");
                XSSFUtil.insertDataRows(invoices, referenceCell, viewItemDefinitions, fileData);
            }

            // create a temp event
            ItemCollection event = new ItemCollection().setItemValue("txtActivityResult",
                    dataDefinition.getItemValue("poi.update"));
            ItemCollection poiConfig = workflowService.evalWorkflowResult(event, "poi-update", dataDefinition,
                    false);
            XSSFUtil.poiUpdate(filter, fileData, poiConfig, workflowService);

            fileData.setName(targetFileName);

            // See:
            // https://stackoverflow.com/questions/9391838/how-to-provide-a-file-download-from-a-jsf-backing-bean
            DataViewPOIHelper.downloadExcelFile(fileData);
        } catch (IOException | QueryException e) {
            throw new PluginException(DataViewController.class.getSimpleName(), ERROR_CONFIG,
                    "Failed to generate Excel Export: " + e.getMessage());
        }

        // return "/pages/admin/excel_export_rechnungsausgang.jsf?faces-redirect=true";
        return "";
    }

    /**
     * This method returns the first excel poi template from the Data Definition
     *
     * @param name in attribute txtname
     *
     *
     */
    private FileData loadTemplate() {

        // first filename
        List<FileData> fileDataList = dataDefinition.getFileData();
        if (fileDataList != null && fileDataList.size() > 0) {
            String fileName = fileDataList.get(0).getName();
            return snapshotService.getWorkItemFile(dataDefinition.getUniqueID(), fileName);
        }
        // no file data available!
        return null;
    }

    /**
     * Helper method to initialize a file download
     *
     * @throws IOException
     */
    public static void downloadExcelFile(FileData fileData) throws IOException {

        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();

        externalContext.responseReset();
        externalContext.setResponseContentType("application/vnd.ms-excel");
        externalContext.setResponseContentLength(fileData.getContent().length);
        externalContext.setResponseHeader("Content-Disposition", "attachment; filename=\"" + fileData.getName() + "\"");

        OutputStream output = externalContext.getResponseOutputStream();

        // Now you can write the InputStream of the file to the above OutputStream the
        // usual way.
        output.write(fileData.getContent());

        facesContext.responseComplete(); // Important! Otherwise Faces will attempt to render the response which
                                         // obviously will fail since it's already written with a file and closed.
    }

}
