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
package com.imixs.workflow.office.web.demo;

import java.io.Serializable;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.event.AjaxBehaviorEvent;
import javax.inject.Inject;
import javax.inject.Named;

import org.imixs.marty.config.ConfigMultiController;
import org.imixs.marty.workflow.WorkflowController;
import org.imixs.workflow.ItemCollection;

/**
 * Dieser Controller aktualisiert die Supplier Daten bei Auswahl eines Supliers
 * aus der vorkonfigurierten Supplier Liste
 * 
 * @see sub_address.xhtml
 * 
 * @author rsoika
 * 
 */
@Named
@SessionScoped
public class SupplierController extends ConfigMultiController implements Serializable {

	private static final long serialVersionUID = 1L;

	
	@Inject
	protected WorkflowController workflowController;

	private static Logger logger = Logger.getLogger(SupplierController.class.getName());

	
	@PostConstruct
	public void init() {
		// set type
		this.setDefaultType("supplier");
	}

	

	
	/**
	 * triggered by f:ajax event in sub_address.xhtml 
	 * 
	 * @return
	 */
	public void updateSupplierData(AjaxBehaviorEvent event) {
		
		if (workflowController.getWorkitem()!=null) {
			String id=workflowController.getWorkitem().getItemValueString("_supplierid");
			
			if (!id.isEmpty()) {
				ItemCollection supplier=workflowController.getDocumentService().load(id);
				if (supplier!=null) {
					logger.fine("Updating supplier data");
					workflowController.getWorkitem().replaceItemValue("_supplier", supplier.getItemValueString("txtName"));
					workflowController.getWorkitem().replaceItemValue("_address", supplier.getItemValueString("_address"));
					
				}
			}
			
		}

	}
}
