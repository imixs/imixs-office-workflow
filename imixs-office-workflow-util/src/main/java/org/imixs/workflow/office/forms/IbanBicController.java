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
package org.imixs.workflow.office.forms;

import java.io.Serializable;
import java.util.logging.Logger;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;

import org.imixs.workflow.faces.data.WorkflowController;

/**
 * The IbanBicController is used to validate IBAN and BIC input data.
 * 
 * @author rsoika
 * @version 1.0
 */

@Named
@RequestScoped
public class IbanBicController implements Serializable {

	public static final String IBAN_PATTERN = "^$|(^[A-Z]{2}(?:[ ]?[A-Z0-9]){13,32}$)";
	public static final String BIC_PATTERN = "^$|(^([a-zA-Z]{4}[a-zA-Z]{2}[a-zA-Z0-9]{2}([a-zA-Z0-9]{3})?))";

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(IbanBicController.class.getName());

	@Inject
	WorkflowController workflowController;

	@Valid
	@Pattern(regexp = BIC_PATTERN)
	public String getBic() {
		logger.finest("......validate _cdtr_bic...");
		return workflowController.getWorkitem().getItemValueString("_bic");
	}

	public void setBic(String bic) {
		workflowController.getWorkitem().setItemValue("_bic", bic);
	}

	@Valid
	@Pattern(regexp = IBAN_PATTERN)
	public String getIban() {
		logger.finest("......validate _cdtr_iban...");
		return workflowController.getWorkitem().getItemValueString("_iban");
	}

	public void setIban(String iban) {
		workflowController.getWorkitem().setItemValue("_iban", iban);
	}

}
