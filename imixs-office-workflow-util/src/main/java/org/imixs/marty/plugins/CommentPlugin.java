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
import org.imixs.workflow.engine.plugins.AbstractPlugin;
import org.imixs.workflow.exceptions.PluginException;

/**
 * This plugin supports a comment feature. Comments entered by a user into the
 * field 'txtComment' are stored in the list property 'txtCommentList' which
 * contains a map for each comment. The map stores the username, the timestamp
 * and the comment. The plugin also stores the last comment in the field
 * 'txtLastComment'. The comment can be also controlled by the corresponding
 * workflow event:
 * 
 * <comment ignore="true" /> a new comment will not be added into the comment
 * list
 * 
 * <comment>xxx</comment> adds a fixed comment 'xxx' into the comment list
 * 
 * 
 * @author rsoika
 * @version 1.0
 * 
 */
public class CommentPlugin extends AbstractPlugin {
    ItemCollection documentContext;

    private static Logger logger = Logger.getLogger(CommentPlugin.class.getName());

    /**
     * This method updates the comment list. There for the method copies the
     * txtComment into the txtCommentList and clears the txtComment field
     * 
     * @param workflowEvent
     */
    @SuppressWarnings("unchecked")
    @Override
    public ItemCollection run(ItemCollection adocumentContext, ItemCollection documentActivity) throws PluginException {

        documentContext = adocumentContext;

        ItemCollection evalItemCollection = this.getWorkflowService().evalWorkflowResult(documentActivity, "item",
                adocumentContext);

        // test if comment is defined in model
        if (evalItemCollection != null) {
            // test ignore
            if ("true".equals(evalItemCollection.getItemValueString("comment.ignore"))) {
                logger.fine("ignore=true - skipping txtCommentLog");
                // save last comment in any case!
                documentContext.replaceItemValue("txtLastComment", documentContext.getItemValueString("txtComment"));
                return documentContext;
            }
        }

        // create new Comment data - important: property names in lower
        // case
        List<Map<String, Object>> vCommentList = documentContext.getItemValue("txtCommentLog");
        Map<String, Object> log = new HashMap<String, Object>();
        String remoteUser = this.getWorkflowService().getUserName();
        log.put("datcomment", documentContext.getItemValueDate(WorkflowKernel.LASTEVENTDATE));
        log.put("nameditor", remoteUser);

        // test for fixed comment
        String sComment = null;
        if (evalItemCollection != null && evalItemCollection.hasItem("comment")) {
            sComment = evalItemCollection.getItemValueString("comment");
        } else {
            sComment = documentContext.getItemValueString("txtComment");
            // clear comment
            documentContext.replaceItemValue("txtComment", "");
        }

        if (sComment != null && !sComment.isEmpty()) {
            log.put("txtcomment", sComment);
            vCommentList.add(0, log);
            documentContext.replaceItemValue("txtcommentLog", vCommentList);
            documentContext.replaceItemValue("txtLastComment", sComment);
        }

        documentContext.removeItem("comment");
        documentContext.removeItem("comment.ignore");
        return documentContext;

    }

}
