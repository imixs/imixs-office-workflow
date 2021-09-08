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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Observes;
import javax.faces.event.ActionEvent;
import javax.inject.Inject;
import javax.inject.Named;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.WorkflowKernel;
import org.imixs.workflow.engine.DocumentService;
import org.imixs.workflow.faces.data.WorkflowController;
import org.imixs.workflow.faces.data.WorkflowEvent;
import org.imixs.workflow.office.util.WorkitemHelper;

/**
 * The HistoryController provides a history navigation over workItems the user
 * has selected. There for the controller listens to the events of the
 * WokflowController. The history workItems containing only the $uniqueid and a
 * minimum of attributes to display a summary.
 * 
 * @see historynav.xhtml
 * 
 * @author rsoika
 * 
 */

@Named
@SessionScoped
public class HistoryController implements Serializable {

	@Inject
	protected WorkflowController workflowController;

	@EJB
	protected DocumentService documentService;

	private static final long serialVersionUID = 1L;
	private List<ItemCollection> workitems = null;
	private String currentId = null;

	public HistoryController() {
		super();

		workitems = new ArrayList<ItemCollection>();
	}

	public String getCurrentId() {
		return currentId;
	}

	public void setCurrentId(String currentId) {
		this.currentId = currentId;
	}

	/**
	 * retuns a list of all workites curently visited
	 * 
	 * @return
	 */
	public List<ItemCollection> getWorkitems() {
		if (workitems == null)
			workitems = new ArrayList<ItemCollection>();
		return workitems;
	}

	/**
	 * The action method removes a workItem from the history list. If the current
	 * workItem was removed the method switches automatically to the next workItem
	 * in the history list. If the list is empty the method returns the action
	 * 'home'. Otherwise it returns action ''.
	 * 
	 * @param id
	 *            - $UniqueID
	 * @param action
	 *            - default action
	 * @return action
	 * 
	 */
	public String removeWorkitem(String aID) {
		// test if exits
		int iPos = findWorkItem(aID);
		if (iPos > -1) {
			// update history list
			workitems.remove(iPos);
			if (aID.equals(currentId)) {
				// current workItem was removed - so select the next workItem in
				// the list.
				currentId = null;
				if (iPos >= workitems.size())
					iPos--;
				if (iPos >= 0) {
					// select the next
					ItemCollection current = workitems.get(iPos);
					if (current != null) {
						currentId = current.getUniqueID();
						workflowController.setWorkitem(documentService.load(currentId));

					} else {
						// set workflowController to null
						workflowController.setWorkitem(null);
					}
				} else {
					// set workflowController to null
					workflowController.setWorkitem(null);
				}
			}
		}
		return (currentId == null ? "home" : "/pages/workitems/workitem?id=" + currentId + "&faces-redirect=true");

	}

	/**
	 * This action listener removes the current WorkItem from the history navigation
	 * and reset the workitem form the workflowcontroller.
	 * 
	 * @param aWorkitem
	 */
	public void closeCurrentWorkitem(ActionEvent event) {
		int iPos = findWorkItem(currentId);
		if (iPos > -1) {
			workitems.remove(iPos);
		}
		currentId = null;
		workflowController.close();
	}

	/**
	 * WorkflowEvent listener listens to WORKITEM events and adds or removes the
	 * current WorkItem from the history nav.
	 * 
	 * If a WorkItem was processed (WORKITEM_AFTER_PROCESS), and the property
	 * 'action' is 'home' or 'notes' then the WorkItem will be removed from the
	 * history
	 * 
	 * 
	 * If a WorkItem was soft deleted (WORKITEM_AFTER_SOFTDELETE), the WorkItem will
	 * be removed from the history
	 * 
	 * @param workflowEvent
	 * 
	 **/
	public void onWorkflowEvent(@Observes WorkflowEvent workflowEvent) {
		if (workflowEvent == null || workflowEvent.getWorkitem() == null) {
			currentId = null;
			return;
		}

		// skip if not a workItem...
		if (!workflowEvent.getWorkitem().getItemValueString("type").startsWith("workitem"))
			return;

		if (WorkflowEvent.WORKITEM_CHANGED == workflowEvent.getEventType()) {
			addWorkItem(workflowEvent.getWorkitem());
		}

		if (WorkflowEvent.WORKITEM_AFTER_PROCESS == workflowEvent.getEventType()) {

			// if the property 'action' is 'home' or 'notes'
			// then remove the WorkItem and clear the currentID
			String result = workflowEvent.getWorkitem().getItemValueString("action");
			if ("home".equals(result) || "notes".equals(result)) {
				removeWorkitem(workflowEvent.getWorkitem().getItemValueString(WorkflowKernel.UNIQUEID));
				setCurrentId("");
			} else {
				addWorkItem(workflowEvent.getWorkitem());
			}
		}

	}

	/**
	 * This method adds a workItem into the current historyList. If the workItem is
	 * still contained it will be updated.
	 */
	private void addWorkItem(ItemCollection aWorkitem) {

		if (aWorkitem == null || !aWorkitem.getItemValueString("type").startsWith("workitem")
				|| aWorkitem.getUniqueID().isEmpty()) {
			currentId = null;
			return;
		}

		ItemCollection clone = WorkitemHelper.clone(aWorkitem);

		// test if exits
		int iPos = findWorkItem(aWorkitem.getUniqueID());
		if (iPos > -1) {
			// update workitem
			workitems.set(iPos, clone);
		} else {
			// add the new workitem into the history
			workitems.add(clone);
		}
		currentId = clone.getUniqueID();

	}

	/**
	 * This method tests if the WorkItem with the corresponding $UniqueID exists in
	 * the history list and returns the position.
	 * 
	 * @param aID
	 *            - $UniqueID of the workitem
	 * @return - the position in the history list or -1 if not contained.
	 */
	private int findWorkItem(String aID) {
		if (aID == null)
			return -1;
		// try to find the woritem in the history list
		for (int i = 0; i < workitems.size(); i++) {
			ItemCollection historyWorkitem = workitems.get(i);
			String sHistoryUnqiueID = historyWorkitem.getUniqueID();
			if (sHistoryUnqiueID.equals(aID)) {
				// Found! - remove it and return..
				return i;
			}
		}
		return -1;
	}

}
