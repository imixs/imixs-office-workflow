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

### Textlist

A textlist is displayed as a input textarea. The entries of separate lines are stored as multiple values in an Item List. This input type can be useful e.g. for lists of E-Mail addresses or a list of order-numbers.

    <item name="references" type="textlist"
            label="Order References" />

### Textarea Input

    <item name="description" type="textarea"
            label="Description" />

### HTML/RichText Input

    <item name="description" type="html"
            label="Description" />

### Date Input

    <item name="invoice.date" type="date"
            label="Date" />

### Currency Input

    <item name="invoice.amount" type="currency"
            label="Amount" />

### Select Boxes

You can also create different type of select boxes with predefined values:

     <item name="invoice.currency" type="selectOneMenu"
    	label="Currency:"
    	options="EUR;CHF;SEK;NOK;GBP;USD" />

You can choose one of the following types for select boxes:

- _selectOneMenu_ - a dropdown menu
- _selectBooleanCheckbox_ - a single checkbox
- _selectManyCheckbox_ - a list of checkboxes (layout=line direction)
- _selectOneRadio_ - radio buttons (layout=line direction)

_selectManyCheckbox_ and _selectOneRadio_ are displayed in line direction per default. If you want to display them in page direction use:

- _selectManyCheckboxPageDirection_ - a list of checkboxes (layout=page direction)
- _selectOneRadioPageDirection_ - radio buttons (layout=page direction)

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
