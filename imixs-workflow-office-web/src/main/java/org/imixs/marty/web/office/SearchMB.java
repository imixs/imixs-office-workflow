package org.imixs.marty.web.office;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import org.imixs.marty.business.WorkitemService;
import org.imixs.marty.web.project.ProjectMB;
import org.imixs.marty.web.workitem.WorkitemListener;
import org.imixs.marty.web.workitem.WorkitemMB;
import org.imixs.marty.web.workitem.WorklistMB;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.jee.ejb.ModelService;

/**
 * The minuteMB is a helper backing bean which holds the status of single minute
 * entries in the minute form to manage the display state of editor, history and
 * details. Only one of the child entries of a minute can be toggled once. It is
 * not possible to two different child entry with a toggle on at the same time.
 * <p>
 * Note: If one of the toggles is switched, than all other toggles will be
 * switched off automatically. Property: headerMode (true/false)
 * <p>
 * The bean holds also the status of the header of a minute to indicate if the
 * header should be displayed in edit or in 'document' mode.
 * <p>
 * The overall goal of this toggle switches is to display a hole minute in a
 * document style without any input fields - so the minute should not look like
 * a form but more as a document.
 * 
 * @author rsoika
 * 
 */
public class SearchMB implements WorkitemListener {
	private WorkitemMB workitemMB = null;
	private WorklistMB worklistMB = null;
	private ProjectMB projectMB = null;
	private ItemCollection searchFilter;

	private ArrayList<SelectItem> processSelection = null;
	private ArrayList<ItemCollection> worklist = null;
	private ArrayList<ItemCollection> updatelist = null;

	private int updatelistRow = 0;
	private boolean endOfUpdatelist = false;
	private int worklistRow = 0;
	private boolean endOfWorklist = false;
	private int count = 5;

	/* Model Service */
	@EJB
	ModelService modelService;

	@EJB
	WorkitemService workitemService;

	/**
	 * This method tries to load the config entity to read default values
	 */
	@PostConstruct
	public void init() {
		// register this Bean as a workitemListener to the current WorktieMB
		this.getWorkitemBean().addWorkitemListener(this);

		searchFilter = new ItemCollection();
		
		// now try to initialize the current workitem state
		onWorkitemChanged(this.getWorkitemBean().getWorkitem());

	}

	/**
	 * helper method to get the current instance of the WorkitemMB
	 * 
	 * @return
	 */
	private WorkitemMB getWorkitemBean() {
		if (workitemMB == null)
			workitemMB = (WorkitemMB) FacesContext.getCurrentInstance()
					.getApplication().getELResolver().getValue(
							FacesContext.getCurrentInstance().getELContext(),
							null, "workitemMB");
		return workitemMB;
	}

	private WorklistMB getWorkListBean() {
		if (worklistMB == null)
			worklistMB = (WorklistMB) FacesContext.getCurrentInstance()
					.getApplication().getELResolver().getValue(
							FacesContext.getCurrentInstance().getELContext(),
							null, "worklistMB");
		return worklistMB;
	}

	public ItemCollection getSearchFilter() {
		return searchFilter;
	}

	/**
	 * This Method Selects the current project and refreshes the Worklist Bean
	 * so wokitems of these project will be displayed after show_worklist
	 * 
	 * @return
	 * @throws Exception
	 */
	public void doSwitchToProject(ActionEvent event) throws Exception {

		// reset search filter settings and current project setting
		this.doClear(event);

		ItemCollection currentSelection = null;
		// suche selektierte Zeile....
		UIComponent component = event.getComponent();

		for (UIComponent parent = component.getParent(); parent != null; parent = parent
				.getParent()) {

			if (!(parent instanceof UIData))
				continue;
			// get current project from row
			currentSelection = (ItemCollection) ((UIData) parent)
					.getRowData();

			this.getWorkListBean().doSwitchToWorklistSearch(event);
			getProjectBean().setWorkitem(currentSelection);
			break;
		}

		// prepare search statement
		this.doSearch(event);

	}

