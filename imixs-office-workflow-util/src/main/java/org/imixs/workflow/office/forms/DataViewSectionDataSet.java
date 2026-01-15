package org.imixs.workflow.office.forms;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;

import org.imixs.workflow.FileData;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.dataview.DataViewController;
import org.imixs.workflow.dataview.DataViewExportEvent;
import org.imixs.workflow.dataview.DataViewPOIHelper;
import org.imixs.workflow.dataview.DataViewService;
import org.imixs.workflow.exceptions.PluginException;
import org.imixs.workflow.exceptions.QueryException;

public class DataViewSectionDataSet implements Serializable {
    private static final long serialVersionUID = 1L;

    public static Logger logger = Logger.getLogger(DataViewSectionController.class.getName());

    List<ItemCollection> data = null;
    private ItemCollection dataViewDefinition;
    private String dataViewName;
    private List<ItemCollection> viewItemDefinitions = null;
    private String query = null;
    private boolean endOfList = false;

    private long totalCount = 0;
    private long totalPages = 0;

    private String sortBy = null;
    private boolean sortReverse = false;
    private int pageSize = 10;
    private int pageIndex = 0;

    private DataViewService dataViewService;

    private ItemCollection workitem;

    public DataViewSectionDataSet(String dataViewName, ItemCollection workitem, DataViewService dataViewService) {
        this.dataViewName = dataViewName;
        this.workitem = workitem;
        this.dataViewService = dataViewService;

        dataViewDefinition = dataViewService.loadDataViewDefinition(dataViewName);

        boolean debug = dataViewDefinition.getItemValueBoolean("debug");
        if (debug) {
            logger.info("resolve query by dataView '" + dataViewName + "'");
        }

        // preload the viewItem definitions
        viewItemDefinitions = this.dataViewService.computeDataViewItemDefinitions(dataViewDefinition);

        // resove query by dataView
        query = this.dataViewService.parseQuery(dataViewDefinition, this.workitem);

        loadData();
    }

    /**
     * load current page
     */
    private void loadData() {
        try {
            data = dataViewService.getWorkflowService().getDocumentService().find(query, getPageSize(), getPageIndex(),
                    getSortBy(), isSortReverse());

            // The end of a list is reached when the size is below or equal the
            // pageSize. See issue #287
            totalCount = dataViewService.getWorkflowService().getDocumentService().count(query);
            totalPages = (long) Math.ceil((double) totalCount / pageSize);

            if (data.size() < pageSize) {
                setEndOfList(true);
            } else {
                // look ahead if we have more entries...
                int iAhead = (getPageSize() * (getPageIndex() + 1)) + 1;
                if (totalCount < iAhead) {
                    // there is no more data
                    setEndOfList(true);
                } else {
                    setEndOfList(false);
                }
            }
        } catch (QueryException e) {
            logger.warning("Failed to load data: " + e.getMessage());
        }
    }

    public List<ItemCollection> getData() {
        return data;
    }

    public ItemCollection getDataViewDefinition() {
        return dataViewDefinition;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public long getTotalPages() {
        return totalPages;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public boolean isEndOfList() {
        return endOfList;
    }

    public void setEndOfList(boolean endOfList) {
        this.endOfList = endOfList;
    }

    public String getDataViewName() {
        return dataViewName;
    }

    public List<ItemCollection> getViewItemDefinitions() {
        return viewItemDefinitions;
    }

    /**
     * returns the maximum size of a search result
     * 
     * @return
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * set the maximum size of a search result
     * 
     * @param searchCount
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public boolean isSortReverse() {
        return sortReverse;
    }

    public void setSortReverse(boolean sortReverse) {
        this.sortReverse = sortReverse;
    }

    /**
     * Exports data into a excel template processed by apache-poi. The method sends
     * a DataViewExport event to allow clients to adapt the export process.
     * 
     * @see DataViewExportEvent
     *
     * @throws PluginException
     * @throws QueryException
     */
    public String export() throws PluginException, QueryException {

        // Build target filename
        boolean debug = dataViewDefinition.getItemValueBoolean("debug");

        // start export
        if (debug) {
            logger.info("│   ├── Query: " + query);
        }

        try {

            // test if query exceeds max count
            int totalCount = dataViewService.getWorkflowService().getDocumentService().count(query);
            // start export
            if (debug) {
                logger.info("│   ├── Count: " + totalCount);
            }
            if (totalCount > DataViewService.MAX_ROWS) {
                throw new PluginException(DataViewController.class.getSimpleName(), DataViewService.ERROR_CONFIG,
                        "Data can not be exported into Excel because dataset exceeds " + DataViewService.MAX_ROWS
                                + " rows!");
            }
            String sortBy = dataViewDefinition.getItemValueString("sort.by");
            if (sortBy.isEmpty()) {
                sortBy = "$modified"; // default
            }
            List<ItemCollection> workitems = dataViewService.getWorkflowService().getDocumentService().find(query,
                    DataViewService.MAX_ROWS, 0, sortBy,
                    dataViewDefinition.getItemValueBoolean("sort.reverse"));

            FileData fileDataExport = dataViewService.poiExport(workitems, dataViewDefinition, viewItemDefinitions);

            // create a temp event
            ItemCollection event = new ItemCollection().setItemValue("txtActivityResult",
                    dataViewDefinition.getItemValue("poi.update"));
            ItemCollection poiConfig = dataViewService.getWorkflowService().evalWorkflowResult(event, "poi-update",
                    dataViewDefinition,
                    false);

            // merge workitem fields (Workaround because custom forms did hard coded map to
            // workflowController instead of workitem

            DataViewPOIHelper.poiUpdate(workitem, fileDataExport, poiConfig, dataViewService.getWorkflowService());

            if (debug) {
                logger.info("├── POI Export completed!");
            }
            // See:
            // https://stackoverflow.com/questions/9391838/how-to-provide-a-file-download-from-a-jsf-backing-bean
            DataViewPOIHelper.downloadExcelFile(fileDataExport);
        } catch (IOException | QueryException e) {
            throw new PluginException(DataViewController.class.getSimpleName(), DataViewService.ERROR_CONFIG,
                    "Failed to generate Excel Export: " + e.getMessage());
        }

        // return "/pages/admin/excel_export_rechnungsausgang.jsf?faces-redirect=true";
        return "";
    }

    /**
     * Returns true if a poi export is defined
     * 
     * @return
     */
    public boolean hasPoiExport() {
        if (dataViewDefinition != null && !dataViewDefinition.getItemValueString("poi.targetfilename").isBlank()) {
            return true;
        }
        return false;
    }
}