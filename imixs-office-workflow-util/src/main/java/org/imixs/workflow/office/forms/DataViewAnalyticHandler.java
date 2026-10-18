package org.imixs.workflow.office.forms;

import java.util.logging.Logger;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.dataview.DataViewService;
import org.imixs.workflow.engine.DocumentService;
import org.imixs.workflow.exceptions.QueryException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

/**
 * The DataViewAnalyticHandler computes the number of documents of a DataView.
 * <p>
 * The event key must start with 'dataview.stats.' followed by a unique
 * identifier. The name of the DataView is defined by the option 'name'.
 * 
 * <pre>
 * {@code
 * <item name="dataview.stats.artikel" type="custom" path="cards/plain"
 *       options=
'{ "class":"lead", "label":"Artikel", "name":"Artikel", "description":"Alle Artikel"}' />
 * }
 * </pre>
 * 
 * The handler only evaluates the query of the DataView and counts the result.
 * The card links to the DataView page.
 *
 * @author rsoika
 */
@ApplicationScoped
public class DataViewAnalyticHandler {

    private static final Logger logger = Logger.getLogger(DataViewAnalyticHandler.class.getName());

    public static final String KEY_PREFIX = "dataview.stats.";
    public static final String LINK_PATTERN = "/pages/dataviews/data.xhtml?id=%s&faces-redirect=true";

    @Inject
    protected DataViewService dataViewService;

    @Inject
    protected DocumentService documentService;

    @Inject
    protected AnalyticController analyticController;

    public void onEvent(@Observes AnalyticEvent event) {

        if (!event.getKey().startsWith(KEY_PREFIX)) {
            return;
        }

        String options = event.getOptions();
        if (options == null || options.isBlank()) {
            logger.warning("Missing options for analytic '" + event.getKey() + "'");
            return;
        }

        String dataViewName = analyticController.getOption(event.getKey(), "name", options, null);
        if (dataViewName == null || dataViewName.isBlank()) {
            logger.warning("Missing option 'name' for analytic '" + event.getKey() + "'");
            return;
        }

        ItemCollection dataViewDefinition = dataViewService.loadDataViewDefinition(dataViewName);
        if (dataViewDefinition == null) {
            logger.warning("DataView '" + dataViewName + "' not found for analytic '" + event.getKey() + "'");
            event.setData(new ItemCollection()
                    .setItemValue("value", "-")
                    .setItemValue("label", dataViewName)
                    .setItemValue("description", "DataView '" + dataViewName + "' not found"));
            return;
        }

        boolean debug = dataViewDefinition.getItemValueBoolean("debug");
        try {
            // Resolve the query without filter values. Placeholders are replaced by '*'
            String query = dataViewService.parseQuery(dataViewDefinition, new ItemCollection());
            if (debug) {
                logger.info("├── count DataView '" + dataViewName + "' - query=" + query);
            }
            long count = documentService.count(query);

            // label and description can be overwritten by the options
            event.setData(new ItemCollection()
                    .setItemValue("value", count)
                    .setItemValue("label", dataViewName)
                    .setItemValue("description", "")
                    .setItemValue("link", String.format(LINK_PATTERN, dataViewDefinition.getUniqueID())));

        } catch (QueryException e) {
            logger.warning("Failed to count DataView '" + dataViewName + "': " + e.getMessage());
            event.setData(new ItemCollection()
                    .setItemValue("value", "-")
                    .setItemValue("label", dataViewName)
                    .setItemValue("description", "Invalid DataView query"));
        }
    }
}