# Define default wildfly version
ARG WILDFLY_VERSION=29.0.1.Final-jdk17

# Base image
FROM quay.io/wildfly/wildfly:${WILDFLY_VERSION}

# Re-declare ARG after FROM (this is important - ARGs before FROM aren't available after FROM without redeclaring)
ARG WILDFLY_VERSION
# Raise JDK serialization filter limits (Imixs stores documents incl. file content as serialized maps)
ENV JDK_SERIAL_FILTER="maxbytes=1073741824;maxdepth=128;maxarray=1073741824;maxrefs=10000000"


LABEL description="Imixs-Office-Workflow"
LABEL maintainer="ralph.soika@imixs.com"

# Copy EclipseLink
COPY ./docker/configuration/wildfly/${WILDFLY_VERSION}/modules/ /opt/jboss/wildfly/modules/

# Setup configuration
COPY ./docker/configuration/wildfly/${WILDFLY_VERSION}/standalone.xml /opt/jboss/wildfly/standalone/configuration/

# Deploy artefact
COPY ./imixs-office-workflow-app/target/imixs-office-workflow*.war /opt/jboss/wildfly/standalone/deployments/

USER root
RUN mkdir -p /opt/jboss/lucene \
 && chown -R jboss:jboss /opt/jboss/lucene \
 && chown -R jboss:jboss /opt/jboss/wildfly/modules/system/layers/base/org/eclipse/persistence/main
USER jboss

WORKDIR /opt/jboss
# Run with management interface
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]