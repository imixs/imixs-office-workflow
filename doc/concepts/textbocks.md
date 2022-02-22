# Textblocks

Textblocks are documents providing a text, html or file content. These textblocks are used in several situations to extend the content. For example a textblock may be used in a E-Mail message or displayed as a text instruction for a workitem.

## Adminstration
Textbocks can be managed in the administration section 'Textblocks'. Each textblock is identified uniquely by its name. 

## TextBlockController

The CDI bean `TextBlockController` provides methods to search and create textblocks. 

 
## Modelling

A textbock can be part of a workitem description. 


	<textblock>MyTextBock</textblock>

In Imixs-Office-Workflow a textbock can also be refered via the BPMN model by its Workflow Group and Workflowstatus if the name of the textblock corresponds to this pattern. 

	<textblock><itemvalue>$workflowgroup</itemvalue> - <itemvalue>$workflowstatus</itemvalue></textblock>

In this way a generic feature exits to include the task description based on textblock information 
	
Example:

	<!-- the following code computes the txtworkflow abstract from the current modelversion and processid  -->
	<ui:param name="instruction" value="#{modelController.getProcessDescription(workflowController.workitem.item['$taskid'],workflowController.workitem.item['$ModelVersion'],workflowController.workitem)}"></ui:param>
	<h:panelGroup layout="block" styleClass="imixs-instruction"
		rendered="#{! empty instruction}">
		<h:outputText escape="false" value="#{instruction}"/>
	</h:panelGroup> 



## Rest API

There is also a RestAPI Endpoint to access the content of a Textblock by its name or $uniqueid:

 - `/api/textblock/{name|$uniqueid}/text`  -> returns the content as text
 - `/api/textblock/{name|$uniqueid}/html`  -> returns the content as html
 - `/api/textblock/{name|$uniqueid}/html`  -> returns a HMTL page with the textblock content
 - `/api/textblock/{name|$uniqueid}/$file/name`  -> returns a file attached to a textbock.
 
 
 

	