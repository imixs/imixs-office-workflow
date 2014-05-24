package org.imixs.workflow.rest;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.services.rest.RestClient;
import org.imixs.workflow.xml.XMLItemCollection;
import org.imixs.workflow.xml.XMLItemCollectionAdapter;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * This test class tests the imixs-jax-rs api. The class can be used as an
 * example for further test cases
 * 
 * The test class checks post scenarios
 * 
 * @author rsoika
 * 
 */
public class TestRestAPIWorkflow extends AbstractWorkflowTest {
	/**
	 * This test post a xml structure to create a new workitem
	 */
	@Test
	@Ignore
	public void testPostXmlWorkitem() {

		RestClient restClient = new RestClient();

		restClient.setCredentials(USERID, PASSWORD);

		ItemCollection workitem = new ItemCollection();
		workitem.replaceItemValue("txtName", "workitem test");
		workitem.replaceItemValue("$ProcessID", 3000);
		workitem.replaceItemValue("$ModelVersion", MODEL_VERSION);
		workitem.replaceItemValue("$ActivityID", 10);

		XMLItemCollection xmlWorkitem;
		try {
			xmlWorkitem = XMLItemCollectionAdapter.putItemCollection(workitem);
			int httpResult = restClient.postEntity(WORKFLOW_URI + "/workitem",
					xmlWorkitem);

			// expected result 200
			Assert.assertEquals(200, httpResult);
		} catch (Exception e) {

			e.printStackTrace();
			Assert.fail();
		}

	}

	/**
	 * <code>
	 * 
			{ "item" : 
			  { "name" : { "$" : "Bill"},
			    "value" : { "$" : "Burke" }
			  }
			}
			...
			{ "item" : 
			  { "name" : { "$" : "Bill"},
			    "value" : [ { "$" : "Burke" } ]
			  }
			}
     * </code>
	 */
	@Test
	@Ignore
	public void testPostJsonWorkitem() {

		RestClient restClient = new RestClient();

		restClient.setCredentials(USERID, PASSWORD);

		// create a json test string
		String json = createJsonObject(MODEL_VERSION, 3000, 10, null);

		// http://www.jsonschema.net/
		try {
			int httpResult = restClient.postJsonEntity(WORKFLOW_URI
					+ "/workitem.json", json);

			String sContent = restClient.getContent();
			// expected result 200
			Assert.assertEquals(200, httpResult);

			Assert.assertTrue(sContent.indexOf("$uniqueid") > -1);
		} catch (Exception e) {

			e.printStackTrace();
			Assert.fail();
		}

	}

	/**
	 * <code>
	 * 
			{ "item" : 
			  { "name" : { "$" : "Bill"},
			    "value" : { "$" : "Burke" }
			  }
			}
			...
			{ "item" : 
			  { "name" : { "$" : "Bill"},
			    "value" : [ { "$" : "Burke" } ]
			  }
			}
	 * </code>
	 */
	@Test
	@Ignore
	public void testPostJsonRueckruf() {
	
		RestClient restClient = new RestClient();
	
		restClient.setCredentials(USERID, PASSWORD);
	
		// create a json test for 'rueckruf'
		
		// create a json test string
		String json = "{\"item\":["
				+ "	{\"name\":\"$processid\",\"value\":{\"@type\":\"xs:int\",\"$\":\"3000\"}},"
				+ "	{\"name\":\"$modelversion\",\"value\":{\"@type\":\"xs:string\",\"$\":\"" + MODEL_VERSION + "\"}},"
				+ "	{\"name\":\"$activityid\",\"value\":{\"@type\":\"xs:int\",\"$\":\"10\"}},"
				+ "	{\"name\":\"type\",\"value\":{\"@type\":\"xs:string\",\"$\":\"workitem\"}},"
				+ "	{\"name\":\"_subject\",\"value\":{\"@type\":\"xs:string\",\"$\":\"TEST \"}},"
				+ "	{\"name\":\"txtprocessref\",\"value\":{\"@type\":\"xs:string\",\"$\":\"Auftragsabwicklung\"}},"
				+ "	{\"name\":\"txtList\",\"value\":[{\"@type\":\"xs:string\",\"$\":\"A\"},{\"@type\":\"xs:string\",\"$\":\"B\"}]},"
				+ "	{\"name\":\"txtname\",\"value\":{\"@type\":\"xs:string\",\"$\":\"workitem json test\"}}";
		
		
		// http://www.jsonschema.net/
		try {
			int httpResult = restClient.postJsonEntity(WORKFLOW_URI
					+ "/workitem.json", json);
	
			String sContent = restClient.getContent();
			// expected result 200
			Assert.assertEquals(200, httpResult);
	
			Assert.assertTrue(sContent.indexOf("$uniqueid") > -1);
		} catch (Exception e) {
	
			e.printStackTrace();
			Assert.fail();
		}
	
	}

	
}
