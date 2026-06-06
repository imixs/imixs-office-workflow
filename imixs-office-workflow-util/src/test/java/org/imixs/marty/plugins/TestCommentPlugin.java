/****************************************************************************
 * Copyright (c) 2022-2025 Imixs Software Solutions GmbH and others.
 * https://www.imixs.com
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * This Source Code may also be made available under the terms of the
 * GNU General Public License, version 2 or later (GPL-2.0-or-later),
 * which is available at https://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0-or-later
 ****************************************************************************/

package org.imixs.marty.plugins;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.engine.MockWorkflowEnvironment;
import org.imixs.workflow.exceptions.ModelException;
import org.imixs.workflow.exceptions.PluginException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openbpmn.bpmn.BPMNModel;

/**
 * Test class for CommentPlugin.
 *
 * Task 100 with two events: - Event 10: <item name="comment.ignore">true</item>
 * (deprecated config style) - Event 11: <comment ignore="true" /> (current
 * config style)
 *
 * Both events must prevent any entry from being added to comment.log while
 * leaving the comment.user field unchanged.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
public class TestCommentPlugin {

    private static final Logger logger = Logger.getLogger(TestCommentPlugin.class.getName());

    protected MockWorkflowEnvironment workflowEnvironment;
    protected CommentPlugin commentPlugin;
    protected ItemCollection workitem;

    @BeforeEach
    public void setUp() throws PluginException, ModelException {
        workflowEnvironment = new MockWorkflowEnvironment();
        workflowEnvironment.setUp();
        workflowEnvironment.loadBPMNModelFromFile("/bpmn/TestCommentPlugin.bpmn");

        commentPlugin = new CommentPlugin();
        commentPlugin.init(workflowEnvironment.getWorkflowService());

        workitem = workflowEnvironment.getDocumentService().load("W0000-00001");
        workitem.model("1.0.0").task(100);
    }

    /**
     * Event 10 uses the deprecated item-tag style:
     * <item name="comment.ignore">true</item>
     *
     * Expected: - comment.log remains empty - comment.user remains unchanged
     * ("hallo welt") - comment.user.last is set to "hallo welt"
     */
    @Test
    public void testIgnoreCommentDeprecatedStyle() {
        workitem.setItemValue("comment.user", "hallo welt");
        workitem.event(10);

        try {
            BPMNModel model = workflowEnvironment.getModelManager().getModelByWorkitem(workitem);
            ItemCollection event = workflowEnvironment.getModelManager().loadEvent(workitem, model);

            workitem = commentPlugin.run(workitem, event);

            // comment.log must remain empty
            List<Map<String, Object>> commentLog = workitem.getItemValue("comment.log");
            assertTrue(commentLog.isEmpty(), "comment.log must be empty when ignore=true");

            // comment.user must be unchanged
            assertEquals("hallo welt", workitem.getItemValueString("comment.user"),
                    "comment.user must not be cleared when ignore=true");

            // comment.user.last must be set
            assertEquals("hallo welt", workitem.getItemValueString("comment.user.last"),
                    "comment.user.last must be set to the current comment.user value");

        } catch (PluginException | ModelException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Event 11 uses the current comment-tag style: <comment ignore="true" />
     *
     * Expected: - comment.log remains empty - comment.user remains unchanged
     * ("hallo welt") - comment.user.last is set to "hallo welt"
     */
    @Test
    public void testIgnoreCommentCurrentStyle() {
        workitem.setItemValue("comment.user", "hallo welt");
        workitem.event(11);

        try {
            BPMNModel model = workflowEnvironment.getModelManager().getModelByWorkitem(workitem);
            ItemCollection event = workflowEnvironment.getModelManager().loadEvent(workitem, model);

            workitem = commentPlugin.run(workitem, event);

            // comment.log must remain empty
            List<Map<String, Object>> commentLog = workitem.getItemValue("comment.log");
            assertTrue(commentLog.isEmpty(), "comment.log must be empty when ignore=true");

            // comment.user must be unchanged
            assertEquals("hallo welt", workitem.getItemValueString("comment.user"),
                    "comment.user must not be cleared when ignore=true");

            // comment.user.last must be set
            assertEquals("hallo welt", workitem.getItemValueString("comment.user.last"),
                    "comment.user.last must be set to the current comment.user value");

        } catch (PluginException | ModelException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Event 11 uses the current comment-tag style: <comment ignore="true" />
     *
     * Expected: - comment.log remains empty - comment.user remains unchanged
     * ("hallo welt") - comment.user.last is set to "hallo welt"
     */
    @Test
    public void testStaticCOmment() {
        workitem.setItemValue("comment.user", "hallo welt");
        workitem.event(21);
        try {
            BPMNModel model = workflowEnvironment.getModelManager().getModelByWorkitem(workitem);
            ItemCollection event = workflowEnvironment.getModelManager().loadEvent(workitem, model);

            workitem = commentPlugin.run(workitem, event);

            // comment.log must remain empty
            List<Map<String, List<Object>>> commentLog = workitem.getItemValue("comment.log");
            assertTrue(commentLog.size() == 2, "comment.log must not be empty");

            ItemCollection firstLogEntry = new ItemCollection(commentLog.get(0));
            ItemCollection secondLogEntry = new ItemCollection(commentLog.get(1));

            // comment.user.last must be set
            assertEquals("hallo welt", firstLogEntry.getItemValueString("message"));
            assertEquals("Hello World", secondLogEntry.getItemValueString("message"));

            assertEquals("", workitem.getItemValueString("comment.user"));

        } catch (PluginException | ModelException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Event 20 - normal comment
     */
    @Test
    public void testSimpleComment() {
        workitem.setItemValue("comment.user", "hallo welt");
        workitem.event(20);
        try {
            BPMNModel model = workflowEnvironment.getModelManager().getModelByWorkitem(workitem);
            ItemCollection event = workflowEnvironment.getModelManager().loadEvent(workitem, model);

            workitem = commentPlugin.run(workitem, event);

            // comment.log must remain empty
            List<Map<String, List<Object>>> commentLog = workitem.getItemValue("comment.log");
            assertTrue(commentLog.size() == 1, "comment.log must not be empty");

            ItemCollection firstLogEntry = new ItemCollection(commentLog.get(0));

            // comment.user.last must be set
            assertEquals("hallo welt", firstLogEntry.getItemValueString("message"));
            assertEquals("manfred", firstLogEntry.getItemValueString("user"));

            assertEquals("", workitem.getItemValueString("comment.user"));

            assertEquals("hallo welt", workitem.getItemValueString("comment.user.last"));
            assertEquals("hallo welt", workitem.getItemValueString("txtLastComment"));

        } catch (PluginException | ModelException e) {
            fail(e.getMessage());
        }
    }
}