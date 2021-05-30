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

package org.imixs.workflow.office.views;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.imixs.marty.config.SetupController;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.Model;
import org.imixs.workflow.engine.DocumentService;
import org.imixs.workflow.engine.ModelService;
import org.imixs.workflow.engine.index.SchemaService;
import org.imixs.workflow.exceptions.ModelException;
import org.imixs.workflow.exceptions.QueryException;
import org.imixs.workflow.faces.util.LoginController;

/**
 ** The MonitorController provides analytic information about the current process
 * instance.
 * 
 * @author rsoika
 * @version 1.0
 */
@Named
@ViewScoped
public class MonitorController implements Serializable {

    private static final long serialVersionUID = 1L;

    // view params
    private String processRef;
    private ItemCollection process = null;
    private List<String> workflowGroups = null;
    private List<String> activeWorkflowGroups = null;

    private Set<BoardCategory> boardCategories;

    @Inject
    protected LoginController loginController = null;

    @Inject
    SetupController setupController;

    private static Logger logger = Logger.getLogger(MonitorController.class.getName());

    @EJB
    ModelService modelService = null;

    @EJB
    DocumentService documentService;

    @EJB
    SchemaService schemaService;

    public MonitorController() {
        super();
    }

    /**
     * Initialize default behavior initialize the processref, page index and phrase
     * 
     */
    @PostConstruct
    public void init() {
        // extract the processref and page from the query string
        FacesContext fc = FacesContext.getCurrentInstance();
        Map<String, String> paramMap = fc.getExternalContext().getRequestParameterMap();

        setProcessRef(paramMap.get("processref"));

        // reset borad stats...
        reset();

    }

    public String getProcessRef() {
        if (processRef == null) {
            processRef = "";
        }
        return processRef;
    }

    public void setProcessRef(String processRef) {
        this.processRef = processRef;
    }

    public ItemCollection getProcess() {
        return process;
    }

    public void setProcess(ItemCollection process) {
        this.process = process;
    }

    /**
     * Returns a string list with all workflow groups
     * 
     * @return
     */
    public List<String> getWorkflowGroups() {
        return workflowGroups;
    }

    /**
     * Returns a string list with all active workflow groups
     * <p>
     * A workflowGroup is ative if the group contains workitems
     * 
     * @return
     */
    public List<String> getActiveWorkflowGroups() {
        return activeWorkflowGroups;
    }

    /**
     * Helper method to convert a Group name into a base64 encoded string.
     * <p>
     * This is necessary to work with the group name as a key in javaScript
     * 
     * @param key
     * @return
     */
    public String getBase64(String key) {
        byte[] data = Base64.getEncoder().encode(key.getBytes());
        String result = new String(data);
        result = result.replace("=", "_");
        return result;
    }

    public Set<BoardCategory> getBoardCategories() {
        return boardCategories;
    }

    public void setBoardCategories(Set<BoardCategory> boardCategories) {
        this.boardCategories = boardCategories;
    }

    /**
     * This method discards the cache an reset the current ref.
     */
    @SuppressWarnings("unchecked")
    public void reset() {
        workflowGroups = new ArrayList<String>();
        if (processRef != null && !processRef.isEmpty()) {
            process = documentService.load(processRef);
            workflowGroups = process.getItemValue("txtWorkflowList");

        }
        // initalize the task list...
        boardCategories = new HashSet<BoardCategory>();
        try {
            countWorkList();
        } catch (QueryException | ModelException e) {
            logger.severe("failed to reset monitoring board: " + e.getMessage());
        }

        // find active groups..
        activeWorkflowGroups = new ArrayList<String>();
        for (BoardCategory cat : boardCategories) {
            if (!activeWorkflowGroups.contains(cat.getWorkflowGroup())) {
                activeWorkflowGroups.add(cat.getWorkflowGroup());
            }
        }

    }

    /**
     * This method discards the cache.
     */
    public void refresh() {

    }

