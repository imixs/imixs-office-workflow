package org.imixs.workflow.office.test;

import java.util.logging.Logger;

import org.imixs.melman.DocumentClient;
import org.imixs.melman.FormAuthenticator;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.office.test.integration.IntegrationTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RestClientTest {
	DocumentClient documentClient;
	final static String USERID = "admin";
	final static String PASSWORD = "adminadmin";
	final static String BASE_URL = "http://localhost:8080/api";

	private final static Logger logger = Logger.getLogger(RestClientTest.class
			.getName());

    private IntegrationTest integrationTest = new IntegrationTest(BASE_URL);

	@Before
	public void setUp() throws Exception {
	    // Assumptions for integration tests
        org.junit.Assume.assumeTrue(integrationTest.connected());

		documentClient  = new DocumentClient(BASE_URL);
		FormAuthenticator formAuth = new FormAuthenticator(BASE_URL,USERID, PASSWORD);
		//BasicAuthenticator basicAuth = new BasicAuthenticator(USERID, PASSWORD);
		// register the authenticator
		documentClient.registerClientRequestFilter(formAuth);
		
	}

	
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
