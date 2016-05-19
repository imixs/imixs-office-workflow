#imixs/vc_config

The imixs/vc_config image provides a data container to be used when running Imixs-Office-Workflow on jboss/wildfly.
The container provides the following mount points

 * /configuration  (containing project specific configuration of wildfly)
 * /log (providing log files from wildfly)
 

##How to build form dockerfile

The docker image 'imixs/vc_config' provides managed volumes which can be shared with other containers based on jboss/wildfly.

Build:

	docker build --tag=imixs/vc_config .


##How to share volumes
A container from the docker image 'imixs/data' must not be started to allow sharing its managed volumes.

You can start the container to explore the data structure


    docker run --name office_1 -it imixs/vc_config bash
    


	
###Run with external mount volumes

You can externalize the configuration moint point by binding a mount volume:

    docker run -it -v ~/git/imixs-office-workflow/src/docker/imixs/vc_config/configuration/:/opt/jboss/wildfly/standalone/configuration imixs/vc_config bash
    
