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

During development you can build the docker image manually - run:

	docker build --tag=imixs/imixs-office-workflow .


For further details see the [imixs/wildfly docker image](https://hub.docker.com/r/imixs/wildfly/).
