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
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.event.Observes;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.imixs.marty.model.ModelController;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.WorkflowKernel;
import org.imixs.workflow.engine.WorkflowService;
import org.imixs.workflow.exceptions.AccessDeniedException;
import org.imixs.workflow.exceptions.ModelException;
import org.imixs.workflow.exceptions.PluginException;
import org.imixs.workflow.exceptions.ProcessingErrorException;
import org.imixs.workflow.faces.data.WorkflowController;
import org.imixs.workflow.faces.data.WorkflowEvent;

/**
 * This Bean acts as a front controller for child WorkItems to be controlled by
 * the imxis-workflow engine. A child workitem references another workitem. Each
 * WorkItem can have a list of child WorkItems.
 * 
 * The default type of a new child WorkItems is 'workitemchild'. The type can be
 * changed and controlled by the workflow model
 * 
 * @author rsoika
 * 
 */
@Named
@ConversationScoped
public class ChildWorkitemController implements Serializable {

	@Inject
	private Conversation conversation;

	@Inject
	protected ModelController modelController;

	@Inject
	protected WorkflowController workflowController;

	@EJB
	WorkflowService workflowService;
	

	public static Logger logger = Logger.getLogger(ChildWorkitemController.class.getName());

	private static final long serialVersionUID = 1L;

	private List<ItemCollection> childList = null;

	private int sortOrder = 1;

	/**
	 * Sort order for child workitem list
	 * 
	 * @return
	 */
	public int getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}

	/**
	 * Returns the parentWorkitem
	 * 
	 * @return - itemCollection
	 */
	public ItemCollection getParentWorkitem() {
		return workflowController.getWorkitem();
	}

	/**
	 * Override process to reset the child list
	 * 
	 * @throws ModelException
	 */
	
	public String process() throws AccessDeniedException, ProcessingErrorException, PluginException, ModelException {
		String result = workflowController.process();
		return result;
	}

	/**
	 * WorkflowEvent listener to update the child list of the current parent
	 * workitem. The method also updates the uniqueIDRef to identify the parent
	 * workitem
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

		if (WorkflowEvent.WORKITEM_CHANGED == workflowEvent.getEventType()) {
			reset();
			// start now the new conversation
			if (conversation.isTransient()) {
				conversation.setTimeout(
						((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest())
								.getSession().getMaxInactiveInterval() * 1000);
				conversation.begin();
				logger.fine("start new conversation, id=" + conversation.getId());
			}

		}
	}

	/**
	 * this method returns a list of all child workitems for the current workitem.
	 * The workitem list is cached. Subclasses can overwrite the method
	 * loadWorkitems() to return a custom result set.
	 * 
	 * @return - list of file meta data objects
	 */
	public List<ItemCollection> getWorkitems() {
		if (childList == null) {
			childList = loadWorkitems();
		}
		return childList;

	}

	/**
	 * This method loads the list of childWorkitems from the EntityService. The
	 * method can be overwritten by subclasses.
	 * 
	 * @return
	 */
	public List<ItemCollection> loadWorkitems() {
		List<ItemCollection> resultList = new ArrayList<ItemCollection>();

		if (getParentWorkitem() != null) {
			String uniqueIdRef = getParentWorkitem().getItemValueString(WorkflowKernel.UNIQUEID);
			// getWorkListByRef(String aref, String type, int pageSize, int pageIndex, int
			// sortorder) {
			List<ItemCollection> col = workflowService.getWorkListByRef(uniqueIdRef,
					"workitemchild", 0, -1, null, false);
			for (ItemCollection aWorkitem : col) {
				resultList.add(cloneWorkitem(aWorkitem));
			}
		}

		return resultList;

	}

	/**
	 * reset the current childlist
	 */
	
	public void reset() {
		
		childList = null;
	}

	
	


	/**
	 * This method is a placeholder which can be used by a subclass to clone
	 * workitems. In the default behavior of the a workitem is not cloned by the
	 * childworkitem controller
	 * 
	 * @param aWorkitem
	 * @return
	 */
	public ItemCollection cloneWorkitem(ItemCollection aWorkitem) {
		// only a placeholder
		// return WorkitemHelper.clone(aWorkitem);
		return aWorkitem;
	}

}
