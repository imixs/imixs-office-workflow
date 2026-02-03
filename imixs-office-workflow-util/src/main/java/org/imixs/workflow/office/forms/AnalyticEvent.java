package org.imixs.workflow.office.forms;

import org.imixs.workflow.ItemCollection;

/**
 * An AnalyticEvent is send by the AnalyticController during initialization
 */
public class AnalyticEvent {

    private String key;
    private String options = null;
    private ItemCollection workitem = null;
    private ItemCollection data = null;

    public AnalyticEvent(String key, ItemCollection workitem, String options) {
        this.key = key;
        this.workitem = workitem;
        this.options = options;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public ItemCollection getWorkitem() {
        return workitem;
    }

    public void setWorkitem(ItemCollection workitem) {
        this.workitem = workitem;
    }

    public ItemCollection getData() {
        if (data == null) {
            data = new ItemCollection();
        }
        return data;
    }

    public void setData(ItemCollection data) {
        this.data = data;
    }

}
