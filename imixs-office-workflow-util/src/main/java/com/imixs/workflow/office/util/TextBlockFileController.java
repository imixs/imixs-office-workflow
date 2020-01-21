package com.imixs.workflow.office.util;

import java.io.Serializable;
import java.util.List;

import javax.enterprise.context.ConversationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

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
// @RequestScoped
@ConversationScoped
public class TextBlockFileController implements Serializable {

	@Inject
	FileUploadController fileUploadController;

	@Inject
	DocumentController documentController;

	private static final long serialVersionUID = 1L;
	
	/**
	 * DocumentEvent listener
	 * 
	 * Before a text block is saved we transfere new files!
	 * 
	 * 
	 * 
	 * @param documentEvent
	 */
	public void onWorkflowEvent(@Observes WorkflowEvent documentEvent) {
		if (documentEvent == null)
			return;

		if (WorkflowEvent.DOCUMENT_BEFORE_SAVE == documentEvent.getEventType()) {
			if ("textblock".equals(documentEvent.getWorkitem().getType())) {

				List<FileData> fileList = fileUploadController.getAttachedFiles();// getFileUploads().getFileData();
				if (fileList == null) {
					return;
				} else {
					documentController.getDocument().removeItem("$file");
					for (FileData filedata : fileList) {
						documentController.getDocument().addFileData(filedata);
					}
				}

			}
		}

	}

}
