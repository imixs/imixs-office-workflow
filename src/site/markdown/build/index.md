# How to create a custom build from Imixs Office Workflow

There a different ways how you can build and customize Imixs Office Workfow.  One possibility is to check out the Imixs Office Project source and build it from source.  After that you can customize each element of your build. 

## Build from Source

This way is typical used if you plan to test, customize and contribute you changes 
 directly to this open source project. The disadvantage of this strategy is that you run out 
 of sync and create a lot of merge conflicts if you work on the trunk version of Imixs Office 
 Workflow.

## Create a custom build from the Imixs Office Archetype

The recommended way to create a custom build from Imixs Office Workflow is using the 
 imixs-office-archetype. In this scenario you create you own custom build based on the 
 latest version of Imixs Office Workflow. You can change the behaviour and layout of your 
 custom build without conflicting with any updates made by the Imixs Marty project. Also 
 you can easily upgrade to any new version of imixs worklow or imixs marty.



## Creating a custom build using Eclipse

To create a new maven project from an archetype you can use the Eclipse IDE with the 
 Maven Plugin (m2e). This Eclipse plugin provides an easy wizard to create a new maven 
 project based on an archetype.

 * 1.) From the main menue choose 'File -> New -> other'

 * 2.) Select 'Maven -> Maven Project' and click next

 * 3.) Leave the default creation setup and click next again

 * 4.) Now search for the Imixs archetype by entering 'com.imixs.workflow'. Eclipse will search the repository for the latest archetype

<img src="build/maven001.png" />

 * 5.) Click next to setup your project, choose a groupid and artefact id

 * 6.) click finish to create the project

## Create a custom build using maven comandline tool

These are the steps to create you own new custom build using the maven command line:

	mvn archetype:generate -Dfilter=imixs-office



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
 
   
 
 