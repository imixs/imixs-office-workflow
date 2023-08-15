# Keycloak

The following section describes how to run Imixs-Office-Workflow with [Keycloak](https://www.keycloak.org/) in a local dev environment.

To start the environment including a Keycloak server and Imixs-Office-Workflow run :

    $ docker-compose -f docker/docker-compose-keycloak up

Also it is necessary for a local test to create the local hostname `keycloak.imixs.local` pointing to the host loopback interface `127.0.0.1`


You can access the keycloak server via

    http://keycloak.imixs.local:8084

For a first login use the userid `admin` with the password `adminadmin`. You can change these user settings in the docker-compose.yaml file. 



## Setup a Client Realm

First you need to setup a new client Realm within the keycloak server. 

Create a new realm with the name `imixs-office-workflow`

<img src="keycloak-001.png" />

This generates a new realm within keycloak to manage users and groups.

## Setup a Client

Next you need to create a client with the name `imixs`:


<img src="keycloak-002.png" />

The client will be be used by Imixs-Office-Workflow to login the user. The `client-id` is the client identifier for the application.

Click 'Next' and activate the option 'Client authentication'. This will generate a client secret.

<img src="keycloak-003.png" />


Click 'Next' and add the redirect URI `http://localhost:8080/*`


<img src="keycloak-004.png" />


To test the OIDC provider capabilities you can request the configuration URL in your web browser:

    http://keycloak.imixs.local:8084/realms/imixs-office-workflow/.well-known/openid-configuration


## Create Roles 

Next create the following Realm Roles 

- org.imixs.ACCESSLEVEL.READERACCESS
- org.imixs.ACCESSLEVEL.AUTHORACCESS
- org.imixs.ACCESSLEVEL.AUTHORACCESS
- org.imixs.ACCESSLEVEL.MANAGERACCESS

<img src="keycloak-005.png" />


## Create Users

Now you can create new User Accounts 

<img src="keycloak-006.png" />

and assign these users to one of the roles:

<img src="keycloak-007.png" />

Also don't forget to set the password for the new user via the tab 'Credentials'. 



## Add a Role Group Mapper

The OpenID Client expects the roles in the scope 'groups'. This scope is not activated by default in Keycloak. To do this you just need to create a group mapper

Choose the menue 'Client Scopes' and select the scope named 'roles' from the list:


<img src="keycloak-008.png" />

Next click on the tab 'Mappers' and add a new Mapper from the predefined mappers and choose 'groups'

<img src="keycloak-009.png" />

Thats it, now the roles are provided in the scope 'groups'. 


## Use OIDC with Keycloak and Imixs-Office-Workflow

[OpenID Connect](https://openid.net/connect/) is an identity layer on top of the OAuth 2.0 protocol. OpenID Connect makes it possible for a client to verify a user’s identity based on authentication that’s performed by an OpenID provider.

WildFly 25, which is used by the latest version of Imixs-Office-Workflow, added the ability to secure applications using OpenID Connect, without needing to make use of the Keycloak client adapter. To integrate Keycloak, WildFly 25 introduced a new elytron-oidc-client subsystem that scans deployments to check if the OpenID Connect (OIDC) authentication mechanism is needed. If the subsystem detects that the OIDC mechanism is required for a deployment, the subsystem will activate this authentication mechanism automatically. This feature makes it easy to connect Imixs-Office-Workflow with Keycloak. 


## Aktivating the OIDC Module

To activate OIDC in Imixs-Office-Workflow you need to build the application with the maven profile 'oidc'

    $ mvn clean install -Pauth-oidc

This will build a version including the library `imixs-office-workflow-oidc-*.jar`. This optional module provides the OIDC Security bean with the corresponding '@OpenIdAuthenticationMechanismDefinition'. 


**Note:** Make sure that the application deployment descriptor `jboss-web.xml` did not set the `<security-domain>`! This will overwrite the OIDC setting and will break the authentication flow.

The configuration of the OpenID Provider Endpoint and the client secret can be done by setting the following environment variables in your Docker image:

```
      OIDCCONFIG_ISSUERURI: "http://keycloak.imixs.local:8084/realms/imixs-office-workflow"
      OIDCCONFIG_CLIENTID: "imixs"
      OIDCCONFIG_CLIENTSECRET: "<YOUR-CLIENT-SCRET>"   
```

The IssuerURI is the endpoint of your Keycloak sever. The client secret can be copied form the 'credentials' tab in your client configuration.

<img src="keycloak-010.png" />

Finally redeploy your application. 

Thats it.



## Wildfly - Disable JASPIC

One tricky point is that by default the elytron subsystem enforces the logged user to be in the default other domain (by default application users are placed in the application-users.properties file in wildfly). The integrated-jaspi option can be set to `false` to avoid that. 

**Note:** The default Docker Image form Imixs-Office-Workflow already have deactivated this option, so no changes are necessary in most cases. 

If you want to set this option manually, this can be done by either editing the standalone.xml file:

```xml 
 ....
 .........
        <subsystem xmlns="urn:jboss:domain:undertow:13.0" default-server="default-server" default-virtual-host="default-host" default-servlet-container="default" default-security-domain="other" statistics-enabled="${wildfly.undertow.statistics-enabled:${wildfly.statistics-enabled:false}}">
           .......
           .............
            <application-security-domains>
                <!-- Disable integrated jaspic -->
                <application-security-domain name="other" security-domain="ApplicationDomain" integrated-jaspi="false"/>        
            </application-security-domains>
        </subsystem>
    .....
```

or with the cli-commandline tool:

    $ ./jboss-cli.sh --connect --controller=remote+http://localhost:9990
    $ /subsystem=undertow/application-security-domain=other:write-attribute(name=integrated-jaspi, value=false)
    $ relaod


## Additional Information

Find more information about Keycloak and Wildfly here: 

 - https://wildfly-security.github.io/wildfly-elytron/blog/securing-wildfly-apps-openid-connect/
 - https://auth0.com/blog/jakarta-ee-oidc/
 - https://blogs.nologin.es/rickyepoderi/

