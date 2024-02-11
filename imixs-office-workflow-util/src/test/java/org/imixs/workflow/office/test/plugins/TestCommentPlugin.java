package org.imixs.workflow.office.test.plugins;

import java.util.List;
import java.util.Map;

import org.imixs.marty.plugins.CommentPlugin;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.engine.WorkflowMockEnvironment;
import org.imixs.workflow.exceptions.AdapterException;
import org.imixs.workflow.exceptions.ModelException;
import org.imixs.workflow.exceptions.PluginException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * This test class will test the comment feature of the CommentPlugin
 * 
 * @author rsoika
 */

public class TestCommentPlugin {

	private CommentPlugin commentPlugin;

	ItemCollection documentActivity;
	ItemCollection documentContext;

	WorkflowMockEnvironment workflowMockEnvironment;

	@Before
	public void setup() throws PluginException, ModelException, AdapterException {

		workflowMockEnvironment = new WorkflowMockEnvironment();
		workflowMockEnvironment.setModelPath("/bpmn/TestCommentPlugin.bpmn");

		workflowMockEnvironment.setup();

		commentPlugin = new CommentPlugin();
		try {
			commentPlugin.init(workflowMockEnvironment.getWorkflowService());
		} catch (PluginException e) {

			e.printStackTrace();
		}

		documentContext = new ItemCollection();
	}

	/**
	 * This simple test verifies the default comment feature
	 * 
	 * @throws PluginException
	 * @throws ModelException
	 * 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testSimpleComment() throws PluginException, ModelException {

		documentContext.replaceItemValue("txtComment", "Some Comment");
		documentActivity = workflowMockEnvironment.getModel().getEvent(200, 10);

		try {
			commentPlugin.run(documentContext, documentActivity);
		} catch (PluginException e) {

			e.printStackTrace();
			Assert.fail();
		}

		List<Map> commentList = documentContext.getItemValue("txtcommentLog");
		String lastComment = documentContext.getItemValueString("txtLastComment");
		String currentComment = documentContext.getItemValueString("txtComment");
		Assert.assertEquals(1, commentList.size());
		Assert.assertEquals("Some Comment", ((Map) commentList.get(0)).get("txtcomment"));
		Assert.assertEquals("Some Comment", lastComment);
		Assert.assertTrue(currentComment.isEmpty());
	}

	/**
	 * This simple test verifies the comment ignore=true flag
	 * 
	 * @throws PluginException
	 * @throws ModelException
	 * 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testIgnoreComment() throws PluginException, ModelException {

		documentContext.replaceItemValue("txtComment", "Some Comment");
		documentActivity = workflowMockEnvironment.getModel().getEvent(100, 10);

		try {
			commentPlugin.run(documentContext, documentActivity);
		} catch (PluginException e) {

			e.printStackTrace();
			Assert.fail();
		}

		List<Map> commentList = documentContext.getItemValue("txtcommentLog");
		String lastComment = documentContext.getItemValueString("txtLastComment");
		String currentComment = documentContext.getItemValueString("txtComment");
		Assert.assertEquals(0, commentList.size());
		Assert.assertEquals("Some Comment", currentComment);
		Assert.assertFalse(lastComment.isEmpty());
	}

	/**
	 * This test verifies a fixed comment text
	 * 
	 * @throws PluginException
	 * @throws ModelException
	 * 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testFixedComment() throws PluginException, ModelException {

		documentContext.replaceItemValue("txtComment", "Some Comment");
		documentActivity = workflowMockEnvironment.getModel().getEvent(100, 20);

		try {
			commentPlugin.run(documentContext, documentActivity);
		} catch (PluginException e) {

			e.printStackTrace();
			Assert.fail();
		}

		List<Map> commentList = documentContext.getItemValue("txtcommentLog");
		String lastComment = documentContext.getItemValueString("txtLastComment");
		String currentComment = documentContext.getItemValueString("txtComment");
		Assert.assertEquals(1, commentList.size());
		Assert.assertEquals("My Comment", ((Map) commentList.get(0)).get("txtcomment"));
		Assert.assertEquals("My Comment", lastComment);
		Assert.assertEquals("Some Comment", currentComment);
	}

}
