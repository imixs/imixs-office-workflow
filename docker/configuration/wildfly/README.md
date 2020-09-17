# Wildfly Docker Image

This container contains configuration filew prepared to run Imixs-Office-Workflow in a the official jboss/wildfly Docker Container.

 - standalone.xml - default configuration for Imixs-Office-Workflow
 - standalone-origin.xml origin standalone.xml form Wildfly 20
 - imixsrealm.properties - role mapping for security realml
 - modules  - eclipselink and postgres libraries
 
## Customize Configuration

The standalone.xml file accepts optional environment variables to configure the setup:

 - POSTGRES_CONNECTION - JDBC Connect URL for Postgres DB
 - POSTGRES_USER - postgres database user
 - POSTGRES_PASSWORD - postgres password 
 - MAILGATEWAY - mail gateway host
 
 
 
The file 'standalne-wildfly20.xml' contains the origin standalone configuration of Wildlfly 20.
 
 
## Kubernetes 