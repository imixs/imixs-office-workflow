# Installation Guide

Imixs-Office-Workflow provides a Docker Container to run the service in any Container Environment.
With this Installation guide you will find some typical setups for different situations.

- [Installation With Docker-Compose](#installation-with-docker-compose)
- [Kubernetes Deployment](#kubernetes-deployment)
- [More Docker Images](#more-docker-images)

To follow the installation you should be familiar with the corresponding container environment.

## Installation With Docker-Compose

Imixs-Office-Workflow is provided in Docker images and can be simply started with Docker or Docker-Compose. You will find the lates Docker images on the [Docker-Hub Repository](https://hub.docker.com/repository/docker/imixs/imixs-office-workflow/general).

1. Create a working directory on you machine (e.g. /office-workflow/)
2. Copy the [docker-compose.yml](https://github.com/imixs/imixs-documents/blob/master/docker/docker-compose.yml) file into work working directory

docker-compose.yml

```yaml
version: "3.6"
services:
  db:
    image: postgres:9.6.1
    environment:
      POSTGRES_PASSWORD: adminadmin
      POSTGRES_DB: office
    volumes:
      - dbdata:/var/lib/postgresql/data
  imixs-documents:
    image: imixs/imixs-documents:latest
    depends_on:
      - db
    environment:
      JAVA_OPTS: "-Dnashorn.args=--no-deprecation-warning"
      POSTGRES_USER: "postgres"
      POSTGRES_PASSWORD: "adminadmin"
      POSTGRES_CONNECTION: "jdbc:postgresql://db/office"
      TZ: "Europe/Berlin"
      MAILGATEWAY: "localhost"
    ports:
      - "8080:8080"
volumes:
  dbdata:
```

### Run the Application

To start Imixs-Office-Workflow run the following docker command from your working directory:

    $ docker-compose up

This will start the database and the application server.
After a few minutes you access the application from your web browser:

[http://localhost:8080/](http://localhost:8080/)

You can login with the default account 'admin' with the password 'adminadmin'. Follow the [Setup Guide](../quickstart.html) to setup a new Instance of Imixs-Office-Workflow.

## Kubernetes Deployment

_Imixs-Documents_ provides a base deployment configuration for Kubernetes. The setup is based on [Kustomize](https://kubernetes.io/docs/tasks/manage-kubernetes-objects/kustomization/) providing a declarative object management.

### Deploy

To create the deployment objects from the base-deployment run:

    $ kubectl apply --kustomize https://github.com/imixs/imixs-documents/kubernetes/

The service endpoint of Imixs-Documents will be published on port 8080.
This basic deployment configuration assumes that a default storage class is defined within your kubernetes cluster. This storage class will be used for the database storage and the search index. You can customize the service and persistence volume configuration to your needs by using a custom setup.

### Custom Setups

To create a custom deployment create an overlay with custom settings based on the base deployment. First create a new folder with the file _kustomization.yaml_:

```yaml
namespace: my-application
bases:
  - https://github.com/imixs/imixs-documents/kubernetes/

resources:
  - 031-network.yaml

patchesStrategicMerge:
  - 010-deployment.yaml
```

The _kustomization.yaml_ file simply points into the base directory hosted on github. It defines the new namespace 'my-application' where the resource objects will be created. Within this directory you can define new resources or resources to be merged in a existing resource.

So will have the following directory structure:

```
.
├── my-deployment
│   ├── 010-deployment.yaml
│   ├── 031-network.yaml
│   └── kustomization.yaml
```

You can now build the overlay with:

    $ kubectl apply --kustomize  ./my-deployment

In this example the file _010-deployment.yaml_ adds a new additional environment variable with the name "ARCHIVE_SERVICE_ENDPOINT" to the imixs-documents deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: imixs-documents
  labels:
    app: imixs-documents
spec:
  template:
    spec:
      containers:
        - name: imixs-documents
          env:
            - name: ARCHIVE_SERVICE_ENDPOINT
              value: "http://imixs-archive:8080/api"
```

This shows how you can overwrite or extend existing deployment settings.

The file _031-network.yaml_ defines a new ingress configuration to publish the sevice endpoint of Imixs-Documents to a public or private Internet address:

```yaml
---
###################################################
# Ingress
###################################################
kind: Ingress
apiVersion: networking.k8s.io/v1beta1
metadata:
  name: documents-imixs-tls
spec:
  rules:
    - host: documents.foo.com
      http:
        paths:
          - path: /
            backend:
              serviceName: imixs-documents
              servicePort: 8080
```

This resource will be added to the base deployment.

You can find further details about Kustomize [here](https://github.com/imixs/imixs-cloud/blob/master/doc/KUSTOMIZE.md).

## Docker Images

You can run Imixs-Office-Workflow directly form one of the official images or your can use one of these images for a [Custom Build](../build/index.html).
The Imxis-Office-Workflow Docker images are based on the official Wildfly images.
The following section gives an overview about the versions. If you are developing a custom build take care that the Docker image maches your Wildfly Server version and Jakarta EE platform version:

|     Imixs-Office-Workflow Docker Image     |      Wildfly Version       | Jakarta EE Platform |
| :----------------------------------------: | :------------------------: | :-----------------: |
| imixs/imixs-office-workflow:5.1.1-SNAPSHOT | wildfly:29.0.1.Final-jdk17 |         10          |
|     imixs/imixs-office-workflow:5.1.0      | wildfly:27.0.1.Final-jdk17 |         10          |
|     imixs/imixs-office-workflow:5.0.0      |    wildfly:25.0.0.Final    |          9          |
|     imixs/imixs-office-workflow:4.6.2      |    wildfly:25.0.0.Final    |          9          |

### Docker Images on Docker-Hub

If you need more functionality Imixs provides a lot of additional Docker images in the [Docker-Hub Repository](https://hub.docker.com/repositories/imixs). You will find technical details about the setup on the corresponding [Github Pages](https://github.com/imixs)
