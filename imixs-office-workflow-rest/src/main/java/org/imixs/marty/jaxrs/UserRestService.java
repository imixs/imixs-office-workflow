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
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.imixs.marty.ejb.ProfileService;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.xml.EntityCollection;
import org.imixs.workflow.xml.XMLItemCollectionAdapter;

/**
 * The UserRestService provides methods to access the marty user profile The
 * Service extends the imixs-workflow-jaxrs api.
 * 
 * 
 * @author rsoika
 * 
 */
@Named("userService")
@RequestScoped
@Path("/user")
@Produces({ "text/html", "application/xml", "application/json" })
public class UserRestService implements Serializable {

	private static final long serialVersionUID = 1L;

	@EJB
	org.imixs.workflow.jee.ejb.EntityService entityService;

	@EJB
	ProfileService profileService;

	/**
	 * Returns the users favorite workitems.
	 * 
	 * @return
	 */
	@GET
	@Path("/favorites")
	public EntityCollection getFavorites(
			@Context HttpServletRequest servletRequest) {
		Collection<ItemCollection> col = null;
		try {

			List<String> favorites = getFavoriteIds(servletRequest);
			if (favorites.size() <= 0)
				return XMLItemCollectionAdapter.putCollection(null);

			// create a JPQL statement....

			// create IN list
			String inStatement = "";
			for (String aID : favorites) {
				inStatement = inStatement + "'" + aID + "',";
			}
			// cut last ,
			inStatement = inStatement.substring(0, inStatement.length() - 1);

			String sQuery = "SELECT DISTINCT wi FROM Entity AS wi ";
			sQuery += " WHERE wi.type IN ('workitem','workitemarchive')";
			sQuery += " AND wi.id IN (" + inStatement + ")";
			sQuery += " ORDER BY wi.modified DESC";

			col = entityService.findAllEntities(sQuery, 0, -1);

			return XMLItemCollectionAdapter.putCollection(col);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return new EntityCollection();
	}

	@GET
	@Path("/favorites.xml")
	@Produces(MediaType.TEXT_XML)
	public EntityCollection getFavoritesXML(
			@Context HttpServletRequest servletRequest) {
		try {
			return getFavorites(servletRequest);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return new EntityCollection();
	}

	@GET
	@Path("/favorites.json")
	@Produces(MediaType.APPLICATION_JSON)
	public EntityCollection getFavoritesJSON(
			@Context HttpServletRequest servletRequest) {
		try {
			return getFavorites(servletRequest);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return new EntityCollection();
	}

	/**
	 * Returns a list with all uniqueids stored in the profile favorites
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<String> getFavoriteIds(HttpServletRequest servletRequest) {
		ItemCollection profile=null;
		Principal user = servletRequest.getUserPrincipal();
		String userid = null;
		if (user != null)
			userid = user.getName();

		String sQuery = "SELECT DISTINCT profile FROM Entity as profile "
				+ " JOIN profile.textItems AS t2"
				+ " WHERE  profile.type= 'profile' "
				+ " AND t2.itemName = 'txtname' " + " AND t2.itemValue = '"
				+ userid + "' ";

		Collection<ItemCollection> col = entityService.findAllEntities(sQuery,
				0, 1);

		if (col.size() > 0)
			profile= col.iterator().next();
		

		if (profile == null)
			return new ArrayList<String>();
		return profile.getItemValue("txtWorkitemRef");
	}

	
}
