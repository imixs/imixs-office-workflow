package org.imixs.workflow.office.forms;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.TimeZone;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;

/**
 * HTML5 Date Converter
 */
@SuppressWarnings("rawtypes")
@FacesConverter("imixsHTML5DateTimeConverter")
public class HTML5DateTimeConverter implements Converter, Serializable {

    // format : 2017-06-01T08:30
    private static final String HTML5_DATE_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm";
    private static final String DEFAULT_TIME_ZONE = "UTC"; // Set the timezone to CET

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {

        if (value == null || value.trim().isEmpty()) {
            return ""; // Convert null to empty string
        }

        try {
            value = value.trim();
            String pattern = HTML5_DATE_FORMAT_PATTERN;

            String timezone = (String) component.getAttributes().get("org.imixs.date.timeZone");
            if (timezone == null || timezone.isEmpty()) {
                timezone = getTimeZoneFromBundle(context);
            }
            DateFormat dateFormat = new SimpleDateFormat(pattern);
            dateFormat.setTimeZone(TimeZone.getTimeZone(timezone));
            return dateFormat.parse(value);
        } catch (ParseException e) {
            throw new RuntimeException("Error converting date", e);
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value == null || "".equals(value)) {
            return ""; // Convert null to empty string
        }

        if (value instanceof Date) {
            String pattern = HTML5_DATE_FORMAT_PATTERN;
            String timezone = (String) component.getAttributes().get("org.imixs.date.timeZone");
            if (timezone == null || timezone.isEmpty()) {
                timezone = getTimeZoneFromBundle(context);
            }

            DateFormat dateFormat = new SimpleDateFormat(pattern);
            dateFormat.setTimeZone(TimeZone.getTimeZone(timezone));
            return dateFormat.format((Date) value);
        }
        return value.toString();
    }

    private String getTimeZoneFromBundle(FacesContext context) {
        // Load the timezone from the message bundle
        ResourceBundle bundle = context.getApplication()
                .getResourceBundle(context, "message");

        try {
            return bundle.getString("timeZone");
        } catch (MissingResourceException e) {
            return DEFAULT_TIME_ZONE; // fallback to UTC
        }
    }
}