	/**
	 * This Method resets the search result and build a new JPQL Statement
	 * 
	 * @param event
	 * @throws Exception
	 */
	public void doSearch(ActionEvent event) {
		String sProjectRef = null;
		String sWorkflowGroup = null;
		String sProcessID = null;
		String sModel = null;
		String sSearch = null;
		try {
			sProjectRef = getProjectBean().getWorkitem().getItemValueString(
					"$uniqueid");

			sWorkflowGroup = this.searchFilter
					.getItemValueString("txtWorkflowGroup");
			if ("-".equals(sWorkflowGroup))
				sWorkflowGroup = "";

			// reset processid?
			if ("".equals(sWorkflowGroup))
				this.searchFilter.replaceItemValue("txtProcessID", "");

			sProcessID = this.searchFilter.getItemValueString("txtProcessID");

			if ("-".equals(sProcessID))
				sProcessID = "";
			System.out.println("Process=" + sProcessID);

			// search phrase
			sSearch = this.searchFilter.getItemValueString("txtSearch");

			String sFilter = "SELECT wi FROM Entity AS wi ";

			if (!"".equals(sProjectRef))
				sFilter += " JOIN wi.textItems AS project ";

			if (!"".equals(sWorkflowGroup)) {
				sModel = sWorkflowGroup.substring(0, sWorkflowGroup
						.indexOf("|") - 0);
				sWorkflowGroup = sWorkflowGroup.substring(sWorkflowGroup
						.indexOf("|") + 1);
				sFilter += " JOIN wi.textItems AS gruppe ";
				sFilter += " JOIN wi.textItems AS model ";

			}

			if (!"".equals(sSearch))
				sFilter += " JOIN wi.textItems AS searchphrase ";

			if (!"".equals(sProcessID))
				sFilter += " JOIN wi.integerItems AS status ";

			sFilter += " WHERE wi.type= 'workitem' ";

			// Projekt Referenz
			if (!"".equals(sProjectRef))
				sFilter += " AND project.itemName = '$uniqueidref' AND project.itemValue='"
						+ sProjectRef + "' ";

			// Grupp und Model
			if (!"".equals(sWorkflowGroup)) {
				sFilter += " AND model.itemName = '$modelversion' AND model.itemValue ='"
						+ sModel + "'";
				sFilter += " AND gruppe.itemName = 'txtworkflowgroup' AND gruppe.itemValue ='"
						+ sWorkflowGroup + "'";
			}

			// Process ID
			if (!"".equals(sProcessID)) {
				String sID = sProcessID.substring(sProcessID.indexOf("|") + 1);
				sFilter += " AND status.itemName = '$processid' AND status.itemValue ='"
						+ sID + "'";

			}

			// Searchphrase
			if (!"".equals(sSearch)) {
				sSearch = sSearch.replace("*", "%");

				sFilter += " AND searchphrase.itemName = 'txtworkflowsummary'"
						+ " AND searchphrase.itemValue LIKE '%" + sSearch
						+ "%'";

			}

			sFilter += " ORDER BY wi.modified desc";

			System.out.println("sFilter=" + sFilter);
			this.getWorkListBean().setSearchQuery(sFilter);
		} catch (Exception e) {
			System.out.println("WARNING - error doRestet SearchView");
			e.printStackTrace();
		}

		this.getWorkListBean().doSwitchToWorklistSearch(event);

		// reset portlet lists
		updatelist = null;
		worklist = null;
	}

	/**
	 * This method clears the current JPQL Statement. THis is used by the search
	 * form to represent an empty result set at the beginning of a new search
	 * 
	 * @param event
	 */
	public void doClear(ActionEvent event) {
		// reset project selection
		getProjectBean().setWorkitem(null);
		// reset filter settings
		searchFilter = new ItemCollection();
	
		this.getWorkListBean().doSwitchToWorklistSearch(event);
		this.getWorkListBean().setSearchQuery(null);
		updatelist = null;
		worklist = null;
		
		updatelistRow = 0;
		worklistRow = 0;
	}

	public ProjectMB getProjectBean() {
		if (projectMB == null)
			projectMB = (ProjectMB) FacesContext.getCurrentInstance()
					.getApplication().getELResolver().getValue(
							FacesContext.getCurrentInstance().getELContext(),
							null, "projectMB");
		return projectMB;
	}

	/**
	 * returns a SelctItem Array containing all Process Ids for the current
	 * selected workflowGroup. The method expects a property 'txtWorkflowGroup'
	 * with a value like 'offic-de-1.0.0|Purchase'
	 * 
	 * the method searches for processID in the same ModelVerson/Group
	 * comppination
	 * 
	 * @return
	 */
	public ArrayList<SelectItem> getProcessListByGroup() {
		String sWorkflowGroup = null;
		String sModel = null;
		// build new groupSelection
		processSelection = new ArrayList<SelectItem>();

		sWorkflowGroup = this.searchFilter
				.getItemValueString("txtWorkflowGroup");
		if ("-".equals(sWorkflowGroup))
			sWorkflowGroup = "";
		if (!"".equals(sWorkflowGroup)) {
			// cut model and version
			sModel = sWorkflowGroup.substring(0,
					sWorkflowGroup.indexOf("|") - 0);
			sWorkflowGroup = sWorkflowGroup.substring(sWorkflowGroup
					.indexOf("|") + 1);

			List<ItemCollection> processList = modelService
					.getAllProcessEntitiesByGroup(sWorkflowGroup);
			for (ItemCollection process : processList) {
				String sModelVersion = process
						.getItemValueString("$modelVersion");
				if (sModel.equals(sModelVersion)) {
					String sValue = sModelVersion + "|"
							+ process.getItemValueInteger("numprocessid");
					processSelection.add(new SelectItem(sValue, process
							.getItemValueString("txtname")));
				}
			}

		}
		return processSelection;

	}

