#imixs/vc_config

The imixs/vc_config image provides a data container to be used when running Imixs-Office-Workflow on jboss/wildfly.
The container provides the following mount points

 * /opt/jboss/wildfly/standalone/configuration  (containing project specific configuration of wildfly)
 * /opt/jboss/wildfly/standalone/log (providing log files from wildfly)
 * /opt/jboss/wildfly/standalone/deployments (deployment folder)
 * /opt/jboss/wildfly/modules/system/layers/base/org/eclipse/persistence/main  (providing the eclipselink configuration)
 

##How to build form dockerfile

To build the imixs/vc_config image use the following docker command:

	docker build --tag=imixs/vc_config .


##How to share volumes

To create a imixs/vc_config container run the following command:

    docker run -d --name imixs-office-workflow-config imixs/vc_config
    
The name can should be project specific. In this example we start a vc_config container for the imixs-office-workflow default setup.
    
After you created the container, the volumes are managed by the Docker daemon. To find out exactly where this folders are located on the host, you can use the following Docker command:

    docker inspect -f "{{json .Volumes}}" imixs-office-workflow-config
     
You can start the container in interactive mode to explore the data structure:

    docker start -i imixs-office-workflow-config
    
After you have created the container you can share this container with a jboss/wildfly instance

    docker run --rm -it --name wildfly -p 8080:8080 --volumes-from imixs-office-workflow-config jboss/wildfly:10.0.0.Final

##Remove the vc_config conatiner

You can run multiple containers form the image imixs/vc_config. To remove a container use the -v option to also remove the managed volumes:

    docker rm -v imixs-office-workflow-config
