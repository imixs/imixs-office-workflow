package org.imixs.workflow.rest;

import java.util.Map;
import java.util.Random;

public abstract class AbstractWorkflowTest {

protected static String MODEL_VERSION="office-de-0.0.2";
	protected static String USERID = "admin";
	protected static String PASSWORD = "adminadmin";
	protected static String WORKFLOW_URI = "http://localhost:8080/office-rest/workflow";
		 
	/**
	 * creates a json structure for a worktiem
	 * 
	 * @param model
	 * @param process 
	 * @param activity
	 * @return
	 */
	public static String createJsonObject(String model, int process,
			int activity, Map<?,?> map) {

		// generate dummy name
		Random randomGenerator = new Random();
		String subject = "JUnit Test-" + randomGenerator.nextInt(10000);

		// create a json test string
		String json = "{\"item\":["
				+ "	{\"name\":\"$processid\",\"value\":{\"@type\":\"xs:int\",\"$\":\"" + process + "\"}},"
				+ "	{\"name\":\"$modelversion\",\"value\":{\"@type\":\"xs:string\",\"$\":\"" + model + "\"}},"
				+ "	{\"name\":\"$activityid\",\"value\":{\"@type\":\"xs:int\",\"$\":\"" + activity + "\"}},"
				+ "	{\"name\":\"_subject\",\"value\":{\"@type\":\"xs:string\",\"$\":\""
				+ subject
				+ "\"}},"
				+ "	{\"name\":\"txtList\",\"value\":[{\"@type\":\"xs:string\",\"$\":\"A\"},{\"@type\":\"xs:string\",\"$\":\"B\"}]},"
				+ "	{\"name\":\"txtname\",\"value\":{\"@type\":\"xs:string\",\"$\":\"workitem json test\"}}";
		
		
		if (map!=null) {
		for (Map.Entry<?,?> entry : map.entrySet()) {
		    System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
		    
		    json=json+ "	{\"name\":\"" + entry.getKey()  +"\",\"value\":{\"@type\":\"xs:string\",\"$\":\"" +entry.getValue() + "\"}}";
			
		}
		}
		json=json+ "]}";

		return json;
	}

	/**
	 * extract the unqiueid from a json structure
	 * 
	 * @param content
	 * @return
	 */
	public String findUnqiueId(String content) {
		// {\"name\":\"$uniqueid\",\"value\":{\"@type\":\"xs:string\",\"$\":\"xxx\"}}"

		int iPos = content.indexOf("$uniqueid");
		if (iPos > -1) {
			iPos = content.indexOf("$", iPos + 1);
			iPos = content.indexOf(":", iPos + 1) + 2;
			int iEnd = content.indexOf("\"", iPos + 1);
			return content.substring(iPos, iEnd);

		}

		return null;

	}

}