    /**
     * This method counts the workitems form the task list and puts them into a
     * cache.
     * 
     * @throws QueryException
     * @throws ModelException
     * 
     */
    private void countWorkList() throws QueryException, ModelException {
        long l = System.currentTimeMillis();

        String query = "(type:\"workitem\" AND $uniqueidref:\"" + getProcessRef() + "\")";

        for (String group : workflowGroups) {
            // find latest version....
            List<String> versions = modelService.findVersionsByGroup(group);
            if (versions != null && versions.size() > 0) {
                // get the latest version
                String version = versions.get(0);

                // load the model
                Model model = modelService.getModel(version);
                // iterate over all tasks....
                List<ItemCollection> tasks = model.findTasksByGroup(group);
                for (ItemCollection task : tasks) {
                    // count the workitems for this task....
                    int taskID = task.getItemValueInteger("numprocessid");
                    String taskName = task.getItemValueString("name");
                    String taskQuery = query + " AND ($taskid:" + taskID + " AND $workflowgroup:\"" + group + "\")";
                    int count = documentService.count(taskQuery);
                    if (count > 0) {
                        // we have active tasks - so cache the info.....

                        BoardCategory tmpCat = new BoardCategory(group, taskName, taskID, count);
                        boardCategories.add(tmpCat);
                    }
                }

            }

        }

        logger.info("...counted all tasks in " + (System.currentTimeMillis() - l) + "ms");

    }

    /**
     * 
     * <pre>
     * data : {
            labels : [ 'Red', 'Blue', 'Yellow' ],
            datasets : [ {
                label : 'My First Dataset',
                data : [ 300, 50, 100 ],
                backgroundColor : [
                        'rgb(255, 99, 132)',
                        'rgb(54, 162, 235)',
                        'rgb(255, 205, 86)' ]
            } ]
        }
     * </pre>
     * 
     * @return
     */
    public String getOverallData() {
        String result = "{";

        // Lables
        result = result + "labels : [ ";
        result = result + workflowGroups.stream().collect(Collectors.joining("','", "'", "'"));
        result = result + "],";

        // Datasets
        result = result + " datasets : [ { ";
        result = result + " label : 'All Groups', ";

        // build array of overall count
        List<String> overAllCount = new ArrayList<String>();
        for (String group : workflowGroups) {
            int count = 0;
            for (BoardCategory cat : boardCategories) {
                if (group.equals(cat.getWorkflowGroup())) {
                    count = count + cat.pageSize;
                }
            }
            overAllCount.add("" + count);
        }
        result = result + " data: [ " + overAllCount.stream().collect(Collectors.joining(",")) + "],";

        // colors...
        result = result + " backgroundColor : [\n" + "                        'rgb(255, 99, 132)',\n"
                + "                        'rgb(54, 162, 235)',\n" + "                        'rgb(255, 205, 86)' ]";

        result = result + " } ] }";
        return result;
    }

    /**
     * This method generates a complex json data structure containing the chart data
     * for each group
     * 
     * <pre>
     * [ { id: 'xxx',
     *     name: 'Order',
     *     data : {...}
          }, ....
        ]
      }
     * </pre>
     * 
     * @return
     */
    public String getGroupData() {
        
        //  "{cars":[ {"id":"Ford"}, "BMW", "Fiat" ]}
        String result = "[";
        
        for (String group: activeWorkflowGroups) {
            result=result + "{id:'"+getBase64(group) + "', name:'"+group + "',";
            result=result + "data:" + buildChartData(group) + "},";
        }
        
        result=result.substring(0,result.length()-1);
        
        result=result+"]";
        return result;
    }
    
    
    /**
     * 
     * <pre>
     *  {
            labels : [ 'Red', 'Blue', 'Yellow' ],
            datasets : [ {
                label : 'My First Dataset',
                data : [ 300, 50, 100 ],
                backgroundColor : [
                        'rgb(255, 99, 132)',
                        'rgb(54, 162, 235)',
                        'rgb(255, 205, 86)' ]
            } ]
        }
     * </pre>
     * 
     * @return
     */
    private String buildChartData(String group) {
        
        // build a list of all lables....
        List<String> statusLabels=new ArrayList<String>();
        for (BoardCategory cat : boardCategories) {
            if (group.equals(cat.getWorkflowGroup())) {
                statusLabels.add(cat.workflowStatus);
            }
        }
        
        String result = "{";

        // Lables
        result = result + "labels : [ ";
        result = result + statusLabels.stream().collect(Collectors.joining("','", "'", "'"));
        result = result + "],";

        // Datasets
        result = result + " datasets : [ { ";
        result = result + " label : '" + group + "', ";

        // build array of overall count
        List<String> statusCount = new ArrayList<String>();
        for (BoardCategory cat : boardCategories) {
            if (group.equals(cat.getWorkflowGroup())) {
                statusCount.add(""+cat.pageSize);
            }
        }
         
        
        result = result + " data: [ " + statusCount.stream().collect(Collectors.joining(",")) + "],";

        // colors...
        result = result + " backgroundColor : [\n" + "                        'rgb(255, 99, 132)',\n"
                + "                        'rgb(54, 162, 235)',\n" + "                        'rgb(255, 205, 86)' ]";

        result = result + " } ] }";
        return result;
    }

}
