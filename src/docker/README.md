#Docker

Imixs-Office-Workflow is provided as a Docker image. You can use the imixs/office-workflow image also for development or custom builds. 
For this purpose you can mount custom standalone.xml configuration and the /deploymetns/ folder as external volumes

This is an example to run imixs/office-workflow with external volumes:

     docker run --name="office-workflow" -d -p 8080:8080 -p 9990:9990 \
             -e WILDFLY_PASS="adminadmin" \
             --link office-postgres:postgres \
             -v /path/to/deployments:/opt/wildfly/standalone/deployments/ \
             imixs/office-workflow

To provide also configuration

     docker run --name="office-workflow" -d -p 8080:8080 -p 9990:9990 \
             -e WILDFLY_PASS="adminadmin" \
             --link office-postgres:postgres \
             -v ~/git/imis-office-workflow/src/path/to/deployments:/opt/wildfly/standalone/deployments/ \
             -v /path/to/configuration:/opt/wildfly/standalone/configuration/ \
             imixs/office-workflow

             
The imixs/office-workflow container need to be linked to a running postgreSQL database container. To start the postgreSQL container with the database 'office' run the following commmand:

	docker run --name office-postgres -e POSTGRES_DB=office -e POSTGRES_PASSWORD=adminadmin postgres:9.5.2


To stop the container run: 

	docker stop office-postgres
	docker stop office-workflow