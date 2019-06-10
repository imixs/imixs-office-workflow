package org.imixs.workflow.office.test;

import java.util.logging.Logger;

import org.imixs.melman.BasicAuthenticator;
import org.imixs.melman.DocumentClient;
import org.imixs.workflow.ItemCollection;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class RestClientTest {
	DocumentClient documentClient;
	final static String USERID = "xxx";
	final static String PASSWORD = "yyy";
	final static String URI = "http://localhost:8080/office-rest/workflow/workitem";

	private final static Logger logger = Logger.getLogger(RestClientTest.class
			.getName());

	@Before
	public void setUp() throws Exception {
		
		documentClient  = new DocumentClient(URI);
		BasicAuthenticator basicAuth = new BasicAuthenticator(USERID, PASSWORD);
		// register the authenticator
		documentClient.registerClientRequestFilter(basicAuth);
		
	}

	@Ignore
	@Test
	public void test() {

		logger.info("create new workitem");
		ItemCollection workitem = new ItemCollection();
		workitem.replaceItemValue("$ModelVersion", "office-de-0.0.2");
		workitem.replaceItemValue("$ProcessID", 1000);
		workitem.replaceItemValue("$ActivityID", 10);

		workitem.replaceItemValue("_subject", "sample record");

		try {
			ItemCollection result = documentClient.saveDocument(workitem); 
					
			Assert.assertNotNull(result);
			
		} catch (Exception e) {

			e.printStackTrace();
			Assert.fail();
		}
		
	}

}
