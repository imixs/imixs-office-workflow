package org.imixs.workflow.office.forms;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import org.imixs.marty.plugins.MinutePlugin;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.ItemCollectionComparator;
import org.imixs.workflow.WorkflowKernel;
import org.imixs.workflow.engine.DocumentService;
import org.imixs.workflow.engine.WorkflowService;
import org.imixs.workflow.exceptions.AccessDeniedException;
import org.imixs.workflow.exceptions.ModelException;
import org.imixs.workflow.exceptions.PluginException;
import org.imixs.workflow.exceptions.QueryException;
import org.imixs.workflow.faces.data.WorkflowEvent;

/**
 * The MinuteController is a session scoped backing bean controlling a list of
 * minute workitems assigned to a parent workitem. This controller can be used
 * for different kind of Minute-Workflows.
 * 
 * The method toggleWorkitem can be used to open/closed embedded editors:
 * 
 * 
 * 
 * @author rsoika
 */
@Named
@ConversationScoped
public class MinuteController implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	Event<WorkflowEvent> events;

	private List<ItemCollection> minutes = null;

	private ItemCollection workitem = null;

	private ItemCollection parentWorkitem = null; // parent minute

	private static Logger logger = Logger.getLogger(MinuteController.class.getName());

	protected FormController formController = null;

	private FormDefinition formDefinition = null;

	@EJB
	DocumentService documentService;

	@EJB
	WorkflowService workflowService;

	/**
	 * Here we initialize the formController for the minute workitem
	 */
	public MinuteController() {
		super();
		// initialize formControlller
		formController = new FormController();
	}

	/**
	 * WorkflowEvent listener to set the current parentWorkitem.
	 * 
	 * @param workflowEvent
	 * @throws AccessDeniedException
	 */
	public void onWorkflowEvent(@Observes WorkflowEvent workflowEvent) throws AccessDeniedException {
		if (workflowEvent == null || workflowEvent.getWorkitem() == null)
			return;

		// skip if not a workItem...
		if (workflowEvent.getWorkitem() != null
				&& !workflowEvent.getWorkitem().getItemValueString("type").startsWith("workitem"))
			return;

		int eventType = workflowEvent.getEventType();

		if ((MinutePlugin.MINUTE_TYPE_PARENT
				.equals(workflowEvent.getWorkitem().getItemValueString(MinutePlugin.MINUTETYPE)))
				&& (WorkflowEvent.WORKITEM_CHANGED == eventType)) {
			minutes = null;
			parentWorkitem = workflowEvent.getWorkitem();
		}
	}

	/**
	 * local formDefintion which is used by the current minute item
	 * 
	 * @return
	 */
	public FormDefinition getFormDefinition() {
		return formDefinition;
	}

	/**
	 * Reset minute list and current minute item
	 */
	public void reset() {
		workitem = null;
		minutes = null;

	}

	/**
	 * This toggle method will either load a new minute workitem or reset the
	 * current the current minute workitem. This method can be used to open/close
	 * embedded editors
	 * 
	 * @param id
	 */
	public void toggleWorkitem(String id) {
		if (getWorkitem().getUniqueID().equals(id)) {
			// reset
			this.setWorkitem(null);
		} else {
			// load by id
			setWorkitem(documentService.load(id));
		}
	}

	/**
	 * Set the current minute workitem and loads the new formDefintion
	 */
	public void setWorkitem(ItemCollection workitem) {
		this.workitem = workitem;
		// workflowController.setWorkitem(workitem);
		// update formDefinition
		formDefinition = formController.computeFormDefinition(workitem);
	}

	/**
	 * Returns the current minute workitem
	 */
	public ItemCollection getWorkitem() {
		if (workitem == null) {
			workitem = new ItemCollection();
		}
		return workitem;
	}

	/**
	 * this method returns a list of all minute workitems for the current workitem.
	 * The workitem list is cached. Subclasses can overwrite the method
	 * loadWorkitems() to return a custom result set.
	 * 
	 * @return - list of file meta data objects
	 */
	public List<ItemCollection> getMinutes() {
		if (minutes == null) {
			minutes = loadMinutes();
		}
		return minutes;

	}

	/**
	 * ActionListener method to process a minute item within the body section.
	 * <p>
	 * A shared workflowController can not be used here because the minute item is
	 * Independently processed from the parent workitem.
	 * <p>
	 * In difference to the origin WorkflowController, this method does not close
	 * the running conversation.
	 * 
	 * @see body_entry.xhtml
	 * @param eventID
	 */
	public void process(int eventID) {
		try {
			if (workitem != null) {
				// workflowController.process(eventID);
				workitem.setEventID(eventID);

				long l1 = System.currentTimeMillis();
				events.fire(new WorkflowEvent(getWorkitem(), WorkflowEvent.WORKITEM_BEFORE_PROCESS));
				logger.finest(
						"......fire WORKITEM_BEFORE_PROCESS event: ' in " + (System.currentTimeMillis() - l1) + "ms");

				// process workItem now...
				workflowService.processWorkItem(workitem);

			}

		} catch (ModelException | PluginException e) {
			logger.warning("failed to process minute item: " + e.getMessage());
		}
		reset();

	}

	/**
	 * This method returns a List of workflow events assigned to the current minute
	 * item.
	 * <p>
	 * A shared workflowController can not be used here because the minute item is
	 * Independently processed from the parent workitem.
	 * 
	 * @return
	 */
	public List<ItemCollection> getEvents() {
		List<ItemCollection> activityList = new ArrayList<ItemCollection>();

		if (workitem == null) {
			return activityList;
		}

		// get Events form workflowService
		try {
			activityList = workflowService.getEvents(workitem);
		} catch (ModelException e) {
			logger.warning("Unable to get workflow event list: " + e.getMessage());
		}

		return activityList;
	}

	/**
	 * This method loads the minute worktiems. If one minute workitem has the result
	 * action = 'OPEN' then this minute workitem will be loaded automatically.
	 * 
	 * @return - sorted list of minutes
	 */
	List<ItemCollection> loadMinutes() {

		logger.fine("load minute list...");
		List<ItemCollection> minutes = new ArrayList<ItemCollection>();

		if (parentWorkitem != null) {
			String uniqueIdRef = parentWorkitem.getItemValueString(WorkflowKernel.UNIQUEID);
			if (!uniqueIdRef.isEmpty()) {
				String sQuery = null;
				sQuery = "( (type:\"workitem\" OR type:\"childworkitem\" OR type:\"workitemarchive\" OR type:\"childworkitemarchive\") ";
				sQuery += " AND ($uniqueidref:\"" + uniqueIdRef + "\")) ";
				try {
					minutes = documentService.find(sQuery, 999, 0);
					// sort by numsequencenumber
					Collections.sort(minutes, new ItemCollectionComparator("numsequencenumber", true));
				} catch (QueryException e) {
					logger.warning("loadWorkitems - invalid query: " + e.getMessage());
				}
			}
		}

		// test if we should open one minute (from last to first)....
		for (int i = minutes.size(); i > 0; i--) {
			ItemCollection minute = minutes.get(i - 1);
			// String minuteID=null;
			String testSubject = minute.getItemValueString("action");
			if (testSubject.equalsIgnoreCase("open")) {
				setWorkitem(minute);
				break;
			}
		}

		return minutes;
	}

}
