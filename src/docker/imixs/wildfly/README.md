#imixs/wildfly


This imixs/wildfly Docker image runs the Java application server [WildFly](http://www.wildfly.org). The image is based on Debian/Jessie. 
The Docker image is runs Wildfly 10.0.0 on OpenJDK 8 and is a little bit smaller than the official [JBoss/Wildfly image](https://hub.docker.com/r/jboss/wildfly/). 
The Dockerfile was inspired by this [image](https://hub.docker.com/r/piegsaj/wildfly/).


##1. Install Docker
Follow the installation instructions for your host system.
To build the image from the Dockerfile: 

    docker build --tag=imixs/wildfly .

##2. Running and stopping a container
The container includes a start script which allows to start Wildfly with an admin password to grant access to the web admin console. You can start an instance of wildfly with the Docker run command:

    docker run --name="wildfly" -d -p 8080:8080 -p 9990:9990 -e WILDFLY_PASS="admin_password" imixs/wildfly

If you leave the environment parameter 'WILDFLY_PASS' empty, the start script will generate a random password. 
If you expose the ports 8080 and 9990 you can access Wildfly via [http://<host-ip>:8080/](http://localhost:8080) and [http://<host-ip>:9990/](http://localhost:9990)

To stop and remove the Docker container run the Docker command: 

    docker stop wildfly && docker rm wildfly

##3. Access WildFly

To follow the wildfly server log: 

    docker logs -f wf

To access the wildfly commandline tool (CLI) run: 

    docker exec -it wf /opt/wildfly/bin/jboss-cli.sh -c -u=admin -p=a_password


To access the shell: 

    docker exec -it wildfly /bin/bash	
	
##How to bind external volumes

If you want to customize the configuration or provided applications you can do so by defining external volumes at the following locations:

* /opt/wildfly/standalone/configuration/  => for custom configuration files like standalone.xml
* /opt/wildfly/standalone/deployments/ => to provide an external autodeploy directory. 

This is an example to run imixs/wildfly with external volumes:

     docker run --name="wildfly" -d -p 8080:8080 -p 9990:9990 \
             -e WILDFLY_PASS="admin_password" \
             -v /path/to/deployments:/opt/wildfly/standalone/deployments/ \
             imixs/wildfly

To provide also configuration:


     docker run --name="wildfly" -d -p 8080:8080 -p 9990:9990 \
             -e WILDFLY_PASS="admin_password" \
             -v ~/git/imis-office-workflow/src/path/to/deployments:/opt/wildfly/standalone/deployments/ \
             -v /path/to/configuration:/opt/wildfly/standalone/configuration/ \
             imixs/wildfly

