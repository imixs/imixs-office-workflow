# Migration

The following section contains migration notes for Imixs-Office-Workflow. 

## Imixs-Office-Workflow 5.0.x	

Wildfily Image has changed! For that reason the volume mapping need to be changed. This is an example for Kubernetes:

```yaml
        volumeMounts:
        - mountPath: /opt/jboss/wildfly/lucene
          name: index
      restartPolicy: Always
      volumes:
      - name: index
        persistentVolumeClaim:
          claimName: index
```

**Note:** This path mapping assumes that the property `imixs-office.IndexDir` points to `lucene/office-workflow-index`





## Imixs-Office-Workflow 4.6.0

 - New Main Layout  - New CSS Class names for header and content container!
 - Upgrade Imixs-Archive API to 2.4.1-SNAPSHOT - This change requires a new Backup-Service Setup in the Archive Deployment too.

## Imixs-Office-Workflow 4.5.1

 - Changed Security Model - core access roles are now mandatory, empty passwords are no longer supported
 
	# Fix Postgres DB Data for empty password of user 'imixs-workflow-service'
	DELETE FROM userid_usergroup WHERE id='imixs-workflow-service';
	DELETE FROM userid WHERE id='imixs-workflow-service';


## Imixs-Office-Workflow 4.5.0

 - changed imixs.properties default entries:
 
	model.default.data=system-de-1.2.0.bpmn
	.....
	.....
	index.fields.store=process.name,txtProcessName,txtWorkflowImageURL


## Imixs-Office-Workflow 4.4.0

 - changed Maven GroupID from com.imixs.workflow => org.imixs.workflow

## Imixs-Office-Workflow 4.3.1

 - Upgrade Imixs-Workflow 5.2.5 => 5.2.6
 - Upgrade Lucene from version 7.5.0 => 7.7.3 - An index update is NOT necessary


## Imixs-Office-Workflow 4.3.0

Docker image changed to the official Wildfly version 20. For that reason the lucene index location has changed:

	#Old
	/home/imixs/
	
	# New
	/opt/jboss/lucene/

For that reason the volume mapping need to be changed. This is an example for Kubernetes:

        volumeMounts:
        - mountPath: /opt/jboss/lucene
          name: lucene
      restartPolicy: Always
      volumes:
      - name: lucene
        persistentVolumeClaim:
          claimName: lucene


**Note:** This path mapping assumes that the property _imixs-office.IndexDir_ points to "lucene/office-workflow-index"



## Imixs-Office-Workflow 4.2.0
	
No migration needed


## Imixs-Office-Workflow 4.1.0

No migration needed


## Imixs-Office-Workflow 4.0.0

Based on Imixs-Workflow 5.0.3

New Imixs.properties:

	model.default.data=system-de-1.1.0.bpmn
	setup.system.model=system-de-1.1


system model version can be removed from resource bundle 'app'


## Imixs-Office-Workflow 3.6.0

* new imixs.property parameter

	lucence.indexFieldListStore=txtProcessName,txtWorkflowImageURL

## Imixs-Office-Workflow 3.2.0

