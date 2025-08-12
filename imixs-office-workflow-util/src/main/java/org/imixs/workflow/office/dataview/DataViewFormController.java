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
package org.imixs.workflow.office.dataview;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.imixs.archive.core.SnapshotService;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.dataview.DataViewCache;
import org.imixs.workflow.dataview.DataViewController;
import org.imixs.workflow.dataview.DataViewDefinitionController;
import org.imixs.workflow.engine.DocumentService;
import org.imixs.workflow.engine.WorkflowService;
import org.imixs.workflow.exceptions.ModelException;
import org.imixs.workflow.exceptions.QueryException;
import org.imixs.workflow.faces.data.ViewController;
import org.imixs.workflow.faces.data.ViewHandler;
import org.imixs.workflow.faces.data.WorkflowController;
import org.imixs.workflow.office.forms.CustomFormController;
import org.imixs.workflow.office.forms.CustomFormSection;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.Conversation;
import jakarta.enterprise.context.ConversationScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;

/**
 * The DataViewController is used to display a data view
 * <p>
 * The controller uses the uniqueId from the URL to load the definition.
 * The bean reads optional cached query data form
 * a session scoped cache EJB and reloads the last state. This is useful for
 * situations where the user navigates to a new page (e.g. open a workitem) and
 * late uses browser history back.
 * <p>
 * Note: This bean is ConversationScoped, because it uses the
 * CustomFormController which expects conversion scope!
 * 
 * 
 * @author rsoika
 * @version 1.0
 */
@Named
@ConversationScoped
public class DataViewFormController extends ViewController {

    private static final long serialVersionUID = 1L;

    public static final String ERROR_CONFIG = "CONFIG_ERROR";
    public static final int MAX_ROWS = 9999;

    private List<CustomFormSection> sections = null;
    private List<ItemCollection> viewItemDefinitions = null;
    protected ItemCollection dataViewDefinition = null;
    private ItemCollection filter;
    private String query;
    private String errorMessage;

    @Inject
    DataViewCache dataViewCache;

    @Inject
    private Conversation conversation;

    @Inject
    private DocumentService documentService;

    @Inject
    private WorkflowService workflowService;

    @Inject
    SnapshotService snapshotService;

    @Inject
    CustomFormController customFormController;

    @Inject
    WorkflowController workflowController;

    @Inject
    DataViewController dataViewController;

    @Inject
    ViewHandler viewHandler;

    private static Logger logger = Logger.getLogger(DataViewFormController.class.getName());

    @Override
    @PostConstruct
    public void init() {
        super.init();
        // this.setQuery(dataViewController.getQuery());
        this.setSortBy("$modified");
        this.setSortReverse(false);
        this.setPageSize(100);
        this.setLoadStubs(false);
    }

    /**
     * This method loads the custom form sections
     */
    public void onLoad() {
        String uniqueid = null;

        // Important: start a new conversation because of the usage of the
        // CustomFormController!
        startConversation();

        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (!facesContext.isPostback() && !facesContext.isValidationFailed()) {
            // ...
            FacesContext fc = FacesContext.getCurrentInstance();
            Map<String, String> paramMap = fc.getExternalContext().getRequestParameterMap();
            // try to extract the uniqueid form the query string...
            uniqueid = paramMap.get("id");
            if (uniqueid == null || uniqueid.isEmpty()) {
                // alternative 'workitem=...'
                uniqueid = paramMap.get("workitem");
            }
            dataViewDefinition = documentService.load(uniqueid);
        }

        if (uniqueid != null && !uniqueid.isEmpty()) {
            filter = dataViewCache.get(uniqueid);
        } else {
            filter = new ItemCollection();
        }

        try {
            // Init new Filter....
            if (dataViewDefinition != null) {
                filter.setItemValue("txtWorkflowEditorCustomForm", dataViewDefinition.getItemValue("form"));
                filter.setItemValue("name", dataViewDefinition.getItemValueString("name"));
                filter.setItemValue("description", dataViewDefinition.getItemValueString("description"));
                viewItemDefinitions = DataViewDefinitionController
                        .computeDataViewItemDefinitions(dataViewDefinition);

                customFormController.computeFieldDefinition(filter);
                sections = customFormController.getSections();

                // Update View Handler settings
                String sortBy = dataViewDefinition.getItemValueString("sort.by");
                if (sortBy.isEmpty()) {
                    sortBy = "$modified";
                }
                this.setSortBy(sortBy);
                this.setSortReverse(dataViewDefinition.getItemValueBoolean("sort.reverse"));
                this.setPageIndex(filter.getItemValueInteger("pageIndex"));
                if (!filter.getItemValueString("query").isEmpty()) {
                    query = filter.getItemValueString("query");
                    // Prefetch data to update total count and page count
                    viewHandler.getData(this);
                }
            }
        } catch (ModelException | QueryException e) {
            logger.warning("Failed to load dataview definition: " + e.getMessage());
        }

    }

    public ItemCollection getDataViewDefinition() {
        return dataViewDefinition;
    }

    public List<CustomFormSection> getSections() {
        return sections;
    }

    public List<ItemCollection> getViewItemDefinitions() {
        return viewItemDefinitions;
    }

    public ItemCollection getFilter() {
        return filter;
    }

    public void setFilter(ItemCollection filter) {
        this.filter = filter;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
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

}
