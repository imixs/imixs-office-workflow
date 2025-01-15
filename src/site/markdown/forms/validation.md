# Form Validation

There are several ways how you can validate the user input before the form is processed by the Imixs Workflow Engine.
The following section provides an overview of the various possibilities.

## Required Inputs

With the tag `required` a mandatory input is defined:

    <item name="order.name" type="text"  label="Description:" required="true"/>

The input field is marked with a '\*' and an error message is shown if no field value was added on submit.
<img class="screenshot" src="validation-01.png" />

### Disabling Validation of Required Inputs

You can also disable the required validation for specific events like a 'Cancel' event by adding the following validation defintion into the correspondign event result:

    <validation name="required">false</validation>

<img class="screenshot" src="validation-04.png" />

### Confirm a Event Action

To force the user to confirm a event action you can optional add the Validation-Confirm Rule:

    <validation name="confirm">Are you sure to cancel this request?</validation>

## Validation Rules

The required tag is a simple way to define a validation. With the help of the [Imixs Rule Plugin](https://www.imixs.org/doc/engine/plugins/ruleplugin.html) you can also define more complex business rules. For example you can verify if an input value has a minimum length:

```
	var result={};
	if (workitem.getItemValueString('order.number').length<5) {
	    result.isValid=false;
	    result.errorMessage='The order number must have at least 5 digits!';
	}
```

You can place this rule in the Rule Property of the corresponding workflow Event:

<img class="screenshot" src="validation-03.png" />

The error text is shown if the validation failed:

<img class="screenshot" src="validation-02.png" />

Or testing a number size:

```
	var result={};
	if (workitem.getItemValueDouble('order.amount')<1.0) {
	    result.isValid=false;
	    result.errorMessage='The order amount must be more than 1 EUR!';
	}
```

See the section [Imixs Rule Plugin](https://www.imixs.org/doc/engine/plugins/ruleplugin.html) of the Imixs Workflow Engine for more details about using business rules.

### Validation of Comments

In most cases you want to force the user to enter a comment if she reject an approval. This can be done with the following business rule:

    var result={};
    result.isValid=true;
    if (workitem.getItemValueString('txtcomment') == '') {
        result.isValid=false;
        result.errorMessage='Please enter a comment.';
    }

### Validation of File Attachments

Using the [Imixs Rule Plugin](https://www.imixs.org/doc/engine/plugins/ruleplugin.html) it is also possible to validate more complex data. For example you can ask if at least one document was attached to the form:

    var result={};
    if (workitem.getItemValueInteger('$file.count')==0) {
        result.isValid=false;
        result.errorMessage='Please attache the Purchase Order Document!';
    }
