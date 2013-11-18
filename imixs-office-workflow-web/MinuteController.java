package org.imixs.minutes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Observes;
import javax.faces.event.ActionEvent;
import javax.inject.Named;

import org.imixs.marty.ejb.ProfileService;
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

	private String searchPhrase = null;
	private String memberInput = null;
	private List<ItemCollection> teamMembers = null;
	private List<ItemCollection> minutes = null;
	private ItemCollection minuteHeader = null;

	@EJB
	private EntityService entityService;

	@EJB
	private ProfileService profileService;

	// @Inject
	// private WorkflowController workflowController;
	//
	// @Inject
	// private ChildWorkitemController childWorkitemController;

	private static Logger logger = Logger.getLogger(MinuteController.class
			.getName());

	/**
	 * New member input to be added. The current will be added into the member
	 * list. ',' will be replaced by ';' and split into separate values. After
	 * the new input was added into the teamList, the memberInput will be
	 * cleared again!.
	 * 
	 * @param memberInput
	 *            - new input value
	 */
	@SuppressWarnings("unchecked")
	public void setMemberInput(String memberInput) {
		this.memberInput = memberInput;

		if (memberInput == null || getMinuteHeader() == null)
			return;

		memberInput = memberInput.trim();
		// replace ,
		memberInput = memberInput.replace(',', ';');
		memberInput = memberInput.trim();
		if (memberInput.isEmpty())
			return;

		logger.info("[MinuteController] addTeamMember: " + memberInput);

		// get current member list
		List<String> team = getMinuteHeader().getItemValue("_team");

		// split memberinput
		StringTokenizer stInput = new StringTokenizer(memberInput, ";");
		while (stInput.hasMoreTokens()) {
			String s = stInput.nextToken();
			ItemCollection profile = null;

			// now lets test if we found a existing profile for that name id or
			// email
			profile = profileService.findProfileById(s);
			if (profile == null) {
				// lookup for email or username
				profile = profileService.lookupProfile(s);
			}
			// if we found a profile, then take the userid (txtname)
			if (profile != null)
				s = profile.getItemValueString("txtName");

			// test if value still exists
			if (team.indexOf(s) == -1)
				team.add(s);
		}

		// clear empty values
		team.removeAll(Collections.singleton(null));
		team.removeAll(Collections.singleton(""));

		// update input
		getMinuteHeader().replaceItemValue("_team", team);

		// clear input
		memberInput = "";

		teamMembers = null;
	}

	public String getMemberInput() {
		return memberInput;
	}

	public String getSearchPhrase() {
		return searchPhrase;
	}

	public void setSearchPhrase(String searchPhrase) {
		this.searchPhrase = searchPhrase;
	}

	public ItemCollection getMinuteHeader() {
		return minuteHeader;
	}

	/**
	 * This method returns a ItemCollection list with the profiles for the
	 * current memberlist (_team) The method uses the ProfileService to lookup
	 * the users profile. The ProfileService is a Application Scoped cache.
	 * 
	 * If no profile is found for one of the current entries, then the method
	 * creats a dummy entry.
	 * 
	 * @return
	 */
	public List<ItemCollection> getTeamMembers() {

		if (teamMembers == null) {
			teamMembers = new ArrayList<ItemCollection>();

			if (getMinuteHeader() == null)
				return teamMembers;

			// get current member list
			List<String> team = getMinuteHeader().getItemValue("_team");
			for (String aMember : team) {
				ItemCollection profile = null;
				// test if aMember is an email address
				if (aMember.indexOf('@') > -1) {
					// create a dummy entry
					profile = new ItemCollection();
					profile.replaceItemValue("txtEmail", aMember);
					profile.replaceItemValue("txtName", aMember);
					profile.replaceItemValue("txtUserName", aMember);

				} else {
					// try to lookup the profile
					profile = profileService.findProfileById(aMember);
					if (profile == null) {
						// no profile found - so we create a dummy profile
						profile = new ItemCollection();
						profile.replaceItemValue("txtName", aMember);
						profile.replaceItemValue("txtUserName", aMember);
					}
				}

				// if we got a profile we add it into the list
				if (profile != null)
					teamMembers.add(profile);
			}

		}

		return teamMembers;
	}

	/**
	 * Search for the current input phrase in the invitation list. If a ',' or
	 * ';' or RETURN was entered, then split the element into the _team
	 * property.
	 * 
	 * 
	 */
	public void search() {
		logger.info("[MinuteController] search:  " + searchPhrase);

	}

	/**
	 * Removes a given entry from the team member list.
	 * 
	 * @param aMember
	 *            - value to be removed
	 */
	@SuppressWarnings("unchecked")
	public void removeTeamMember(String aMember) {
		if (getMinuteHeader() == null)
			return;

		logger.fine("[MinuteController] remove Member " + aMember);
		List<String> team = getMinuteHeader().getItemValue("_team");

		team.remove(aMember);
		getMinuteHeader().replaceItemValue("_team", team);

		// reset list
		teamMembers = null;
	}

	/**
	 * If the workitem has changed, the method lookups the minuteHeader and the
	 * cached teamMembers will be reset to null
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
				minuteHeader = workflowEvent.getWorkitem();
				teamMembers = null;
				setMemberInput(null);
			} else {
				// lookup the header....
				String sID = workflowEvent.getWorkitem().getItemValueString(
						"$UniqueIDRef");
				minuteHeader = this.getEntityService().load(sID);
				teamMembers = null;
				setMemberInput(null);
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

	@Override
	public void reset(ActionEvent event) {

		super.reset(event);
		minutes = null;
	}

}
