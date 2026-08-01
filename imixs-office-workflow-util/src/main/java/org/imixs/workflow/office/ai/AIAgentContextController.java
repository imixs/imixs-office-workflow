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
package org.imixs.workflow.office.ai;

import java.io.Serializable;
import java.util.logging.Logger;

import org.imixs.ai.ImixsAIContextHandler;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.exceptions.ModelException;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * The AIAgentContextController provides methods to extract the last assistant
 * answer from a ai conversation context.
 * 
 * @author rsoika
 */
@Named("aiAgentContextController")
@RequestScoped
public class AIAgentContextController implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	ImixsAIContextHandler contextHandler;

	private static Logger logger = Logger.getLogger(AIAgentContextController.class.getName());

	private String lastAnswer = "";

	/**
	 * This method returns the last assistant message from the given ai context.
	 * 
	 * @return
	 * @throws ModelException
	 */
	public String loadLastAnswer(ItemCollection workitem, String contextItemName) {

		contextHandler.importContext(workitem, contextItemName);
		lastAnswer = contextHandler.getLastMessage(ImixsAIContextHandler.ROLE_ASSISTANT);
		return lastAnswer;
	}

}
