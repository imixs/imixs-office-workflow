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

        return configItemCollection;
    }

    public boolean hasDashboard() {
        configItemCollection = getConfiguration();
        return !configItemCollection.getItemValueString("dashboard.form").isBlank();
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
