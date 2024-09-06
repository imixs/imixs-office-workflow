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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.Model;
import org.imixs.workflow.engine.DocumentService;
import org.imixs.workflow.engine.ModelService;
import org.imixs.workflow.exceptions.ModelException;
import org.imixs.workflow.exceptions.QueryException;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 ** The MonitorController provides analytic information about the current process
 * instance.
 * 
 * @author rsoika
 * @version 1.1
 */
@Named
@ViewScoped
public class MonitorController implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<String> workflowGroups = null;
    private List<String> activeWorkflowGroups = null;
    private Set<BoardCategory> boardCategories;

    private ItemCollection filter = null;
    @Inject
    protected BoardController boardController;

    private static Logger logger = Logger.getLogger(MonitorController.class.getName());

    @EJB
    ModelService modelService = null;

    @EJB
    DocumentService documentService;

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
        boardController.setProcessRef(paramMap.get("processref"));

        // get all workflow groups
        workflowGroups = modelService.getGroups();
        // reset borad stats...
        reset();
    }

    public ItemCollection getFilter() {
        return filter;
    }

    public String getProcessRef() {
        return boardController.getProcessRef();
    }

    public ItemCollection getProcess() {
        return boardController.getProcess();
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
     * Returns a string list with all active workflow groups within the current
     * processRef
     * <p>
     * A workflowGroup is active if the group contains workItems
     * 
     * @return
     */
    public List<String> getActiveWorkflowGroups() {
        return activeWorkflowGroups;
    }

    public Set<BoardCategory> getBoardCategories() {
        return boardCategories;
    }

    public void setBoardCategories(Set<BoardCategory> boardCategories) {
        this.boardCategories = boardCategories;
    }

    /**
     * This method returns the total count of workItems in a given Group.
     * 
     * @param group
     * @return
     */
    public long countTotalWorkitems(String group) {
        int result = 0;
        if (group != null) {
            for (BoardCategory boardCat : boardCategories) {
                if (group.equals(boardCat.getWorkflowGroup())) {
                    result = result + boardCat.getPageSize();
                }
            }
        }
        return result;
    }

    /**
     * This method discards the cache and rebuilds the board categories for the
     * current ProcessRef.
     * 
     */
    public void reset() {
        filter = new ItemCollection();
        refresh();
    }

    /**
     * This method discards the cache.
     */
    public void refresh() {
        // initalize the task list...
        buildBoardCategories();

        // find active groups..
        activeWorkflowGroups = new ArrayList<String>();
        for (BoardCategory cat : boardCategories) {
            if (!activeWorkflowGroups.contains(cat.getWorkflowGroup())) {
                activeWorkflowGroups.add(cat.getWorkflowGroup());
            }
        }

        // sort active workflowGroups
        Collections.sort(activeWorkflowGroups);
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
    @Deprecated
    public String getOverallData() {
        String result = "{";

        // Lables
        result = result + "labels : [ ";
        result = result + activeWorkflowGroups.stream().collect(Collectors.joining("','", "'", "'"));
        result = result + "],";

        // Datasets
        result = result + " datasets : [ { ";
        result = result + " label : 'All Groups', ";
        // build array of overall count
        List<String> overAllCount = new ArrayList<String>();
        // for (String group : workflowGroups) {
        for (String group : activeWorkflowGroups) {
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
        result = result + MonitorController.generateBackgroundColorScheme();
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

        // "{cars":[ {"id":"Ford"}, "BMW", "Fiat" ]}
        String result = "[";

        for (String group : activeWorkflowGroups) {
            result = result + "{id:'" + getBase64(group) + "', name:'" + group + "',";
            result = result + "data:" + buildChartData(group) + "},";
        }

        result = result.substring(0, result.length() - 1);

        result = result + "]";
        return result;
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

    /**
     * This method counts the workItems for each WorklfowGroup/TaskID combination
     * within the current ProcessRef. The results are stored in a Map. The
     * BoardCategories are used to computed chart data.
     * 
     */
    private void buildBoardCategories() {
        long l = System.currentTimeMillis();

        boardCategories = new HashSet<BoardCategory>();

        try {
            String query = "(type:\"workitem\" AND $uniqueidref:\"" + getProcessRef() + "\")";
            Date datFrom = filter.getItemValueDate("date.from");
            Date datTo = filter.getItemValueDate("date.to");

            // search date range?
            String sDateFrom = "191401070000"; // because * did not work here
            String sDateTo = "211401070000";
            SimpleDateFormat dateformat = new SimpleDateFormat("yyyyMMddHHmm");

            if (datFrom != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(datFrom);
                sDateFrom = dateformat.format(cal.getTime());
            }
            if (datTo != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(datTo);
                cal.add(Calendar.DATE, 1);
                sDateTo = dateformat.format(cal.getTime());
            }

            if (datFrom != null || datTo != null) {
                // expected format $created:[20020101 TO 20030101]
                query += " AND ($created:[" + sDateFrom + " TO " + sDateTo + "]) ";
            }

            logger.info(query);
            for (String group : workflowGroups) {
                // find latest version....
                List<String> versions = modelService.findVersionsByGroup(group);
                if (versions != null && versions.size() > 0) {
                    // get the latest version
                    String version = versions.get(0);

                    // load the model
                    Model model;
                    model = modelService.getModel(version);

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

            logger.info("...build " + boardCategories.size() + " BoardCategories in " + (System.currentTimeMillis() - l)
                    + "ms");
        } catch (ModelException | QueryException e) {
            logger.severe("...failed to BoardCategories: " + e.getMessage());
        }

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
        List<String> statusLabels = new ArrayList<String>();

        // create a sublist of categories for the given group
        List<BoardCategory> sortedBoardCategoriesByGroup = new ArrayList<BoardCategory>();
        for (BoardCategory cat : boardCategories) {
            if (group.equals(cat.getWorkflowGroup())) {
                sortedBoardCategoriesByGroup.add(cat);
            }
        }

        // sortedBoardCategoriesByGroup.addAll(0, sortedBoardCategoriesByGroup)

        // now we sort the categories by $taskID
        Collections.sort(sortedBoardCategoriesByGroup, new Comparator<BoardCategory>() {
            @Override
            public int compare(BoardCategory p1, BoardCategory p2) {
                return p1.toString().compareTo(p2.toString());
            }
        });

        // get the status lables
        for (BoardCategory cat : sortedBoardCategoriesByGroup) {
            // if (group.equals(cat.getWorkflowGroup())) {
            statusLabels.add(cat.workflowStatus);
            // }
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
        for (BoardCategory cat : sortedBoardCategoriesByGroup) {
            if (group.equals(cat.getWorkflowGroup())) {
                statusCount.add("" + cat.pageSize);
            }
        }

        result = result + " data: [ " + statusCount.stream().collect(Collectors.joining(",")) + "],";

        // colors...
        result = result + MonitorController.generateBackgroundColorScheme();

        result = result + " } ] }";
        return result;
    }

    /**
     * This helper method generates a backgroundColorScheme for chart diagrams.
     * 
     * @return
     */
    public static String generateBackgroundColorScheme() {
        String result = " \"backgroundColor\" : [\n"
                + "   \"#3B6B82\",\"#70B088\",\"#EBA05F\",\"#9B3425\",\"#CFE9F5\",\"#F1D437\",\"#E73B65\",\"#462645\",\"#2D6CA8\",\"#2B5C33\",\"#F28E1C\",\"#F3E500\" ]";
        return result;
    }

}
