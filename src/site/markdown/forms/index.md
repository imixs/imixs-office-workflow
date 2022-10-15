# Forms

With Imixs-Office-Workflow you can create forms completely model-based. No programming knowledge in HTML 
or Java Script is required.
The Imixs-Office-Workflow Form Component enables you to design your forms at runtime. This is also named a 'Custom Form'.  The definition of a custom form is done within the Imixs-BPMN modeler. First you need ot define a "Data Object Element" with can than be associated with on or many task elements:

<img class="screenshot" src="imixs-bpmn-custom-forms.png" /> 

To activate this feature you leaf the section "*Application -> Input Form*" of your task element empty or add the form name `form_panel#custom` :

<img class="screenshot" src="custom_forms_01.png" /> 




## Form Layout

A form definition splits up into a layout section and the input field definitions. The layout is divided into sections with a label an one or multiple columns:


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
	
  
## Form Sections

A custom form is separated by sections. A section can have an optional label and up to 3 columns:



	 ....
	 <imixs-form-section label="Controlling" columns="2">
	 	.....
	 </imixs-form-section>
	 ....

Within a `imixs-form-section` you can define input fields mapping to a item in a process instance. 
  
There are various input elements defined which can be used. See the section [form input parts](parts.html) for more details.



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




## Read More

 * [Form Input Parts](parts.html)
 * [Custom Input Parts](parts-custom.html)
 * [Country Codes](countrycodes.html)
 * [Input Validation](validation.html)
 