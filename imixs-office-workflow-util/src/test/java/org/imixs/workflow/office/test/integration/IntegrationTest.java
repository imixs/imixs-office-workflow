package org.imixs.workflow.office.test.integration;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

/**
 * This JUnit Helper Class is used to skip integration tests.
 * <p>
 * This class tests if a connection to the rest service end-point can be
 * established.
 * 
 * @author rsoika
 * @version 1.0
 */
public class IntegrationTest {

	private String serviceEndpoint;
	private final static Logger logger = Logger.getLogger(IntegrationTest.class.getName());

	public IntegrationTest(String uri) {
		this.serviceEndpoint = uri;
	}

	public boolean connected() {
		try {
			HttpURLConnection urlConnection = (HttpURLConnection) new URL(serviceEndpoint).openConnection();
			urlConnection.connect();
			return true;
		} catch (IOException e) {
			logger.info("Integration Test skipped!");
			return false;
		}
	}
}
