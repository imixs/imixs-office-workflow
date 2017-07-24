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

import org.imixs.workflow.archive.hadoop.jca.HelloWorldConnection;
import org.imixs.workflow.archive.hadoop.jca.HelloWorldConnectionFactory;

@WebServlet("/JCATest")
public class JCATestServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Resource(mappedName = "java:/jca/org.imixs.workflow.hadoop")
	private HelloWorldConnectionFactory connectionFactory;

	public JCATestServlet() {
		super();

	} 

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String result = "JCA Test - Hello World - geth noch: "+getJCAData();

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

	
	private String getJCAData() {
		String result = "";
		 HelloWorldConnection connection = null;
         try {
              connection = connectionFactory.getConnection();               
              result = connection.helloWorld();
              
              result=result+connection.helloWorld(" Das war ein erfolgreicher Test...");
             
         } catch (ResourceException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         
         return result;

	}
}