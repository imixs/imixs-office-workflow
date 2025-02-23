# Form Layout

Forms defined in Imixs-Office-Workflow can have a flexible layout. A form is divided into `section` tags containing the `item` tags. A section is mandatory and used to group items.

```xml
	<?xml version="1.0"?>
	<imixs-form>
	  <imixs-form-section label="Controlling">
	    <item name="description" type="textarea"
	        label="Short Description" />
	  </imixs-form-section>
	  <imixs-form-section>
	    <item name="details" type="html" label="Description" />
	  </imixs-form-section>
	</imixs-form>
```

### Sections

A section can have an optional label and a default layout of 1 to 12 columns:

```xml
	 ....
	 <imixs-form-section label="Controlling" columns="2">
	 	.....
	 </imixs-form-section>
	 ....
```

If you define columns the items are arranged into separate rows with the defined amount of columns.

### Custom Section Layout

The Layout of a form section is divided into a grid with 12 virtual columns. The number of columns a single item takes is defined by the default column definition of the section. For example in a 3-Column layout each item spans over 4 grid-columns.

You can customize the layout of a section for each item by defining a separate span tag. The span defines the columns a single item takes.

```xml
  <imixs-form-section label="Address:">
    <item name="debug.zip" type="text"  label="ZIP:" span="2" />
    <item name="debug.city" type="text"  label="City:" span="6" />
    <item name="debug.country" type="text"  label="Country:" span="4" />
  </imixs-form-section>
```

In this example the zip item takes only 2 grid columns, the city 6 and the country 4. It is important to ensure that a custom layout is always based on 12 columns:

<img class="screenshot" src="custom-grid-layout.png" />

## Form Tabs

You can also define additional Tabs that group Sections horizontal. For this you define a tag named `imixs-subform` to separate a group of sections

```xml
<imixs-form>
  <imixs-subform label="Basic Information">
	<imixs-form-section label="Address:">
		<item name="debug.zip" type="text"  label="ZIP:" span="2" />
		<item name="debug.city" type="text"  label="City:" span="6" />
		<item name="debug.country" type="text"  label="Country:" span="4" />
	</imixs-form-section>
  </imixs-subform>
  <imixs-subform label="Advanced Information">
	 ...
  </imixs-subform>
</imixs-form>
```

<img class="screenshot" src="custom_forms_02.png" />
