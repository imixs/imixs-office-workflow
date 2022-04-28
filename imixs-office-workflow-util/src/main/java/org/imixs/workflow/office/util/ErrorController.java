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

package org.imixs.workflow.office.util;

import java.io.Serializable;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

/**
 * The ErrorController provides methods to analyse a exception stack trace.
 * The controller is used in the errorhandler.xhtml page to redirect the user to a corresponding error page.
 * 
 * @author rsoika
 */
@Named
@RequestScoped
public class ErrorController implements Serializable {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(ErrorController.class.getName());

	private String message=null;
	
	public ErrorController() {
		super();
	}

    /**
     * Returns true if the exception is caused by a specific Exception type
     * @param e
     * @return
     */
    public boolean isCausedBy(Exception e, String excpetionType) {
        if (e==null) {
            return false;
        }
      
        // iterate over the full exception stack trace
        Throwable cause = e.getCause();
        while (cause!=null) {
            if (cause.toString().contains(excpetionType)) {
                logger.warning("...exception caused by " + excpetionType );
                setMessage(cause.getMessage());
                return true;
            }
            // take next cause from stack
            cause=cause.getCause();
        }
        return false;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    
}
