# Imixs-Office-Workflow

'imixs-offcie-workflow' provides a open source workflow suite for small, medium and large enterprises.
The Project is build on top of the '[Imixs-Marty](https://github.com/imixs/imixs-marty)' project and is licenced under the GPL.  

The goal of this project is to provide a powerfull and simple to use business process management suite. The project can be extended with custom plugins and modules. 

You can find further information on the web site [Imixs-Office-Workflow](http://www.office-workflow.de).

* **Technical information** can be found on the [marty project home](http://www.imixs.org/marty).

* **Migration notes** can be found [here](MIGRATION-NOTES.md)


## Build
Imixs-Office-Workflow is based on maven and runs on the Java EE stack. Imixs-Office-Workflow can be deployed on JBoss/Wildfly server or other Java EE application servers.
To build the Java EE artifact run the maven install command first:

	mvn clean install

To deploy the artifact the application server must provide a database pool named "java:/jdbc/office" and a security domain/realm named 'office'. See also the [Imixs-Workflow Deployment Guide](http://www.imixs.org/doc/deployment/index.html).

As an alternative to setup a server environment by yourself you can use docker to run Imixs-Office-Workflow locally or in production. 


<br /><br /><img src="small_h-trans.png" />


Imixs-Office-Workflow provides a Docker Container to run the service on any Docker host. 
The docker image is based on the docker image [imixs/wildfly](https://hub.docker.com/r/imixs/wildfly/).

## Docker for Development
Developers can use a docker image for testing and the development of new features. To build a new container first build the maven artefact running: 

	mvn clean install

Next the docker container can be build with the docker command:

	docker build --tag=imixs/imixs-office-workflow .

To start Imixs-Office-Workflow with docker the docker-compose command can be used:

	docker-compose up

Note: this command will start two containers, a postgreSQL server and a Wildfly Server. See the docker-compose.yml file for the configuration details.

### Mount Points
The default configuration sets a local mount point at the following location:

	~/git/imixs-office-workflow/src/docker/.deployments

Make sure that this directory exits. During development new versions can easily deployed into this directory which is the auto-deployment folder of Wildfly. For further details see the [imixs/wildfly docker image](https://hub.docker.com/r/imixs/wildfly/).


## Docker for Production

To run Imixs-Office-Workflow in a Docker production environment the project proveds serveral additional maven profiles:


### docker-build

With the profile '_docker-build_' a docker container based on the current version of Imixs-Office-Workflow is created locally
 
	mvn clean install -Pdocker-build


### docker-push

With the '_docker-push_' profile the current version of Imixs-Office-Workflow can be pushed to a remote repository:

	mvn clean install -Pdocker-push -Dmyorg.imixs.docker.registry=localhost:5000

where 'localhost:5000' need to be replaced with the host of a private registry. See the [docker-push command](https://docs.docker.com/docker-cloud/builds/push-images/) for more details.

### docker-hub

Imixs-Office-Workflow is also available on [Docker-Hub](https://hub.docker.com/r/imixs/imixs-office-workflow/tags/). The public docker images can be used for development and production. If you need technical support please conntact [imixs.com](http://www.imixs.com) 

