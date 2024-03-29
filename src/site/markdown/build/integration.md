# How to integrate from an external System?

 Imixs Office Workflow supports several ways to integrate from any external software system. 
 An integration from an external system means to interact with the imixs workflow to 
 read or write data. For example these are some typical scenarios of an integration:

 * create a new process instance (workitem) for a specific workflow process

 * process an existing process instance (workiem) with a predefined workflow activity.

 * loading a process instance (workitem) to read process information like the current state.

 * fetching a list of workitems for a specify user scenario (e.g. worklist, my-tasks, open issues,...)

 * fetching information about the actual workflow model


 Before you can interact with the various modules of Imixs Office Workflow from an 
 external system you need to provide a user principal. As all of the interfaces can only 
 be accessed with an authenticated user (principal), the user principal used for interaction 
 need to have at lease one of the following user roles

 * org.imixs.ACCESSLEVEL.READERACCESS

 * org.imixs.ACCESSLEVEL.AUTHORACCESS

 * org.imixs.ACCESSLEVEL.EDITORACCESS

 * org.imixs.ACCESSLEVEL.MANAGERACCESS



 If you want to create or update a process instance from Imixs Office Workflow the 
 external system has to know the workflow models deployed on Imixs Office Workflow. 
 For example if you want to create a new instance of a process you have to know the 
 model version ($modelVersion) and the initial process id ($processID). Those information 
 can also be fetched dynamically from the Imixs model service.

 As Imixs Workflow is based on the Java Enterprise Edition (JEE) there are several 
 common ways and interfaces which can be used for an integration. The following section 
 describes the recommended usage of these interfaces.

## The Imixs REST api

 REST is an architectural style that can be used to guide the construction of web services 
 in a more easy kind as a service which uses the SOAP specification. The Imixs XML & Web 
 Services provide a REST Service Implementation which allows you to use the Imixs Workflow 
 in a RESTful way.

 An URI is a unique resource identifier. In REST an URI represents a resource and is used to 
 get or modify a resource. In the meaning of the Imixs Workflow Technologies the URIs 
 represent WorkItems, Attachments, Reports or any other kind of Object provided by the 
 Imixs Workflow. There are three main resources available in Imixs Office Workflow where 
 each represents one different aspect of the Imixs Workflow components:

 * /workflow The Workflow resource provides resources and methods to get, create or modify workitems

 * /model The Model resource provides resources and methods to get, create or modify a workflow model entities

 * /report The Report resource provides resources and methods to get the result of a report definition
 
 []
 
 Each of three main resources provides a set of sub resources which represent different 
 business objects managed by the Imixs Workflow components. Each business object can be 
 provided in different formats depending on one of the following request header

 * text/html

 * application/xml

 * application/json
 

  

