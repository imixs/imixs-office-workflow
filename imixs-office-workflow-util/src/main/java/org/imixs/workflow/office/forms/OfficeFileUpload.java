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
package org.imixs.workflow.office.forms;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.imixs.workflow.FileData;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.exceptions.AccessDeniedException;
import org.imixs.workflow.faces.data.WorkflowController;
import org.imixs.workflow.faces.data.WorkflowEvent;

import jakarta.enterprise.context.ConversationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.Part;

/**
 * The CDI ChildItemController provides methods to display and edit sub items in
 * a sub-form. The controller provides methods to add or remove a childItem.
 * 
 * Each childItem is represented internally as a HashMap. To the front-end the
 * ChildItemController provides a list of ItemCollections
 * 
 * This CDI Controller can be used to provide different sub-views. The property
 * '_ChildItems' of the current WorkItem hold the items.
 * 
 *
 * 
 * @author rsoika
 * @version 1.0
 */
@Named
@ConversationScoped
public class OfficeFileUpload implements Serializable {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(OfficeFileUpload.class.getName());

	private List<Part> files; // +getter +setter

	@Inject
	WorkflowController workflowController;

	public List<Part> getFiles() {
		return files;
	}

	public void setFiles(List<Part> files) {
		this.files = files;
	}

	/**
	 * WorkflowEvent listener to convert embeded HashMaps into ItemCollections and
	 * reconvert them before processing
	 * 
	 * @param workflowEvent
	 * @throws AccessDeniedException
	 */
	public void onWorkflowEvent(@Observes WorkflowEvent workflowEvent) throws AccessDeniedException {

		int eventType = workflowEvent.getEventType();
		ItemCollection workitem = workflowEvent.getWorkitem();
		if (workitem == null || workflowController == null) {
			return;
		}

		// before the workitem is saved we update the field txtOrderItems
		if (WorkflowEvent.WORKITEM_BEFORE_PROCESS == eventType) {
			logger.log(Level.INFO, "uploaded file size:{0}", files.size());
			for (Part part : files) {
				String submittedFilename = part.getSubmittedFileName();
				String name = Paths.get(part.getSubmittedFileName()).getFileName().toString();
				long size = part.getSize();
				String contentType = part.getContentType();
				logger.log(Level.INFO, "uploaded file: submitted filename: {0}, name:{1}, size:{2}, content type: {3}",
						new Object[] {
								submittedFilename, name, size, contentType
						});

				part.getHeaderNames()
						.forEach(headerName -> logger.log(Level.INFO, "header name: {0}, value: {1}", new Object[] {
								headerName, part.getHeader(headerName)
						}));

				try {
					InputStream inputStream = part.getInputStream();
					final byte[] bytes;
					try (inputStream) {
						bytes = inputStream.readAllBytes();
					}

					FileData filedata = new FileData(name, bytes, contentType, null);
					workflowController.getWorkitem().addFileData(filedata);

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}

	}

}
