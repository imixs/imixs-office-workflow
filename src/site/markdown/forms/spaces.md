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

**Note:** No `name` is required here, as a section is always bound to the item `space.ref`. This is an internal data item resolved by the [Team Controller](../teams/teams.html)

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

To allow only the selection of a subset of sections, assigned to the current process, the option `byprocess=true;` can be set:

```xml
    <item type="custom"
          path="spaceref"
          options="byprocess=true;"
          label="Department:" />
```

### Selection by Regular Expression

The selection of sections can be restricted in addition by a regular expression with the option `regex=...`


```xml
    <item type="custom"
          path="spaceref"
          options="regex=Production*;"
          label="Department:" />
```

This example will filter all sections starting with the String 'Production'.

**Note:** The `regex` option can be combined with the option `byprocess=true;`.  See the full list of options below.

### Default Selection

It is also possible to pre-select a default value for this component. In this case the first section where the current user is member of will be assigned.
The feature can be activated by the option  `default-selection=[member|manager|team|assist];`. 

Possible member options are:

 - member - Current user must be a member of one of the section roles
 - manager - Current user must be manager of the section 
 - team - Current user must be team member of the section 
 - assist - Current user must be assist of the section 
 

 Example:

```xml
    <item type="custom"
          path="spaceref"
          options="default-selection=team;"
          label="Department:" />
```

This will set the default selection to the first section where the current user is a team member.


**Note:** The default selection also depends on the options `byprocess=true` and `regex=`.  See the full list of options below.


## Options

The following options are supported by this component.


| Option                    | Description                                                                                 |
| ------------------------- | ------------------------------------------------------------------------------------------- |
| byprocess=true            | Selection only shows sections assigned to the current  process                 |
| regex=....;               | Selection only shows sections  matching the given regular expression  (section name)          |
| default-selection=member  | The first section, the current user is member of will be pre-selected.  |
| default-selection=team    | The first section, the current user is team member of will be pre-selected.  |
| default-selection=manager | The first section, the current user is manager of will be pre-selected.   |
| default-selection=assist  | The first section, the current user is assist of will be pre-selected.   |
