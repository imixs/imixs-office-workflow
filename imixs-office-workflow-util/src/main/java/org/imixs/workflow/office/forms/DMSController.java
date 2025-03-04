/*******************************************************************************
 *  Imixs Workflow 
 *  Copyright (C) 2001, 2011 Imixs Software Solutions GmbH,  
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
 *  Project: 
 *  	http://www.imixs.org
 *  	http://java.net/projects/imixs-workflow
 *  
 *  Contributors:  
 *  	Imixs Software Solutions GmbH - initial API and implementation
 *  	Ralph Soika - Software Developer
 *******************************************************************************/

package org.imixs.workflow.office.forms;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.imixs.workflow.FileData;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.ItemCollectionComparator;
import org.imixs.workflow.engine.DocumentService;
import org.imixs.workflow.exceptions.AccessDeniedException;
import org.imixs.workflow.faces.data.WorkflowController;
import org.imixs.workflow.faces.data.WorkflowEvent;
import org.imixs.workflow.faces.util.LoginController;

import jakarta.enterprise.context.ConversationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * This Bean acts a a front controller for the DMS feature. The Bean provides
 * additional properties for attached files and can also manage information
 * about file references to external file servers
 * 
 * @see org.imixs.workflow.jee.faces.fileupload.FileUploadController
 * @author rsoika
 * 
 */
@Named("dmsController")
@ConversationScoped
public class DMSController implements Serializable {

	public static final String REGEX_URL_PATTERN = "^(http|https|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";

	@Inject
	protected LoginController loginController = null;

	@Inject
	protected WorkflowController workflowController;

	@Inject
	protected DocumentService documentService;

	private static final long serialVersionUID = 1L;

	private List<ItemCollection> dmsList = null;

	private String link = null;

	private Map<String, List<ItemCollection>> dmsListCache = null;

	private static Logger logger = Logger.getLogger(DMSController.class.getName());

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	/**
	 * WorkflowEvent listener to update the DMS property if a WorkItem has changed,
	 * processed or saved.
	 * 
	 * The read and write access for a BlobWorkitem will be updated by the
	 * org.imixs.marty.plugins.BlobPlugin.
	 * 
	 * The DMSController also updates the file Properties after a workItem was
	 * processed. This is because a plug-in can add a new file (like the
	 * reportPlugIn), and so the plug-in also updates the $file information of the
	 * parent WorkItem. For that reason the DMS property will be updated by the
	 * DmsController on the WorkflowEvent WORKITEM_AFTER_PROCESS
	 * 
	 * @param workflowEvent
	 * @throws AccessDeniedException
	 */
	public void onWorkflowEvent(@Observes WorkflowEvent workflowEvent) throws AccessDeniedException {
		if (workflowEvent == null)
			return;

		// skip if not a workItem...
		if (workflowEvent.getWorkitem() != null
				&& !workflowEvent.getWorkitem().getItemValueString("type").startsWith("workitem"))
			return;

		int eventType = workflowEvent.getEventType();

		if (WorkflowEvent.WORKITEM_CREATED == eventType || WorkflowEvent.WORKITEM_CHANGED == eventType) {
			// reset DMS List..
			reset();
		}

		if (WorkflowEvent.WORKITEM_BEFORE_PROCESS == eventType) {
			// reconvert the List<ItemCollection> into a List<Map>
			if (dmsList != null) {
				putDmsList(workflowEvent.getWorkitem(), dmsList);
			}
		}

	}

