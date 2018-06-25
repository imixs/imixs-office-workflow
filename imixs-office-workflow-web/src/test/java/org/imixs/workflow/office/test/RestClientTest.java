package org.imixs.workflow.office.test;

import java.util.logging.Logger;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.services.rest.RestClient;
import org.imixs.workflow.xml.XMLDocumentAdapter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class RestClientTest {
	RestClient restClient;
	final static String USERID = "xxx";
	final static String PASSWORD = "yyy";
	final static String URI = "http://localhost:8080/office-rest/workflow/workitem";

	private final static Logger logger = Logger.getLogger(RestClientTest.class
			.getName());

	@Before
	public void setUp() throws Exception {
		restClient = new RestClient();
		restClient.setCredentials(USERID, PASSWORD);
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
			int iHTTPResult = restClient.postEntity(URI,
					XMLDocumentAdapter.getDocument(workitem));

			if (iHTTPResult < 200 || iHTTPResult > 299) {
				if (iHTTPResult == 404)
					logger.severe("The requested resource could not be found. Please verifiy your web service location.");
				else if (iHTTPResult == 403)
					logger.severe("The username/password you entered were not correct. Your request was denied as you have no permission to access the server. Please try again.");
				else
					logger.severe("The model data could not be uploaded to the workflow server. Please verifiy your server settings. HTTP Result");

				Assert.fail();
			}
			Assert.assertTrue(iHTTPResult >= 200 && iHTTPResult < 300);

		} catch (Exception e) {

			e.printStackTrace();
			Assert.fail();
		}
		
	}

}
