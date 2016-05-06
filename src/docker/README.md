#Docker

Imixs-Office-Workflow provides a Docker configuration to run the Imixs-Workflow environment in different containers.
The environment is split up into three separate containers:

 * imixs/wildfly - provides the wildfly application server environment
 * postgres - provides a postreSQL database server
 * imixs/config - provides a data-container to separat configuration and data files

All containers are composed to one environment using docker-compose. But you can also run the containers manually with a custom kind of configuration. 


#Docker-Compose

We use Docker-Compose to configure all Docker containers used by the Imixs-Workflow environment in one setup file. 

Start with: 

	docker-compose up

##Installation 
To install Docker-Compose see the installation guide here: https://docs.docker.com/compose/install/

For linux users:

Download form GitHub and change mod:

    curl -L https://github.com/docker/compose/releases/download/1.7.0/docker-compose-`uname -s`-`uname -m` > /usr/local/bin/docker-compose
    chmod +x /usr/local/bin/docker-compose

Verify installation:

	docker-compose --version

should result in:

	docker-compose version 1.7.0, build 0d7bf73

You can install lower versions of docker-compose if your version is not running with your docker machine. 
E.g. error message like:  

	client and server don't have same version (client : 1.18, server: 1.17)
 



#The Containers

We following section list all Docker containers and there configuration and environment setup used by Imixs-Office-Workflow.
For Imixs containers we use the tag /imixs 

To build an image form a Imixs Dockerfile use:

    docker build --tag=imixs/xxxx .

##imixs-config

This image defines configuration and development directories used for Wildfly deployments. The image is based on debian:jessie


    docker build --tag=imixs/config .
    
run:


    docker run -it imixs/config bash
 
##postgres
The postgres default container provides a postgreSQL database server. To start the postgres image provided by DockerHub run:

	docker run -e POSTGRES_PASSWORD=password postgres:9.5.2

To start the postgres container for the imixs-workflow environment you need define a custom database named 'office-' and a custome container name 'office-postgres':
	
	docker run --name office-postgres -e POSTGRES_DB=office -e POSTGRES_PASSWORD=mysecretpassword postgres:9.5.2
 
Data databases are persisted in the container file system. The container can also be used to start different databases. 
To start, stop and remove the container run:

    docker start office-postgres
    docker stop office-postgres
    docker rm office-postgres 
 
 


##imixs/wildfly

The docker image 'imixs/wildfly' extends the wildfly:10.0.0.Final image and adds the EclipseLink library as also the postgres JDBC driver.
See also jboss/wildfly

Build:

	docker build --tag=imixs/wildfly .

Run wildfly with life console: 

	docker run -it imixs/wildfly

Run wildfly in background

	docker run -it -d --name office-wildfly imixs/wildfly 


    docker build --tag=imixs/wildfly .

Stop container:

	docker stop office-wildfly
 
###Run with external configuration

The imixs/wildfly image provides a default configuration located in /opt/jboss/wildfly/standalone/configuration/. This configuration can be mapped to a external volume to customize or add additional configuration settings. 
    
    docker run -it -v ~/git/office-cmp/src/docker/office/wildfly/configuration/:/opt/jboss/wildfly/standalone/configuration imixs/wildfly bash
    
To map the Wildfly standard ports use -p option: 
    
    docker run -it -v ~/git/office-cmp/src/docker/office/wildfly/configuration/:/opt/jboss/wildfly/standalone/configuration  -p 8080:8080 -p 9990:9990 imixs/wildfly bash
    

###jboss/wildfly

jboss/wildfly is the official wildfly image provided by DockerHub (https://hub.docker.com/r/jboss/wildfly/). To start the container in standalone mode:

    docker run -it jboss/wildfly
    
Start with bash

    docker run -it jboss/wildfly bash



Externalize /standalone/ volume locally:


    docker run -it -v /opt/wildfly-10.0.0.Final/standalone/:/opt/jboss/wildfly/standalone jboss/wildfly:10.0.0.Final bash

    


 
#Monitoring

Use the following docker command to remove all containers:

	# List all containers
	docker ps -a
	# Stop all containers
	docker stop $(docker ps -a -q)
	# Delete all containers
	docker rm $(docker ps -a -q)
	# Delete all images
	docker rmi $(docker images -q)
