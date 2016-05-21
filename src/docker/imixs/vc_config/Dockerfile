FROM debian:jessie

# Imixs-Config Version 1.0.0
MAINTAINER ralph.soika@imixs.com


####################################################################
# Setup User Imxis
####################################################################
USER root

# add jessie backports
RUN echo "#Backports" >> /etc/apt/sources.list && echo deb http://http.debian.net/debian jessie-backports main >> /etc/apt/sources.list

# install vi and ne
RUN apt-get update \
	&& apt-get install -y vim ne


# Create a user and group used to launch processes
# The user ID 1000 is the default for the first "regular" user on Fedora/RHEL,
# so there is a high chance that this ID will be equal to the current user
# making it easier to use volumes (no permission issues)
RUN groupadd -r imixs -g 1000 && useradd -u 1000 -r -g imixs -m -d /opt/imixs -s /sbin/nologin -c "Imixs user" imixs && \
    chmod 755 /opt/imixs

# Set the working directory to imixs' user home directory
WORKDIR /opt/imixs

# copy wildfly config directories

COPY configuration/ /opt/jboss/wildfly/standalone/configuration/
RUN chown -R imixs /opt/jboss/wildfly/standalone/configuration/


VOLUME /opt/jboss/wildfly/standalone/configuration
VOLUME /opt/jboss/wildfly/standalone/logs

# Specify the user which should be used to execute all commands below
USER imixs

CMD ["/bin/bash"]