## Migration Guide

After deplyoment and migration, the system model need to be updated.

Delete the old system model file via admin client

Upload the new model file via rest api


	curl --user admin:adminadmin --request POST -Tsystem-de.bpmn http://localhost:8080/office-rest/model/bpmn

Redeploy the application to reset the model store 