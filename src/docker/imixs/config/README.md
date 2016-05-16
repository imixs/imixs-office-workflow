#imixs/config

The imixs/config image provides a data container to be used when running Imixs-Office-Workflow
The image provides volumes for the /configuration folder of wildfly.

###Run with external configuration

The imixs/wildfly image provides a default configuration located in /opt/jboss/wildfly/standalone/configuration/. This configuration can be mapped to a external volume to customize or add additional configuration settings. 
    
    docker run -it -v ~/git/office-cmp/src/docker/office/wildfly/configuration/:/opt/jboss/wildfly/standalone/configuration imixs/wildfly bash
    
To map the Wildfly standard ports use -p option: 
    
    docker run -it -v ~/git/office-cmp/src/docker/office/wildfly/configuration/:/opt/jboss/wildfly/standalone/configuration  -p 8080:8080 -p 9990:9990 imixs/wildfly bash