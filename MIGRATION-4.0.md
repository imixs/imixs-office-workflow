## Migration Guide


 1. Undeploy Application (with Imixs-Workflow 3.x)

 2. Shutdown server, remove Fulltextindex from Filesystem, and remove old artifacts
 
 3. Extend FieldListNoAnalyse (txtemail, datdate, datfrom, datto, numsequencenumber, txtUsername)
 
 3a. update lucene version to 5.5.2 
     remove lucene adapter project
 3b. java compiler plugin version 1.7
 3c. change ejb-jar.xml - class 'org.imixs.workflow.engine.WorkflowService', 
     remove PropertyService and LucenService Refs
	 change interceptor Binding tu DocumentService
 3d. change persistenc.xml - persistence-unit=org.imixs.workflow.jpa	 
	 
 4. Restart Server and deploy new Application (with Imixs-Workflow 4.x) 
  
 5. Start Migration Job from Imixs-Admin Interface
 
 6. Upload new Models
 
 
 
 

After deplyoment and migration, the system model need to be updated.

Delete the old system model file via admin client

Upload the new model file via rest api


	curl --user admin:adminadmin --request POST -Tsystem-de.bpmn http://localhost:8080/office-rest/model/bpmn

Redeploy the application to reset the model store




#Messure results
 
**EntityService**

	22:17:14,173 FINE  [org.imixs.workflow.jee.ejb.EntityService] (default task-26) [EntityService] findAllEntities - Query=SELECT DISTINCT wi FROM Entity AS wi  JOIN wi.textItems as pref  JOIN wi.textItems as groups  JOIN wi.integerItems as processid  WHERE wi.type IN('workitem') AND pref.itemName = '$uniqueidref' AND pref.itemValue IN ('14c1454c28f-1cb3e6dd' ) AND groups.itemName = 'txtworkflowgroup' and groups.itemValue IN ('Angebot' ) AND processid.itemName = '$processid' AND processid.itemValue IN (1700) ORDER BY wi.created desc
	22:17:14,174 FINE  [org.imixs.workflow.jee.ejb.EntityService] (default task-26) [EntityService] findAllEntities - Startpos=0 maxcount=10
	22:17:16,401 FINE  [org.imixs.workflow.jee.ejb.EntityService] (default task-26) [EntityService] findAllEntities - getResultList in 2227 ms
	...
	.....
	22:17:47,754 FINE  [org.imixs.workflow.jee.ejb.EntityService] (default task-34) [EntityService] findAllEntities - Query=SELECT DISTINCT wi FROM Entity AS wi  JOIN wi.textItems as pref  JOIN wi.textItems as groups  JOIN wi.integerItems as processid  WHERE wi.type IN('workitem') AND pref.itemName = '$uniqueidref' AND pref.itemValue IN ('14c1454c28f-1cb3e6dd' ) AND groups.itemName = 'txtworkflowgroup' and groups.itemValue IN ('Angebot' ) AND processid.itemName = '$processid' AND processid.itemValue IN (1700) ORDER BY wi.created desc
	22:17:47,755 FINE  [org.imixs.workflow.jee.ejb.EntityService] (default task-34) [EntityService] findAllEntities - Startpos=0 maxcount=10
	22:17:50,269 FINE  [org.imixs.workflow.jee.ejb.EntityService] (default task-34) [EntityService] findAllEntities - getResultList in 2512 ms

 