Version 3.2.0 introduces the new [Imixs-Archive](https://github.com/imixs/imixs-archive) concept. A migration of data is not necessary. But some configuration issues:

### 1. DMS Plugin is deprecated and can be removed from custom models.

### 2. New Maven dependencies: 

	<!-- Workflow Versions -->
	<org.imixs.workflow.version>4.2.4</org.imixs.workflow.version>
	<org.imixs.marty.version>3.2.0-SNAPSHOT</org.imixs.marty.version>
	<org.imixs.office.version>3.2.0-SNAPSHOT</org.imixs.office.version>
	<lucene.version>6.6.1</lucene.version>
	<org.imixs.adapters.version>1.5.2-SNAPSHOT</org.imixs.adapters.version>
	
	<!-- Imixs-Archive -->
	<org.imixs.archive.version>0.0.2-SNAPSHOT</org.imixs.archive.version>
	<apache.poi.version>3.17</apache.poi.version>
	<apache.pdfbox.version>2.0.7</apache.pdfbox.version>
	<apache.tika.version>1.16</apache.tika.version>
	.....
		<!-- Imixs-Archive -->
			<dependency>
				<groupId>org.imixs.workflow</groupId>
				<artifactId>imixs-archive-api</artifactId>
				<version>${org.imixs.archive.version}</version>
				<scope>provided</scope>
			</dependency>
			<dependency>
				<groupId>org.imixs.workflow</groupId>
				<artifactId>imixs-archive-ui</artifactId>
				<version>${org.imixs.archive.version}</version>
				<scope>provided</scope>
			</dependency>
			
			<!-- Core Document Parser -->
			<dependency>
				<groupId>org.apache.poi</groupId>
				<artifactId>poi</artifactId>
				<version>${apache.poi.version}</version>
				<scope>provided</scope>
			</dependency>
			<dependency>
				<groupId>org.apache.poi</groupId>
				<artifactId>poi-ooxml</artifactId>
				<version>${apache.poi.version}</version>
				<scope>provided</scope>
			</dependency>
			<dependency>
				<groupId>org.apache.poi</groupId>
				<artifactId>poi-scratchpad</artifactId>
				<version>${apache.poi.version}</version>
				<scope>provided</scope>
			</dependency>
			<dependency>
			    <groupId>org.apache.pdfbox</groupId>
			    <artifactId>pdfbox</artifactId>
			    <version>${apache.pdfbox.version}</version>
				<scope>provided</scope>
			</dependency>
			
		
			<dependency>
				<groupId>org.imixs.workflow</groupId>
				<artifactId>imixs-adapters-documents</artifactId>
				<version>${org.imixs.adapters.version}</version>
				<scope>provided</scope>
			</dependency>
   
### 3. New imixs.properties:

	snapshot.history=1
	snapshot.overwriteFileContent=false 

 Take care about the lucene index field txtUsername
 
	lucence.indexFieldListAnalyze=txtUsername
 
### 4. EAR pom.xml

   new Jar Moduels:
   
	<JarModule>
		<groupId>org.imixs.workflow</groupId>
		<artifactId>imixs-workflow-jax-rs</artifactId>
		<bundleDir>/</bundleDir>
	</JarModule>
	<JarModule>
		<groupId>org.imixs.workflow</groupId>
		<artifactId>imixs-archive-api</artifactId>
		<bundleDir>/</bundleDir>
	</JarModule>
	<JarModule>
		<groupId>org.imixs.workflow</groupId>
		<artifactId>imixs-adapters-documents</artifactId>
		<bundleDir>/</bundleDir>
	</JarModule> 
 

and add dependencies....
   
   
		<dependency>
			<groupId>org.imixs.workflow</groupId>
			<artifactId>imixs-workflow-jax-rs</artifactId>
			<type>jar</type>
		</dependency>
	 <!-- Imixs-Archive -->
		<dependency>
			<groupId>org.imixs.workflow</groupId>
			<artifactId>imixs-archive-api</artifactId>
			 <scope>compile</scope>
		</dependency>
		
		<!-- Core Document Parser -->
		<dependency>
			<groupId>org.apache.poi</groupId>
			<artifactId>poi</artifactId>
			<scope>compile</scope>
		</dependency>
		<dependency>
			<groupId>org.apache.poi</groupId>
			<artifactId>poi-ooxml</artifactId>
			<scope>compile</scope>
		</dependency>
		<dependency>
			<groupId>org.apache.poi</groupId>
			<artifactId>poi-scratchpad</artifactId>
			<scope>compile</scope>
		</dependency>
		<dependency>
		    <groupId>org.apache.pdfbox</groupId>
		    <artifactId>pdfbox</artifactId>
		    <scope>compile</scope>
		</dependency>
	
		
		<!-- Imixs-Adapters -->
		<dependency>
			<groupId>org.imixs.workflow</groupId>
			<artifactId>imixs-adapters-documents</artifactId>
			<type>jar</type>
			<scope>compile</scope>
		</dependency>
 
 also add the imixs-workflow-jax-rs dependencies
 
### 5.) EJB pom.xml  

in the ejb pom.xml, the add dependencies:
 
 
		<dependency>
			<groupId>org.imixs.workflow</groupId>
			<artifactId>imixs-archive-api</artifactId>
		</dependency>
		
		<dependency>
			<groupId>org.imixs.workflow</groupId>
			<artifactId>imixs-adapters-documents</artifactId>
		</dependency> 
 
 add EJB MANIFEST entries:
 
       ...imixs-workflow-jax-rs-${org.imixs.workflow.version}.jar imixs-archive-api-${org.imixs.archive.version}.jar imixs-adapters-documents-${org.imixs.adapters.version}.jar...
 
### 6.) Web Modules

remove the web.xml if possible... (jax-rs servlets no longer needed)
  
## Imixs-Office-Workflow 3.1.1

* Update  properties _lucence.indexFieldListAnalyze_ - txtusername - moved from  properties _lucence.indexFieldListNoAnalyze_

* Update Fulltextindex for type=profile





# General Migration Guide for Imixs-Workflow Version 4.x

This is a migration guide to migrate a existing Imixs-Office-Workflow instance from Imixs-Workflow Version 3.x to 4.x


 1. Undeploy Application (with Imixs-Office-Workflow with Imixs-Workflow version 3.x)

 2. Update imixs.properties equal to the current master release of Imixs-Office-Workflow (See extend properties _lucence.indexFieldListAnalyze_ and _lucence.indexFieldListNoAnalyse_)
 
 3a. update lucene version to 6.6.0 
     remove lucene adapter project
     
 3b. java compiler plugin version 1.7
 
 3c. change ejb-jar.xml - class 'org.imixs.workflow.engine.WorkflowService', 
     remove PropertyService and LucenService Refs
	 change interceptor Binding tu DocumentService
	 
 3d. change persistenc.xml - persistence-unit=org.imixs.workflow.jpa	 	 
	 
 3e. Update web module resource bundle app with entry: 
      workflowversion_system=system-de-1.0.0	 
	 
 4. Restart Server and deploy new Application (with Imixs-Workflow 4.x) 
  
 5. Start Migration Job from Imixs-Admin Interface
 
 6. Upload new Models
 
 7. Call SystemCheck form Web UI 
 
To upload a model file manually via rest api:

	curl --user admin:adminadmin --request POST -Tsystem-de.bpmn http://localhost:8080/office-rest/model/bpmn






# Gregorian Calendar Bug

Search for entity type 'Kernprozess' and remove
 