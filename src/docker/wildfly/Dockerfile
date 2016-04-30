FROM jboss/wildfly:10.0.0.Final


# Imixs-Office-Workflow Wildfly Version 1.0.0
MAINTAINER ralph.soika@imixs.com


# wildfly configuration 
COPY eclipselink.jar /opt/jboss/wildfly/modules/system/layers/base/org/eclipse/persistence/main/
COPY module.xml /opt/jboss/wildfly/modules/system/layers/base/org/eclipse/persistence/main/
COPY postgresql-9.3-1102.jdbc41.jar /opt/jboss/wildfly/standalone/deployments/


# Set the default command to run on boot
# This will boot WildFly in the standalone mode and bind to all interface
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0"]

########################################################
# NOTE: 
# run it with          : docker run -it -p 8080:8080 imixs-office
# run in admin mode use: docker run -it -p 8080:8080 -p 9990:9990 imixs-microservice /opt/wildfly/bin/standalone.sh -b 0.0.0.0 -bmanagement 0.0.0.0
########################################################
