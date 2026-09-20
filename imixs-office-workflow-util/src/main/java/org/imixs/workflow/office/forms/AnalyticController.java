package org.imixs.workflow.office.forms;

import java.io.Serializable;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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

	/**
	 * This helper method returns the value from the data object as a formatted
	 * string.
	 * The value is formated in case a format value exists depending on the object
	 * type of the value
	 * 
	 * @param value
	 * @return
	 */
	public String getFormatedValue(ItemCollection data) {
		List<Object> valueList = data.getItemValue("value");
		String format = data.getItemValueString("format");
		String result = "";
		if (valueList.size() == 0) {
			return "";
		}

		if (format.isBlank()) {
			return valueList.get(0).toString();
		}

		// we have a format string and an object.
		// format the object if it is double, float, integer, Date.
		Object object = valueList.get(0);
		if (object == null) {
			return "";
		}
		Locale locale = resolveLocale(data.getItemValueString("locale"));
		try {
			if (object instanceof Number) {
				DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(locale);
				DecimalFormat df = new DecimalFormat(format, symbols);
				if (object instanceof Double || object instanceof Float) {
					return df.format(((Number) object).doubleValue());
				}
				return df.format(((Number) object).longValue());
			}
			if (object instanceof Date) {
				SimpleDateFormat sdf = new SimpleDateFormat(format, locale);
				return sdf.format((Date) object);
			}
			if (object instanceof LocalDate) {
				return ((LocalDate) object).format(DateTimeFormatter.ofPattern(format, locale));
			}
			if (object instanceof LocalDateTime) {
				return ((LocalDateTime) object).format(DateTimeFormatter.ofPattern(format, locale));
			}
			return object.toString();
		} catch (IllegalArgumentException e) {
			// Ungültiges Pattern – Fallback auf toString
			return object.toString();
		}

	}

	/**
	 * Parst das locale-Feld (BCP-47, z.B. "de-DE", "en-US", "de").
	 * Fällt auf die JVM-Default-Locale zurück, wenn nichts Gescheites drinsteht.
	 */
	private Locale resolveLocale(String localeString) {
		if (localeString == null || localeString.isBlank()) {
			return Locale.getDefault();
		}
		Locale locale = Locale.forLanguageTag(localeString.replace('_', '-'));
		// forLanguageTag liefert Locale.ROOT ("") wenn der Tag nicht parsebar ist
		if (locale.getLanguage().isEmpty()) {
			return Locale.getDefault();
		}
		return locale;
	}

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
			throw new PluginException(AnalyticController.class.getSimpleName(),
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
			}
		}

		// analytic value is now already cached!
		// return explodeDetails(workitem, key);
		return data;
	}

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
