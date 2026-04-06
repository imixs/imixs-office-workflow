# Layout

You can customize the layout of Imixs-office-Workflow is various ways.

## CSS

The layout is defined by teh CSS files located in `layout/css/`. You can add your own custom layout by setting new CSS rules in the file `custom.css`.

### Imixs Containers

The layout concept of marty provides the following general containers:

- imixs-form - For forms
- imixs-view - to display worklists or views
- imixs-portlet - a contaiener placed to separate elements on page

Each of these containers can contain

- imixs-header - a header section. This section typical defines also a h1 or h2 style
- imixs-body - the body section
- imixs-footer - the footer section

Example:

    <div class="imixs-form>
      <div class="imixs-header">
        ....
      </div>
      <div class="imixs-body">
        ....
      </div>
      <div class="imixs-footer">
        ....
      </div>
    </div>
      ...

### Icons

Imixs-Office-Worklfow supports the font library [fontawesome](https://fontawesome.com/). You can use all [free icons](https://fontawesome.com/search?ic=free-collection)

To integrate an icon use the following HTML snippet:

```
<i class="fas fa-cogs"></i>
```

## Resource Bundles

The following resourcebundles are provided:

- messages = general labels
- app = applications specific labels
- custom = custom labels

For example the component _org.imixs.marty.workflow.FormController_ uses the bundles _app_ and _custom_ to compute the form title. All bundle files are located at:

    /src/main/resources/bundle/
