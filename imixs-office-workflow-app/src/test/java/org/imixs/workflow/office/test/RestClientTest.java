package org.imixs.workflow.office.test;

import java.util.logging.Logger;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.services.rest.BasicAuthenticator;
import org.imixs.workflow.services.rest.RestClient;
import org.imixs.workflow.xml.XMLDocument;
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
		restClient.registerRequestFilter(new BasicAuthenticator(USERID, PASSWORD));
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
			XMLDocument xmlResult = restClient.postXMLDocument(URI,
					XMLDocumentAdapter.getDocument(workitem));

			Assert.assertNotNull(xmlResult);
			
		} catch (Exception e) {

			e.printStackTrace();
			Assert.fail();
		}
		
	}

}
