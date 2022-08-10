# How to Get Started

*Imixs-Office-Workflow* is a **Open Source Business Process Management Suite** that enables you to digitize your business processes in a fast and easy way. You can either start with a production ready application setup following our [Quick Start Guide](./quickstart.html), or you can build your own business process management platform by developing a custom build. A custom build gives you more flexibility in design and allows you to integrate into more complex enterprise business workflows. 

<img class="screenshot" src="office-workflow-screen-201-1024x549.png" />

Of course your are not alone. You can take a look into the [discussion forum](https://github.com/imixs/imixs-workflow/discussions) and ask the community for help, or you can get professional support from our team using the [contact form](https://www.office-workflow.com/contact/).

In the following sections you will learn some core functionality of Imixs-Office-Workflow and how to get started. 

## The Goal

The goal of the project is to provide you with aa powerful and easy-to-use *Business Process Management Suite*.
With the help of '[Imixs-BPMN](https://www.imixs.org/sub_modeler.html)', business processes can be designed within the BPMN 2.0 standard and easily adapted to the individually needs of an enterprise. This means there is no need to write code. Most of the functionality of a business process can be designed in a BPMN 2.0 workflow model. This includes defining business rules, manage access rights to your business data, defining input forms or send out notifications via E-Mail. 

In the following you will get a short overview about some core functionality:

 - [Quick Install Guide](#Quick_Install_Guide)
 - [Workflow Models](#Workflow_Models)
 - [Forms](#Forms)

## Quick Install Guide

Imixs-Office-Workflow provides a Docker Container to run this application on any Docker host. There are various predefined container setups available and also workflow models which will help your to get started. The only thing you need is a Docker environment to run Imixs-Office-Workflow with Docker. Of course you can also run Imixs-Office-Worklfow in any Kubernetes platform. Read the [Quick-Installation guide](./quickstart.html) to learn how you can setup an instance of Imixs-Office-Workflow in minutes. 

In the section [Custom Build](./build/index.html) you will learn how to create a custom build from Imixs Office Workflow which allows you to adapt and extend the platform in various ways. And in the [Integration Guide](./build/integration.html) you will learn how to integrate from an external System or an Microservice Architecture. 

## Workflow Models

All business workflows can be designed in Imixs-Office-Workflow using the BPMN 2.0 Standard. *Imixs-Office-Workflow* also provides a selection of predefined standard workflow models that can be used for a quick start.
The workflow models are provided in different languages. Switch into your preferred language for further details.

 - [German Workflow Models](https://github.com/imixs/imixs-documents/tree/master/workflow/de)
 - [English international workflow models](https://github.com/imixs/imixs-documents/tree/master/workflow/en)

All standard models are including a multi-level approval workflow. The approval is determined by team management at the process and space levels. 

<img class="screenshot" src="modeler-workspace-001-768x432.png" />

To add a management approval just add a manager into the corresponding process manager section. Learn more about organizing teams in the section [Team Management](./teams/index.html).


## Forms

With Imixs-Office-Workflow you can create individual input forms based on your BPMN 2.0 model. No programming knowledge in HTML, 
Java or Java Script is required.

<img src="./images/bpmn-02.png" />

Read the section [Custom-Forms](./forms/index.html) to learn how you can create your own forms.
