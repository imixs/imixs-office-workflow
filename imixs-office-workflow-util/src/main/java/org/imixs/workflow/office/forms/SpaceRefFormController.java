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

package org.imixs.workflow.office.forms;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.imixs.marty.team.TeamController;
import org.imixs.workflow.ItemCollection;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * The SpaceRefFormController provides method to support the custom part
 * 'spaceref'
 * 
 * The component is used as a custom form part. For example:
 * 
 * <pre>
 * {@code
 *     <item type="custom"
 *         path="spaceref"
 *         options="default-selection;"
 *         label="Department:" />
 *       }
 </pre>
 * 
 * The options are used by the controller methods to resolve the different
 * setups
 * 
 * 
 * @author rsoika
 * 
 */
@Named
@SessionScoped
public class SpaceRefFormController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    TeamController teamController;

    private static Logger logger = Logger.getLogger(SpaceRefFormController.class.getName());

    /**
     * Returns the spaces to part of the selection.
     * 
     * Depending on the options the list can be either the complete list of spaces
     * or a subset.
     * 
     * Possible Options are:
     * 
     * items="#{byprocess?teamController.getSpacesByProcessId(workflowController.workitem.item['process.ref']):teamController.spaces}"
     * var="space">
     * 
     * @param workitem
     * @param options
     * @return
     */
    public List<ItemCollection> getSpaces(ItemCollection workitem, String options) {
        if (options == null) {
            options = "";
        }
        List<ItemCollection> result = null;
        if (options.toLowerCase().contains("byprocess=true")) {
            result = teamController.getSpacesByProcessId(workitem.getItemValueString("process.ref"));
        } else {
            result = teamController.getSpaces();
        }

        // apply regex filter?
        if (options.contains("regex=")) {
            String _regex = extractRegexValue(options);
            Pattern pattern = Pattern.compile(_regex);
            return result.stream()
                    .filter(space -> pattern.matcher(space.getItemValueString("name")).matches())
                    .collect(Collectors.toList());
        }
        // default result
        return result;

    }

    /**
     * This method updates the space.ref item with the first space ID the current
     * user is member of.
     * The method is used as a default selector by form part 'spaceref.xhtml'
     * 
     * The method updates the items space.ref and space.name and returns the
     * space.ref
     * 
     * Possible Options are:
     * 
     * <pre>
     * default-selection=member  | The first section, the current user is member of will be pre-selected.     
     * default-selection=team    | The first section, the current user is team member of will be pre-selected. 
     * default-selection=manager | The first section, the current user is manager of will be pre-selected.  
     * default-selection=assist  | The first section, the current user is assist of will be pre-selected.
     * </pre>
     */
    public String setDefaultSpace(ItemCollection workitem, String options) {
        if (options == null) {
            options = "";
        }
        ItemCollection defaultSpace = null;
        List<ItemCollection> _spaceList = getSpaces(workitem, options);

        // find a matching space in the spaces list.

        if (_spaceList != null) {
            for (ItemCollection space : _spaceList) {
                if (options.contains("default-selection=assist") && space.getItemValueBoolean("isAssist")) {
                    defaultSpace = space;
                    break;
                }
                if (options.contains("default-selection=team") && space.getItemValueBoolean("isTeam")) {
                    defaultSpace = space;
                    break;
                }
                if (options.contains("default-selection=manager") && space.getItemValueBoolean("isManager")) {
                    defaultSpace = space;
                    break;
                }

                if (options.contains("default-selection=member") && space.getItemValueBoolean("isMember")) {
                    defaultSpace = space;
                    break;
                }
            }
        }

        // do we have a match?
        if (defaultSpace != null) {
            workitem.setItemValue("space.ref", defaultSpace.getUniqueID());
            workitem.setItemValue("space.name", defaultSpace.getItemValueString("name"));
            return defaultSpace.getUniqueID();
        }

        // no match !
        return "";
    }

    private static String extractRegexValue(String input) {
        Pattern pattern = Pattern.compile("regex=([^;]+)");
        Matcher matcher = pattern.matcher(input);
        return matcher.find() ? matcher.group(1) : null;
    }
}
