#Docker

	# Delete all containers
	docker rm $(docker ps -a -q)
	# Delete all images
	docker rmi $(docker images -q)


#Containers

We following section list all Docker containers used by Imixs-Office-Workflow.
For Imixs containers we use the tag /imixs 

To build an image form a Imixs Dockerfile use:

    docker build --tag=imixs/xxxx .

##imixs-config

This image defines configuration and development directories used for Wildfly deployments. The image is based on debian:jessie


    docker build --tag=imixs/config .
    
run:


    docker run -it imixs/config bash
 
##postgres
The postgres default container is used to provide a postgreSQL database. Start the postgres image provided by DockerHub run:

	docker run -e POSTGRES_PASSWORD=password postgres:9.5.2

To define a custom database and container name:
	
	docker run --name office-postgres -e POSTGRES_DB=office -e POSTGRES_PASSWORD=mysecretpassword postgres:9.5.2
 
 
To stop and remove:

    docker stop office-postgres
    docker rm office-postgres 
 
 
##jboss/wildfly

jboss/wildfly is the official wildfly image provided by DockerHub (https://hub.docker.com/r/jboss/wildfly/). To start the container in standalone mode:

    docker run -it jboss/wildfly
    
Start with bash

    docker run -it jboss/wildfly bash



Externalize /standalone/ volume locally:


    docker run -it -v /opt/wildfly-10.0.0.Final/standalone/:/opt/jboss/wildfly/standalone jboss/wildfly:10.0.0.Final bash

    

##imixs/wildfly

The docker image 'imixs/wildfly' extends the wildfly:10.0.0.Final image and adds the EclipseLink library as also the postgres JDBC driver.
See also jboss/wildfly

Build:

	docker build --tag=imixs/wildfly .

Run: 

	docker run -it imixs/wildfly





    docker build --tag=imixs/wildfly .

 
 
###Run with external configuration
    
    docker run -it -v ~/git/office-cmp/src/docker/office-cmp/wildfly/configuration/:/opt/jboss/wildfly/standalone/configuration imixs/wildfly bash
    
    
    docker run -it -v ~/git/office-cmp/src/docker/office-cmp/wildfly/configuration/:/opt/jboss/wildfly/standalone/configuration  -p 8080:8080 -p 9990:9990 imixs/wildfly bash
    
    


#Docker-Compose

See installation guide here: https://docs.docker.com/compose/install/

For Linux:

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
 
 s