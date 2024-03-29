# Forms

With Imixs-Office-Workflow you can create forms completely model-based. No programming knowledge in HTML 
or Java Script is required.
The Imixs-Office-Workflow Form Component enables you to design your forms at runtime. This is also named a 'Custom Form'.  The definition of a custom form is done within the Imixs-BPMN modeler. First you need ot define a "Data Object Element" with can than be associated with on or many task elements:

<img class="screenshot" src="imixs-bpmn-custom-forms.png" /> 

To activate this feature you leaf the section "*Application -> Input Form*" of your task element empty or add the form name `form_panel#custom` :

<img class="screenshot" src="custom_forms_01.png" /> 




## Form Layout

A form definition splits up into `section` a `item` tags. A section is mandatory and used to group items.

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
	
  
### Sections

A section can have an optional label and a default layout of 1 to 6 columns:

	 ....
	 <imixs-form-section label="Controlling" columns="2">
	 	.....
	 </imixs-form-section>
	 ....

If you define columns the items are arranged into separate rows with the defined amount of columns. 

### Items

Within a `imixs-form-section` you can define input fields. Each input field is defined as an `<item>` tags that maps its value to the corresponding  process instance. 

	<item name="customer.name" type="text"  label="Name:" />

The default width of an item is defined by the number of columns of the containing section. In addition each item has the following properties:

 
| Property  | Type 	  | Mandatory | Description												|
|-----------|---------|-----------|---------------------------------------------------------|
| name		| text    | x         | Name of the item                                        |
| type		| text    | x         | Item type (e.g. text, currency, date,...)               |
| label		| text    |           | Optional label for the Input field                      |
| required	| boolean |           | Optional indicates that the input field in mandatory    |
| readonly	| boolean |           | Optional indicates that the input field is read only    |
												

### Custom Layout

The Layout of a form section is divided into a grid with 12 virtual columns. The number of columns a single item takes is defined by the default column count of the section. For example in a 3-Column layout each item spans over 4 grid-columns.

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




## Input Fields & Item Names

Even if you can define the item names of your input fields in your custom form free, it is recommended to use a naming concept. This allows you to reuse code in a more easy way. *Imixs-Office-Workflow* defines already a set of standard item names used for different business objects. This naming convention makes it more easy to group related items and to exchange data with your business process architecture. 

The following sections list the business items predefined by *Imixs-Office-Workflow*.
For application specific item names the ‘dot.Case’ format is recommended. It’s basically a convention that makes it easier to see what properties are related.


 
| Item            | Type   	| Description													|
|-----------------|---------|---------------------------------------------------------------|
|**Order** 	      |      	|                                                               |
|order.name       | text 	| Order name													|
|order.number     | text	| Order number													|
|order.delivery   | date	| Delivery date													|
|**Contract** 	  |      	|                                                               |
|contract.name    | text 	| Contract name													|
|contract.partner | text 	| Contract partner name											|
|contract.number  | text	| Contract number												|
|contract.start   | date	| Contract start date											|
|contract.end     | date 	| Contract end date												|
|contract.fee     | float 	| Contract fee per billing cycle								|
|**Creditor**     |        	|                                                               |
|cdtr.name        | text  	| Creditor name													|
|cdtr.iban        | text  	| IBAN number													|
|cdtr.bic         | text  	| BIC number													|
|**Debitor**  	  |        	|                                                               |
|dbtr.name        | text  	| debitor name													|
|dbtr.iban        | text  	| IBAN number													|
|dbtr.bic         | text  	| BIC number													|
|**Invoice**      |     	|                                                               |
|invoice.number   | text   	| Invoice number												|
|invoice.date     | date  	| Invoice Date													|
|invoice.total    | float  	| Invoice total amount											|
|invoice.vat      | float  	| Invoice vat 													|
|invoice.gross    | float  	| Invoice gross amount 											|
|invoice.currency | text    | currency											|
|**Payment**      |        	|                                                               |
|payment.type 	  | text   	| credit card, SEPA												|
|payment.date 	  | date   	| payment date													|
|payment.amount	  | float   | payment amount												|
|payment.currency | text    | currency											|
|payment.cycle 	  | text  	| payment cycle (monthly, yearly, fixed date)					|



There are various input elements defined which can be used. See the section [form input parts](parts.html) for more details.



## Read More

 * [Form Input Parts](parts.html)
 * [Custom Input Parts](parts-custom.html)
 * [Country Codes](countrycodes.html)
 * [Input Validation](validation.html)
 