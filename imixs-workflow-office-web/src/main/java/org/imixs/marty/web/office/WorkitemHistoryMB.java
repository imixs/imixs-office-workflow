package org.imixs.marty.web.office;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.component.UIParameter;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import org.imixs.marty.web.workitem.WorkitemListener;
import org.imixs.marty.web.workitem.WorkitemMB;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.plugins.jee.extended.LucenePlugin;

/**
 * This ManagedBean supports Histoy navigation over workitems the user has
 * selected
 * 
 * @see historynav.xhtml
 * 
 * @author rsoika
 * 
 */
public class WorkitemHistoryMB implements WorkitemListener {

	private static Logger logger = Logger.getLogger("org.imixs.workflow");

	// Workflow Manager
	@EJB
	org.imixs.workflow.jee.ejb.WorkflowService workflowService;

	private WorkitemMB workitemMB = null;
	private List<ItemCollection> workitems = null;
	private String lastRemoveActionResult = null;

	public WorkitemHistoryMB() {
		super();

	}

	/**
	 * This method register the bean as an workitemListener
	 */
	@PostConstruct
	public void init() {
		// register this Bean as a workitemListener to the current WorktieMB
		this.getWorkitemBean().addWorkitemListener(this);
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
	 * selects the workitem from the history list. A parameter 'id' with the
	 * $uniqueid is expected
	 * 
	 * @param event
	 * @return
	 * @throws Exception
	 */
	public void doSwitchToWorkitem(ActionEvent event) throws Exception {
		// Activity ID raussuchen und in activityID speichern
		List children = event.getComponent().getChildren();
		String aID = null;

		for (int i = 0; i < children.size(); i++) {
			if (children.get(i) instanceof UIParameter) {
				UIParameter currentParam = (UIParameter) children.get(i);
				if (currentParam.getName().equals("id")
						&& currentParam.getValue() != null) {
					aID = currentParam.getValue().toString();
					break;
				}
			}
		}

		if (aID != null) {
			ItemCollection itemCol = workflowService.getWorkItem(aID);
			getWorkitemBean().setWorkitem(itemCol);
		}

	}

	/**
	 * this method removes the workitem from the history list A parameter 'id'
	 * with the $uniqueid is expected
	 * 
	 * @param event
	 * @return
	 * @throws Exception
	 */
	public void doRemoveWorkitem(ActionEvent event) throws Exception {
		// Activity ID raussuchen und in activityID speichern
		List children = event.getComponent().getChildren();
		String aID = null;

		for (int i = 0; i < children.size(); i++) {
			if (children.get(i) instanceof UIParameter) {
				UIParameter currentParam = (UIParameter) children.get(i);
				if (currentParam.getName().equals("id")
						&& currentParam.getValue() != null) {
					aID = currentParam.getValue().toString();
					break;
				}
			}
		}

		// try to find the woritem in the history list
		if (aID != null) {
			// test if still stored?
			for (ItemCollection historyWorkitem : workitems) {
				String sHistoryUnqiueID = historyWorkitem
						.getItemValueString("$Uniqueid");
				if (sHistoryUnqiueID.equals(aID)) {
					// Found! - remove it and return..
					workitems.remove(historyWorkitem);
					break;
				}
			}

		}

		// now lets compute the next action result...
		if (getWorkitemBean().getWorkitem() != null
				&& getWorkitemBean().getWorkitem()
						.getItemValueString("$uniqueid").equals(aID))

			lastRemoveActionResult = "home";
		else
			lastRemoveActionResult = "open_workitem";

	}

	/**
	 * this action method returns the action 'home' if the current workitem is
	 * closed. Otherwise the method returns 'open_workitem' The method is used
	 * in conjunction with doRemoveWorkitem.
	 * This action method is only used by the page historynav_workitem.xhtml!
	 * 
	 * @param event
	 * @return
	 * @throws Exception
	 */
	public String getRemoveWorkitemAction() {		        
		return lastRemoveActionResult;

	}

	public WorkitemMB getWorkitemBean() {
		if (workitemMB == null)
			workitemMB = (WorkitemMB) FacesContext
					.getCurrentInstance()
					.getApplication()
					.getELResolver()
					.getValue(FacesContext.getCurrentInstance().getELContext(),
							null, "workitemMB");
		return workitemMB;
	}

	public void onWorkitemCreated(ItemCollection e) {
	}

	/**
	 * this method adds the workitem into the history list if not yet available.
	 * The method only stores the really necessary properties
	 */
	public void onWorkitemChanged(ItemCollection aworkitem) {

		String sID = aworkitem.getItemValueString("$Uniqueid");
		if ("".equals(sID))
			return;

		// test if still stored?
		for (ItemCollection historyWorkitem : workitems) {
			String sHistoryUnqiueID = historyWorkitem
					.getItemValueString("$Uniqueid");
			if (sHistoryUnqiueID.equals(sID))
				// still stored
				return;
		}
		// now add this new workitem in a short version to the history
		try {
			ItemCollection newItemCol = new ItemCollection();
			newItemCol.replaceItemValue("$Uniqueid", sID);
			newItemCol.replaceItemValue("txtWorkflowSummary",
					aworkitem.getItemValue("txtWorkflowSummary"));

			workitems.add(newItemCol);
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	public void onWorkitemProcess(ItemCollection e) {
	}

	public void onWorkitemProcessCompleted(ItemCollection e) {

	}

	public void onWorkitemDelete(ItemCollection e) {
	}

	public void onWorkitemDeleteCompleted() {
	}

	public void onWorkitemSoftDelete(ItemCollection e) {
	}

	public void onWorkitemSoftDeleteCompleted(ItemCollection e) {
	}

	public void onChildProcess(ItemCollection e) {
	}

	public void onChildProcessCompleted(ItemCollection e) {
	}

	public void onChildCreated(ItemCollection e) {
	}

	public void onChildDelete(ItemCollection e) {
	}

	public void onChildDeleteCompleted() {
	}

	public void onChildSoftDelete(ItemCollection e) {
	}

	public void onChildSoftDeleteCompleted(ItemCollection e) {
	}

}
