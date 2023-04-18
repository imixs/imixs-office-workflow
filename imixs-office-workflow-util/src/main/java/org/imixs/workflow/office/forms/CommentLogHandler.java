package org.imixs.workflow.office.forms;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Named;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.exceptions.AccessDeniedException;
import org.imixs.workflow.faces.data.WorkflowEvent;

/**
 * This CDI bean fixes corrupted data in the item txtcommentLog. Because of a
 * previous bug the item txtCommentLog can contain an invalid data structure.
 * The CDI bean reaction the WORKITEM_CHANGED event and fixes the item if a
 * corrupted data structure was detected. (Issue #289)
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

		if (WorkflowEvent.WORKITEM_CHANGED == workflowEvent.getEventType()
				&& workflowEvent.getWorkitem().hasItem("txtCommentLog")) {
			convertCommentLog(workflowEvent.getWorkitem());
		}
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
	private void convertCommentLog(ItemCollection workitem) {

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