	/**
	 * This method updates the attributes of the FileData objects stored in the
	 * current workitem.
	 * 
	 * @param workitem
	 *                 - the workitem to be updated
	 * @param dmsList
	 *                 - the dms metha data to be put into the workitem
	 * @version 1.0
	 */
	private void putDmsList(ItemCollection workitem, List<ItemCollection> dmsList) {
		for (ItemCollection dmsEntry : dmsList) {
			String filename = dmsEntry.getItemValueString("txtname");
			if (filename.isEmpty()) {
				logger.warning("Invalid DMS entry: txtname is empty!");
				return;
			}
			FileData fileData = workitem.getFileData(filename);
			if (fileData != null) {
				fileData.setAttributes(dmsEntry.getAllItems());
				workitem.addFileData(fileData);
			} else {
				// if no file data object was found this could be a Link Data object.....
				if (filename.matches(REGEX_URL_PATTERN)) {
					byte[] empty = {};
					FileData linkData = new FileData(filename, empty, null, dmsEntry.getAllItems());
					workitem.addFileData(linkData);
				} else {
					logger.warning("Invalid DMS entry: '" + filename + "' is unknown!");
				}
			}
		}
	}

	/**
	 * this method returns a list of all attached files and the file meta data
	 * provided in a list of ItemCollection.
	 * 
	 * @return - list of file meta data objects
	 */
	public List<ItemCollection> getDmsList() {
		if (dmsList == null) {
			dmsList = getDmsListByWorkitem(workflowController.getWorkitem());
		}

		// dmsList = new ArrayList<ItemCollection>();
		return dmsList;

	}

	/**
	 * This method returns a list of ItemCollections for all custom attributes of
	 * attached files.
	 * <p>
	 * The method is used by the DmsController to display the dms meta data.
	 * <p>
	 * The method caches the data result internally to avoid multiple calls because
	 * of JSF behavior
	 * (https://stackoverflow.com/questions/2090033/why-jsf-calls-getters-multiple-times/2090062)
	 * 
	 * @param workitem
	 *                 - source of meta data, sorted by $creation
	 * @version 1.0
	 */
	@SuppressWarnings("unchecked")
	public List<ItemCollection> getDmsListByWorkitem(final ItemCollection workitem) {

		if (dmsListCache == null) {
			dmsListCache = new HashMap<String, List<ItemCollection>>();
		}

		// if workitem is not defined or has no UniqueID then return an empty list
		if (workitem == null || workitem.getUniqueID().isEmpty()) {
			return new ArrayList<ItemCollection>();
		}

		// check cache....
		List<ItemCollection> _dmsList = dmsListCache.get(workitem.getUniqueID());
		if (_dmsList != null) {
			return _dmsList;
		}

		// build a new dms List
		_dmsList = new ArrayList<ItemCollection>();

		ItemCollection _workitem = workitem;
		// we need to load the full workitem, because we can not get the filedata from a
		// stub
		if (!_workitem.hasItem("$file")) {
			_workitem = documentService.load(workitem.getUniqueID());
			logger.finest("......loaded full data for " + workitem.getUniqueID());
		}
		List<FileData> files = _workitem.getFileData();
		for (FileData fileData : files) {

			// Issue #509
			// fix format of deprecated dms item
			// in some cases we have no attributes (old workitems)
			if (fileData.getAttributes().get("txtname") == null) {
				// create attributes on demand
				List<Object> values = new ArrayList<Object>();
				values.add(fileData.getName());
				fileData.setAttribute("txtname", values);
				// create dummy date
				fileData.setAttribute("$created", workitem.getItemValue("$created"));
			}

			ItemCollection _dmsItemCol = new ItemCollection(fileData.getAttributes());
			// add new item names (txtname will be deprecated)
			_dmsItemCol.setItemValue("name", _dmsItemCol.getItemValueString("txtname"));

			// add encoded filename
			try {
				String encodedName = URLEncoder.encode(_dmsItemCol.getItemValueString("name"), "UTF-8");
				_dmsItemCol.setItemValue("name.encoded", encodedName);
			} catch (UnsupportedEncodingException e) {
				logger.warning("unable to URL encode the filename '" + fileData.getAttribute("name") + "'!");
			}
			_dmsItemCol.setItemValue("contentType", fileData.getContentType());
			_dmsList.add(_dmsItemCol);
		}

		// sort list by $created
		Collections.sort(_dmsList, new ItemCollectionComparator("$created", true));

		dmsListCache.put(workitem.getUniqueID(), _dmsList);
		return _dmsList;
	}

