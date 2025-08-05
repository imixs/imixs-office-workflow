package org.imixs.workflow.office.forms;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Logger;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;

/**
 * Custom Date Converter support empty values
 * 
 * For implementation details also see this discussions
 * 
 * https://github.com/eclipse-ee4j/mojarra/issues/5378
 * https://jakarta.ee/specifications/faces/4.0/apidocs/jakarta/faces/component/uicomponent#getAttributes()
 * https://stackoverflow.com/questions/7684465/valueexpression-map-of-a-jsf-component
 * Component attributes map breaks Map semantics #640
 */
@FacesConverter("imixsDateConverter")
public class DateConverter implements Converter, Serializable {
    private static Logger logger = Logger.getLogger(DateConverter.class.getName());

    private static final String DEFAULT_DATE_FORMAT_PATTERN = "yyyy-mm-dd";
    private static final String DEFAULT_TIME_ZONE = "UTC"; // Set the timezone to CET

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {

        if (value == null || value.trim().isEmpty()) {
            return ""; // Convert null to empty string
        }

        try {
            value = value.trim();
            String pattern = (String) component.getAttributes().get("org.imixs.date.pattern");
            if (pattern == null || pattern.isEmpty()) {
                pattern = DEFAULT_DATE_FORMAT_PATTERN;
            }
            String timezone = (String) component.getAttributes().get("org.imixs.date.timeZone");
            if (timezone == null || timezone.isEmpty()) {
                timezone = DEFAULT_TIME_ZONE;
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
            String pattern = (String) component.getAttributes().get("org.imixs.date.pattern");
            if (pattern == null || pattern.isEmpty()) {
                pattern = DEFAULT_DATE_FORMAT_PATTERN;
            }
            String timezone = (String) component.getAttributes().get("org.imixs.date.timeZone");
            if (timezone == null || timezone.isEmpty()) {
                timezone = DEFAULT_TIME_ZONE;
            }

            DateFormat dateFormat = new SimpleDateFormat(pattern);
            dateFormat.setTimeZone(TimeZone.getTimeZone(timezone));
            return dateFormat.format((Date) value);
        } else {
            logger.warning("Invalid value type: " + value.getClass().getName());
            return "";
        }
    }
}