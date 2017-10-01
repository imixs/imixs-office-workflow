## Migration Guide

This is a migration guide to migrate a existing Imixs-Office-Workflow instance from version 2.x to 3.x


 1. Undeploy Application (with Imixs-Office-Workflow 2.x)

 2. Shutdown server, remove Fulltextindex from Filesystem, and remove old artifacts
 
 3. Extend FieldListNoAnalyse (txtemail, datdate, datfrom, datto, numsequencenumber, txtUsername)
 
 3a. update lucene version to 6.3.0 
     remove lucene adapter project
     
 3b. java compiler plugin version 1.7
 
 3c. change ejb-jar.xml - class 'org.imixs.workflow.engine.WorkflowService', 
     remove PropertyService and LucenService Refs
	 change interceptor Binding tu DocumentService
	 
 3d. change persistenc.xml - persistence-unit=org.imixs.workflow.jpa	 
	 
	 
 3e. Update web module resource bundle app with entry: 
      workflowversion_system=system-de-1.0.0	 

 3f. Check pom.xm. build instructions if web module	 
	 
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

-----------------------------------

**DocumentService**


	18:30:22,849 FINE  [org.imixs.workflow.engine.lucene.LuceneSearchService] (default task-36) lucene search: pageNumber=0 pageSize=10
	18:30:22,850 FINE  [org.imixs.workflow.engine.lucene.LuceneSearchService] (default task-36) lucene createIndexSearcher...
	18:30:22,850 FINE  [org.imixs.workflow.engine.lucene.LuceneSearchService] (default task-36) lucene createIndexDirectory...
	18:30:22,855 FINE  [org.imixs.workflow.engine.lucene.LuceneSearchService] (default task-36) lucene DefaultOperator: AND
	18:30:22,856 FINE  [org.imixs.workflow.engine.lucene.LuceneSearchService] (default task-36) lucene final searchTerm=(type:"workitem") AND ($uniqueidref:"14c1454c28f-1cb3e6dd" ) AND (txtworkflowgroup:"Angebot" ) AND ($processid:"1700" ) 
	18:30:22,857 FINE  [org.imixs.workflow.engine.lucene.LuceneSearchService] (default task-36) lucene result sorted by score 
	18:30:22,868 FINE  [org.imixs.workflow.engine.lucene.LuceneSearchService] (default task-36) lucene returned 10 documents in 12 ms - total hits=20644
	18:30:22,868 FINE  [org.imixs.workflow.engine.lucene.LuceneSearchService] (default task-36) lucene lookup $uniqueid=15673b96c3e-6be2821
	18:30:22,874 FINE  [org.imixs.workflow.engine.lucene.LuceneSearchService] (default task-36) lucene lookup $uniqueid=15673c2b95c-1d400a23
	18:30:22,882 FINE  [org.imixs.workflow.engine.lucene.LuceneSearchService] (default task-36) lucene lookup $uniqueid=15673c1e7be-243dbb36
	18:30:22,890 FINE  [org.imixs.workflow.engine.lucene.LuceneSearchService] (default task-36) lucene lookup $uniqueid=15673c2dbfe-104f3fc3
	18:30:22,894 FINE  [org.imixs.workflow.engine.lucene.LuceneSearchService] (default task-36) lucene lookup $uniqueid=15673c1522b-447b93
	18:30:22,899 FINE  [org.imixs.workflow.engine.lucene.LuceneSearchService] (default task-36) lucene lookup $uniqueid=15673c510d6-123c072d
	18:30:22,904 FINE  [org.imixs.workflow.engine.lucene.LuceneSearchService] (default task-36) lucene lookup $uniqueid=15673c62b41-2e7ac8c5
	18:30:22,908 FINE  [org.imixs.workflow.engine.lucene.LuceneSearchService] (default task-36) lucene lookup $uniqueid=15673c5e30b-19126e51
	18:30:22,913 FINE  [org.imixs.workflow.engine.lucene.LuceneSearchService] (default task-36) lucene lookup $uniqueid=15673c30993-bb0f9e6
	18:30:22,921 FINE  [org.imixs.workflow.engine.lucene.LuceneSearchService] (default task-36) lucene lookup $uniqueid=15673c4f3b8-158c14a2
	18:30:22,929 FINE  [org.imixs.workflow.engine.lucene.LuceneSearchService] (default task-36) lucene search result computed in 79 ms







# Gregorian Calendar Bug

Search for entity type 'Kernprozess' and remove
 