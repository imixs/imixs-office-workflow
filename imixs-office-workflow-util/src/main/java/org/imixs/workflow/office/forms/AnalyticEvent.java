package org.imixs.workflow.office.forms;

import org.imixs.workflow.ItemCollection;

/**
 * An AnalyticEvent is send by the AnalyticController during initialization
 */
public class AnalyticEvent {

    private String key;
    // private Object value = null;
    private String options = null;
    // private String label = "";
    // private String description = "";
    // private String link = "";
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
        return data;
    }

    public void setData(ItemCollection data) {
        this.data = data;
    }

    // public void setLink(String link) {
    // this.link = link;
    // }

    // public Object getValue() {
    // return value;
    // }

    // public void setValue(Object value) {
    // this.value = value;
    // }

    // public String getLabel() {
    // return label;
    // }

    // public void setLabel(String label) {
    // this.label = label;
    // }

    // public String getDescription() {
    // return description;
    // }

    // public void setDescription(String description) {
    // this.description = description;
    // }

}
