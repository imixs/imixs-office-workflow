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
package org.imixs.workflow.office.dataview;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

import org.imixs.workflow.FileData;

import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;

/**
 * The DataViewPOIHelper provides methods to update a excel template with the
 * help of the Apache POI framework.
 * 
 * 
 * @author rsoika
 * @version 1.0
 */

public class DataViewPOIHelper {
    private static Logger logger = Logger.getLogger(DataViewPOIHelper.class.getName());
    public static final String ERROR_CONFIG = "CONFIG_ERROR";

    /**
     * Helper method to initialize a file download
     *
     * @throws IOException
     */
    public static void downloadExcelFile(FileData fileData) throws IOException {

        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();

        externalContext.responseReset();
        externalContext.setResponseContentType("application/vnd.ms-excel");
        externalContext.setResponseContentLength(fileData.getContent().length);
        externalContext.setResponseHeader("Content-Disposition", "attachment; filename=\"" + fileData.getName() + "\"");

        OutputStream output = externalContext.getResponseOutputStream();

        // Now you can write the InputStream of the file to the above OutputStream the
        // usual way.
        output.write(fileData.getContent());

        facesContext.responseComplete(); // Important! Otherwise Faces will attempt to render the response which
                                         // obviously will fail since it's already written with a file and closed.
    }

}
