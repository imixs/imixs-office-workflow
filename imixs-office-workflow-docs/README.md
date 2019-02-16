# Imixs-Office-Documents

Imixs-Office-Documents is an **Open Source DMS Solution** for small, medium and large enterprises.
The Project is build on top of the '[Imixs-Office-Workflow](https://www.office-workflow.com)' project and is licensed under the GPL.  

The goal of the project is to provide a powerful and easy-to-use document management suite for companies and organizations.
With the help of '[Imixs-BPMN](https://www.imixs.org/sub_modeler.html)', business processes can be designed within the BPMN 2.0 standard and easily adapted to the individually needs of an enterprise.

## Open Source
Imixs-Office-Documents is a free and open source technology. Therefore, this solution can be easily customized and extended with custom plugins and modules. 

* **Blog & News** can be found on the [home page](https://www.office-workflow.com).
* **Release notes** can be read [here](https://github.com/imixs/imixs-office-workflow/releases).

## Contribute
The source code of Imixs-Office-Documents is free available on [Github](https://github.com/imixs/imixs-office-workflow). 
If you have any questions, you can ask your question on the [GitHub Issue Tracker](https://github.com/imixs/imixs-office-workflow/issues). 
With a pull request on GitHub you can share your ideas or improvements that you want to contribute.

 

## Need Help?

[Imixs Software Solutions GmbH](http://www.imixs.com) is an open source company and we are specialized in business process management solutions (BPMS). If you need professional services or consulting for your own individual software project [please contact us](mailto:info@imixs.com). 

 


<br /><br /><img src="small_h-trans.png" />


**Imixs-Office-Documents** provides a Docker Image to run the service on any Docker host. 
The docker image is based on the docker image [imixs/wildfly](https://hub.docker.com/r/imixs/wildfly/) which can be used for development as also for production.


## Docker for Development
Developers can use a docker image for testing and the development of new features. To build a new container first build the maven artefact running: 

	$ mvn clean install -Pdocker

To start Imixs-Office-Documents with docker, the docker-compose file 'docker-compose-dev.yml can be used:

	$ docker-compose -f docker-compose-dev.yml up

Note: this command will start several containers, 

* a Postgre SQL database server 
* a Wildfly Server running Imixs-Office-Documents
* and a Wildfly Server funning the [Imixs-Admin tool](https://www.imixs.org/doc/administration.html) 


### Mount Points
The development configuration sets a local mount point at the following location:

	~/git/imixs-office-workflow/imixs-office-workflow-docs/src/docker/deployments

Make sure that this directory exits. During development new versions can easily deployed into this directory which is the auto-deployment folder of Wildfly. For further details see the [imixs/wildfly docker image](https://hub.docker.com/r/imixs/wildfly/).


### docker-hub

Imixs-Office-Documents is also available on [Docker-Hub](https://hub.docker.com/r/imixs/Imixs-Office-Documents/). The public docker images can be used for development and production. If you need technical support please contact [imixs.com](http://www.imixs.com) 




## Maven Build
Imixs-Office-Documents is based on [Maven](http://maven.apache.org/) and runs on the Jakarta EE stack. Imixs-Office-Documents can be deployed on JBoss/Wildfly server or other Java EE application servers.
To build the application from sources, run the maven install command first:

	$ mvn clean install

Please check the pom.xml file for dependencies and versions. The master-branch of the project is continuously under development and is typically 
against the latest snapshot releases form the Imixs-Workflow project. To run a stable version please build a [tagged version](https://github.com/imixs/imixs-office-documents/releases). 
To deploy the artifact the application server must provide a database pool named "java:/jdbc/office" and a security domain/realm named 'office'. See also the [Imixs-Workflow Deployment Guide](http://www.imixs.org/doc/deployment/index.html) for further details.

