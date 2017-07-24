package com.imixs.workflow.office.web;

import java.io.Serializable;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.resource.ResourceException;

import org.imixs.workflow.archive.hadoop.jca.HelloWorldConnection;
import org.imixs.workflow.archive.hadoop.jca.HelloWorldConnectionFactory;

/**
 * Test Hadoop service
 * 
 * @author rsoika
 * 
 */
@Named
@RequestScoped
public class HaddopController implements Serializable {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(HaddopController.class.getName());

	
	@Resource(mappedName = "java:/jca/org.imixs.workflow.hadoop")
	private HelloWorldConnectionFactory connectionFactory;

		

	public String getStatus()  {
		logger.info("Wir testen den Service..");
		
		String result = null;
		 HelloWorldConnection connection = null;
        try {
             connection = connectionFactory.getConnection();               
             result = connection.helloWorld();
            
        } catch (ResourceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        

/*		if (hadoopService != null) {
			
			
			try {
				hadoopService.createConfiguration("Das ist ein Test");
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return "1hadoop ok :-)";
		} else
		
		*/
			return "2hadoop not ok :-(";
	}

}
