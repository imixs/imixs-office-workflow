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

package com.imixs.workflow.office.forms;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Observes;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.inject.Inject;
import javax.inject.Named;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.ItemCollectionComparator;
import org.imixs.workflow.exceptions.AccessDeniedException;
import org.imixs.workflow.faces.data.WorkflowController;
import org.imixs.workflow.faces.data.WorkflowEvent;
import org.imixs.workflow.faces.util.LoginController;

/**
 * This Bean acts a a front controller for the DMS feature. The Bean provides
 * additional properties for attached files and can also manage information
 * about file references to external file servers
 * 
 * 
 * The DmsController observes WorkflowEvents and manages the file uploads during
 * the Processing events.
 * 
 * NOTE: if a plug-in adds a new file (like the reportPlugIn), and the plug-in
 * also updates the $file information of the parent WorkItem, then the DMS
 * property will be updated by the DmsController.
 * 
 * 
 * @see org.imixs.workflow.jee.faces.fileupload.FileUploadController
 * @author rsoika
 * 
 */
@Named("dmsController")
@SessionScoped
public class DmsController implements Serializable {

	public final static String DMS_ITEM = "dms";

	@Inject
	protected LoginController loginController = null;

	@Inject
	protected WorkflowController workflowController;

	private static final long serialVersionUID = 1L;

	private List<ItemCollection> dmsList = null;
	private String link = null;

	private static Logger logger = Logger.getLogger(DmsController.class.getName());

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

		if (WorkflowEvent.WORKITEM_BEFORE_PROCESS == eventType) {
			// reconvert the List<ItemCollection> into a List<Map>
			if (dmsList != null) {
				putDmsList(workflowEvent.getWorkitem(), dmsList);
			}
		}

		// if workItem has changed, then update the dms list
		if (WorkflowEvent.WORKITEM_CHANGED == eventType || WorkflowEvent.WORKITEM_AFTER_PROCESS == eventType) {
			// convert dms property into a list of ItemCollection
			dmsList = getDmsListByWorkitem(workflowEvent.getWorkitem());
		}

	}

	/**
	 * This method converts a list of ItemCollections for DMS elements into Map
	 * objects and updates the workitem property 'dms'.
	 * 
	 * The method is used by the DmsController to update dms data provided by the
	 * user.
	 * 
	 * @param workitem - the workitem to be updated
	 * @param dmsList  - the dms metha data to be put into the workitem
	 * @version 1.0
	 */
	@SuppressWarnings("rawtypes")
	public static void putDmsList(ItemCollection workitem, List<ItemCollection> dmsList) {
		// convert the List<ItemCollection> into a List<Map>
		List<Map> vDMSnew = new ArrayList<Map>();
		if (dmsList != null) {
			for (ItemCollection dmsEntry : dmsList) {
				vDMSnew.add(dmsEntry.getAllItems());
			}
		}
		// update the workitem
		workitem.replaceItemValue(DMS_ITEM, vDMSnew);
	}

	/**
	 * this method returns a list of all attached files and the file meta data
	 * provided in a list of ItemCollection.
	 * 
	 * @return - list of file meta data objects
	 */
	public List<ItemCollection> getDmsList() {
		if (dmsList == null)
			dmsList = new ArrayList<ItemCollection>();
		return dmsList;

	}

	/**
	 * This method returns a list of ItemCollections for all DMS elements attached
	 * to the current workitem. The DMS meta data is read from the property 'dms'.
	 * 
	 * The dms property is updated in the run() method of this plug-in.
	 * 
	 * The method is used by the DmsController to display the dms meta data.
	 * 
	 * @param workitem - source of meta data, sorted by $creation
	 * @version 1.0
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List<ItemCollection> getDmsListByWorkitem(ItemCollection workitem) {
		// build a new fileList and test if each file contained in the $files is
		// listed
		List<ItemCollection> dmsList = new ArrayList<ItemCollection>();
		if (workitem == null)
			return dmsList;

		List<Map> vDMS = workitem.getItemValue(DMS_ITEM);
		// first we add all existing dms informations
		for (Map aMetadata : vDMS) {
			dmsList.add(new ItemCollection(aMetadata));
		}

		// sort list by name
		// Collections.sort(dmsList, new ItemCollectionComparator("txtname",
		// true));
		// sort list by $modified
		Collections.sort(dmsList, new ItemCollectionComparator("$created", true));

		return dmsList;
	}

	/**
	 * This method removes a file form the current dms list and also from the
	 * workitem
	 * 
	 * @param aFile
	 */
	public void removeFile(String aFile) {
		// remove file from dms list
		for (ItemCollection aEntry : dmsList) {
			if (aFile.equals(aEntry.getItemValueString("txtname"))) {
				dmsList.remove(aEntry);
				break;
			}
		}

		workflowController.getWorkitem().removeFile(aFile);
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
	 * This method adds a new entry into the dms property. The method returns the
	 * updated DMS List.
	 * 
	 * The method is used by the DMSController to add links.
	 * 
	 * @param aworkitem - the workitem to be updated
	 * @param dmsEntity - the metha data to be added into the dms item
	 * @version 1.0
	 */
	public static List<ItemCollection> addDMSEntry(ItemCollection aworkitem, ItemCollection dmsEntity) {
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

}
