# Layout

For layout web applications we recommend to use the CSS Framework [imixs-responsive](https://github.com/imixs/imixs-responsive).
This framework is for example used by teh project [Imixs-Office-Workflow](https://github.com/imixs/imixs-office-workflow) for a general site layout. 
Furthermore imixs-marty supports the concept of resource bundles which helps to internationalize labes and text blocks in a JSF application.


## Resoucre Bundles

The following resourcebundles are provided:

* messages = general labels 
* app = applications specific labels 
* custom = custom labels

For example the component _org.imixs.marty.workflow.FormController_ uses the bundles _app_ and _custom_ to compute the form title. All bundle files are located at: 

	/src/main/resources/bundle/ 
 
## CSS Layout
 
Based on the creatix-css framework there a some additional concepts to layout forms and views 
 which will be explained on the following sections.
 
### Imixs Containers

The layout concept of marty provides the following general containers:

 * imixs-form - For forms
 * imixs-view - to display worklists or views
 * imixs-portlet - a contaiener placed to separate elements on page
  

Each of these containers can contain

 * imixs-header - a header section. This section typical defines also a h1 or h2 style
 * imixs-body - the body section
 * imixs-footer - the footer section  

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

 
### Layout Pages and Forms
 
To layout the content of a singel page or form the following containers are defined:
 
 * imixs-form-panel - general section for input fields
 * imixs-form-section - separates a form-panel into sections 

 To layout a input form form in general a 'imixs-form-panel' should be used. This is the
 general container for input elements. To layout separate sections in a form a
 'imxis-form-section' can be used.
 
	<div class="imixs-body"> 
	  <div class="imixs-form-panel">
	     <div class="imixs-form-section">
	     <!-- content -->
	  ...
  
 
HTML Input elements can be placed inside a imixs-form-section.  A imixs-form-section provides general layout for dl/dt/dd elements. 
 
**Note:** you should always place Label/Input pairs in separate DL sections!

You can use imixs-form-section-2 to use a 2-column layout. See the following example

	<div class="imixs-form-section">
		<!-- Contract Form -->
		<dl>
		<dt>
			<h:outputLabel value="#{message['form.subject']}" />
		</dt>
		<dd>
			<h:inputText value="#{workflowController.workitem.item['_subject']}" />
		</dd>
			<dt>
			<h:outputLabel value="#{message['form.customer']}" />
		</dt>
		<dd>
			<h:inputText value="#{workflowController.workitem.item['_customer']}" />
		</dd>
		</dl>
	</div>
	<div class="imixs-form-section-2">
		<dl>
		<dt>
			<h:outputLabel value="#{message['form.project']}" />
		</dt>
		<dd>
			<marty:workitemLink workitem="#{workflowController.workitem}"  filter="$processid:6..." />
		</dd>
		</dl>
		<dl>
		<dt>
			<h:outputLabel value="#{message['form.contact']}" />
		</dt>
		<dd>
			<marty:workitemLink workitem="#{workflowController.workitem}"  filter="$processid:9..." />
		</dd>
		</dl>
	</div>
