# Wildfly Docker Image

This direcotry contains configuration files prepared to run build a Docker image to run Imixs-Office-Workflow in a the official jboss/wildfly Docker Container version 20.0.1.Final.

The directory provides the following files:

 - standalone.xml - default configuration for Imixs-Office-Workflow
 - standalone-origin.xml origin standalone.xml form Wildfly 20
 - imixsrealm.properties - role mapping for security realml
 - modules  - eclipselink and postgres libraries
 
The docker file creates a Wildfly container with PostgresSQL jdbc driver and eclipse link installed. To build the docker image run:

	$ mvn clean install -Pdocker
	

## Customize Configuration

The standalone.xml file accepts optional environment variables to configure the setup:

 - POSTGRES_CONNECTION - JDBC Connect URL for Postgres DB
 - POSTGRES_USER - postgres database user
 - POSTGRES_PASSWORD - postgres password 
 - MAILGATEWAY - mail gateway host
 
 
The file 'standalne-wildfly20.xml' contains the origin standalone configuration of Wildlfly 20. You can use this file as a template for your individual configuraiton.
 
## Custom JAVA_OPTS

The JVM Options used to start Wildfly can be customized by the environmetn variable JAVA_OPTS. For example you can disable the nashorn warning:

	JAVA_OPTS: "$JAVA_OPTS -Dnashorn.args=--no-deprecation-warning"
	
Using the $JAVA_OPTS variable you can extend the existing setup which is the recommended way to use JAVA_OPTS.
	
 
## Debug Mode

We provide a docker image to run Wildfly in Debug mode. This is usefull durign development. To build the corresponding image run:

	$ mvn clean install -Pdocker-wildfly-debug


The container will than accept debug remote connections on Port 8787.

**Note:** You need to rebuild the image in standard mode befor you go into production!
	 
 
## Kubernetes 