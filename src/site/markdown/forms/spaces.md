# Sections

Imixs-Office-Workflow provides a set of custom input parts to assign a workitem to a section (space). 


## The spaceref Input

The item part `spaceref` can be used to select a section from the organization matrix. 

```xml
    <item type="custom"
          path="spaceref"
          label="Department:" />
```

<img class="screenshot" src="spaceref-01.png" />

**Note:** No `name` is required here as a section is always assigned to the item `space.ref`. This is an internal data item to be resolved by the [Team Controller](../teams/teams.html)

## Read Mode

The component can also be displayed in read mode:

```xml
      <item type="custom" 
            path="spaceref"  
            label="Department:" 
            readonly="true" />
```

<img class="screenshot" src="spaceref-02.png" />

### Selection by Process

To allow only the selection of a subset of sections, assigned to the current process, the option `byprocess` can be set:

```xml
    <item type="custom"
          path="spaceref"
          options="byprocess;"
          label="Department:" />
```



### Default Selection

It is also possible to pre-select a default value for this component. In this case the first section where the current user is member of will be assigned.

 Example:


```xml
    <item type="custom"
          path="spaceref"
          options="default-selection;"
          label="Department:" />
```
  
**Note:** If the workitem is assigned to a process (`process.ref`) than sections assigned to this process will be fetched first!
