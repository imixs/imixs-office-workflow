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
package org.imixs.workflow.office.dashboard;

import java.io.Serializable;
import java.util.logging.Logger;

import org.imixs.marty.profile.UserController;
import org.imixs.workflow.engine.DocumentEvent;
import org.imixs.workflow.exceptions.ModelException;
import org.imixs.workflow.faces.data.WorkflowController;
import org.imixs.workflow.office.forms.CustomFormController;

import jakarta.enterprise.context.ConversationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * The DataViewFormController updates the
 * CustomFormController. This controller is used to display the user dashboard.
 * The controler holads a instance of the user profile.
 * <p>
 * Note: This bean is ConversationScoped, because it uses the
 * CustomFormController which expects conversion scope!
 * 
 * @author rsoika
 * @version 1.0
 */
@Named
@ConversationScoped
public class DashboardFormController implements Serializable {

    private static final long serialVersionUID = 1L;

    private static Logger logger = Logger.getLogger(DashboardFormController.class.getName());

    @Inject
    CustomFormController customFormController;

    @Inject
    DashboardController dashboardController;

    @Inject
    UserController userController;

    @Inject
    WorkflowController workflowController;

    /**
     * This method loads the dashboard form information and prefetches the data
     */
    public void onLoad() {
        try {
            workflowController.setWorkitem(userController.getWorkitem());
            customFormController.computeFieldDefinition(dashboardController.getDashboardData());
        } catch (ModelException e) {
            logger.warning("Failed to compute custom form sections: " + e.getMessage());
        }

    }

    /**
     * Reacts on ON_DOCUMENT_SAVE of the config item and reloads the layout
     * 
     * @see DocumentEvent
     * @param documentEvent
     */
    public void onDocumentEvent(@Observes DocumentEvent documentEvent) {

        if (documentEvent.getEventType() == DocumentEvent.ON_DOCUMENT_SAVE
                &&
                dashboardController.getSetupConfigUniqueID().equals(documentEvent.getDocument().getUniqueID())) {
            logger.info("reload dashboard layout...");
            dashboardController.init();
        }

    }

}
