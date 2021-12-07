# Pats

Imixs-Office-Workflow provides so called 'Form Parts' to be used to design custom forms via the BPMN model.

The following section describs some custom part elements

# Workitem Linking

Imixs-Office-Workflow provides a way to link workitems together - independent from the property 
 "$UnqiueIDRef". The uniqueIDs from workitems linked to the current workitem are stored in 
 the property "txtWorkitemRef".

The custom ui widget 'workitemlink' can be used to link a workitem with other workitems.  The widget is provided as a custom ui component and can be added into a jsf page using the marty component library. See the following example:

		<f:subview id="order">
			<ui:include src="/pages/workitems/parts/workitemlink.xhtml">
				<ui:param name="options" value="$workflowgroup:Order" />
			</ui:include>
		</f:subview>


The custom tag 'workitemlink' provides the following attributes:

 * workitem : defines the workitem the references should be added to
 
 * filter : a custom filter for the lucene search and display existing references (reg expressions are supported here)

 * hidereferences : default = false - true hides the reference list from the widget.


 
Note: the references displayed by the widget are bound directly to the workitem managed by 
 the workflowController bean. This is independet from the property 'workitem'
 
### Display external references
It is also possible to display workitems referred by the current workitem using the widget _workitemExternalReferences_

	<marty:workitemExternalReferences workitem="#{workflowController.workitem}"
					filter="$processid:9..." /> 

External references can not be edited by the current workitem. 

 
### The Marty Tag Libray

The WorkitemLink component requires that the subform workitemlink_resultlist is places at once in the ui:form
			
	<ui:include src="/pages/workitems/parts/workitemlinkSearch.xhtml" />
			   
			
 

##Layout and CSS
There are different CSS classes defined wthin the imixs-marty.css file. You can overwrite
  the layout of the workitemlink widget.

 * marty-workitemlink - defines the main widget container

 * marty-workitemlink-referencebox - containing the reference workitem entries
 
 * marty-workitemlink-referencebox-entry - a single workitem entry

 * marty-workitemlink-inputbox - container with the input field

 * marty-workitemlink-resultlist - the container where the suggest result is presented

 * marty-workitemlink-resultlist-entry - a single workitem entry
 

 