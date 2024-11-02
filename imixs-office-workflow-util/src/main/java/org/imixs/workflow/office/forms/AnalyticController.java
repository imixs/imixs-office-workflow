package org.imixs.workflow.office.forms;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.faces.data.WorkflowController;

import jakarta.enterprise.context.ConversationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * The AnalyticController is a conversationScoped controller that provides
 * values for the analytic-custom parts.
 * <p>
 * A custom implementation can react on AnalyticEvent to compute values and
 * datasets.
 * <p>
 * The controller implements a caching mechanism to avoid repeated calls for new
 * analytic values. If the analytic value is already stored in the current
 * workitem, no new value will be fired.
 * 
 * 
 * @author rsoika
 *
 */
@Named
@ConversationScoped
public class AnalyticController implements Serializable {

	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(AnalyticController.class.getName());

	@Inject
	protected Event<AnalyticEvent> analyticEvents;

	@Inject
	protected WorkflowController workflowController;

	/**
	 * Returns a analytic value as a String for a given key.
	 * 
	 * @param key
	 * @return
	 */
	public String getAsString(String key) {
		ItemCollection analyticData = computeValue(key);
		return analyticData.getItemValueString("value");
	}

	/**
	 * Returns a analytic value as a Json String for a given key.
	 * 
	 * @param key
	 * @return
	 */
	public String getAsJson(String key) {
		ItemCollection analyticData = computeValue(key);
		String jsonval = analyticData.getItemValueString("value");
		if (jsonval == null || jsonval.isEmpty()) {
			return "null";
		} else {
			System.out.println(jsonval);
			return jsonval;
		}
	}

	/**
	 * Returns a analytic value as a Double for a given key.
	 * 
	 * @param key
	 * @return
	 */
	public double getAsDouble(String key) {
		ItemCollection analyticData = computeValue(key);
		return analyticData.getItemValueDouble("value");
	}

	/**
	 * Returns the analytic label for a given key
	 * 
	 * @param key
	 * @return
	 */
	public String getLabel(String key) {
		ItemCollection analyticData = computeValue(key);
		return analyticData.getItemValueString("label");
	}

	/**
	 * Returns the analytic optional link for a given key
	 * 
	 * @param key
	 * @return
	 */
	public String getLink(String key) {
		ItemCollection analyticData = computeValue(key);
		return analyticData.getItemValueString("link");
	}

	/**
	 * Returns the analytic description for a given key
	 * 
	 * @param key
	 * @return
	 */
	public String getDescription(String key) {
		ItemCollection analyticData = computeValue(key);
		return analyticData.getItemValueString("description");
	}

	/**
	 * Computes am analytic value. The method cache the value in the item
	 * 'analytic.KEY' to avoid feierring repeated AnalyticEvents.
	 * 
	 * @param key
	 * @return
	 */
	protected ItemCollection computeValue(String key) {
		if (workflowController.getWorkitem() != null) {

			logger.fine("fire analytic event for key '" + key + "'");
			// Fire the Analytics Event for this key
			AnalyticEvent event = new AnalyticEvent(key, workflowController.getWorkitem());
			if (analyticEvents != null) {
				analyticEvents.fire(event);
				if (event.getValue() != null) {
					ItemCollection details = new ItemCollection();
					details.setItemValue("value", event.getValue());
					details.setItemValue("label", event.getLabel());
					details.setItemValue("description", event.getDescription());
					details.setItemValue("link", event.getLink());
					implodeDetails(key, details);
				}
			}

			// try loading from cache
			ItemCollection details = explodeDetails(key);
			if (details == null) {
				// set dummy value
				details = new ItemCollection();
				details.setItemValue("value", "");
				details.setItemValue("label", "");
				details.setItemValue("description", "No data available");
				implodeDetails(key, details);
			}

		}

		// analytic value is now already cached!
		return explodeDetails(key);
	}

	/**
	 * Convert the List of ItemCollections back into a List of Map elements
	 * 
	 * @param workitem
	 */
	@SuppressWarnings({ "rawtypes" })
	private void implodeDetails(String key, ItemCollection details) {
		// convert the child ItemCollection elements into a List of Map
		List<Map> detailsList = new ArrayList<Map>();
		detailsList.add(details.getAllItems());
		workflowController.getWorkitem().replaceItemValue(key, detailsList);
	}

	/**
	 * converts the Map List of a workitem into a List of ItemCollectons
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private ItemCollection explodeDetails(String key) {
		// convert current list of childItems into ItemCollection elements
		List<Object> mapOrderItems = workflowController.getWorkitem().getItemValue(key);
		if (mapOrderItems != null && mapOrderItems.size() > 0) {
			ItemCollection itemCol = new ItemCollection((Map) mapOrderItems.get(0));
			return itemCol;
		}
		// return empty collection
		return new ItemCollection();
	}

}
