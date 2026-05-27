package org.imixs.workflow.office.plugins;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.engine.WorkflowService;
import org.imixs.workflow.engine.plugins.AbstractPlugin;
import org.imixs.workflow.exceptions.PluginException;
import org.imixs.workflow.exceptions.QueryException;
import org.imixs.workflow.faces.data.WorkflowController;
import org.imixs.workflow.office.views.SearchController;

import jakarta.inject.Inject;

/**
 * The InputValidationPlugin can validate various input data.
 * In the mode 'DUPLICATE' it validates duplicated data inputs.
 * <p>
 *
 * Example imixs-validation Prompt:
 *
 * <pre>
 * {@code
 <imixs-validation name="DUPLICATE">
   <input-item>invoice.number</input-item>
   <query>"(type:workitem OR type:workitemarchive) AND ($modelversion:rechnungseingang* OR $modelversion:approval*)</query>
   <error-message>The invoice number was alredy used!</error-message>
   <strict>true</strict>
 </imixs-validation>
 * }
 * </pre>
 * <p>
 * 
 */
public class InputValidationPlugin extends AbstractPlugin {

    private static Logger logger = Logger.getLogger(InputValidationPlugin.class.getName());

    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";

    @Inject
    WorkflowController workflowController;

    @Inject
    WorkflowService workflowService;

    @Inject
    protected SearchController searchController;

    @Override
    public ItemCollection run(ItemCollection workitem, ItemCollection event) throws PluginException {

        List<ItemCollection> inputDuplicateDefinitions = workflowService.evalWorkflowResultXML(event,
                "imixs-validation",
                "DUPLICATE", workitem, true);
        for (ItemCollection validationDefinition : inputDuplicateDefinitions) {

            validateInvoiceNumber(workitem, validationDefinition);
        }
        return workitem;

    }

    /**
     * This method validates if the input data already exists. If this is the case
     * the method throes a PluginException
     * <p>
     * Query Example:
     * <p>
     * <code>NOT $uniqueid:"9f72fa50-4845-41ea-b6b9-ccd518c353be" AND 
                _invoicenumber:"45"</code>
     * 
     * <p>
     * in case a duplicate invoicenumber was detected the item
     * _invoicenumber_duplicate is
     * filled. The case is displayed as a
     * warning in the form.
     * 
     * @throws PluginException
     * 
     */
    private void validateInvoiceNumber(ItemCollection workitem, ItemCollection validationDefinition)
            throws PluginException {

        String itemName = validationDefinition.getItemValueString("input-item");
        String queryFilter = validationDefinition.getItemValueString("query-filter");
        String errorMessage = validationDefinition.getItemValueString("error-message");
        boolean strict = validationDefinition.getItemValueBoolean("strict");
        boolean debug = validationDefinition.getItemValueBoolean("debug");

        // read

        String inputValue = workitem.getItemValueString(itemName);
        String inputValueDuplicate = workitem.getItemValueString(itemName + "_duplicate");

        // lookup workitems...
        String query = "(NOT ($uniqueid:\""
                + workitem.getUniqueID() + "\") AND (" + itemName + ":\"" + inputValue + "\"))";

        if (!queryFilter.isBlank()) {
            query = query + " AND (" + queryFilter + ")";
        }

        if (debug) {
            logger.info("├── Validate duplicate Input");
            logger.info("│   ├── input-item= " + itemName);
            logger.info("│   ├── input-value= " + inputValue);
            logger.info("│   ├── query= " + query);
            logger.info("│   ├── strict= " + strict);
        }

        try {
            int resultAktuell = workflowService.getDocumentService().count(query, 1);

            if (resultAktuell > 0) {
                // if <item>_duplicate already exists, we do not validate again!
                if (!strict && inputValue.equals(inputValueDuplicate)) {
                    logger.warning(
                            "├── skipped validation - duplicate input value '" + inputValue + "' confirmed by user");
                } else {
                    // set <item>_duplicate - used to show message
                    workitem.replaceItemValue(itemName + "_duplicate", inputValue);
                    if (resultAktuell > 0) {
                        workitem.replaceItemValue(itemName + "_duplicate_processing", inputValue);
                    }
                    String[] stringArray = { "workitem", "workitemarchive" };
                    List<String> stringList = new ArrayList<>(Arrays.asList(stringArray));
                    searchController.getSearchFilter().setItemValue("type", stringList);
                    throw new PluginException(InputValidationPlugin.class.getName(), VALIDATION_ERROR,
                            errorMessage);
                }
            } else {
                // clear !
                workitem.replaceItemValue(itemName + "_duplicate_history",
                        workitem.getItemValue(itemName + "_duplicate"));
                workitem.replaceItemValue(itemName + "_duplicate", "");
                workitem.replaceItemValue(itemName + "_duplicate_processing", "");
            }
        } catch (QueryException e) {
            throw new PluginException(InputValidationPlugin.class.getName(), "QUERY ERROR",
                    e.getMessage(), e);
        }

    }

}
