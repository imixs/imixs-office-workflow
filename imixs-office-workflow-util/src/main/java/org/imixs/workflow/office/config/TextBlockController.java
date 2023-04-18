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
package org.imixs.workflow.office.config;

import java.io.Serializable;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

import org.imixs.workflow.ItemCollection;

/**
 * The TextBlockController is a CDI bean providing a methods to load a textBlock
 * from the TextblockService EJB which is using an ApplicationScoped cache. The
 * controller can be used by front-end pages to display a textBlock.
 * 
 * @See TextBlockService
 * @author rsoika
 * 
 */
@Named
@RequestScoped
public class TextBlockController implements Serializable {

	private static final long serialVersionUID = 1L;

	@EJB
	private TextBlockService textBlockService;

	/**
	 * Loads a text-block item by Name or uniqueID from the ApplicationScoped
	 * TextBlockService EJB.
	 * 
	 * @param name
	 * @return
	 */
	public ItemCollection load(String name) {
		return textBlockService.loadTextBlock(name);
	}

}
