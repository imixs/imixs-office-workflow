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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.imixs.marty.profile.ProfileService;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.engine.DocumentService;
import org.imixs.workflow.engine.index.Category;
import org.imixs.workflow.engine.index.SearchService;
import org.imixs.workflow.office.views.MonitorController;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

/**
 ** The MonitorController provides analytic information about the current process
 * instance.
 * 
 * @author rsoika
 * @version 1.1
 */
@Stateless
@Produces({ MediaType.TEXT_HTML, MediaType.APPLICATION_XHTML_XML, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON,
        MediaType.TEXT_XML })
@Path("/monitor")
public class MonitorRestService implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    SearchService searchService;

    @Inject
    ProfileService profileService;
    @EJB
    DocumentService documentService;

    private static Logger logger = Logger.getLogger(MonitorRestService.class.getName());

    public MonitorRestService() {
        super();
    }

    /**
     * This method loads taxonomy data for a workflow group within a given process
     * and builds a ChartJS data structure in JSON format
     * 
     * @param workflowgroup
     * @param task
     * @return
     */
    @GET
    @Path("/{processid}/{workflowgroup}/{task}/{category}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XHTML_XML })
    public String taxonomyChartData(@PathParam("processid") String processid,
            @PathParam("workflowgroup") String workflowgroup, @PathParam("task") String task,
            @PathParam("category") String category, @QueryParam("filter") String filter) {

        String result;
        logger.finest("taxonomy computing: " + workflowgroup + " " + task);
        // String query="$workflowgroup:Bedarfsmeldung AND $taskid:5100";

        String query = "(type:workitem AND $uniqueidref:" + processid + " AND $workflowgroup:\"" + workflowgroup
                + "\" AND $workflowstatus:\"" + task + "\")";

        if (filter != null && !filter.isEmpty()) {
            query += " AND " + filter + " ";
        }

        logger.finest(query);
        List<Category> taxResult = searchService.getTaxonomyByQuery(query, category);

        // from the result we can now build a chart data structure
        // data : {
        // labels : [ 'Red', 'Blue', 'Yellow' ],
        // datasets : [ {
        // label : 'My First Dataset',
        // data : [ 300, 50, 100 ],
        // backgroundColor : [
        // 'rgb(255, 99, 132)',
        // 'rgb(54, 162, 235)',
        // 'rgb(255, 205, 86)' ]
        // } ]
        // }

        result = "{";

        // we expect one cateogory...
        if (taxResult.size() > 0) {
            Category cat = taxResult.get(0);
            // Lables
            Map<String, Integer> labelMap = cat.getLabels();

            // if owner/creator/editor - translate the label into the profile display name
            List<String> labels = new ArrayList<String>();
            if ("$owner".equals(category) || "$creator".equals(category) || "$editor".equals(category)) {

                for (String id : labelMap.keySet()) {
                    // try to lookup the id....
                    String name = id;
                    ItemCollection profile = profileService.findProfileById(id);
                    if (profile != null) {
                        name = profile.getItemValueString("txtusername");
                        if (name.isEmpty()) {
                            name = id;
                        }
                    }
                    labels.add(name);
                }

            } else if ("space.ref".equals(category)) {
                // if space.ref translate into space.name
                for (String id : labelMap.keySet()) {
                    // try to lookup the space.name
                    String name = id;
                    ItemCollection space = documentService.load(id);
                    if (space != null) {
                        name = space.getItemValueString("name");
                        if (name.isEmpty()) {
                            name = id;
                        }
                    }
                    labels.add(name);
                }

            } else {
                // take lables as is....
                labels.addAll(labelMap.keySet());
            }

            result = result + "\"labels\" : [ ";
            result = result + labels.stream().collect(Collectors.joining("\",\"", "\"", "\""));
            result = result + "],";

            // print origin userids in a extra field (htis is to store the userid beside the
            // displayname which is only necessary for $owner)
            result = result + "\"origins\" : [ ";
            result = result + labelMap.keySet().stream().collect(Collectors.joining("\",\"", "\"", "\""));
            result = result + "],";

            result = result + "\"datasets\": [{";
            result = result + "\"label\": \"By Category\",";

            result = result + "\"data\" : [ ";
            List<String> valueList = new ArrayList<String>();
            for (Integer dat : labelMap.values()) {
                valueList.add("" + dat);
            }
            // result = result + valueList.stream().collect(Collectors.joining("\",\"",
            // "\"", "\""));
            result = result + valueList.stream().collect(Collectors.joining(",", "", ""));
            result = result + "],";

        } else {
            // no data found we need an empty array.

            result = result + "\"labels\" : [ ],";
            result = result + "\"datasets\": [{";
            result = result + "\"label\": \"By Category\",";
            result = result + "\"data\" : [ ],";
        }
        result = result + MonitorController.generateBackgroundColorScheme();
        result = result + "}]}";

        for (Category cat : taxResult) {

            logger.finest("cat: " + cat.getName() + " = " + cat.getCount());
            Map<String, Integer> labelMap = cat.getLabels();

            labelMap.forEach((key, value) -> {
                logger.finest("   " + key + " : " + value);
            });

        }

        logger.finest("result=" + result);

        return result;
    }

}
