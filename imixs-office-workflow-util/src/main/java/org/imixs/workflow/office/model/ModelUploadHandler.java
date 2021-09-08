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

package org.imixs.workflow.office.model;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import org.imixs.workflow.ItemCollection;

/**
 * The ModelUploadHandler is used by the ModelController and provides a
 * ItemCollection with uploaded model information.
 * 
 * @author rsoika
 */
@Named()
@SessionScoped
public class ModelUploadHandler implements Serializable {

	private static final long serialVersionUID = 1L;

	private ItemCollection modelUploads;

	public ModelUploadHandler() {
		super(); 
		reset(); 
	}

	public ItemCollection getModelUploads() {
		if (modelUploads == null) {
			modelUploads = new ItemCollection();
		}
		return modelUploads;
	}

	public void setModelUploads(ItemCollection _modelUploads) {
		this.modelUploads = _modelUploads;
	}

	
	public void reset() {
		modelUploads = new ItemCollection();
	}
}
