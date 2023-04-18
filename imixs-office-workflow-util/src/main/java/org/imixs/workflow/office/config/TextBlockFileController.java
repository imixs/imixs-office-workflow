package org.imixs.workflow.office.config;

import java.io.Serializable;
import java.util.List;

import jakarta.enterprise.context.ConversationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.imixs.workflow.FileData;
import org.imixs.workflow.faces.data.DocumentController;
import org.imixs.workflow.faces.data.WorkflowEvent;
import org.imixs.workflow.faces.fileupload.FileUploadController;

/**
 * The TextBlockController is a DocumentController with a file updload feature
 * 
 * @author rsoika
 *
 */
@Named
@ConversationScoped
public class TextBlockFileController implements Serializable {

    @Inject
    FileUploadController fileUploadController;

    @Inject
    DocumentController documentController;

    private static final long serialVersionUID = 1L;

    /**
     * DocumentEvent listener
     * <p>
     * Before a text block is saved we transfer new attached files!
     * 
     * @param documentEvent
     */
    public void onWorkflowEvent(@Observes WorkflowEvent documentEvent) {
        if (documentEvent == null)
            return;

        if (WorkflowEvent.DOCUMENT_BEFORE_SAVE == documentEvent.getEventType()) {
            if ("textblock".equals(documentEvent.getWorkitem().getType())) {
                List<FileData> fileList = fileUploadController.getAttachedFiles();
                // if no new files were uploaded then skip update $file item...
                if (fileList == null || fileList.isEmpty()) {
                    // skip
                    return;
                } else {
                    // add new files
                    for (FileData filedata : fileList) {
                        documentController.getDocument().addFileData(filedata);
                    }
                }
            }
        }

    }

}
