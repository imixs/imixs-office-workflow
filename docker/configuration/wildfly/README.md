# Docker build

We provide Docker images for different projects and widfly versions.
The `devi` bash script provides a build option to build specific versions:

```bash
$ ./devi docker latest
```

builds the curent verison as the 'latest' docker image (based on wildfly 27.0.1)

```bash
$ ./devi docker 29.0.1.Final-jdk17
```

Builds a specific docker image (e.g. for wildfly 29.0.1)

## Wildfly Configuration

The wildfly setup is defined in the configuration directories `docker/configuration/wildfly/*`
Each config directory holds the configuration details for a specific wildfly build containing the database and security default options.
Read the 'README.md' file for each config version for details.

## Wildfly Debug Mode

To build a specific wildfly image in debug mode (used for development only) you can add the optional debug param

DEBUG_MODE=$3
