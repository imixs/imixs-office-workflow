package org.imixs.workflow.office.rest;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * RestServiceApp is the main service class for the rest-service interface.
 * 
 * Resource = /api
 * 
 * @author rsoika
 *
 */
@ApplicationPath("/api")
public class RestServiceApp extends Application {
	
	public RestServiceApp() {
		super();
	}
	
}
