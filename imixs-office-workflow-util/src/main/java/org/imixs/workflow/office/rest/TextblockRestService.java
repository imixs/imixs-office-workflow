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

package org.imixs.workflow.office.rest;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Logger;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.Encoded;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context; 
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.imixs.archive.core.api.SnapshotRestService;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.engine.DocumentService;
import org.imixs.workflow.office.config.TextBlockService;

/**
 * The TextblockRestService provides methods to access the content of a textbock
 * in various ways.
 * 
 * @author rsoika
 * 
 */
@Named
@RequestScoped
@Path("/textblock")
@Produces({ MediaType.TEXT_HTML, MediaType.APPLICATION_XHTML_XML, MediaType.TEXT_PLAIN })
public class TextblockRestService implements Serializable {

    private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(TextblockRestService.class.getSimpleName());

    @EJB
    DocumentService entityService;

    @Inject
    TextBlockService textBlockService;
    
    @Inject
    SnapshotRestService snapshotRestService;

    @GET
    @Path("/{name}/html")
    @Produces({ MediaType.TEXT_HTML })
    public Response getTextBlock(@PathParam("name") String name) {

        ItemCollection textBlock = textBlockService.loadTextBlock(name);
        if (textBlock != null) {
            return Response.ok(textBlock.getItemValueString("txtcontent")).build();
        } else {
            return Response.ok("Not Found").status(Response.Status.NOT_ACCEPTABLE).build();
        }

    }

    /**
     * HTML Page returns the textblock content as a complete html page in standard
     * output. For this the method redirects the user to /pages/tex
     * 
     * @param name
     * @return
     * 
     */
    @GET
    @Path("/{name}/htmlpage")
    @Produces({ MediaType.TEXT_HTML })
    public Response getTextBlockAsPage(@PathParam("name") String name)  {

        ItemCollection textBlock = textBlockService.loadTextBlock(name);
        if (textBlock != null) {
            try {
               return Response.temporaryRedirect(new URI("../pages/textbock.xhtml?id="+textBlock.getUniqueID())).build();
            } catch (URISyntaxException e) {
                return Response.ok("Not Found").status(Response.Status.NOT_ACCEPTABLE).build();
            }
        } else {
            return Response.ok("Not Found").status(Response.Status.NOT_ACCEPTABLE).build();
        }

    }

    @GET
    @Path("/{name}/text")
    @Produces({ MediaType.TEXT_PLAIN })
    public Response getTextBlockPlain(@PathParam("name") String name) {

        ItemCollection textBlock = textBlockService.loadTextBlock(name);
        if (textBlock != null) {
            return Response.ok(textBlock.getItemValueString("txtcontent")).build();
        } else {
            return Response.ok("Not Found").status(Response.Status.NOT_ACCEPTABLE).build();
        }

    }
    
    @GET
    @Path("/{name}/file/{file}")
    @Produces({ MediaType.TEXT_PLAIN })
    public Response getTextblockFile(@PathParam("name") String name, @PathParam("file") @Encoded String file,
            @Context UriInfo uriInfo) {
        ItemCollection textBlock = textBlockService.loadTextBlock(name);
        return snapshotRestService.getWorkItemFile(textBlock.getUniqueID(), file, uriInfo);
    }
}
