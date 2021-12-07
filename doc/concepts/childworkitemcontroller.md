# The ChildWorkitemController

The ChildWorkitemController acts as a front controller for child workitems. 
 A child workitem references another workitem. Each workitem can have a list of child
 workitems. The Controller provides methods to list the child references to a workitem. 
 So the ChildWorkitem can be used together with the wokflowController. The default type 
 of a new child worktiem is 'workitemchild'. The type can be changed and controlled by the 
 workflow model.

## Starting a child process
To start a child process you can define sub workflow groups to any define workflow group 
 in a model. A sub workflow group contains a '~' which separates the main workflow Group to 
 a sub workflow group.

 For example:

 * Project

 * Project~Task
 
 * Project~Minute


 
The Project workflow has two sub workflows which can be used together with the 
 ChildWorkitemController.

This is an example how to start a child process depending on sub workfow groups:

	<ui:repeat
			value="#{modelController.getSubWorkflowGroups(workflowController.workitem.item['txtWorkflowGroup'])}"
			var="group">
			<ui:param name="entity"
				value="#{modelController.getInitialProcessEntityByGroup(group)}"></ui:param>
		
			<h:commandButton value="#{fn:substringAfter(group,'~')}"
				class="ui-button-primary">
				<f:setPropertyActionListener
					target="#{childController.workitem.item['$modelversion']}"
					value="#{workflowController.workitem.item['$modelversion']}" />
				<f:setPropertyActionListener
				target="#{childController.workitem.item['$processid']}"
					value="#{entity.item['numProcessID']}" />
				<f:setPropertyActionListener
					target="#{childController.workitem.item['$uniqueidRef']}"
					value="#{workflowController.workitem.item['$uniqueid']}" />
				</h:commandButton>
		</ui:repeat>

	
## The ChildController

The CDI Bean ChildItemController acts as a front controller for child workitems. 
 A child item is an embedded List of entities represented as Map interfaces. 
 The Controller provides methods to manage and list the child items of a workitem. 
 The List of embedded Items is converted into ItemCollection entries to simplify the 
 usage in a JSF Page.
 
The child items are stored in the property '_childItems'.
 
### Embedding child items

The following example illustrates the usage of the ChildItemController in JSF page:

	<ui:composition xmlns="http://www.w3.org/1999/xhtml"
		xmlns:ui="http://java.sun.com/jsf/facelets"
		xmlns:f="http://java.sun.com/jsf/core"
		xmlns:c="http://java.sun.com/jsp/jstl/core"
		xmlns:h="http://java.sun.com/jsf/html">
	
	
	<h:panelGroup layout="block" styleClass="imixs-form-section" id="oderlist">
	
			<table class="imixsdatatable imixs-orderitems">
	
				<tr>
					<th>Bezeichnung</th>
					<th>Einzelpreis</th>
					<th>Menge</th>
					<th>Gesamtpreis</th>
					<th>Lieferant</th>
				</tr> 
				<ui:repeat var="orderitem" value="#{childItemController.childItems}">
						<tr>
							<td><h:inputText value="#{orderitem.item['name']}" /></td>
							<td><h:inputText value="#{orderitem.item['price']}"/></td>
							<td><h:inputText value="#{orderitem.item['qty_ordered']}" /></td>
							<td><h:inputText value="#{orderitem.item['price_incl_tax']}" /</td>
						</tr>
				</ui:repeat>
			</table>
			<h:commandButton value="#{message.add}" actionListener="#{childItemController.add}">
				<f:ajax render="oderlist"></f:ajax>
			</h:commandButton>
		</h:panelGroup>
	
	</ui:composition>
	