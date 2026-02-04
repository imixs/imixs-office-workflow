package org.imixs.workflow.office.forms;

import java.io.Serializable;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.exceptions.PluginException;

import jakarta.enterprise.context.ConversationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

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

	private Map<String, Map<String, String>> optionsCache = new HashMap<>();

	private Map<String, ItemCollection> dataCache = new HashMap<>();

	/**
	 * Returns a analytic data object for a given key.
	 * 
	 * @param key
	 * @return
	 * @throws PluginException
	 */
	public ItemCollection getData(ItemCollection workitem, String key) throws PluginException {
		return getData(workitem, key, null);

	}

	public ItemCollection getData(ItemCollection workitem, String key, String options) throws PluginException {
		return computeData(workitem, key, options);
	}

	// public String getValueAsString(ItemCollection workitem, String key) throws
	// PluginException {
	// return this.getValueAsString(workitem, key, null);
	// }

	// /**
	// * Returns a analytic value as a Json String for a given key.
	// *
	// * @param key
	// * @return
	// * @throws PluginException
	// */
	// public String getValueAsJson(ItemCollection workitem, String key, String
	// options) throws PluginException {
	// ItemCollection analyticData = computeValue(workitem, key, options);
	// String jsonval = analyticData.getItemValueString("value");
	// if (jsonval == null || jsonval.isEmpty()) {
	// return "null";
	// } else {
	// return jsonval;
	// }
	// }

	// public String getValueAsJson(ItemCollection workitem, String key) throws
	// PluginException {
	// return getValueAsJson(workitem, key, null);
	// }

	// /**
	// * Returns a analytic value as a Double for a given key.
	// *
	// * @param key
	// * @return
	// * @throws PluginException
	// */
	// public double getValueAsDouble(ItemCollection workitem, String key, String
	// options) throws PluginException {
	// ItemCollection analyticData = computeValue(workitem, key, options);
	// return analyticData.getItemValueDouble("value");
	// }

	// public double getValueAsDouble(ItemCollection workitem, String key) throws
	// PluginException {
	// return getValueAsDouble(workitem, key, null);
	// }

	// /**
	// * Returns the analytic label for a given key
	// *
	// * @param key
	// * @return
	// * @throws PluginException
	// */
	// public String getLabel(ItemCollection workitem, String key, String options)
	// throws PluginException {
	// ItemCollection analyticData = computeValue(workitem, key, options);
	// return analyticData.getItemValueString("label");
	// }

	// public String getLabel(ItemCollection workitem, String key) throws
	// PluginException {
	// return this.getLabel(workitem, key, null);
	// }

	// /**
	// * Returns the analytic optional link for a given key
	// *
	// * @param key
	// * @return
	// */
	// public String getLink(ItemCollection workitem, String key, String options)
	// throws PluginException {
	// ItemCollection analyticData = computeValue(workitem, key, options);
	// return analyticData.getItemValueString("link");
	// }

	// public String getLink(ItemCollection workitem, String key) throws
	// PluginException {
	// return this.getLink(workitem, key, null);
	// }

	// /**
	// * Returns the analytic description for a given key
	// *
	// * @param key
	// * @return
	// */
	// public String getDescription(ItemCollection workitem, String key, String
	// options) throws PluginException {
	// ItemCollection analyticData = computeValue(workitem, key, options);
	// return analyticData.getItemValueString("description");
	// }

	// public String getDescription(ItemCollection workitem, String key) throws
	// PluginException {
	// return this.getDescription(workitem, key, null);
	// }

	/**
	 * Computes an analytic data value. The method sends CDI events of the type
	 * 'AnalyticEvent'.
	 * <p>
	 * Note: An observer CDI bean is responsible to cache or reset the data values
	 * if needed.
	 * <p>
	 *
	 * @param key
	 * @return
	 * @throws PluginException
	 */
	protected ItemCollection computeData(ItemCollection workitem, String key, String options) throws PluginException {

		if (workitem == null) {
			throw new PluginException(AIController.class.getSimpleName(),
					"ERROR", "Analytic Value can not be computed - workitem is null");
		}

		ItemCollection data = dataCache.get(key);

		if (data == null) {
			// compute data...

			logger.fine("fire analytic event for key '" + key + "'");
			// Fire the Analytics Event for this key
			AnalyticEvent event = new AnalyticEvent(key, workitem, options);
			if (analyticEvents != null) {
				analyticEvents.fire(event);

				data = event.getData();
				if (data == null) {
					logger.severe("Data Object is null!");
					data = new ItemCollection();
				}

				if (options != null) {
					logger.fine("options=" + options);
					Map<String, String> optionValues = parseJsonOptions(options);
					for (Map.Entry<String, String> i : optionValues.entrySet()) {
						if (!"value".equals(i.getKey())) {
							data.setItemValue(i.getKey(), i.getValue());
						}
					}
				}

				dataCache.put(key, data);
				// if (event.getValue() != null) {
				// ItemCollection details = new ItemCollection();
				// details.setItemValue("value", event.getValue());

				// if (!event.getLabel().isEmpty()) {
				// details.setItemValue("label", event.getLabel());
				// } else {
				// // if we do not have a label we try to resolve it from the options..
				// details.setItemValue("label", getOption(key, "label", options, ""));
				// }

				// if (!event.getDescription().isEmpty()) {
				// details.setItemValue("description", event.getDescription());
				// } else {
				// // if we do not have a description we try to resolve it from the options..
				// details.setItemValue("description", getOption(key, "description", options,
				// ""));
				// }

				// if (!event.getLink().isEmpty()) {
				// details.setItemValue("link", event.getLink());
				// } else {
				// // if we do not have a link we try to resolve it from the options..
				// details.setItemValue("link", getOption(key, "link", options, ""));
				// }

				// // cache result
				// implodeDetails(workitem, key, details);
				// }
			}
		}

		// analytic value is now already cached!
		// return explodeDetails(workitem, key);
		return data;

	}

	/**
	 * Convert the List of ItemCollections back into a List of Map elements
	 * 
	 * @param workitem
	 */
	// @SuppressWarnings({ "rawtypes" })
	// private void implodeDetails(ItemCollection workitem, String key,
	// ItemCollection details) {
	// // convert the child ItemCollection elements into a List of Map
	// List<Map> detailsList = new ArrayList<Map>();
	// detailsList.add(details.getAllItems());
	// workitem.replaceItemValue(key, detailsList);
	// }

	// /**
	// * converts the Map List of a workitem into a List of ItemCollections
	// */
	// @SuppressWarnings({ "rawtypes", "unchecked" })
	// private ItemCollection explodeDetails(ItemCollection workitem, String key) {
	// // convert current list of childItems into ItemCollection elements
	// List<Object> mapOrderItems = workitem.getItemValue(key);
	// if (mapOrderItems != null && mapOrderItems.size() > 0) {
	// ItemCollection itemCol = new ItemCollection((Map) mapOrderItems.get(0));
	// return itemCol;
	// }
	// // return empty collection
	// ItemCollection dummy = new ItemCollection();
	// dummy.setItemValue("value", "");
	// dummy.setItemValue("label", "");
	// dummy.setItemValue("description", "No data available");

	// return dummy;
	// }

	/**
	 * This helper method returns an optional JSON value from the 'options'
	 * attriubte
	 * 
	 * @param key
	 * @param optionName
	 * @param jsonOptions
	 * @param defaultValue
	 * @return
	 */
	public String getOption(String key, String optionName, String jsonOptions, String defaultValue) {
		Map<String, String> options = getOptions(key, jsonOptions);
		return options.getOrDefault(optionName, defaultValue);
	}

	/**
	 * Helper method to parse the json options for a specific key
	 * 
	 * @param key         - name of the analytic key
	 * @param jsonOptions - a json string to be parsed
	 * @return a key value map
	 */
	private Map<String, String> getOptions(String key, String jsonOptions) {
		if (!optionsCache.containsKey(key)) {

			Map<String, String> optionsMap = parseJsonOptions(jsonOptions);
			optionsCache.put(key, optionsMap);
		}
		return optionsCache.get(key);
	}

	/**
	 * Parse JSON options using Jakarta EE JSON-P API
	 * 
	 * @param jsonString - JSON string to parse
	 * @return Map with parsed key-value pairs
	 */
	private Map<String, String> parseJsonOptions(String jsonString) {
		Map<String, String> options = new HashMap<>();

		if (jsonString != null && !jsonString.isEmpty()) {
			// Convert single quotes to double quotes for JSON compatibility
			String normalizedJson = jsonString.replace("'", "\"");
			try (JsonReader jsonReader = Json.createReader(new StringReader(normalizedJson))) {
				JsonObject jsonObject = jsonReader.readObject();

				// Convert JsonObject to Map<String, String>
				for (Map.Entry<String, JsonValue> entry : jsonObject.entrySet()) {
					String key = entry.getKey();
					JsonValue value = entry.getValue();

					// Convert JsonValue to String based on type
					String stringValue = convertJsonValueToString(value);
					options.put(key, stringValue);
				}

			} catch (Exception e) {
				logger.log(Level.WARNING, "Failed to parse card options JSON: " + jsonString, e);
			}
		}

		return options;
	}

	/**
	 * Convert JsonValue to String representation
	 * 
	 * @param jsonValue - the JsonValue to convert
	 * @return String representation of the value
	 */
	private String convertJsonValueToString(JsonValue jsonValue) {
		switch (jsonValue.getValueType()) {
			case STRING:
				return ((JsonString) jsonValue).getString();
			case NUMBER:
				return jsonValue.toString();
			case TRUE:
				return "true";
			case FALSE:
				return "false";
			case NULL:
				return null;
			default:
				// For arrays or objects, return the JSON representation
				return jsonValue.toString();
		}
	}

}
