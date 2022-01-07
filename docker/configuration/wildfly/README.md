# Wildfly

This directory contains configuration files prepared to run build a Docker based on the official jboss/wildfly Docker Container. The directory provides the following files:

 - standalone.xml - default configuration for Imixs-Office-Workflow
 - standalone-origin.xml origin standalone.xml form a specific Wildfly Image version
 - modules  - eclipselink and postgres libraries
 
The docker file creates a Wildfly container with PostgresSQL jdbc driver and eclipse link installed.

To build the image run:

	$ mvn clean install -Pdocker
	
To upload the image to hub.docker.com use the corresponding maven profiles.

## Customize standalone.xml

The simplest and cleanest approach to customize the wildfly configuration is to edit the standalone.xml file. This way you have full control over the configuration at any time. The only disadvantage is that you need to update and maintain the file yourself if a new version of WildFly is released.

The following is a short tutorial how we prepare a new configuration file:

1) Start the official wildfly image with a specific version:

	$ docker run -it --name wildfly_setup jboss/wildfly:25.0.0.Final

2) execute a bash in the running wildfly server:

	$ docker exec -it wildfly_setup bash

3) copy the standalone.xml file

	$ docker cp wildfly_setup:/opt/jboss/wildfly/standalone/configuration/standalone.xml ./standalone.xml
	
This will copy the origin standalone configuration file into your current location. Now you can customize the file

### Remove Extentions

You can remove (comment) some unused extentions

        <!-- 
        <extension module="org.jboss.as.webservices"/>
        <extension module="org.wildfly.extension.batch.jberet"/>
        <extension module="org.wildfly.extension.microprofile.opentracing-smallrye"/>
         -->
### Add Eclipselink

activate the Eclipselink module 

    <system-properties>
        <property name="eclipselink.archive.factory" value="org.jipijapa.eclipselink.JBossArchiveFactoryImpl"/>
    </system-properties>
    
### Add DataSource

Add the postgreSQL driver and Imixs-Office-Workflow Datasoruce    

	.....
              	<!-- imixs-workflow datasource -->
                <datasource jta="true" jndi-name="java:/jdbc/office" pool-name="office" enabled="true" use-ccm="true">
                	<connection-url>${env.POSTGRES_CONNECTION}</connection-url>
				    <driver-class>org.postgresql.Driver</driver-class>
				    <driver>postgresql</driver>
				    <security>
				      <user-name>${env.POSTGRES_USER}</user-name>
				      <password>${env.POSTGRES_PASSWORD}</password>
				    </security>
                     <validation>
                        <valid-connection-checker class-name="org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLValidConnectionChecker"/>
                        <!-- any background-validation-millis > 0 will enable background validation. See https://jira.jboss.org/browse/JBAS-4088. -->
                        <background-validation-millis>60000</background-validation-millis>
                        <exception-sorter class-name="org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLExceptionSorter"></exception-sorter>
                    </validation>                
                </datasource>                
                <drivers>
                    <driver name="h2" module="com.h2database.h2">
                        <xa-datasource-class>org.h2.jdbcx.JdbcDataSource</xa-datasource-class>
                    </driver>
                    <driver name="postgresql" module="org.postgresql">
                		<driver-class>org.postgresql.Driver</driver-class>
            		</driver>
                </drivers>
            </datasources>
	.....


### Add Security

Add the security entires for the eltron framework to create the imixsrealm


### Add Mailhost

Finally add the mail-smtp configuration

        <outbound-socket-binding name="mail-smtp">
            <remote-destination host="${env.MAILGATEWAY}" port="25"/>
        </outbound-socket-binding>

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
	
	
