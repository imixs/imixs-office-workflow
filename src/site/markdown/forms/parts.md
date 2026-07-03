# Form Input Parts

Within a [Form Definition](index.html) you can define different type of input parts within a `imixs-form-section`:

```xml
<imixs-form-section label="Controlling">
    <item name="name" type="text" label="Name" />
    <item name="description" type="textarea" label="Short Description" />
</imixs-form-section>
```

Each section can has a label and can be displayed in edit mode (default) or in readonly mode (readonly="true"). If the readonly mode is `true` all input parts defined in this section will be displayed in readonly mode expect the input part overwrites the edit mode.

You can place various input parts within a form-section:

## Text Input

A simple text input field.

```xml
    <item name="description" type="text" label="Topic" />
```

<img class="screenshot" src="item-text.png" />

## Textlist

A textlist is displayed as a input textarea. The entries of separate lines are stored as multiple values in an Item List. This input type can be useful e.g. for lists of E-Mail addresses or a list of order-numbers.

```xml
    <item name="references" type="textlist" label="Order References" />
```

<img class="screenshot" src="item-textlist.png" />

## Textarea Input

A text area for longer text with multiple lines.

```xml
    <item name="description" type="textarea" options="height: 15em;" label="Description" />
```

<img class="screenshot" src="item-textarea.png" />

The parameter 'options' is optional and can be used for css styles only.

## HTML/RichText Input

A HTHML WYSIWYG Editor

```xml
    <item name="description" type="html" label="Description" />
```

## Date Input

Date input part. The date format is aligned to the users locale.

```xml
    <item name="invoice.date" type="date" label="Date" />
```

<img class="screenshot" src="item-date.png" />

## HTML5 Date Input

The default HTML 5 Date input provided by the web browser

```xml
    <item name="invoice.date" type="html5date" label="Date" />
```

<img class="screenshot" src="item-html5date.png" />

## HTML5 Date Time Input

The default HTML 5 Date/Time input provided by the web browser

```xml
    <item name="invoice.date" type="html5datetime" label="Date/Time" />
```

<img class="screenshot" src="item-html5datetime.png" />

## Currency Input

A currency input to enter monetary values.

```xml
    <item name="invoice.amount" type="currency" label="Amount" />
```

<img class="screenshot" src="item-currency.png" />

## IBAN / BIC Input

IBAN / BIC input with automatic validation.

```xml
    <item name="dbtr.iban" type="iban" label="IBAN" />
    <item name="dbtr.bic" type="bic"  label="BIC" />
```

<img class="screenshot" src="item-ibanbic.png" />

Supports a IBAN/BIC Input validation.

## Select Boxes

You can also create different type of select boxes with predefined values:

```xml
     <item name="invoice.currency" type="selectOneMenu"
    	label="Currency:"
    	options="EUR;CHF;SEK;NOK;GBP;USD" />
```

<img class="screenshot" src="item-select.png" />

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

## Markup

With the Markup Editor you can provide a WYSIWYG Editor for Markup Text

```xml
  <item name="request.response.text" type="custom" path="markdowneditor" />
```

<img class="screenshot" src="item-markdown.png" />
