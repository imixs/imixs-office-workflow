# How to build Imixs-Office-Workflow

Imixs Office Workflow is based on the Imixs Workflow project and the subproject 'marty'. Both projects are open source.
 All artifacts are based on maven so it is quite simple to build the imixs-office-workflow  manually form sources. 

## Build latest devevelopment release
  
To build the latest development release of Imixs-Office-Workflow  follow these steps:
 
### Build the Imixs-Marty modules
First you need to checkout the marty project from GitHub and build the marty components: 
 
	git clone https://github.com/imixs/imixs-marty.git
	cd imixs-marty
	mvn install 
 
### Build Imixs-Office-Workflow

Next you can checkout the Imixs-Office-Workflow project from GitHub and build the EAR file: 
 
	git clone https://github.com/imixs/imixs-office-workflow.git
	cd imixs-office-workflow
	mvn install 
 
After a successful build the WAR file is located under the target/ folder.
  
 Per default the build is created for Wildfly Application Server. You can also build the project for different platforms:
 
	mvn install -Ppayara-micro

## Snapshot Release

The required artifacts from the Imixs Workflow project are hosted by the maven  centrale repository. So there is no need to checkout and build these artifacts manually. If  you are working with the latest snapshot releases please make sure that the sonatype 
 maven snapshot repository is listed in your maven ./m2/settings.xml file:

	....
	<repositories>
	....
	<!-- Sonatype Snapshot repository -->
	<repository>
	<id>sonatype-snaptshots</id>
	<name>Sonatype Snapshot repository</name>
	<url>http://oss.sonatype.org/content/repositories/snapshots </url>
	</repository>
	</repositories>
	....

## Configuration

After the maven build process was successful you can install the war file into your application server. The WAR artefact expects the following configurations:

 * a jndi database pool named 'jdbc/imixs_office' 

 * a jaas security realm named 'imixsrealm' with at least one user assigned to the group 'IMIXS-WORKFLOW-Manager'

 
 A detailed description of the install process on wildfly can be found {{{../install/wildfly.html}here}}.
 
  