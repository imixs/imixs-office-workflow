# Open Liberty

This folder contains configuration files used to build a docker container running Open Libertry

To build the Docker Image for Payara run:

	$ mvn clean install -Pdocker-build-openliberty
	
After you have build the payara image with the sample application you can start the corresponding Docker-Stack with:

	$ docker-compose -f docker-compose-openliberty.yml up
	
and run the sample applciation at:

	http://localhost:9080/


## Configuration

You can find the server configuration details for the Open Libertry server in the config directory

	/src/docker/configuration/openliberty/

	