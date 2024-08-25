FROM quay.io/wildfly/wildfly:27.0.1.Final-jdk17

LABEL description="Imixs-Office-Workflow"
LABEL maintainer="ralph.soika@imixs.com"

# Copy EclipseLink
COPY ./docker/configuration/wildfly/modules/ /opt/jboss/wildfly/modules/

# Setup configuration
COPY ./docker/configuration/wildfly/standalone.xml /opt/jboss/wildfly/standalone/configuration/

# Deploy artefact
ADD ./imixs-office-workflow-app/target/imixs-office-workflow*.war /opt/jboss/wildfly/standalone/deployments/
USER root
RUN mkdir /opt/jboss/lucene
RUN chown -R jboss:jboss /opt/jboss/lucene
USER jboss

WORKDIR /opt/jboss
# Run with management interface
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]