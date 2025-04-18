# Payara Micro

This folder contains configuration files used to build a docker container running Payara Server-Full 6.2025.4-jdk17

Find configuration details here: https://docs.payara.fish/community/docs/Technical%20Documentation/Payara%20Server%20Documentation/Payara%20Server%20Docker%20Image.html

To build the Docker Image for Payara run:

    $ devi payara

After you have build the payara image with the sample application you can start the corresponding Docker-Stack with:

    $ docker-compose -f docker-compose-dev.yaml up

and run the sample applciation at:

    http://localhost:8080/

## Configuration

You can find the configuration details for the payara server in the config directory

    /docker/configuration/payara/
