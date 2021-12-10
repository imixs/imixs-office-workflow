package org.imixs.workflow.office.forms;

import java.io.Serializable;

import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * The MinuteController is a session scoped backing bean controlling a list of
 * minute workitems assigned to a parent workitem. This controller can be used
 * for different kind of Minute-Workflows.
 * 
 * The method toggleWorkitem can be used to open/closed embedded editors:
 * 
 * 
 * 
 * @author rsoika
 */
@Named
@ConversationScoped
public class MinuteController implements Serializable {

    private static final long serialVersionUID = 1L;

    private String selectedMinute = null;
    
    @Inject
    ChildItemController childItemController;

    /**
     * Here we initialize the formController for the minute workitem
     */
    public MinuteController() {
        super();
    }
    
    
    public  void add() {
        childItemController.add();
        selectedMinute=""+childItemController.getChildItems().size();
    }

    /**
     * This method toggles the current minute workitem to be edited or closed
     * 
     * @param id
     */
    public void toggleMinute(String id) {
        if (selectedMinute != null && selectedMinute.equals(id)) {
            // close edit mode
            selectedMinute = "";
        } else {
            selectedMinute = id;
        }
    }

    public String getSelectedMinute() {
        return selectedMinute;
    }

    public void setSelectedMinute(String selectedMinute) {
        this.selectedMinute = selectedMinute;
    }

}
