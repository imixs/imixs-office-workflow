/*******************************************************************************
 *  Imixs Workflow Technology
 *  Copyright (C) 2003, 2008 Imixs Software Solutions GmbH,  
 *  http://www.imixs.com
 *  
 *  This program is free software; you can redistribute it and/or 
 *  modify it under the terms of the GNU General Public License 
 *  as published by the Free Software Foundation; either version 2 
 *  of the License, or (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful, 
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 *  General Public License for more details.
 *  
 *  You can receive a copy of the GNU General Public
 *  License at http://www.gnu.org/licenses/gpl.html
 *  
 *  Contributors:  
 *  	Imixs Software Solutions GmbH - initial API and implementation
 *  	Ralph Soika
 *  
 *******************************************************************************/
package org.imixs.workflow.office.forms;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.imixs.workflow.dataview.DataViewService;
import org.imixs.workflow.exceptions.QueryException;
import org.imixs.workflow.faces.data.WorkflowController;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.Conversation;
import jakarta.enterprise.context.ConversationScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;

/**
 * The DataViewFormSectionController provides methods to display a data view in
 * as a
 * custom form section
 * 
 * @see pages/workitems/sections/dataview.xhtml
 * @author rsoika
 * @version 1.0
 */

@Named
@ConversationScoped
public class DataViewSectionController implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int MAX_SEARCH_RESULT = 1000;
    public static Logger logger = Logger.getLogger(DataViewSectionController.class.getName());

    @Inject
    WorkflowController workflowController;

    @Inject
    private Conversation conversation;

    @Inject
    DataViewService dataViewService;

    private Map<String, DataViewSectionDataSet> dataSets = null;

    @PostConstruct
    public void init() {
        startConversation();
        dataSets = new HashMap<>();
    }

    /**
     * Starts a new conversation
     */
    protected void startConversation() {
        if (conversation.isTransient()) {
            conversation.setTimeout(
                    ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest())
                            .getSession().getMaxInactiveInterval() * 1000);
            conversation.begin();
            logger.log(Level.FINEST, "......start new conversation, id={0}",
                    conversation.getId());
        }
    }

    /**
     * Returns a single value from a Option key/value list
     * 
     * @param options - options
     * @param key     - option key
     * @return option value
     */
    public String getOptionValue(String options, String key) {
        // Null checks
        if (options == null || key == null || options.trim().isEmpty() || key.trim().isEmpty()) {
            return null;
        }

        // Split options into key/value pairs (separated by semicolon)
        String[] pairs = options.split(";");

        for (String pair : pairs) {
            // Split each pair into key and value (separated by equals sign)
            String[] keyValue = pair.split("=", 2); // Limit to 2 in case value contains "="

            if (keyValue.length == 2) {
                String currentKey = keyValue[0].trim();
                String currentValue = keyValue[1].trim();

                // Check if the searched key was found
                if (key.equals(currentKey)) {
                    return currentValue;
                }
            }
        }

        // Key not found
        return null;
    }

    /**
     * Initializes a new DataViewSectionDataSet based on the given options
     * (parameter 'name').
     * 
     * The expected format of the options string is:
     * <p>
     * "name=DATAVIEWNAME"
     * <p>
     * The method loads the corresponding dataView definition immediately and stores
     * the dataSet in a local cache.
     * 
     * @return view result
     * @throws QueryException
     */
    public DataViewSectionDataSet loadDataSet(String options) throws QueryException {
        DataViewSectionDataSet dataSet = dataSets.get(options);
        // Extract and set dataViewName from options
        if (dataSet == null) {
            String _dataViewName = getOptionValue(options, "name");
            if (_dataViewName != null) {
                logger.info("...build new dataSet by options: " + options);
                dataSet = new DataViewSectionDataSet(_dataViewName, workflowController.getWorkitem(), dataViewService);
                dataSets.put(options, dataSet);
            }
        }
        return dataSet;
    }

}
