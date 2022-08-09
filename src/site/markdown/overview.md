*Imixs-Documents* provides a **Open Source Document Management Suite** for small, medium and large enterprises.
The Project is based on the Workflow Suite [Imixs-Office-Workflow](https://github.com/imixs/imixs-office-workflow/)
and is licensed under the GPL.  

The goal of the project is to provide a powerful and easy-to-use *Business Process* and *Document Management* suite for companies and organizations.
With the help of '[Imixs-BPMN](https://www.imixs.org/sub_modeler.html)', business processes can be designed within the BPMN 2.0 standard and easily adapted to the individually needs of an enterprise.

 - [Quick-Installation guide](./install)
 - [Workflow Models](#workflow-models)
 - [Input Forms](#input-forms)

## Quick-Installation Guide

Imixs-Office-Workflow provides a Docker Container to run the service on any Docker host. With the [Quick-Installation guide](./install) you can setup an instance of Imixs-Office-Workflow in minutes.

## Workflow Models

*Imixs-Documents* provides a selection of standard workflow models that can be used for a quick start.
The workflow modls are provided in different laguages. Switch into your prefered language for futher details.

 - [German Workflow Models](https://github.com/imixs/imixs-documents/tree/master/workflow/de)
 - [English international workflow models](https://github.com/imixs/imixs-documents/tree/master/workflow/en)

All standard models have included a multi-level approval workflow. The approval is determined by team management at the process and space levels. 
To add a management approval just add a manager into the corresponding process manager section.


## Input Forms

With Imixs-Office-Workflow you can create individual forms completely based on your BPMN 2.0 model. No programming knowledge in HTML 
or Java Script is required.

<img src="./images/bpmn-02.png" />

Read the section [Custom-Forms](./modeling/CUSTOM_FORMS.md) to learn how you can create your own forms.

### Input Fields & Item Names

It is recommended to use a naming concept when defining your custom forms. For that reason *Imixs-Documents* defines a set of standard item names to be used for different business objects. This naming convention makes it more easy to group related items. 

The following sections list the business items predefined by *Imixs-Documents*.
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
|**Payment**      |        	|                                                               |
|payment.type 	  | text   	| credit card, SEPA												|
|payment.date 	  | date   	| payment date													|
|payment.total 	  | float   | payment amount												|
|payment.cycle 	  | text  	| payment cycle (monthly, yearly, fixed date					|



