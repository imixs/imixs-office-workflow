package org.imixs.workflow.office.forms;

import org.imixs.workflow.ItemCollection;

/**
 * An AnalyticEvent is send by the AnalyticController during initialization
 */
public class AnalyticEvent {

    private String key;
    private Object value = null;
    private String label = "";
    private String description = "";
    private String link = "";
    private ItemCollection workitem = null;

    public AnalyticEvent(String key, ItemCollection workitem) {
        this.key = key;
        this.workitem = workitem;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public ItemCollection getWorkitem() {
        return workitem;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
