package org.imixs.workflow.office.test;

import java.util.Random;
import java.util.logging.Logger;

import org.imixs.melman.BasicAuthenticator;
import org.imixs.melman.DocumentClient;
import org.imixs.workflow.ItemCollection;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * This test generates random test data
 * 
 * @author rsoika
 * 
 */
public class GenerateTestData {
	DocumentClient documentClient;
	final static String USERID = "admin";
	final static String PASSWORD = "adminadmin";
	final static String URI = "http://localhost:8080/office-rest/workflow/workitem";

	final static int MAX_COUNT = 5000;

	Random generator = null;
	private final static Logger logger = Logger
			.getLogger(GenerateTestData.class.getName());

	@Before
	public void setUp() throws Exception {
		generator = new Random(19580427);
		documentClient  = new DocumentClient(URI);
		BasicAuthenticator basicAuth = new BasicAuthenticator(USERID, PASSWORD);
		// register the authenticator
		documentClient.registerClientRequestFilter(basicAuth);
		
	}

	@Ignore
	@Test
	public void generateRandomWorkitems() {

		logger.info("create " + MAX_COUNT + " new workitems....");

		for (int i = 0; i < MAX_COUNT; i++) {
			ItemCollection workitem = createRandomWorkitem(i);

			try {
				ItemCollection doc=documentClient.saveDocument(workitem);
				
				Assert.assertNotNull(doc);

			} catch (Exception e) {

				e.printStackTrace();
				Assert.fail();
			}
		}

	}

	private ItemCollection createRandomWorkitem(int i) {
		logger.info("create new workitem");
		ItemCollection workitem = new ItemCollection();
		workitem.replaceItemValue("type", "workitem");
		workitem.replaceItemValue("$ModelVersion", "office-de-0.0.2");
		workitem.replaceItemValue("$ProcessID", 1000);
		workitem.replaceItemValue("$ActivityID", 10);

		workitem.replaceItemValue("$uniqueidref", "14154510767-16938bd7");

		workitem.replaceItemValue("_customer", "C" +i);

		workitem.replaceItemValue("_subject",
				"sample record" + generator.nextInt());

		return workitem;
	}
}
