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

package org.imixs.marty.jaxrs;

import java.io.Serializable;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.ws.rs.Encoded;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.engine.DocumentService;
import org.imixs.workflow.jaxrs.WorkflowRestService;

/**
 * The SnapshotRestService is a wrapper for the WorkflowRestService and provides a method 
 * to get a file content based on the $uniqueid of the origin workitem.
 * 
 * @author rsoika
 */
@Named("snapshotService")
@RequestScoped
@Path("/snapshot")
@Produces({ MediaType.TEXT_HTML, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.TEXT_XML })
public class SnapshotRestService implements Serializable {

	private static final long serialVersionUID = 1L;

	@EJB
	DocumentService documentService;

	@EJB
	WorkflowRestService workflowRestService;

	/**
	 * This method wraps the WorkflowRestService method 'getWorkItemFile' and
	 * verifies the target id which can be either a $snapshotid or the deprecated
	 * $blobWorkitemid.
	 * 
	 * Finally the method calls the origin method getWorkItemFile
	 * 
	 * @param uniqueid
	 * @return
	 */
	@GET
	@Path("/{uniqueid}/file/{file}")
	public Response getWorkItemFile(@PathParam("uniqueid") String uniqueid, @PathParam("file") @Encoded String file,
			@Context UriInfo uriInfo) {

		ItemCollection workItem;
		String sTargetID = uniqueid;
		// load workitem
		workItem = documentService.load(uniqueid);
		// test if we have a $snapshotid
		if (workItem != null && workItem.hasItem("$snapshotid")) {
			sTargetID = workItem.getItemValueString("$snapshotid");
		} else {
			// support deprecated blobworkitem....
			if (workItem != null && workItem.hasItem("$blobworkitem")) {
				sTargetID = workItem.getItemValueString("$blobworkitem");
			}
		}
		return workflowRestService.getWorkItemFile(sTargetID, file, uriInfo);
	}

}
