# Custom Parts

With 'Form Parts' you can design custom forms via the BPMN model. 

A custom part is a HTML element located under the path `/pages/workitesm/parts/`. The location of a part can be defined in the attribute `path` of a custom input type:


	<item name="mycustomitem" 
	      path="[PART_NAME]" 
	      type="custom" 
	      label="My Custom Label" 
	      required="true" 
	      readonly="false" />

Note: A custom item is defined by a JSF ui:composition placed in the directory `/pages/workitems/parts/`

	/pages/workitems/parts/[PART_NAME].xhtml

The mandatory path attribute contains the path for JSF component relative to the /pages/workitems/parts/ directory with the .xhtml extension. You can also use sub directories to group custom input items.

	/pages/workitems/parts/[SUB_DIR]/[PART_NAME].xhtml

See the following example of a custom input field definition:

	<ui:composition xmlns="http://www.w3.org/1999/xhtml"
		xmlns:ui="http://xmlns.jcp.org/jsf/facelets"
		xmlns:f="http://xmlns.jcp.org/jsf/core"
		xmlns:c="http://xmlns.jcp.org/jsp/jstl/core"
		xmlns:a="http://xmlns.jcp.org/jsf/passthrough"
		xmlns:h="http://xmlns.jcp.org/jsf/html">
		<span class="custom-class"> 
		   <h:inputText style="border:2px solid blue;" 
		                value="#{workitem.item[item.name]}"
		                a:placeholder="enter custom data..." 
		                rendered="#{!readonly}" />
		    <h:outputText style="border:2px solid blue;" 
		                value="#{workitem.item[item.name]}"
		                rendered="#{readonly}" />
		</span>
	</ui:composition>

Note: This custom input form uses also the attributes ‘readonly’ and ‘required’ to determine if the input component is editable or readonly.

## Custom Input Section

As an alternative to the custom input fields you can also define custom sections. A custom section defines a complete form section and is used for more complex input forms including ajax.

	<imixs-form-section label="My Custom section" path="sub_custom_form" /> 

Note: A custom section is defined by a JSF ui:composition placed in the directory:

	/pages/workitems/forms/

See the following example of a custom input field definition:

	<ui:composition xmlns="http://www.w3.org/1999/xhtml"
		xmlns:ui="http://xmlns.jcp.org/jsf/facelets"
		xmlns:f="http://xmlns.jcp.org/jsf/core"
		xmlns:c="http://xmlns.jcp.org/jsp/jstl/core"
		xmlns:h="http://xmlns.jcp.org/jsf/html"
		xmlns:i="http://xmlns.jcp.org/jsf/composite/imixs"
		xmlns:marty="http://xmlns.jcp.org/jsf/composite/marty">
		<!-- show outgoing references -->
		<div class="imixs-form-section">
			<dl>
				<dt class="imixs-no-print">
					<h:outputText value="#{message['form.reference.outgoing']}" />
				</dt>
				<dd>
					<marty:workitemLink workitem="#{workitem}" readonly="#{readonly}" 
						filter="" />
				</dd>
			</dl>
		</div>
	</ui:composition>

Note: Also in a custom section you can use the attribute ‘readonly’ to determine if the input components are editable or readonly.

## Ajax Support

Within a custom form part or a custom section you can also trigger Ajax events refreshing other form parts or sections within the same form definition. For this behaviour you can refer to the clientId of the component ‘formComponents’. See the following example:

	<h:selectOneMenu 
		required="#{empty required?false:required}"
		value="#{workflowController.workitem.item['space.ref']}">
			<f:selectItem itemLabel="a"></f:selectItem>
			<f:selectItem itemLabel="b"></f:selectItem>
			<f:selectItem itemLabel="c"></f:selectItem>
			
		<!-- trigger refresh over all components -->	
		<f:ajax render="#{formComponents.clientId}"  />
		
	</h:selectOneMenu>

In this example choosing a new option from the select menu will refresh all other form components.
The customFormComponents

If you just want to update the elements of the custom form you can refere to the custom forms section by the binding customFormComponents

	<h:selectOneMenu value="#{workitem.item['myselection']}">
			<f:selectItem itemLabel=""></f:selectItem>
			.....
			<f:ajax render="#{customFormComponents.clientId}"/>
	</h:selectOneMenu>

This will trigger a render event only on the parts within the customForm Container
Input Fields & Item Names

Even if you can define the item names of your input fields in your custom form free, it is recommended to use a naming concept. This allows you to reuse code in a more easy way. Imixs-Office-Workflow defines already a set of standard item names used for different business objects. This naming convention makes it more easy to group related items and to exchange data with your business process architecture.

The following sections list the business items predefined by Imixs-Office-Workflow. For application specific item names the ‘dot.Case’ format is recommended. It’s basically a convention that makes it easier to see what properties are related.


## Workitem Linking

Imixs-Office-Workflow provides a way to link workitems together:

	<item name="project.ref" 
	      type="custom"  
	      path="workitemlink"
	      options="$workflowgroup:Projekt"
	      label="Project:" />


With the `options` tag you can specify the search filter to lookup for workitems. 

Independent from the property 
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
 
## Display external references

It is also possible to display workitems with a referrence to the current workitem:

	<item name="payment.ref" 
      type="custom"  
      path="workitemref"
      options="($workflowgroup:Payment) "
      label="Payments" />

External references can not be edited by the current workitem.

The references can also be displayed in a table layout.
 
	<item name="payment.ref" 
      type="custom"  
      path="workitemreftable"
      options="($workflowgroup:Payment) "
      label="Payments" />
 
 

## Layout and CSS
There are different CSS classes defined wthin the imixs-marty.css file. You can overwrite
  the layout of the workitemlink widget.

 * marty-workitemlink - defines the main widget container

 * marty-workitemlink-referencebox - containing the reference workitem entries
 
 * marty-workitemlink-referencebox-entry - a single workitem entry

 * marty-workitemlink-inputbox - container with the input field

 * marty-workitemlink-resultlist - the container where the suggest result is presented

 * marty-workitemlink-resultlist-entry - a single workitem entry
 

 