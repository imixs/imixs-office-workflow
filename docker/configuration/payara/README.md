# Payara Micro

This folder contains configuration files used to build a docker container running Payara Micro 5.2021.9

To build the Docker Image for Payara-Micro run:

	$ mvn clean install -Pdocker-payara
	
After you have build the payara image with the sample application you can start the corresponding Docker-Stack with:

	$ docker-compose -f docker-compose-payara-micro.yaml up
	
and run the sample applciation at:

	http://localhost:8080/
	
## Configuration

You can find the configuration details for the payara server in the config directory

	/src/docker/configuration/payara/

	