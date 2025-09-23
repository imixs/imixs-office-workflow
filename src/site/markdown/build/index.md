# How to Build your Custom Version of Imixs Office Workflow

Imixs-Office-Workflow is a powerful and highly customizable workflow application platform. Although Imixs-Office-Workflow can be used out of the box as a [standard application](../install/quick_install.html), it is also possible to create a so-called custom build. A custom build inherits all the functionality of the Imixs-Office-Workflow but also provides the ability to add or customize new features.

## Create a custom build from the Imixs Office Archetype

Imixs-Office-Workflow is a Java Enterprise Application. You don't have to be a Java EE developer, however, it is useful to be familiar with the concepts of Java and Java EE. Imixs-Office-Workflow itself is running on an application server. With the help of Docker a manual installation of a Java EE application server is not required. If you go through this tutorial, the application will be deployed into a docker container. This Docker image can also be used in production environment, though, a custom configuration is recommended for most cases.
Maven 3.x & Docker

Imixs-Office-Workflow is build on [maven](https://maven.apache.org/) build tool for Java projects. It is recommended that you have installed Apache Maven 3.0 or higher. Also you should be familiar with the build concept of maven. To run you application we use Docker. Make sure you have installed the Docker runtime before you start.
Let's get Started!

When you met the prerequisites, then you can start with your custom build. As Imixs-Office-Workflow is based on Maven even the custom build is created by a Maven Archetype.

You can change the behavior and layout of your custom build without conflicting with any updates made by the Imixs-Office-Workflow project. Also you can easily upgrade to any new version.
Creating a custom build using Eclipse

To create a new maven project from an archetype you can use the maven commandline tool:

```bash
$ mvn archetype:generate\
    -DarchetypeGroupId=org.imixs.workflow\
    -DarchetypeArtifactId=imixs-office-archetype\
    -DarchetypeVersion=5.1.0
```

you will be asked for a groupId and an artefactId of your new project:

```bash
...
Define value for property 'groupId': foo.my-app
Define value for property 'artifactId': office-custom-app
Define value for property 'version' 1.0-SNAPSHOT:
Define value for property 'package' foo.my-app:
Confirm properties configuration:
groupId: foo.my-app
artifactId: office-my-app
version: 1.0.0
package: foo.my-app
 Y: Y
```

The result will be created in a directory structure like this:

```
my-office-app/
├── devi
├── pom.xml
├── src/
│   └── main/
│       ├── java/
│       │   └── (optional new java classes)
│       └── webapp/
│           ├── WEB-INF/
│           │   └── (optional new configuration settings)
│           └── (new pages)
└── target/
```

The directory contains already a bash script to build and start the environment. Change the execution flag if necessary (Linux)

```bash
$ chmod u+x devi
```

You can setup the environment with:

```bash
$ ./devi setup
```

To start the docker environment run:

```bash
$ ./devi start
```

To build the application run:

```bash
$ ./devi build
```

After you have startet you application follow the [Quick-Start Guide](../quickstart.html) to setup you new custom instance of Imixs-Office-Workflow

## Mount Points

The default configuration sets a local mount point for a hot deployment and the import of data files:

```
....
    volumes:
      - ./docker/deployments:/opt/jboss/wildfly/standalone/deployments/
      - ./import_examples:/opt/jboss/import_examples/
...
```

## Upgrade Versions

You can change the version to upgrade your application to the latest Build of Imixs-Office-Workflow and Imixs-Workflow.
Open the pom.xml file and change the version in the sections `properties`. After that you can rebuild you application with `$ ./devi setup`.

## Customization

To customize the layout of your application you can edit the file `src/main/webapp/layout/css/custom.css`.

You can also add new pages, custom form parts and sections. Just add the new files into `/src/main/webapp`.

Also you can implement your own CDI Beans and Services under `/src/main/java`.