	/**
	 * This method removes a file form the current dms list and also from the
	 * workitem
	 * 
	 * @param aFile
	 */
	public void removeFile(String aFile) {
		// remove file from dms list
		workflowController.getWorkitem().removeFile(aFile);
		reset();

	}

	/**
	 * Returns true if the given file name is no longer part of the worktiem
	 * Used by the chronicle component
	 **/
	public boolean isRemoved(String file) {
		return !workflowController.getWorkitem().getFileNames().contains(file);
	}

	/**
	 * Reset the current dms list
	 */
	public void reset() {
		dmsList = null;
		dmsListCache = null;
	}

	/**
	 * This Method adds a new Link (url) into the DMS list.
	 * 
	 * @param event
	 */
	public void addLink(ActionEvent event) {
		String sLink = getLink();

		if (sLink != null && !"".equals(sLink)) {

			// test for protocoll
			if (!sLink.contains("://"))
				sLink = "http://" + sLink;

			FacesContext context = FacesContext.getCurrentInstance();
			ExternalContext externalContext = context.getExternalContext();
			String remoteUser = externalContext.getRemoteUser();

			ItemCollection dmsEntry = new ItemCollection();
			dmsEntry.replaceItemValue("url", sLink);

			dmsEntry.replaceItemValue("$created", new Date());
			dmsEntry.replaceItemValue("$modified", new Date());
			dmsEntry.replaceItemValue("namCreator", remoteUser);
			dmsEntry.replaceItemValue("$Creator", remoteUser);
			dmsEntry.replaceItemValue("txtName", sLink);

			dmsList = addDMSEntry(workflowController.getWorkitem(), dmsEntry);

			// clear link
			setLink("");

		}

	}

	/**
	 * This method adds a new entry and returns the updated DMS List.
	 * <p>
	 * The method is used by the DMSController to add links.
	 * 
	 * @param aworkitem
	 *                  - the workitem to be updated
	 * @param dmsEntity
	 *                  - the metha data to be added into the dms item
	 * @version 1.0
	 */
	private List<ItemCollection> addDMSEntry(ItemCollection aworkitem, ItemCollection dmsEntity) {
		List<ItemCollection> dmsList = getDmsListByWorkitem(aworkitem);
		String sNewName = dmsEntity.getItemValueString("txtName");
		String sNewUrl = dmsEntity.getItemValueString("url");

		// test if the entry already exists - than overwrite it....
		for (Iterator<ItemCollection> iterator = dmsList.iterator(); iterator.hasNext();) {
			ItemCollection admsEntry = iterator.next();
			String sName = admsEntry.getItemValueString("txtName");
			String sURL = admsEntry.getItemValueString("url");
			if (sURL.endsWith(sNewUrl) && sName.equals(sNewName)) {
				// Remove the current element from the iterator and the list.
				iterator.remove();
				logger.fine("remove dms entry '" + sName + "'");
			}
		}
		dmsList.add(dmsEntity);
		putDmsList(aworkitem, dmsList);

		return dmsList;
	}

	/**
	 * Computes the file size into a user friendly format
	 * 
	 * @param size
	 * @return
	 */
	public String userFriendlyBytes(int bytes) {
		boolean si = true;
		int unit = si ? 1000 : 1024;
		if (bytes < unit)
			return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}

	public String documentType(String name) {
		name = name.toLowerCase();
		if (name.endsWith(".doc") || name.endsWith(".docx") || name.endsWith(".xls") || name.endsWith(".xlsx")) {
			return "win";
		}

		if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".gif") || name.endsWith(".tif")
				|| name.endsWith(".tiff")) {
			return "pic";
		}

		if (name.endsWith(".pdf")) {
			return "pdf";
		}

		if (name.endsWith(".eml")) {
			return "eml";
		}

		return "doc";
	}

}
