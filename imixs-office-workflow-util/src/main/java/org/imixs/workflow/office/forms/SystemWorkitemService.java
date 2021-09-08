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

package org.imixs.workflow.office.forms;

import java.util.logging.Logger;

import javax.annotation.security.RunAs;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.exceptions.AccessDeniedException;
import org.imixs.workflow.exceptions.ModelException;
import org.imixs.workflow.exceptions.PluginException;
import org.imixs.workflow.exceptions.ProcessingErrorException;

/**
 * This EJB provides a administrative access to the service layer. The bean can
 * be used for processing workitems with anonymous users. The bean runns with
 * manager access. Be careful in usage.
 * 
 * glassfish-ejb-jar.xml
 * <p>
 * <code>
 	  <ejb>
			<ejb-name>SystemWorkitemService</ejb-name>
			<principal>
				<name>IMIXS-WORKFLOW-Service</name>
			</principal>
		</ejb>

 * </code>
 * 
 * @author rsoika
 * 
 */

@Stateless
@LocalBean
@RunAs("org.imixs.ACCESSLEVEL.MANAGERACCESS")
public class SystemWorkitemService extends WorkitemService {

	private static Logger logger = Logger.getLogger(SystemWorkitemService.class
			.getName());

	@Override
	public ItemCollection processWorkItem(ItemCollection aworkitem)
			throws AccessDeniedException, ProcessingErrorException,
			PluginException, ModelException {

		logger.fine("SystemWorkitemService - processWorkItem with org.imixs.ACCESSLEVEL.MANAGERACCESS");
		return super.processWorkItem(aworkitem);
	}


	/**
	 * The method saves an entity
	 * 
	 * @throws AccessDeniedException
	 * 
	 * @throws Exception
	 * 
	 * 
	 */
	public ItemCollection save(ItemCollection entity)
			throws AccessDeniedException {
		logger.fine("Entity saved by SystemWorkitemService running with org.imixs.ACCESSLEVEL.MANAGERACCESS");
		return workflowService.getDocumentService().save(entity);

	}
}