	public List<ItemCollection> getWorklist() { 
		Collection<ItemCollection> col = null;
		if (worklist == null) {
			worklist = new ArrayList<ItemCollection>();
			col = workitemService.findWorkitemsByOwner(null, null, worklistRow,
					count, WorkitemService.SORT_BY_MODIFIED,
					WorkitemService.SORT_ORDER_DESC);

			for (ItemCollection aworkitem : col) {
				worklist.add((aworkitem));
			}

			endOfWorklist = col.size() < count;
		}
		return worklist;
	}

	/**
	 * returns a list of recently updated workitems editable by the author
	 * 
	 * @return
	 */
	public List<ItemCollection> getAuthorlist() {
		Collection<ItemCollection> col = null;
		if (updatelist == null) {
			updatelist = new ArrayList<ItemCollection>();
			col = workitemService.findWorkitemsByAuthor(null, null,
					updatelistRow, count, WorkitemService.SORT_BY_MODIFIED,
					WorkitemService.SORT_ORDER_DESC);

			for (ItemCollection aworkitem : col) {
				updatelist.add((aworkitem));
			}

			endOfUpdatelist = col.size() < count;
		}
		return updatelist;
	}
	
	/**
	 * returns a list of all recently updated workitems 
	 * 
	 * @return
	 */
	public List<ItemCollection> getReaderlist() {
		Collection<ItemCollection> col = null;
		if (updatelist == null) {
			updatelist = new ArrayList<ItemCollection>();
			col = workitemService.findAllWorkitems(null, null,
					updatelistRow, count, WorkitemService.SORT_BY_MODIFIED,
					WorkitemService.SORT_ORDER_DESC);

			for (ItemCollection aworkitem : col) {
				updatelist.add((aworkitem));
			}

			endOfUpdatelist = col.size() < count;
		}
		return updatelist;
	}

	/**
	 * returns a list of recently updated workitems created by the current editor
	 * 
	 * @return
	 */
	public List<ItemCollection> getCreatorlist() {
		Collection<ItemCollection> col = null;
		if (updatelist == null) {
			updatelist = new ArrayList<ItemCollection>();
			col = workitemService.findWorkitemsByCreator(null, null,
					updatelistRow, count, WorkitemService.SORT_BY_MODIFIED,
					WorkitemService.SORT_ORDER_DESC);

			for (ItemCollection aworkitem : col) {
				updatelist.add((aworkitem));
			}

			endOfUpdatelist = col.size() < count;
		}
		return updatelist;
	}
	
	
	/**
	 * Navigation
	 */
	public void doUpdatelistLoadNext(ActionEvent event) {
		updatelistRow = updatelistRow + count;
		updatelist = null;
	}

	public void doUpdatelistLoadPrev(ActionEvent event) {
		updatelistRow = updatelistRow - count;
		if (updatelistRow < 0)
			updatelistRow = 0;
		updatelist = null;
	}

	public int getUpdatelistRow() {
		return updatelistRow;
	}

	public boolean isEndOfUpdatelist() {
		return endOfUpdatelist;
	}

	public void doWorklistLoadNext(ActionEvent event) {
		worklistRow = worklistRow + count;
		worklist = null;
	}

	public void doWorklistLoadPrev(ActionEvent event) {
		worklistRow = worklistRow - count;
		if (worklistRow < 0)
			worklistRow = 0;
		worklist = null;
	}

	public int getWorklistRow() {
		return worklistRow;
	}

	public boolean isEndOfWorklist() {
		return endOfWorklist;
	}

	
	public void onWorkitemChanged(ItemCollection arg0) {

	}

	public void onWorkitemProcess(ItemCollection e) {

	}

	public void onChildProcess(ItemCollection arg0) {
		// nothing
	}

	public void onChildCreated(ItemCollection arg0) {
		// nothing

	}

	public void onChildProcessCompleted(ItemCollection arg0) {
		// nothing
	}

	public void onWorkitemCreated(ItemCollection arg0) {
		// nothing
	}

	public void onWorkitemProcessCompleted(ItemCollection arg0) {
		this.doSearch(null);
	}

	public void onChildDelete(ItemCollection e) {
		// TODO Auto-generated method stub

	}

	public void onChildDeleteCompleted() {
		// TODO Auto-generated method stub

	}

	public void onChildSoftDelete(ItemCollection e) {
		// TODO Auto-generated method stub

	}

	public void onChildSoftDeleteCompleted(ItemCollection e) {
		// TODO Auto-generated method stub

	}

	public void onWorkitemDelete(ItemCollection e) {
		// TODO Auto-generated method stub

	}

	public void onWorkitemDeleteCompleted() {
		this.doSearch(null);
	}

	public void onWorkitemSoftDelete(ItemCollection e) {
		// TODO Auto-generated method stub

	}

	public void onWorkitemSoftDeleteCompleted(ItemCollection e) {
		// TODO Auto-generated method stub

	}

}