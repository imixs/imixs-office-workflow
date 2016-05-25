FROM debian:jessie-backports

MAINTAINER Ralph Soika <ralph.soika@imixs.com>

USER root

# install packages: vi ne pwgen curl openjdk-8-jdk
RUN apt-get update \
 && apt-get install --no-install-recommends -y \
 vim ne pwgen curl openjdk-8-jdk-headless \
 # Slim down image
 && apt-get clean \
 && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/* /usr/share/man/?? /usr/share/man/??_*


	
# Create a user and group used to launch processes
# The user ID 1000 is the default for the first "regular" user on Fedora/RHEL,
# so there is a high chance that this ID will be equal to the current user
# making it easier to use volumes (no permission issues)
RUN groupadd -r wildfly -g 1000 && useradd -u 1000 -r -g wildfly -m -d /opt/wildfly -s /sbin/nologin -c "wildfly user" wildfly && \
    chmod 755 /opt/wildfly

# Set the working directory to jboss' user home directory
WORKDIR /opt/wildfly

# Specify the user which should be used to execute all commands below
USER wildfly


# Add the WildFly distribution to /opt, and make wildfly the owner of the extracted tar content
# Make sure the distribution is available from a well-known place
ENV WILDFLY_VERSION 10.0.0.Final
RUN curl http://download.jboss.org/wildfly/$WILDFLY_VERSION/wildfly-$WILDFLY_VERSION.tar.gz | tar zx && mv $HOME/wildfly-$WILDFLY_VERSION/* $HOME

RUN ls -la /opt/
RUN ls -la /opt/wildfly/



	
# Add the Wildfly start script 
ADD wildfly_start.sh /opt/wildfly/
ADD wildfly_add_admin_user.sh /opt/wildfly/       
USER root
RUN chmod +x /opt/wildfly/wildfly_add_admin_user.sh /opt/wildfly/wildfly_start.sh
USER wildfly

# Expose the ports we're interested in
EXPOSE 8080 9990

CMD ["/opt/wildfly/wildfly_start.sh"]