So if you typing in the URI into a Web Browser you will typical receife an HTML representation  of the business Objects. An detailed overview about the general usege of the imixs rest api can be found in the documentation of the [Imixs Workflow Rest API](https://www.imixs.org/doc/restapi/index.html).

 
**Note:** Using the Imixs Workflow REST api is the recommended way to interact with Imixs Office Workflow.

### Java Example:

This is an example how to use the Imixs Rest Client api to interact with a rest service.   The example creates a new workitem for a pre defined workflow model. You can also use  any other http client:

```java
package org.imixs.workflow.office.test;
import java.util.logging.Logger;
 
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.services.rest.RestClient;
import org.imixs.workflow.xml.XMLItemCollectionAdapter;
import org.junit.Assert;
import org.junit.Before;
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
	XMLItemCollectionAdapter.putItemCollection(workitem));
	 
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
```

 Please note that this example uses jax-b which makes it easy to create and post a xml 
 object to the rest service. It is also possible to post a XML stream directly. See the 
 details in the Imixs REST api.

## Imixs Script


 Imixs Script provides another easy way to interact with the imxis REST Api based on the 
 javaScript library jQuery. Using Imixs Script simplifies the way to interact with the 
 imixs rest api from a web application. See : http://www.imixs.org/script/

## Remote EJB calls

 All Imixs Workflow Services provide EJB remote interfaces based on EJB 3.1. Having access 
 to the remote EJB container any service can be accessed remotely. A remote interface of a 
 imixs workflow service ejb provide the same functionality like local calls. Please note 
 that using EJB remote calls requires a Java Enterprise environment. Remote EJB calls are 
 typical used in cases when two Java enterprise applications are tight coupled.

 A remote lookup of an ejb depends on the deployment scenario. The following example 
 demonstrates the remote lookup of an ejb running on the same application server instance.

```
try {
// building the global jndi name....
globalJNDIName = "java:global/"
+ module
+ "/WorkflowService!org.imixs.workflow.jee.ejb.WorkflowServiceRemote";
InitialContext ic = new InitialContext();
workflowService = (WorkflowServiceRemote) ic.lookup(globalJNDIName);
...
} catch (Exception e) {

```

With the workflowService instance different methods can be called:

```
@EJB
WorkflowService wfm;
	....
	ItemCollection workitem=new ItemCollection();
	// set some data
	//....
	workitem.replaceItemValue("$modelversion", "office-de-0.0.1");
	workitem.replaceItemValue("$processid", 100);
	workitem.replaceItemValue("$activityid", 10);
	workitem.replaceItemValue("some_data", "abc");
	// process workitem
	workitem=workflowService.processWorkItem(workitem);
	.......
```


This example creates a new ItemCollection and provides some data. Next the ItemCollection  is processed by the WorkflowService EJB which is returning a new instance of a workitem.

To process a workItem at least the properties "$modelversion", "$processid" and "$activityid"  need to be provided. These properties are selecting the corresponding process- and activity-entity in the current workflow model. If these model entities selected by the workitem are not defined in the current workflow model the WorkflowManager throws an ProcessingErrorException.

After the workitem was processed by the WorkflowManager you will get a new instance of your workitem containing all additional workflow information controlled by the WorkflowManager. For example the internal universalID or the current workflow status:

```
//....
workitem=wfm.processWorkItem(workitem);
String unqiueID=workitem.getItemValueString("$unqiueid");
String status=worktiem.getItemValueString("txtworkflowstatus");
```

The property "$uniqueid" identifies the workitem controlled by the workflowManager.  This ID can be used to load a workitem from the WorkflowManager:

```
  workitem=wfm.getWorkItem(uniqueid);
```

You can also receive a list of workitems for the current user by calling the method getWorkList().

```
  List<ItemCollection> worklist=wfm.getWorkList();
```

The method returns a list of all workItems assigned to the current user. A workitem is typically managed by a WorkflowManger for the complete life cycle. To remove a workitem from the WorkflowManager underlying database you can call the removeWorkitem method:

```
  wfm.removeWorkItem(workitem);
```

This method removes an instance of a workitem form the WorkflowManger. See also the section WorkflowService at http://www.imixs.org for additional information.

## Programatic login

 Using a programatic GlassFish login is another way to interact with Imixs Workflow. This is typical used in jUnit Tests. To use GlassFish programatic login you need to add the gf_client.jar directly into your classpath. The gf_client.jar is located in your Glassfish Installation at

```
$GLASSFISH_HOME/glassfish/lib/gf-client.jar
```

 See the following junit example for a simple remote lookup to Imixs Entity Service inside a jUnit test:

```
public class TestEntityService {
EntityServiceRemote entityService = null;
 
@Before
public void setup() {
	try {
		// set jndi name
		String ejbName = "java:global/imixs-workflow-web-sample-0.0.5-SNAPSHOT/EntityService!org.imixs.workflow.jee.ejb.EntityServiceRemote";
		InitialContext ic = new InitialContext();
		entityService = (EntityServiceRemote) ic.lookup(ejbName);
	} catch (Exception e) {
		e.printStackTrace();
		entityService = null;
	}
}
 
@Test
@Category(org.imixs.workflow.jee.ejb.EntityServiceRemote.class)
public void testService() {
	Assert.assertNotNull(entityService);
	//....
 
}
```

As remote ejbs are annotated with the security annotation @RolesAllowed you need to  authenticate your remote lookup. In GlassFish this can be done using the programmatic Login. To setup a programmatic login in your JUnit test first create a File named `auth.conf`. The content of that file should look like this:

``` 
default { 
 com.sun.enterprise.security.auth.login.ClientPasswordLoginModule required debug=false; 
 }; 
```

 This in an example how you can setup a login in a jUnit test:

```
@Before
public void setup() {
	try {
		// set jndi name
		String ejbName = "java:global/imixs-workflow-web-sample-0.0.5-SNAPSHOT/EntityService!org.imixs.workflow.jee.ejb.EntityServiceRemote";
		// setup programmatic login for GlassFish 3
		System.setProperty("java.security.auth.login.config", "/home/rsoika/eclipse_37/imixs-workflow/imixs-workflow-engine/src/test/resources/auth.conf");
		ProgrammaticLogin programmaticLogin = new ProgrammaticLogin();
		// set password
		programmaticLogin.login("Anna", "anna");
		InitialContext ic = new InitialContext();
		entityService = (EntityServiceRemote) ic.lookup(ejbName);
	} catch (Exception e) {
		e.printStackTrace();
		entityService = null;
	}
}
```

 The username/password have to be defined in this case in a file realm which is typically 
 the default security realm of GlassFish
 