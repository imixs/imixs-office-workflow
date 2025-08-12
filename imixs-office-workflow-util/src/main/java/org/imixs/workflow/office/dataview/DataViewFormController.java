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

import java.util.logging.Logger;

import org.imixs.workflow.dataview.DataViewController;
import org.imixs.workflow.exceptions.ModelException;
import org.imixs.workflow.office.forms.CustomFormController;

import jakarta.enterprise.context.ConversationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * The DataViewFormController extends the DataViewController and updates the
 * CustomFormController. This controller is used to display the filter form
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
public class DataViewFormController extends DataViewController {

    private static final long serialVersionUID = 1L;

    @Inject
    CustomFormController customFormController;

    private static Logger logger = Logger.getLogger(DataViewFormController.class.getName());

    /**
     * This method overloads the onLoad method and updates the customFormController
     * with the form definition
     */
    public void onLoad() {

        super.onLoad();

        try {
            customFormController.computeFieldDefinition(filter);
        } catch (ModelException e) {
            logger.warning("Failed to compute custom form sections: " + e.getMessage());
        }

    }

}
