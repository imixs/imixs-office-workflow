package org.imixs.test;

import java.io.IOException;
import java.io.PrintWriter;

import javax.annotation.Resource;
import javax.resource.ResourceException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.connectorz.files.Bucket;
import org.connectorz.files.BucketStore;

@WebServlet("/GenericJCATest")
public class JCATestServletGeneric extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Resource(mappedName = "java:/jca/org.imixs.workflow.hadoop.generic")
	 BucketStore bucketStore;

	public JCATestServletGeneric() {
		super();
 
	} 

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String result = "Generic JCA Bucket Test - Hello World - geth auch noch: "+getJCAGenericData();

		PrintWriter out = response.getWriter();
		out.println(result);

		out.flush();
		// connection.close();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

	
	private String getJCAGenericData() {
		String result = "";
		
		
			 Bucket bucket = bucketStore.getBucket();
			 
			 bucket.write("bucket-test-file.txt", "Some Bucket content - thanks Adam Bien".getBytes());
		if (bucket!=null)
              result=result+" Das war ein erfolgreicher Generic Bucket Test...";
             
       
         
         return result;

	}
}