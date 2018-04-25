FROM imixs/wildfly:1.2.1

# Setup configuration
COPY ./src/docker/configuration/imixsrealm.properties ${WILDFLY_CONFIG}/
COPY ./src/docker/configuration/standalone.xml ${WILDFLY_CONFIG}/

# Deploy artefact
COPY ./imixs-office-workflow-ear/target/imixs-office-workflow-ear-*.ear ${WILDFLY_DEPLOYMENT}/
