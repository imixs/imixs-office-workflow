package org.imixs.workflow.office.plugins;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.engine.WorkflowService;
import org.imixs.workflow.engine.plugins.AbstractPlugin;
import org.imixs.workflow.exceptions.PluginException;
import org.imixs.workflow.exceptions.QueryException;
import org.imixs.workflow.faces.data.WorkflowController;
import org.imixs.workflow.office.views.SearchController;

import jakarta.el.ValueExpression;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.context.FacesContext;
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
   <error-severity>warning</error-severity>
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
        String severity = validationDefinition.getItemValueString("error-severity");
        boolean debug = validationDefinition.getItemValueBoolean("debug");

        // read

        String inputValue = workitem.getItemValueString(itemName);
        if (inputValue.isEmpty()) {
            // no op
            return;
        }
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
            logger.info("│   ├── severity= " + severity);
        }

        try {
            int resultAktuell = workflowService.getDocumentService().count(query, 1);

            if (resultAktuell > 0) {
                // if <item>_duplicate already exists, we do not validate again!
                if ((severity.equalsIgnoreCase("warning") || severity.equalsIgnoreCase("info"))
                        && inputValue.equals(inputValueDuplicate)) {
                    logger.warning(
                            "├── skipped validation - duplicate input value '" + inputValue + "' confirmed by user");
                } else {
                    // set <item>_duplicate - used to show message
                    workitem.replaceItemValue(itemName + "_duplicate", inputValue);
                    if (resultAktuell > 0) {
                        workitem.replaceItemValue(itemName + "_duplicate_processing", inputValue);

                        // After detecting duplicate — add FacesMessage directly to the field
                        // FacesContext facesContext = FacesContext.getCurrentInstance();
                        // if (facesContext != null && facesContext.getViewRoot() != null) {
                        // String clientId = findClientIdByItemName(facesContext,
                        // facesContext.getViewRoot(),
                        // itemName, debug);
                        // FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        // errorMessage,
                        // errorMessage);
                        // // If component found → message appears at field; otherwise in global message
                        // // area
                        // facesContext.addMessage(clientId, msg);
                        // }
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

    /**
     * Searches the JSF component tree recursively for an input component
     * whose pass-through attribute "data-item" matches the given item name.
     *
     * @param context  current FacesContext
     * @param parent   starting component (usually UIViewRoot)
     * @param itemName the Imixs item name to search for (e.g. "invoice.number")
     * @return the client ID of the matching component, or null if not found
     */

    private String findClientIdByItemName(FacesContext context, UIComponent parent, String itemName, boolean debug) {
        for (UIComponent child : parent.getChildren()) {

            if (child instanceof UIInput) {
                Map<String, Object> passthroughAttrs = child.getPassThroughAttributes(false);
                if (passthroughAttrs != null) {
                    Object dataItem = passthroughAttrs.get("data-item");

                    // Evaluate the EL expression if it is a ValueExpression object
                    if (dataItem instanceof ValueExpression) {
                        dataItem = ((ValueExpression) dataItem).getValue(context.getELContext());
                    }

                    if (debug) {
                        logger.info("│   ├── resolved data-item: " + dataItem);
                    }

                    if (itemName.equals(dataItem)) {
                        logger.info("│   └── MATCH FOUND: " + child.getClientId(context));
                        return child.getClientId(context);
                    }
                }
            }

            // Recurse into children
            String found = findClientIdByItemName(context, child, itemName, debug);
            if (found != null) {
                return found;
            }

            // Recurse into facets
            for (UIComponent facet : child.getFacets().values()) {
                String found2 = findClientIdByItemName(context, facet, itemName, debug);
                if (found2 != null) {
                    return found2;
                }
            }
        }
        return null;
    }
}
