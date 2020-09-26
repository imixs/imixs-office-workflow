package org.imixs.workflow.office.test.integration;

import java.util.List;

import org.imixs.melman.EventLogClient;
import org.imixs.melman.FormAuthenticator;
import org.imixs.melman.RestAPIException;
import org.imixs.workflow.ItemCollection;
import org.junit.Before;
import org.junit.Test;

import junit.framework.Assert;

/**
 * This test simmulates batch processing with high load
 * 
 * @author rsoika
 *
 */
public class EventLogLoadRunner {

    static String BASE_URL = "http://localhost:8080/api";
    static String USERID = "admin";
    static String PASSWORD = "adminadmin";
    EventLogClient eventLogClient = null;

    private IntegrationTest integrationTest = new IntegrationTest(BASE_URL);

    /**
     * The setup method deploys the ticket workflow into the running workflow
     * instance.
     * 
     * @throws Exception
     */
    @Before
    public void setup() throws Exception {
        // Assumptions for integration tests
        org.junit.Assume.assumeTrue(integrationTest.connected());

        eventLogClient = new EventLogClient(BASE_URL);
        // create a default basic authenticator
        FormAuthenticator formAuth = new FormAuthenticator(BASE_URL,USERID, PASSWORD);
        // register the authenticator
        eventLogClient.registerClientRequestFilter(formAuth);

    }

    /**
     * Run 100 creations of a process instance...
     * 
     * @throws RestAPIException
     */
    @Test
    public void pollEventLogTest() throws RestAPIException {

        int MAX_COUNT = 1000;
        int count = 0;
        long l = System.currentTimeMillis();
        System.out.println("Start polling event log 1000 times...");
        while (count < MAX_COUNT) {

            eventLogClient.setPageSize(100);
            List<ItemCollection> events = eventLogClient.searchEventLog("snapshot.add", "snapshot.remove");

            Assert.assertNotNull(events);
            count++;
        }
        long time = System.currentTimeMillis() - l;

        System.out.println("Finished - processing time=" + (time) + "ms");

    }

}