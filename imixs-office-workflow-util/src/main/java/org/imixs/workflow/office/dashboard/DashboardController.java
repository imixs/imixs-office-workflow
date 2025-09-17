package org.imixs.workflow.office.dashboard;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.engine.DocumentEvent;
import org.imixs.workflow.engine.DocumentService;
import org.imixs.workflow.exceptions.QueryException;

import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Der DashboardController holds the dashboard configuration in a session
 *
 * @author rsoika
 *
 */
@Named
@SessionScoped
public class DashboardController implements Serializable {

    private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(DashboardAnalyticController.class.getName());

    public static String DASHBOARD_DEFAULT_DEFINITION = "<imixs-form>\n" + //
            "  <imixs-form-section columns=\"4\" label=\"\">\n" + //
            "     <item name=\"dashboard.worklist.owner.count.today\" type=\"custom\" path=\"cards/plain\"  label=\"\"\n"
            + //
            "           options='{\"class\":\"flat success\", \"icon\":\"fa-inbox\", \"label\":\"Neue Aufgaben\", \"description\":\"Neue Aufgaben seit Heute\"}'   />\n"
            + //
            "     <item name=\"dashboard.worklist.owner.count.oneweek\" type=\"custom\" path=\"cards/plain\"  label=\"\"\n"
            + //
            "           options='{\"class\":\"flat warning\", \"icon\":\"fa-exclamation-triangle\", \"label\":\"Zu Beachten\", \"description\":\"Aufgaben seit einer Woche offen\"}'    />\n"
            + //
            "     <item name=\"dashboard.worklist.owner.count.urgent\" type=\"custom\" path=\"cards/plain\"  label=\"\"\n"
            + //
            "           options='{\"class\":\"flat error\", \"icon\":\"fa-fire\", \"label\":\"Dringend\", \"description\":\"Aufgaben seit mehr als 1 Woche offen\"}'    />\n"
            + //
            "    <item name=\"dashboard.worklist.participant.count.all\" type=\"custom\" path=\"cards/plain\" label=\"\"\n"
            + //
            "           options='{\"class\":\"flat\", \"icon\":\"fa-tasks\", \"label\":\"Meine Vorgänge\", \"description\":\"Offene Vorgänge mit Ihrer Beteiligung.\"}' />\n"
            + //
            "   </imixs-form-section>\n" + //
            "  <imixs-form-section columns=\"2\">\n" + //
            "    <item name=\"dashboard.worklist.owner\" type=\"custom\" path=\"cards/worklist\"\n" + //
            "         options='{ \"label\":\"Meine Aufgaben\", \"description\":\"Aufgaben, die Ihrer Bearbeitung bedürfen.\", \"pagesize\":\"10\"}'/>\n"
            + //
            "    <item name=\"dashboard.worklist.favorite\" type=\"custom\" path=\"cards/worklist\"\n" + //
            "         options='{ \"label\":\"Meine Favoriten\", \"description\":\"Mit Stern markierte Aufgaben, die Sie im Blick behalten möchten.\", \"pagesize\":\"10\"}'/>\n"
            + //
            "  </imixs-form-section>\n" + //
            "</imixs-form> ";

    ItemCollection configItemCollection;
    private String setupConfigUniqueID = null;

    @Inject
    protected DocumentService documentService;

    /**
     * Load dashboard definition
     */
    public ItemCollection getConfiguration() {
        if (configItemCollection == null) {
            List<ItemCollection> result;
            try {
                result = documentService.find("type:configuration AND txtname:BASIC", 1, 0, "$created", true);
                if (result != null && result.size() > 0) {
                    configItemCollection = result.get(0);
                    if (configItemCollection != null) {
                        setupConfigUniqueID = configItemCollection.getUniqueID();
                    }
                }
            } catch (QueryException e) {
                logger.warning("Failed to load BASIC configuration!");
            }
        }

        // Test if a 'dashboard.from' is defined. If not, we set the default from here.
        if (configItemCollection.getItemValueString("dashboard.form").isBlank()) {
            // set default form
            logger.info("Set DASHBOARD_DEFAULT_DEFINITION...");
            configItemCollection.setItemValue("dashboard.form", DASHBOARD_DEFAULT_DEFINITION);
        }

        return configItemCollection;
    }

    /**
     * Reacts on ON_DOCUMENT_SAVE of the config item and reloads the configuration
     * 
     * @see DocumentEvent
     * @param documentEvent
     */
    public void onDocumentEvent(@Observes DocumentEvent documentEvent) {

        if (setupConfigUniqueID != null
                && documentEvent.getEventType() == DocumentEvent.ON_DOCUMENT_SAVE
                && setupConfigUniqueID.equals(documentEvent.getDocument().getUniqueID())) {
            logger.info("reload dashboard layout...");
            configItemCollection = documentEvent.getDocument();
        }

    }

}
