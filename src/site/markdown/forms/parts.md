# Form Input Parts

Within a Imixs-Office-Workflow [Form Definition](index.html) you can define different type of input parts within a `imixs-form-section`:
	
	<imixs-form-section label="Controlling">
		<item name="name" type="text"
		      label="Name" />
		<item name="description" type="textarea"
		      label="Short Description" />
	</imixs-form-section>


## Input Parts

The various input item definitions are called 'input parts':

### Text Input

	<item name="description" type="text"
	        label="Topic" />

### Textarea Input

	<item name="description" type="textarea"
	        label="Description" />

### HTML/RichText Input

	<item name="description" type="html"
	        label="Description" />

### Date Input

	<item name="invoice.date" type="date"
	        label="Date" />

### Select Boxes

You can also create different type of select boxes with predefined values:

	 <item name="invoice.currency" type="selectOneMenu" 
		label="Currency:"
		options="EUR;CHF;SEK;NOK;GBP;USD" />


You can choose one of the following types for select boxes:

 - *selectOneMenu*  - a dropdown menu
 - *selectBooleanCheckbox* - a single checkbox
 - *selectManyCheckbox* - a list of checkboxes  (layout=line direction)
 - *selectOneRadio* - radio buttons (layout=line direction)
 
*selectManyCheckbox* and *selectOneRadio* are displayed in line direction per default. If you want to display them in page direction use:

 - *selectManyCheckboxPageDirection* - a list of checkboxes (layout=page direction)
 - *selectOneRadioPageDirection* - radio buttons (layout=page direction)


You can also add a mapping of the name displayed in the select box and an optional value by using the '|' char:


	<item name"myfield" type="selectOneMenu" required="true" label="Your Choice"
	  options="management.it|Option A;management.backoffice|Option B" />
						




### User Input

The user input can be used to edit a single user name. The part provides a lookup feature for profile names

	<item name="user" 
	      type="custom"  
	      path="userinput"
	      label="User:" />
	      



### User-List Input

The User-List-Input alowes to enter a list of user names. The part provides a lookup feature for profile names

	<item name="userlist" 
	      type="custom"  
	      path="userinput"
	      label="User:" />



### Workitem Linking

Imixs-Office-Workflow provides a way to link workitems together:

	<item name="project.ref" 
	      type="custom"  
	      path="workitemlink"
	      options="$workflowgroup:Projekt"
	      label="Project:" />


With the `options` tag you can specify the search filter to lookup for workitems. 



## Required Inputs
With the tag 'required' a mandatory input is defined:

	<item name="_date" type="date" required="true"
	        label="Date" />

   	 
 
<img class="screenshot" src="imixs-bpmn-custom-forms-example-768x538.png" /> 




