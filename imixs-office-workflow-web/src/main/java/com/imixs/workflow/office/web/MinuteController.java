package com.imixs.workflow.office.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Observes;
import javax.faces.event.ActionEvent;
import javax.inject.Named;

import org.imixs.marty.workflow.ChildWorkitemController;
import org.imixs.marty.workflow.WorkflowEvent;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.exceptions.AccessDeniedException;
import org.imixs.workflow.jee.ejb.EntityService;

/**
 * The MinuteController is a session scoped backing bean to provide a list of
 * team member profiles. The controller provides alos methods to add or remove a
 * entry into the member list
 *  
 * @author rsoika
 * 
 */
@Named
@SessionScoped
public class MinuteController extends ChildWorkitemController implements
		Serializable {

	private static final long serialVersionUID = 1L;

	private List<ItemCollection> minutes = null;
	private ItemCollection minuteHeader = null;

	@EJB
	private EntityService entityService;

	

	private static Logger logger = Logger.getLogger(MinuteController.class
			.getName());



	public ItemCollection getMinuteHeader() {
		return minuteHeader;
	}

	

	/**
	 * If the workitem has changed, the method lookups the minuteHeader
	 */
	@Override
	public void onWorkflowEvent(@Observes WorkflowEvent workflowEvent)
			throws AccessDeniedException {

		// default behavior
		super.onWorkflowEvent(workflowEvent);

		// skip if null..
		if (workflowEvent.getWorkitem() == null)
			return;

		String type = workflowEvent.getWorkitem().getItemValueString("type");
		// skip if not a workItem...
		if (!(type.startsWith("workitem") || type.startsWith("childworkitem")))
			return;

		if (WorkflowEvent.WORKITEM_CHANGED == workflowEvent.getEventType()
				|| WorkflowEvent.WORKITEM_AFTER_PROCESS == workflowEvent
						.getEventType()) {
			int processID = workflowEvent.getWorkitem().getItemValueInteger(
					"$ProcessID");

			if (processID >= 1000 && processID <= 1999) {
				logger.fine("set miniute header");;
				minuteHeader = workflowEvent.getWorkitem();
				//teamMembers = null;
				//setMemberInput(null);
			} else {
				logger.fine("set miniute header");;
				// lookup the header....
				String sID = workflowEvent.getWorkitem().getItemValueString(
						"$UniqueIDRef");
				minuteHeader = this.getEntityService().load(sID);
				//teamMembers = null;
				//setMemberInput(null);
			}

		}
	}

	/**
	 * This method overwrites the behavior of the childWorkitemController. The
	 * method returns only workitems (no invitations)
	 * 
	 * @return - list minutes
	 */
	public List<ItemCollection> getWorkitems() {

		if (minutes != null)
			return minutes;

		minutes = new ArrayList<ItemCollection>();

		if (super.getUniqueIdRef() != null) {

			String sQuery = null;
			sQuery = "SELECT wi FROM Entity as wi ";
			sQuery += " JOIN wi.textItems as r ";
			sQuery += " JOIN wi.integerItems as n ";
			sQuery += " WHERE wi.type IN ('workitem','childworkitem','workitemarchive','childworkitemarchive')  ";
			sQuery += " AND r.itemName = '$uniqueidref' and r.itemValue = '"
					+ super.getUniqueIdRef() + "'";
			sQuery += " AND n.itemName = 'numsequencenumber' ";
			sQuery += " ORDER BY n.itemValue";

			// List<ItemCollection> result
			minutes = entityService.findAllEntities(sQuery, 0, -1);
			// for (ItemCollection aWorkitem : result) {
			// minutes.add(cloneWorkitem(aWorkitem));
			// }
		}

		return minutes;

	}


	public void reset(ActionEvent event) {
		super.reset();
		minutes = null;
	}

}
