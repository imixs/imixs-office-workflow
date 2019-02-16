/*******************************************************************************
 *  Imixs IX Workflow Technology
 *  Copyright (C) 2001, 2008 Imixs Software Solutions GmbH,  
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
 *******************************************************************************/
package com.imixs.workflow.office.docs.util;

import java.util.List;
import java.util.logging.Logger;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RunAs;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.enterprise.event.Observes;

import org.imixs.marty.ejb.SetupEvent;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.engine.DocumentService;
import org.imixs.workflow.engine.PropertyService;
import org.imixs.workflow.engine.WorkflowService;
import org.imixs.workflow.exceptions.AccessDeniedException;
import org.imixs.workflow.exceptions.ModelException;
import org.imixs.workflow.exceptions.PluginException;
import org.imixs.workflow.exceptions.ProcessingErrorException;
import org.imixs.workflow.exceptions.QueryException;

/**
 * The SetupDocsService EJB initializes the system settings by creating a
 * default process entity.
 * 
 * @author rsoika
 * 
 */
@DeclareRoles({ "org.imixs.ACCESSLEVEL.MANAGERACCESS" })
@RunAs("org.imixs.ACCESSLEVEL.MANAGERACCESS")
@Singleton
public class DocsSetupService {

	@EJB
	protected PropertyService propertyService;

	@EJB
	DocumentService documentService;

	@EJB
	WorkflowService workflowService;
	private static Logger logger = Logger.getLogger(DocsSetupService.class.getName());

	/**
	 * This method updates the UserGroup List be reaction on the CDI event
	 * UserGroupEvent. The method uses an internal caching mechanism to avoid
	 * multiple database lookups.
	 */
	public void onUserGroupEvent(@Observes SetupEvent setupEvent) {

		logger.info("....SetupDocsService started...");
		ItemCollection rootProcess = getProcess();
		try {

			if (rootProcess == null) {
				logger.info("....creating root process...");
				rootProcess = new ItemCollection();
				rootProcess.model(getSystemWorkflowVersion()).task(300).event(10);
				rootProcess.replaceItemValue("txtname", "Posteingang");

				workflowService.processWorkItem(rootProcess);

			} else {
				logger.info("....root process already exits...");
			}

		} catch (AccessDeniedException | ProcessingErrorException | PluginException | ModelException e) {
			logger.warning("Unable to create root process: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Returns the system workflow version defined by the imixs.properties file
	 * 
	 * <pre>
	 * {@code
	 * system.model.version
	 * }
	 * </pre>
	 * 
	 * @return the system Model version.
	 */
	public String getSystemWorkflowVersion() {
		String version = null;
		version = this.propertyService.getProperties().getProperty("system.model.version", "");
		if (version.isEmpty()) {
			logger.warning("property 'system.model.version' is not defined!");
		}
		return version;
	}

	/**
	 * Returns the root process
	 * 
	 * @return
	 */
	public ItemCollection getProcess() {
		ItemCollection rootProcess = null;
		;
		List<ItemCollection> processes = null;
		try {
			processes = documentService.find("(type:\"process\" AND txtname:\"Posteingang\")", 1, 0);
		} catch (QueryException e) {

			e.printStackTrace();
		}
		if (processes != null && processes.size() > 0) {
			rootProcess = processes.get(0);
		}
		return rootProcess;
	}

}