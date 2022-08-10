# How to Get Started

*Imixs-Office-Workflow* is a **Open Source Business Process Management Suite** that enables you to digitize your business processes in a fast and easy way. You can either start with a production ready application setup following our [Quick Start Guide](./quickstart.html), or you can build your own business process management platform by developing a custom build. A custom build gives you more flexibility in design and allows you to integrate Imixs-Office-Workflow with more complex enterprise business workflows. 

<img class="screenshot" src="office-workflow-screen-201-1024x549.png" />

Of course your are not alone. You can take a look into the [discussion forum](https://github.com/imixs/imixs-workflow/discussions) and ask the community for help, or you can get professional support from our team using the [contact form](https://www.office-workflow.com/contact/).

In the following sections you will learn some core functionality of Imixs-Office-Workflow and how to get started. 

## The Goal

The goal of the project is to provide a powerful and easy-to-use *Business Process* and *Document Management* suite for companies and organizations.
With the help of '[Imixs-BPMN](https://www.imixs.org/sub_modeler.html)', business processes can be designed within the BPMN 2.0 standard and easily adapted to the individually needs of an enterprise. This means there is no need to write code. Most of the functionality of a business process can be designed in a BPMN 2.0 workflow model. This includes defining business rules, manage access rights to your business data, defining input forms or send out notifications via E-Mail. 

 - [Quick Install Guide](#Quick_Install_Guide)
 - [Workflow Models](#Workflow_Models)
 - [Forms](#Forms)

## Quick Install Guide

Imixs-Office-Workflow provides a Docker Container to run the service on any Docker host. There are various predefined container setups and workflow models which can help to get started easily. The only thing you need is a Docker environment to run Imixs-Office-Workflow in Docker. Of course you can also run Imixs-Office-Worklfow in any Kubernetes platform. 

Read the [Quick-Installation guide](./install) to learn how you can setup an instance of Imixs-Office-Workflow in minutes.

## Workflow Models

*Imixs-Documents* provides a selection of standard workflow models that can be used for a quick start.
The workflow modls are provided in different laguages. Switch into your prefered language for futher details.

 - [German Workflow Models](https://github.com/imixs/imixs-documents/tree/master/workflow/de)
 - [English international workflow models](https://github.com/imixs/imixs-documents/tree/master/workflow/en)

All standard models have included a multi-level approval workflow. The approval is determined by team management at the process and space levels. 

<img class="screenshot" src="modeler-workspace-001-768x432.png" />

To add a management approval just add a manager into the corresponding process manager section.


## Forms

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



