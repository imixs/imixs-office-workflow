# Workitem Linking

Imixs-Office-Workflow provides a way to link workitems together. This means a workitem can point to another workitem which may have a different workflow. The link is defined by the `$uniqueId` stored in the item `$workitemref`. A workitem can have one or many links.

The workitems linked from a workitem are called _outbound workitem links_.

## Form Part

To set a new linked workitem in a form, you can use the custom formpart 'workitemlink'. See the following example:

```xml
<item name="project.ref"
      type="custom"
      path="workitemlink"
      readonly="false"
      options="$workflowgroup:Projekt"
      label="Project:" />
```

<img src="workitemlink_01.png" />

The user can enter a search phrase to search for a workitem within the index. With the attribute `options` an optional search filter can be applied. The tag `name` is used to hold a list of linked workitems of this type.

If you set `readonly="true"` the formpart shows only a list of linked workitems.

<img src="workitemlink_02.png" />

### Layout and CSS

There are different CSS classes defined wthin the imixs-marty.css file. You can overwrite
the layout of the workitemlink widget.

- marty-workitemlink - defines the main widget container
- marty-workitemlink-referencebox - containing the reference workitem entries
- marty-workitemlink-referencebox-entry - a single workitem entry
- marty-workitemlink-inputbox - container with the input field
- marty-workitemlink-resultlist - the container where the suggest result is presented
- marty-workitemlink-resultlist-entry - a single workitem entry

## Inbound Workitem Links

While _outbound workitem links_ describe the workitems that a workitem points to, _inbound workitem links_ define the reverse relationship: They show all workitems that reference the current workitem.

When Workitem A references Workitem B (through an entry in $workitemref of Workitem A), then:

- Workitem A has an outbound link to Workitem B
- Workitem B has an inbound link from Workitem A

Inbound links are not stored explicitly but are determined dynamically via the index. To find all workitems that reference a specific workitem, use the following Lucene query:

```sql
(type:"workitem") AND ($workitemref: "<$UNIQUEID>")
```

Here, `<$UNIQUEID>` is replaced with the actual $uniqueId of the workitem for which you want to retrieve the inbound links.

This query is particularly useful to:

- Identify dependencies on a workitem
- Check if a workitem is still referenced by others (e.g., before deletion)
- Get an overview of all assigned or linked processes

Unlike outbound links, which are actively maintained by the user, inbound links emerge automatically from the references of other workitems.

## Display Inbound Workitem Links

To display a list of inbound linked workitems you can use the formpart `workitemlink_inbound`

```xml
<item
      type="custom"
      path="workitemlink_inbound"
      readonly="false"
      options="$workflowgroup:Projekt"
      label="Project:" />
```

<img src="workitemlink_02.png" />

**Note:** The inbound linked workitems can only be referred by the item `$workitemref`. The attribute 'name' is in this case not relevant and will be ignored.

### Table Layout

The linked workitems can also be displayed in a table layout:

 <img src="workitemlink_03.png" />

Use the part `workitemlink_outbound_table` :

```xml
<item name="payment.ref"
      type="custom"
      path="workitemlink_outbound_table"
      options="($workflowgroup:Payment) "
      label="Payments" />
```

Or to display inbound linked workitems use `workitemlink_inbound_table`:

```xml
<item name="payment.ref"
      type="custom"
      path="workitemlink_inbound_table"
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

## Data Groups & Split Workitems

With the sub project [Data Groups](https://github.com/imixs/imixs-data/tree/main/imixs-data-groups) you can create linked workitems by a Adapter Class

Also the [Split and Join Plugin](https://www.imixs.org/doc/engine/plugins/splitandjoinplugin.html) provides a way to generate outgoing workitem links.
