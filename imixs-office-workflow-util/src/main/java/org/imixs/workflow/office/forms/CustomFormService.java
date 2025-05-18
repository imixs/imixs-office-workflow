package org.imixs.workflow.office.forms;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.ModelManager;
import org.imixs.workflow.engine.ProcessingEvent;
import org.imixs.workflow.engine.WorkflowService;
import org.imixs.workflow.exceptions.AccessDeniedException;
import org.imixs.workflow.exceptions.ModelException;
import org.openbpmn.bpmn.BPMNModel;

import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

/**
 * The CustomFormController computes a set of fields based on a data object
 * provided by the model. This data is used by the {@link CustomFormController}
 * to display sections and fields.
 * 
 * 
 * @author rsoika
 * 
 */
@DeclareRoles({ "org.imixs.ACCESSLEVEL.NOACCESS",
        "org.imixs.ACCESSLEVEL.READERACCESS",
        "org.imixs.ACCESSLEVEL.AUTHORACCESS",
        "org.imixs.ACCESSLEVEL.EDITORACCESS",
        "org.imixs.ACCESSLEVEL.MANAGERACCESS" })
@RolesAllowed({ "org.imixs.ACCESSLEVEL.NOACCESS",
        "org.imixs.ACCESSLEVEL.READERACCESS",
        "org.imixs.ACCESSLEVEL.AUTHORACCESS",
        "org.imixs.ACCESSLEVEL.EDITORACCESS",
        "org.imixs.ACCESSLEVEL.MANAGERACCESS" })
@Stateless
@LocalBean
public class CustomFormService implements Serializable {

    @Inject
    WorkflowService workflowService;

    private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(CustomFormService.class.getName());

    /**
     * WorkflowEvent listener to update the current FormDefinition.
     * 
     * @param processingEvent
     * @throws AccessDeniedException
     * @throws ModelException
     */
    public void onWorkflowEvent(@Observes ProcessingEvent processingEvent) throws ModelException {
        if (processingEvent == null)
            return;
        // skip if not a workItem...
        if (processingEvent.getDocument() != null
                && !processingEvent.getDocument().getItemValueString("type").startsWith("workitem")) {
            return;
        }

        int eventType = processingEvent.getEventType();
        if (ProcessingEvent.BEFORE_PROCESS == eventType || ProcessingEvent.AFTER_PROCESS == eventType) {
            // update the custom form definition
            ModelManager modelManager = new ModelManager(workflowService);
            updateCustomFieldDefinition(processingEvent.getDocument(), modelManager);
        }
    }

    /**
     * This method updates the custom Field Definition based on a given workitem.
     * The method first looks if the model contains a custom definition and stores
     * the data into the field txtWorkflowEditorCustomForm.
     * <p>
     * In case the model does not provide a custom Field Definition but the workitem
     * has stored one the method returns the existing one and did not update the
     * item 'txtWorkflowEditorCustomForm'
     * 
     * @return
     * @throws ModelException
     */
    public String updateCustomFieldDefinition(ItemCollection workitem, ModelManager modelManager)
            throws ModelException {
        String content = fetchFormDefinitionFromModel(workitem, modelManager);
        if (content.isEmpty()) {
            // take the existing one to be returned...
            content = workitem.getItemValueString("txtWorkflowEditorCustomForm");
        } else {
            workitem.replaceItemValue("txtWorkflowEditorCustomForm", content);
        }
        return content;
    }

    /**
     * read the form definition from a dataObject and search for a dataobject with a
     * imixs-form tag. If not matching dataobject is defined then return an empty
     * string.
     * 
     * @param workitem
     * @return
     */
    @SuppressWarnings("unchecked")
    private String fetchFormDefinitionFromModel(ItemCollection workitem, ModelManager modelManager) {

        ItemCollection task;
        try {
            BPMNModel model = modelManager.getModelByWorkitem(workitem);
            task = modelManager.loadTask(workitem, model);

        } catch (ModelException e) {
            logger.warning("unable to parse data object in model: " + e.getMessage());
            return "";
        }

        List<List<String>> dataObjects = task.getItemValue("dataObjects");
        for (List<String> dataObject : dataObjects) {
            // there can be more than one dataOjects be attached.
            // We need the one with the tag <imixs-form>
            String templateName = dataObject.get(0);
            String content = dataObject.get(1);
            // we expect that the content contains at least one occurrence of <imixs-form>
            if (content.contains("<imixs-form>")) {
                logger.finest("......DataObject name=" + templateName);
                logger.finest("......DataObject content=" + content);
                return content;
            } else {
                // seems not to be a imixs-form definition!
            }
        }
        // nothing found!
        return "";
    }

}
