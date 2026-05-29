package org.imixs.workflow.office.forms;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.exceptions.AccessDeniedException;
import org.imixs.workflow.faces.data.WorkflowEvent;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Named;

/**
 * This CDI bean fixes corrupted data in the item txtcommentLog. Because of a
 * previous bug the item txtCommentLog can contain an invalid data structure.
 * The CDI bean reaction the WORKITEM_CHANGED event and fixes the item if a
 * corrupted data structure was detected. (Issue #289)
 * 
 * In addition the handler migrates the old item txtcommentlog into the new
 * 'comment.log' (Issue #728)
 * 
 * @author rsoika
 */
@Named
@RequestScoped
public class CommentLogHandler implements Serializable {

	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(CommentLogHandler.class.getName());

	/**
	 * WorkflowEvent listener to fix a corrupted txtCommentLog item.
	 * 
	 * @param workflowEvent
	 * @throws AccessDeniedException
	 */
	public void onWorkflowEvent(@Observes WorkflowEvent workflowEvent) throws AccessDeniedException {

		// Migrate deprecated item name
		if (WorkflowEvent.WORKITEM_CHANGED == workflowEvent.getEventType()) {

			// migrate old comment format
			if (workflowEvent.getWorkitem().hasItem("txtcommentlog")
					&& !workflowEvent.getWorkitem().hasItem("comment.log")) {
				convertWrongCommentLog(workflowEvent.getWorkitem());

				// finally migrate old txtcommentlog into new 'comment.log'
				// see issue #728
				convertDeprecatedCommentLog(workflowEvent.getWorkitem());
			}

		}

		// support old busienss rules
		if (WorkflowEvent.WORKITEM_BEFORE_PROCESS == workflowEvent.getEventType()) {
			if (!workflowEvent.getWorkitem().getItemValueString("comment.user").isBlank()) {
				// set old value temporarily
				workflowEvent.getWorkitem().setItemValue("txtcomment",
						workflowEvent.getWorkitem().getItemValueString("comment.user"));
			}
		}
		if (WorkflowEvent.WORKITEM_AFTER_PROCESS == workflowEvent.getEventType()) {
			// remove deprecated item
			workflowEvent.getWorkitem().removeItem("txtcomment");
			workflowEvent.getWorkitem().removeItem("txtcommentlog");
		}

	}

	/**
	 * This method converts the old content of the txtcommentLog into the newest
	 * version with the items 'date', 'message', 'user'
	 *
	 * 
	 * @return
	 */
	private void convertDeprecatedCommentLog(ItemCollection workitem) {
		if (workitem == null) {
			return;
		}
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> oldList = workitem.getItemValue("txtcommentlog");
		List<Map<String, Object>> newList = new ArrayList<>();

		for (Map<String, Object> comment : oldList) {
			Map<String, Object> newComment = new HashMap<>(comment);

			// Replace deprecated key 'datcomment' with 'date'
			if (newComment.containsKey("datcomment")) {
				newComment.put("date", newComment.remove("datcomment"));
			}
			// Replace deprecated key 'nameditor' with 'user'
			if (newComment.containsKey("nameditor")) {
				newComment.put("user", newComment.remove("nameditor"));
			}
			// Replace deprecated key 'txtcomment' with 'message'
			if (newComment.containsKey("txtcomment")) {
				newComment.put("message", newComment.remove("txtcomment"));
			}
			newList.add(newComment);
		}

		if (!newList.isEmpty()) {
			logger.warning("Deprecated comment log format detected - migrated " + newList.size()
					+ " entries from 'txtcommentlog' to 'comment.log'");
		}
		workitem.setItemValue("comment.log", newList);
	}

	/**
	 * This method converts the content of the txtcommentLog.
	 * <p>
	 * Wir haben alt daten mit falschen Objekttypen - list objekte anstatt direkte
	 * values. Dies wird hier behoben und automatisch beim update gefixed.
	 * 
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private void convertWrongCommentLog(ItemCollection workitem) {

		if (workitem == null) {
			return;
		}

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> currentList = workitem.getItemValue("txtCommentLog");

		for (Map<String, Object> comment : currentList) {

			// test date object type.....
			Object date = comment.get("datcomment");
			if ((date instanceof List)) {
				logger.warning("...Comment Log: date object is of type: " + date.getClass().getName()
						+ " - data type will be fixed on next save event.");
				// it is a list so we can hopefully replace it with the first entry
				comment.put("datcomment", ((List) date).get(0));
			}

			Object editor = comment.get("nameditor");
			if ((editor instanceof List)) {
				logger.warning("...Comment Log: nameditor object is of type: " + editor.getClass().getName()
						+ " - data type will be fixed on next save event.");
				// it is a list so we can hopefully replace it with the first entry
				comment.put("nameditor", ((List) editor).get(0));
			}

			Object text = comment.get("txtcomment");
			if ((text instanceof List)) {
				logger.warning("...Comment Log: nameditor object is of type: " + text.getClass().getName()
						+ " - data type will be fixed on next save event.");
				// it is a list so we can hopefully replace it with the first entry
				comment.put("txtcomment", ((List) text).get(0));
			}
		}

	}

}
