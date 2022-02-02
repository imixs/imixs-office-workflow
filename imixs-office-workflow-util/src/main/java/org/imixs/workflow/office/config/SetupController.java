/*******************************************************************************
 *  Imixs Workflow 
 *  Copyright (C) 2001, 2011 Imixs Software Solutions GmbH,  
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
 *  Project: 
 *  	http://www.imixs.org
 *  	http://java.net/projects/imixs-workflow
 *  
 *  Contributors:  
 *  	Imixs Software Solutions GmbH - initial API and implementation
 *  	Ralph Soika - Software Developer
 *******************************************************************************/

package org.imixs.workflow.office.config;

import java.util.Optional;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.imixs.marty.profile.ProfileService;
import org.imixs.marty.team.TeamController;
import org.imixs.workflow.WorkflowKernel;
import org.imixs.workflow.engine.DocumentService;
import org.imixs.workflow.engine.ModelService;
import org.imixs.workflow.exceptions.AccessDeniedException;
import org.imixs.workflow.exceptions.ModelException;
import org.imixs.workflow.office.model.ModelController;

/**
 * This SetupController extends the ConfigController and holds the data from the
 * configuration entity 'BASIC'. This is the general configuration entity used
 * by Imixs-Office-Workflow.
 * <p>
 * The item 'properties' managed by the 'BASIC' configuration entity is also
 * used by the MartyConfigSource to provide optional config parameters.
 * <p>
 * Since version 4.5.1 the SetupController provides a list of fixed UserGroups.
 * @see AccessRoleController
 * 
 * @see PropertiesConfigSource
 * @author rsoika
 * 
 */
@Named
@ApplicationScoped
public class SetupController extends ConfigController {

    private static final long serialVersionUID = 1L;

    public final static String CONFIGURATION_NAME = "BASIC";
    public final static int DEFAULT_PORTLET_SIZE = 5;

 

    @Inject
    @ConfigProperty(name = "setup.system.model", defaultValue = "")
    Optional<String> systemModelVersion;

    @Resource(lookup = "java:module/ModuleName")
    private String moduleName;

    @Resource(lookup = "java:app/AppName")
    private String appName;

    @Inject
    protected ModelController modelController;

    @Inject
    protected TeamController teamController;

    @EJB
    protected DocumentService documentService;

    @EJB
    protected ModelService modelService;

    @EJB
    protected PropertiesLoader martyPropertyLoader;

    @EJB
    protected ProfileService profileService;

    private static Logger logger = Logger.getLogger(SetupController.class.getName());

    public SetupController() {
        super();
        // set name
        this.setName(CONFIGURATION_NAME);
    }

    /**
     * This method loads the config entity. If the entity did not yet exist, the
     * method creates one.
     * 
     *
     */
    @PostConstruct
    @Override
    public void init() {

        super.init();

        // test systemModelVersion
        if (!systemModelVersion.isPresent() || systemModelVersion.get().isEmpty()) {
            logger.warning("Missing imixs.property named 'setup.system.model' - system model can not be validated!");
        } else {
            // try to load system model
            try {
                modelService.getModel(systemModelVersion.get());
                logger.info("...System Model '" + systemModelVersion + "' OK");
            } catch (ModelException e) {
                // no model found!
                logger.warning(
                        "Missing system model - please upload the system model version '" + systemModelVersion + "'");
            }

        }

        // if the BASIC configuration was not yet saved before we need to
        // Initialize it with a default setup
        if (!getWorkitem().hasItem(WorkflowKernel.UNIQUEID)) {         
            getWorkitem().replaceItemValue("keyenableuserdb", true);
            this.save();
        }
    }

    /**
     * Returns the sortBy criteria form the config workitem or the default value
     * '$modified' if not yet defined.
     * 
     * @return
     */
    public String getSortBy() {
        String result = getWorkitem().getItemValueString("sortby");
        if (result.isEmpty()) {
            return "$lasteventdate";
        } else {
            return result;
        }

    }

    /**
     * Returns the max count of entries for a front-end protlet
     * 
     * @return
     */
    public int getPortletSize() {
        int count = getWorkitem().getItemValueInteger("portletViewCount");
        if (count <= 0) {
            count = DEFAULT_PORTLET_SIZE;
        }
        return count;
    }

    /**
     * Returns the sortorder form the config workitem or the default value 'true' if
     * not yet defined.
     * 
     * @return
     */
    public boolean getSortReverse() {
        String result = getWorkitem().getItemValueString("sortorder");
        if ("0".equals(result)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns the EAR application name. Useful for JNDI lookups
     * 
     * @return
     */
    public String getAppName() {
        return appName;
    }

    /**
     * Returns the Web module name. Useful for JNDI lookups
     * 
     * @return
     */
    public String getModuleName() {
        return moduleName;
    }

    
    /**
     * This method resets the propertyService and modelController
     * 
     * @param event
     * @throws Exception
     */
    public void reset() {
        // reset services....
        logger.info("Reset application cache...");
        martyPropertyLoader.reset();
        profileService.reset();
        teamController.reset();
    }

    /**
     * After save the application property cache is reset automatically.
     */
    @Override
    public void save() throws AccessDeniedException {
        super.save();
        martyPropertyLoader.reset();
    }

    // public PropertyService getPropertyService() {
    // return propertyService;
    // }

}
