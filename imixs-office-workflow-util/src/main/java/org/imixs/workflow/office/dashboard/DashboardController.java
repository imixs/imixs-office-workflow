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

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.office.config.SetupController;
import org.imixs.workflow.office.forms.CustomFormController;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * The DataViewFormController extends the DataViewController and updates the
 * CustomFormController. This controller is used to display the filter form
 * <p>
 * Note: This bean is SessionScoped to cash data during the user session
 * 
 * 
 * @author rsoika
 * @version 1.0
 */
@Named
@SessionScoped
public class DashboardController implements Serializable {

    private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(DashboardFormController.class.getName());

    @Inject
    SetupController setupController;

    private String setupConfigUniqueID = null;

    private ItemCollection dashboardData = new ItemCollection();

    /**
     * This method loads the dashboard form information and prefetches the data
     */
    /**
     * This method load the config entity after postContstruct. If no Entity
     * exists than the ConfigService EJB creates a new config entity.
     * 
     */
    @PostConstruct
    public void init() {
        String content = setupController.getWorkitem().getItemValueString("dashboard.form");
        setupConfigUniqueID = setupController.getWorkitem().getUniqueID();
        dashboardData.setItemValue(CustomFormController.ITEM_CUSTOM_FORM, content);
    }

    public String getDashboardForm() {
        String content = setupController.getWorkitem().getItemValueString("dashboard.form");
        return content;
    }

    public ItemCollection getDashboardData() {
        return dashboardData;
    }

    public void setDashboardData(ItemCollection dashboardData) {
        this.dashboardData = dashboardData;
    }

    public String getSetupConfigUniqueID() {
        if (setupConfigUniqueID == null) {
            setupConfigUniqueID = "";
        }
        return setupConfigUniqueID;
    }

}
