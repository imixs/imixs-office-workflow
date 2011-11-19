package org.imixs.marty.web.office;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import org.imixs.marty.web.workitem.WorkitemListener;
import org.imixs.marty.web.workitem.WorkitemMB;
import org.imixs.workflow.ItemCollection;

/**
 * This bean provides functionallity to add time-sheet entries to a workitem.
 * The Bean works inside the Parent workitem 6000-6099 and the child process
 * 6100-5199
 * 
 * The bean supports only workitems in the 61xxx range - this is currently
 * hard-coded! (see method getProcessFitlerSelection)
 * 
 * @author rsoika
 */
public class TimeSheetMB implements WorkitemListener {
	private WorkitemMB workitemMB = null;
	private ItemCollection filter = null; // search filter
	private ArrayList<SelectItem> processSelection = null;

	private static Logger logger = Logger.getLogger("org.imixs.workflow");

	/**
	 * This method register the bean as an workitemListener
	 */
	@PostConstruct
	public void init() {
		// register this Bean as a workitemListener to the current WorktieMB
		this.getWorkitemBean().addWorkitemListener(this);
	}

	/**
	 * creates a new child with default values
	 * 
	 * @param event
	 * @throws Exception
	 */
	public void doCreate(ActionEvent event) throws Exception {

		this.getWorkitemBean().doCreateChildWorkitem(event);

	}

	public void doEdit(ActionEvent event) throws Exception {
		this.getWorkitemBean().doEditChild(event);
	}
	
	public void doReset(ActionEvent event) throws Exception {
		
	}

	/**
	 * returns the current search filter
	 * 
	 * @return
	 */
	public ItemCollection getFilter() {
		if (filter == null) {
			filter = new ItemCollection();
			
			// set date
			try {
				filter.replaceItemValue("datfrom",new Date());
				filter.replaceItemValue("datto",new Date());
						} catch (Exception e) {
				
				e.printStackTrace();
			}
			
		}
		return filter;
	}

	public void setFilter(ItemCollection filter) {
		this.filter = filter;
	}

	/**
	 * returns a SelctItem Array containing all Process Ids for the current
	 * workflowGroupFilter. The workflowGroupFilter property has the format:
	 * 'offic-de-1.0.0|Purchase'
	 * 
	 * the method searches only for processIDs in the same ModelVerson and Group
	 * 
	 * 
	 * @return a SelectItem array list with the corresponding ProcessIDs (int)
	 */
	public ArrayList<SelectItem> getProcessFilterSelection() {

		if (processSelection != null)
			return processSelection;

		// build new processSelection
		processSelection = new ArrayList<SelectItem>();

		try {
			// find first process entity
			
			ItemCollection startProcess;
			// current model Version
			String sModelVersion= this
					.getWorkitemBean().getWorkitem().getItemValueString("$modelversion");
			startProcess = this
					.getWorkitemBean()
					.getModelService()
					.getProcessEntityByVersion(6100,sModelVersion);

			if (startProcess == null) {
				logger.warning("TimeSheetMB unable to find start ProcessEntity - ID=6100 !");
				return processSelection;
			}

			String sWorkflowGroup = startProcess
					.getItemValueString("txtWorkflowGroup");
			List<ItemCollection> processList = this
					.getWorkitemBean()
					.getModelService()
					.getAllProcessEntitiesByGroupByVersion(sWorkflowGroup,sModelVersion);
			for (ItemCollection process : processList) {
				processSelection.add(new SelectItem(process
						.getItemValueInteger("numprocessid"), process
						.getItemValueString("txtname")));
				
			}
		} catch (Exception e) {
			logger.severe("TimeSheetMB unable - error computing process list");
			e.printStackTrace();
		}
		return processSelection;

	}

	/**
	 * helper method to get the current instance of the WorkitemMB
	 * 
	 * @return
	 */
	private WorkitemMB getWorkitemBean() {
		if (workitemMB == null)
			workitemMB = (WorkitemMB) FacesContext
					.getCurrentInstance()
					.getApplication()
					.getELResolver()
					.getValue(FacesContext.getCurrentInstance().getELContext(),
							null, "workitemMB");
		return workitemMB;
	}

	public void onWorkitemChanged(ItemCollection arg0) {
		// set default date
		// workitem=arg0;
	}

	/**
	 * updates the comment list Therefor the method copies the txtComment into
	 * the txtCommentList and clears the txtComment field
	 */
	public void onWorkitemProcess(ItemCollection workitem) {

	}

	public void onChildProcess(ItemCollection arg0) {
		// nothing
	}

	public void onChildCreated(ItemCollection arg0) {
		// nothing

		try {
			arg0.replaceItemValue("_date", new Date());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void onChildProcessCompleted(ItemCollection arg0) {
		// nothing
	}

	public void onWorkitemCreated(ItemCollection arg0) {
		// nothing
	}

	public void onWorkitemProcessCompleted(ItemCollection arg0) {

	}

	public void onChildDelete(ItemCollection e) {
		// nothing
	}

	public void onChildDeleteCompleted() {
		// nothing
	}

	public void onChildSoftDelete(ItemCollection e) {
		// nothing
	}

	public void onChildSoftDeleteCompleted(ItemCollection e) {
		// nothing
	}

	public void onWorkitemDelete(ItemCollection e) {
		// nothing
	}

	public void onWorkitemDeleteCompleted() {
		// nothing
	}

	public void onWorkitemSoftDelete(ItemCollection e) {

	}

	public void onWorkitemSoftDeleteCompleted(ItemCollection e) {
		// nothing
	}

}