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

package org.imixs.marty.plugins;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.WorkflowKernel;
import org.imixs.workflow.bpmn.BPMNUtil;
import org.imixs.workflow.engine.plugins.AbstractPlugin;
import org.imixs.workflow.exceptions.PluginException;

/**
 * This plugin supports a comment feature. Comments entered by a user into the
 * field 'comment.user' are stored in the list property 'comment.log' which
 * contains a map for each comment. The map stores the username, the timestamp
 * and the comment. The plugin also stores the last comment in the field
 * 'comment.user.last'. The comment can be also controlled by the corresponding
 * workflow event:
 * 
 * 
 * <pre>
 * {@code
 * <comment>
 *     <ignore>true</ignore>
 *     <message>This is a system comment</message>
 * </comment>
 * }
 * </pre>
 *
 * 
 * 
 * @author rsoika
 * @version 1.0
 * 
 */
public class CommentPlugin extends AbstractPlugin {

    private static Logger logger = Logger.getLogger(CommentPlugin.class.getName());

    /**
     * This method updates the comment.log. There for the method copies the item
     * 'comment.user' into the comment.log and clears the comment.user field
     * 
     * @param workflowEvent
     */
    @SuppressWarnings("unchecked")
    @Override
    public ItemCollection run(ItemCollection workitem, ItemCollection event) throws PluginException {

        boolean ignore = false;
        String commentUser = workitem.getItemValueString("comment.user");
        String commentStatic = "";

        // Test Deprecated format first...
        ItemCollection evalItemCollection = this.getWorkflowService().evalWorkflowResult(event, "item",
                workitem);
        // test if comment is defined in model
        if (evalItemCollection != null && evalItemCollection.hasItem("comment.ignore")) {
            // test ignore
            if ("true".equals(evalItemCollection.getItemValueString("comment.ignore"))) {
                ignore = true;
            }
            workitem.removeItem("comment.ignore");
        } else {
            // New XML Format
            String workflowResult = event.getItemValueString(BPMNUtil.EVENT_ITEM_WORKFLOW_RESULT);
            // Support deprecated item name
            if (workflowResult.isEmpty()) {
                workflowResult = event.getItemValueString("txtActivityResult");
            }

            List<ItemCollection> commentList = this.getWorkflowService().evalXMLExpressionList(workflowResult,
                    "comment", "",
                    workitem, true);

            if (commentList == null || commentList.size() == 0) {
                return workitem;
            }
            if (commentList.size() > 1) {
                throw new PluginException("CONFIG_ERROR", CommentPlugin.class.getSimpleName(),
                        "Comment Tag is only allowed once in a BPMN workflow result!");
            }

            ItemCollection commentConfig = commentList.get(0);
            ignore = commentConfig.getItemValueBoolean("ignore");
            commentStatic = commentConfig.getItemValueString("message");
        }

        if (ignore) {
            logger.fine("ignore=true - skipping txtCommentLog");
            // save last comment in any case!
            workitem.replaceItemValue("txtLastComment", commentUser);
            workitem.replaceItemValue("comment.user.last", commentUser);
            return workitem;
        }

        // if no comment is defined, return!
        if (commentStatic.isBlank() && commentUser.isBlank()) {
            return workitem;
        }

        // create new Comment log entry
        List<Map<String, Object>> vCommentList = workitem.getItemValue("comment.log");

        String remoteUser = this.getWorkflowService()
                .getUserName();

        if (!commentStatic.isBlank()) {
            Map<String, Object> log = new HashMap<String, Object>();
            log.put("date", workitem.getItemValueDate(WorkflowKernel.LASTEVENTDATE));
            log.put("user", remoteUser);
            log.put("message", commentStatic);
            vCommentList.add(0, log);

        }
        if (!commentUser.isBlank()) {
            Map<String, Object> log = new HashMap<String, Object>();
            log.put("date", workitem.getItemValueDate(WorkflowKernel.LASTEVENTDATE));
            log.put("user", remoteUser);
            log.put("message", commentUser);
            vCommentList.add(0, log);
            workitem.setItemValue("comment.user.last", commentUser);
            workitem.replaceItemValue("txtLastComment", commentUser);
            workitem.setItemValue("comment.user", "");
        }

        workitem.setItemValue("comment.log", vCommentList);

        return workitem;

    }

}
