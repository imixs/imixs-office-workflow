/****************************************************************************
 * Copyright (c) 2022-2025 Imixs Software Solutions GmbH and others.
 * https://www.imixs.com
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * This Source Code may also be made available under the terms of the
 * GNU General Public License, version 2 or later (GPL-2.0-or-later),
 * which is available at https://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0-or-later
 ****************************************************************************/

package org.imixs.workflow.office.plugins;

import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.engine.plugins.AbstractPlugin;
import org.imixs.workflow.exceptions.AccessDeniedException;
import org.imixs.workflow.exceptions.ModelException;
import org.imixs.workflow.exceptions.PluginException;
import org.imixs.workflow.exceptions.ProcessingErrorException;
import org.imixs.workflow.util.XMLParser;

/**
 * The Imixs Split&Join Plugin provides functionality to create and update
 * sub-process instances from a workflow event in an origin process. It is also
 * possible to update the origin process from the sub-process instance.
 * 
 * The plugin evaluates the txtactivityResult and the items with the following
 * names:
 * 
 * subprocess_create = create a new subprocess assigned to the current workitem
 * 
 * subprocess_update = update an existing subprocess assigned to the current
 * workitem
 * 
 * origin_update = update the origin process assigned to the current workitem
 * 
 * 
 * A subprocess will contain the $UniqueID of the origin process stored in the
 * property $uniqueidRef. The origin process will contain a link to the
 * subprocess stored in the property txtworkitemRef. So both workitems are
 * linked together.
 * 
 * 
 * @author Ralph Soika
 * @version 1.0
 * @see http://www.imixs.org/doc/engine/plugins/splitandjoinplugin.html
 * 
 */
public class WorkitemLinkPlugin extends AbstractPlugin {

    public static final String INVALID_FORMAT = "INVALID_FORMAT";
    public static final String METHOD_COPYDATA = "copydata";

    private static final Logger logger = Logger.getLogger(WorkitemLinkPlugin.class.getName());

    /**
     * The method evaluates the workflow activity result for items with name:
     * 
     * copydata
     * 
     * 
     * For each method a corresponding processing cycle will be started.
     * 
     * @throws @throws ProcessingErrorException @throws
     *                 AccessDeniedException @throws
     * 
     */
    @SuppressWarnings("unchecked")
    public ItemCollection run(ItemCollection workitem, ItemCollection event)
            throws PluginException, AccessDeniedException, ProcessingErrorException {
        ItemCollection evalItemCollection = getWorkflowContext().evalWorkflowResult(event, "workitemlink", workitem,
                false);

        if (evalItemCollection == null) {
            // no configuration found!
            return workitem;
        }

        try {
            // 1.) copy items from ref workitem
            if (evalItemCollection.hasItem(METHOD_COPYDATA)) {
                // extract the definitions...
                List<String> processValueList = evalItemCollection.getItemValue(METHOD_COPYDATA);
                copyData(processValueList, workitem);
            }

        } catch (ModelException e) {
            throw new PluginException(e.getErrorContext(), e.getErrorCode(), e.getMessage(), e);

        }

        return workitem;
    }

    /**
     * This method syncs the items from the ref workitem into this process instance
     * 
     * 
     * 
     * @param definitions
     * @param originWorkitem
     * @throws AccessDeniedException
     * @throws ProcessingErrorException
     * @throws PluginException
     * @throws ModelException
     */
    protected void copyData(final List<String> definitions, final ItemCollection workitem)
            throws AccessDeniedException, ProcessingErrorException, PluginException, ModelException {

        if (definitions == null || definitions.size() == 0) {
            // no definition found
            return;
        }

        // we iterate over each declaration of a SUBPROCESS_CREATE item....
        for (String processValue : definitions) {

            if (processValue.trim().isEmpty()) {
                // no definition
                continue;
            }
            // evaluate the item content (XML format expected here!)
            ItemCollection processData = XMLParser.parseItemStructure(processValue);
            boolean debug = processData.getItemValueBoolean("debug");
            logger.info("├── Copy Data...");
            if (processData != null) {
                String refItem = processData.getItemValueString("ref");
                String refId = workitem.getItemValueString(refItem);

                logger.info("│   ├── refItem: " + refItem);
                logger.info("│   ├── refId  : " + refId);

                ItemCollection refWorkitem = this.getWorkflowService().getDocumentService()
                        .load(refId);
                if (refWorkitem != null) {
                    // copy items
                    // now clone the field list...
                    copyItemList(processData.getItemValueString("items"), refWorkitem, workitem, debug);
                } else {
                    logger.warning("├── ⚠️ Ref Workitem not found!");
                }

            }
        }
        logger.info("├── ✅ copyData successful");
        return;

    }

    /**
     * This Method copies the fields defined in 'items' into the targetWorkitem.
     * Multiple values are separated with comma ','.
     * <p>
     * In case a item name contains '|' the target field name will become the right
     * part of the item name.
     * <p>
     * Example: {@code
     *   txttitle,txtfirstname
     *   
     *   txttitle|newitem1,txtfirstname|newitem2
     *   
     * }
     * 
     * <p>
     * Optional also reg expressions are supported. In this case mapping of the item
     * name is not supported.
     * <p>
     * Example: {@code
     *   (^artikel$|^invoice$),txtTitel|txtNewTitel
     *   
     *   
     * } A reg expression must be includes in brackets.
     * 
     */
    protected void copyItemList(String items, ItemCollection source, ItemCollection target, boolean debug) {
        // clone the field list...
        StringTokenizer st = new StringTokenizer(items, ",");
        while (st.hasMoreTokens()) {
            String field = st.nextToken().trim();

            // test if field is a reg ex
            if (field.startsWith("(") && field.endsWith(")")) {
                Pattern itemPattern = Pattern.compile(field);
                Map<String, List<Object>> map = source.getAllItems();
                for (String itemName : map.keySet()) {
                    if (itemPattern.matcher(itemName).find()) {
                        target.replaceItemValue(itemName, source.getItemValue(itemName));
                        if (debug) {
                            logger.info("│   ├── copy: " + itemName + " ...");
                        }
                    }
                }

            } else {
                // default behavior without reg ex
                int pos = field.indexOf('|');
                if (pos > -1) {
                    target.replaceItemValue(field.substring(pos + 1).trim(),
                            source.getItemValue(field.substring(0, pos).trim()));
                    if (debug) {
                        logger.info("│   ├── copy: " + source.getItemValue(field.substring(0, pos).trim()) + " -> "
                                + field.substring(pos + 1).trim());
                    }
                } else {
                    target.replaceItemValue(field, source.getItemValue(field));
                    if (debug) {
                        logger.info("│   ├── copy: " + field + " ...");
                    }
                }
            }
        }
    }

}
