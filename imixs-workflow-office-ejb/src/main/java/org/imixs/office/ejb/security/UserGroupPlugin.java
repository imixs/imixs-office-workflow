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

package org.imixs.office.ejb.security;

import java.util.Collection;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.Plugin;
import org.imixs.workflow.WorkflowContext;
import org.imixs.workflow.exceptions.AccessDeniedException;
import org.imixs.workflow.exceptions.PluginException;
import org.imixs.workflow.plugins.jee.AbstractPlugin;

/**
 * This Plugin handles the update of the user group objects based on the 
 * current user profile.
 * 
 * @see UserGroupService
 * @author rsoika
 * @version 1.0
 * 
 */
public class UserGroupPlugin extends AbstractPlugin {
	UserGroupService userGroupService = null;;

	int sequenceNumber = -1;
	ItemCollection workitem = null;
	private static Logger logger = Logger.getLogger("org.imixs.office");

	public void init(WorkflowContext actx) throws PluginException {
		super.init(actx);

		// lookup profile service EJB
		String jndiName = "ejb/UserGroupService";
		InitialContext ictx;
		try {
			ictx = new InitialContext();

			Context ctx = (Context) ictx.lookup("java:comp/env");
			userGroupService = (UserGroupService) ctx.lookup(jndiName);
		} catch (NamingException e) {
			throw new PluginException(
					"[UserGroupPlugin] unable to lookup UserGroupService: ",
					e);
		}
	}

	/**
	 * This method updates the user object and the group relation ships
	 * 
	 * @return
	 * @throws PluginException
	 */
	@Override
	public int run(ItemCollection documentContext,
			ItemCollection documentActivity) throws PluginException {

		workitem = documentContext;

		// check type
		String sType = workitem.getItemValueString("Type");
		if (!("profile".equals(sType) ))
			return Plugin.PLUGIN_OK;

		
		
		logger.info("[UserGroupPlugin] update profile....");
		userGroupService.updateUser(workitem);
		
		
		
		return Plugin.PLUGIN_OK;
	}

	@Override
	public void close(int status) throws PluginException {

	}

}
