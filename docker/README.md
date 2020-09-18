# Imixs-Office-Workflow Docker Image

Imixs-Office-Workflow provides an official Docker Image to run the application in a container based environment. The Docker image is based on Wildfly application server. 

	/imixs/imixs-office-workflow

The configuration directory contains files prepared to build a Docker based image on the official jboss/wildfly Docker Container version 20.0.1.Final. The directory provides the following files:

 - standalone.xml - default configuration for Imixs-Office-Workflow
 - standalone-origin.xml origin standalone.xml form Wildfly 20
 - imixsrealm.properties - role mapping for security realml
 - modules  - eclipselink and postgres libraries
 
The docker file creates a Wildfly container with PostgresSQL jdbc driver and eclipse link installed.

## Build

To build the latest Docker container image locally run:

	$ mvn clean install -Pdocker
	
To create the current version image (based on the project version) run:

	$ mvn clean install -Pdocker-build

	
**Debug Mode**

To run Wildfly in Debug mode you can build a custom image. This can be useful during development. To build the corresponding image run the maven profile 'docker-wildfly-debug':

	$ mvn clean install -Pdocker-wildfly-debug

The container will than accept debug remote connections on Port 8787.

**Note:** You need to rebuild the image in standard mode before you go into production!

**Docker Repo**

To push the current version image (based on the project version)  to a private Docker repository run:

	$ mvn clean install -Pdocker-push

The Docker image will be pushed to the repo defined by the maven property ${org.imixs.docker.registry}. 

**Docker Hub**

To push the current version image (based on the project version)  to the official docker hub repository run:

	$ mvn clean install -Pdocker-hub

To push the latest Docker version image to the official docker hub repository run:

	$ mvn clean install -Pdocker-hub-latest


## Customize Configuration

The standalone.xml file accepts optional environment variables to configure the setup:

 - POSTGRES_CONNECTION - JDBC Connect URL for Postgres DB
 - POSTGRES_USER - postgres database user
 - POSTGRES_PASSWORD - postgres password 
 - MAILGATEWAY - mail gateway host
 
 
**Custom JAVA_OPTS**

The JVM Options used to start Wildfly can be customized by the environmetn variable JAVA_OPTS. For example you can disable the nashorn warning:

	JAVA_OPTS: "$JAVA_OPTS -Dnashorn.args=--no-deprecation-warning"
	
Using the $JAVA_OPTS variable you can extend the existing setup which is the recommended way to use JAVA_OPTS.

## Customize Docker Images
	
You can customize the Docker image in various ways and run Imixs-Office-Workflow on different kind of application servers. 

**Wildfly**

For individual builds of the wildfly Docker image and additional configuration see the section [Wildfly](./configuration/wildfly/README.md).

**Payara**

To build a Docker image based on Payara micro see the section [Payara Micro](./configuration/payara/README.md).
	
**Open Liberty**

To build a Docker image based on Payara micro see the section [OpenLiberty](./configuration/openlibertry/README.md).
 
## Build Your Own Image

Based on the official Imixs-Office-Workflow Docker image you can build your own custom image with a customized build of Imixs-Office-Workflow. 

	FROM imixs/imixs-office-workflow
	
	# Deploy artefact
	RUN rm -r /opt/jboss/wildfly/standalone/deployments/*
	COPY ./*-app/target/*-app-*.war /opt/jboss/wildfly/standalone/deployments/

	
	
