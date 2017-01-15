#Imixs-Office-Workflow

'imixs-offcie-workflow' provides a open source workflow suite for small, medium and large enterprises.
The Project is build on top of the '[Imixs-Marty](https://github.com/imixs/imixs-marty)' project and is licenced under the GPL.  

The goal of this project is to provide a powerfull and simple to use business process management suite. The project can be extended with custom plugins and modules. 

You can find further information on the web site [Imixs-Office-Workflow](http://www.office-workflow.de).

Technical information can be found on the [marty project home](http://www.imixs.org/marty).





<br /><br /><img src="small_h-trans.png" />


Imixs-Office-Workflow provides a Docker Container to be used to run the service on a Docker host. 
The docker image is based on the docker image [imixs/wildfly](https://hub.docker.com/r/imixs/wildfly/).


## 1. Build Imixs-Office-Workflow

Imixs-Office-Workflow is based on maven. To build the Java EE artifact run:

	mvn clean install

## 2. Build the docker image
To build the docker image run

	docker build --tag=imixs/imixs-office-workflow .


## 3. Run 
You can start the Imixs-Office-Workflow docker container with the docker-compose command:

	docker-compose up

Note: this command will start two container, a postgreSQL server and a Wildfly Server. 


# Development

During development the docker container can be used with mounting an external deployments/ folder:

	docker run --name="imixs-office-workflow" -d -p 8080:8080 -p 9990:9990 \
         -e WILDFLY_PASS="admin_password" \
         -v ~/git/imixs-office-workflow/deployments:/opt/wildfly/standalone/deployments/:rw \
         imixs/imixs-office-workflow

## Docker-Compose

Imixs-Office-Workflow needs a postgreSQL database to be run. To start both containers a docker-compose script is provided to simplify the startup. 
The following example shows the docker-compose.yml for imixs-microservice. You can customize this .yml file to your needs:

	postgresoffice:
	  image: postgres:9.6.1
	  environment:
	    POSTGRES_PASSWORD: adminadmin
	    POSTGRES_DB: office
	
	imixsofficeworkflow:
	  image: imixs/imixs-office-workflow
	  environment:
	    WILDFLY_PASS: adminadmin
	  ports:
	    - "8080:8080"
	    - "9990:9990"
	  links: 
	    - postgresoffice:postgres

 
Take care about the link to the postgres container. The host name 'postgres' is needed to be used in the standalone.xml configuration file in wildfly to access the postgres server for the database pool configuration.


For further details see the [imixs/wildfly docker image](https://hub.docker.com/r/imixs/wildfly/).
