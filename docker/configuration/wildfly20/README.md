# Wildfly

This directory contains configuration files prepared to run build a Docker based on the official jboss/wildfly Docker Container version 20.0.1.Final. The directory provides the following files:

 - standalone.xml - default configuration for Imixs-Office-Workflow
 - standalone-origin.xml origin standalone.xml form Wildfly 20
 - imixsrealm.properties - role mapping for security realml
 - modules  - eclipselink and postgres libraries
 
The docker file creates a Wildfly container with PostgresSQL jdbc driver and eclipse link installed.

## Build

To build the latest Docker container image locally run:

	$ mvn clean install -Pdocker
	
**Debug Mode**

To run Wildfly in Debug mode you can build a custom image. This can be useful during development. To build the corresponding image run the maven profile 'docker-wildfly-debug':

	$ mvn clean install -Pdocker-wildfly-debug

The container will than accept debug remote connections on Port 8787.

## Customize Configuration

The standalone.xml file accepts optional environment variables to configure the setup:

 - POSTGRES_CONNECTION - JDBC Connect URL for Postgres DB
 - POSTGRES_USER - postgres database user
 - POSTGRES_PASSWORD - postgres password 
 - MAILGATEWAY - mail gateway host
 
For further customization you can edit the standalone.xml file for your individual needs. The file 'standalone-wildfly20.xml' contains the origin standalone configuration of Wildlfly 20. You can use this file as a template for an individual setup of Wildfly.
 
## Custom JAVA_OPTS

The JVM Options used to start Wildfly can be customized by the environmetn variable JAVA_OPTS. For example you can disable the nashorn warning:

	JAVA_OPTS: "$JAVA_OPTS -Dnashorn.args=--no-deprecation-warning"
	
Using the $JAVA_OPTS variable you can extend the existing setup which is the recommended way to use JAVA_OPTS.
	
	
