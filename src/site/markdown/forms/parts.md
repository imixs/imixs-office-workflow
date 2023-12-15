# Form Input Parts

Within a Imixs-Office-Workflow [Form Definition](index.html) you can define different type of input parts within a `imixs-form-section`:

```xml
<imixs-form-section label="Controlling">
    <item name="name" type="text" label="Name" />
    <item name="description" type="textarea" label="Short Description" />
</imixs-form-section>
```


## Input Parts

The various input item definitions are called 'input parts':

### Text Input

```xml
    <item name="description" type="text"
            label="Topic" />
```

### Textlist

A textlist is displayed as a input textarea. The entries of separate lines are stored as multiple values in an Item List. This input type can be useful e.g. for lists of E-Mail addresses or a list of order-numbers.

```xml
    <item name="references" type="textlist"
            label="Order References" />
```

### Textarea Input

```xml
    <item name="description" type="textarea"
            label="Description" />
```

### HTML/RichText Input

```xml
    <item name="description" type="html"
            label="Description" />
```

### Date Input

```xml
    <item name="invoice.date" type="date"
            label="Date" />
```

### Currency Input

```xml
    <item name="invoice.amount" type="currency"
            label="Amount" />
```




### IBAN / BIC  Input

```xml
    <item name="dbtr.iban" type="iban" 
            label="IBAN" />
    <item name="dbtr.bic" type="bic" 
            label="BIC" />
```

Supports a IBAN/BIC Input validation. 

### Select Boxes

You can also create different type of select boxes with predefined values:

```xml
     <item name="invoice.currency" type="selectOneMenu"
    	label="Currency:"
    	options="EUR;CHF;SEK;NOK;GBP;USD" />
```

You can choose one of the following types for select boxes:

- _selectOneMenu_ - a dropdown menu
- _selectBooleanCheckbox_ - a single checkbox
- _selectManyCheckbox_ - a list of checkboxes (layout=line direction)
- _selectOneRadio_ - radio buttons (layout=line direction)

_selectManyCheckbox_ and _selectOneRadio_ are displayed in line direction per default. If you want to display them in page direction use:

- _selectManyCheckboxPageDirection_ - a list of checkboxes (layout=page direction)
- _selectOneRadioPageDirection_ - radio buttons (layout=page direction)

You can also add a mapping of the name displayed in the select box and an optional value by using the '|' char:

```xml
    <item name"myfield" type="selectOneMenu" required="true" label="Your Choice"
      options="management.it|Option A;management.backoffice|Option B" />
```


