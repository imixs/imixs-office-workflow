# Migration

The following section contains migration notes for Imixs-Office-Workflow. 


## Imixs-Office-Workflow 3.2.1

* Update  properties _lucence.indexFieldListAnalyze_ - txtusername - moved from  properties _lucence.indexFieldListNoAnalyze_

* Update Fulltextindex for type=profile





## General Migration Guide for Imixs-Workflow Version 4.x

This is a migration guide to migrate a existing Imixs-Office-Workflow instance from Imixs-Workflow Version 3.x to 4.x


 1. Undeploy Application (with Imixs-Office-Workflow with Imixs-Workflow version 3.x)

 2. Update imixs.properties equal to the current master release of Imixs-Office-Workflow (See extend properties _lucence.indexFieldListAnalyze_ and _lucence.indexFieldListNoAnalyse_)
 
 3a. update lucene version to 6.6.0 
     remove lucene adapter project
     
 3b. java compiler plugin version 1.7
 
 3c. change ejb-jar.xml - class 'org.imixs.workflow.engine.WorkflowService', 
     remove PropertyService and LucenService Refs
	 change interceptor Binding tu DocumentService
	 
 3d. change persistenc.xml - persistence-unit=org.imixs.workflow.jpa	 	 
	 
 3e. Update web module resource bundle app with entry: 
      workflowversion_system=system-de-1.0.0	 
	 
 4. Restart Server and deploy new Application (with Imixs-Workflow 4.x) 
  
 5. Start Migration Job from Imixs-Admin Interface
 
 6. Upload new Models
 
 7. Call SystemCheck form Web UI 
 
To upload a model file manually via rest api:

	curl --user admin:adminadmin --request POST -Tsystem-de.bpmn http://localhost:8080/office-rest/model/bpmn






# Gregorian Calendar Bug

Search for entity type 'Kernprozess' and remove
 