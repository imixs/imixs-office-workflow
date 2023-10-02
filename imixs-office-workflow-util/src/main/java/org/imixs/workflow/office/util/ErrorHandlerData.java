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

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;

/**
 * The ErrorHandlerData provides data about the cause of an exception.
 * The ErrorHanlderData object is used by the ErrorHandlerServlet.
 * 
 * @author rsoika
 */
@Named
@SessionScoped
public class ErrorHandlerData implements Serializable {

    private static final long serialVersionUID = 1L;

    private String message = null;
    private String exception = null;
    private String exceptionType = null;

    public String getExceptionType() {
        return exceptionType;
    }

    private String uri = null;
    private int statusCode = 500;

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
        if (exception.contains(".")) {
            exceptionType = exception.substring(exception.lastIndexOf(".") + 1);
        }
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public ErrorHandlerData() {
        super();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
