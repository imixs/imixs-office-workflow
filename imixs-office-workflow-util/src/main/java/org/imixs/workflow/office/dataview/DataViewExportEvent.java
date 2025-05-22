package org.imixs.workflow.office.dataview;

import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.imixs.workflow.ItemCollection;

/**
 * An DataViewExportEvent is send by the DataViewController during exporting a
 * data view into a Excel Template.
 * An observer can customize the export behavior and mark an export as
 * completed by setting the flag 'completed' to true
 * 
 */
public class DataViewExportEvent {

    private List<ItemCollection> dataset = null;
    private ItemCollection dataViewDefinition = null;
    private List<ItemCollection> viewItemDefinitions = null;
    private XSSFWorkbook xssfWorkbook = null;
    private boolean completed = false;

    public DataViewExportEvent(List<ItemCollection> dataset, ItemCollection dataViewDefinition,
            List<ItemCollection> viewItemDefinitions,
            XSSFWorkbook xssfWorkbook) {
        this.dataset = dataset;
        this.dataViewDefinition = dataViewDefinition;
        this.xssfWorkbook = xssfWorkbook;
        this.viewItemDefinitions = viewItemDefinitions;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public List<ItemCollection> getDataset() {
        return dataset;
    }

    public void setDataset(List<ItemCollection> dataset) {
        this.dataset = dataset;
    }

    public ItemCollection getDataViewDefinition() {
        return dataViewDefinition;
    }

    public void setDataViewDefinition(ItemCollection dataViewDefinition) {
        this.dataViewDefinition = dataViewDefinition;
    }

    public List<ItemCollection> getViewItemDefinitions() {
        return viewItemDefinitions;
    }

    public void setViewItemDefinitions(List<ItemCollection> viewItemDefinitions) {
        this.viewItemDefinitions = viewItemDefinitions;
    }

    public XSSFWorkbook getXssfWorkbook() {
        return xssfWorkbook;
    }

    public void setXssfWorkbook(XSSFWorkbook xssfWorkbook) {
        this.xssfWorkbook = xssfWorkbook;
    }

}
