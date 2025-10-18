# Workitem Linking

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

The custom ui widget 'workitemlink' can be used to link a workitem with other workitems. The widget is provided as a custom ui component and can be added into a jsf page using the marty component library. See the following example:

    	<f:subview id="order">
    		<ui:include src="/pages/workitems/parts/workitemlink.xhtml">
    			<ui:param name="options" value="$workflowgroup:Order" />
    		</ui:include>
    	</f:subview>

The custom tag 'workitemlink' provides the following attributes:

- workitem : defines the workitem the references should be added to

- filter : a custom filter for the lucene search and display existing references (reg expressions are supported here)

- hidereferences : default = false - true hides the reference list from the widget.

Note: the references displayed by the widget are bound directly to the workitem managed by
the workflowController bean. This is independet from the property 'workitem'

## Display references

It is also possible to display linked workitems in a form.

<img src="workitemref_01.png" />

Because a linked work item can be linked directly from the current workitem or indirectly, two differnt types of lookups are supported.

To show direct referred workitems use the part `workitemref` :

```xml
    <item name="payment.ref"
      type="custom"
      path="workitemref"
      options="($workflowgroup:Payment) "
      label="Payments" />
```

To display external workitems holding a link to the current workitem part `workitemref_external` can be used:

```xml
    <item name="payment.ref"
      type="custom"
      path="workitemref_external"
      options="($workflowgroup:Payment) "
      label="Payments" />
```

### Table Layout

The linked workitems can also be displayed in a table layout:

 <img src="workitemref_02.png" />

Use the part `workitemreftable` :

```xml
    <item name="payment.ref"
      type="custom"
      path="workitemreftable"
      options="($workflowgroup:Payment) "
      label="Payments" />
```

Or to display external workitems use `workitemreftable_external`:

```xml
	<item name="payment.ref"
      type="custom"
      path="workitemreftable_external"
      options="($workflowgroup:Payment) "
      label="Payments" />
```

## Copy Data

Optional you can copy a list of items from the linked workitem into the current workitem. Therefore use the plugin

`org.imixs.workflow.office.plugins.WorkitemLinkPlugin`

The plugin can be configured by the following workflow result definition:

```xml
<workitemlink name="copydata">
	<ref>payment.ref</event>
	<items>customer.name, customer.number</items>
      <debug>false</debug>
</workitemlink>
```

This definition copies the items 'customer.name' and 'customer.number' from the workitem referred by 'payment.ref' into the current workitem.

To avoid name conflicts, the item name can be mapped to a different name by separating the new name with the ‘|’ char.

```xml
   <items>customer.name|contact.name</items>
```

In this example the item `customer.name` will be copied into the target workitem with the new item name `contact.name` .
Copy Items by Regex

The items tag also supports regular expression. See the following example with will copy all items starting with 'customer.':

```xml
<workitemlink name="copydata">
	<ref>payment.ref</event>
  <items>(^customer\.)</items>
</workitemlink>
```

A regular expression must always be included in brackets.

Note: In case of a regular expression you can not use item name mapping with the ‘|’ character.

The option `<debug>true</debug>` prints details of the copy process into the server log

## Layout and CSS

There are different CSS classes defined wthin the imixs-marty.css file. You can overwrite
the layout of the workitemlink widget.

- marty-workitemlink - defines the main widget container

- marty-workitemlink-referencebox - containing the reference workitem entries

- marty-workitemlink-referencebox-entry - a single workitem entry

- marty-workitemlink-inputbox - container with the input field

- marty-workitemlink-resultlist - the container where the suggest result is presented

- marty-workitemlink-resultlist-entry - a single workitem entry
