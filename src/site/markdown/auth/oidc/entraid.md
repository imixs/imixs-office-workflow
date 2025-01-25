# Secure Imixs-Office-Workflow with Microsoft Entra ID & OpenID Connect

The recent release of Imixs-Office-Workflow for Wildfly includes native support for OpenID Connect (OIDC) via the elytron-oidc-client subsystem. OIDC is an identity layer that enables clients, to verify a user’s identity based on authentication performed by an OpenID provider. For example, you can secure Imixs-Office-Workflow using Microsoft Entra ID (formerly Azure Active Directory) as the OpenID provider.

## Prerequisites

Ensure that you have a Microsoft Azure account with an active subscription (find details below)

Add the following Maven dependency into your custom build. This will include the Imixs-Office-Worklfwo OIDC Module:

```xml
    <dependency>
        <groupId>org.imixs.workflow</groupId>
        <artifactId>imixs-office-workflow-oidc</artifactId>
        <version>${org.imixs.office.version}</version>
        <scope>compile</scope>
    </dependency>
```

This will build a version including the library `imixs-office-workflow-oidc-*.jar`. This optional module provides the OIDC Security bean with the corresponding '@OpenIdAuthenticationMechanismDefinition'.

**Note:** Make sure that the application deployment descriptor `jboss-web.xml` and `jboss-ejb3.xml` did not set the `<security-domain>`! This will overwrite the OIDC setting and will break the authentication flow.

The configuration of the OpenID Provider Endpoint and the client secret can be done by setting the following environment variables in your Docker image:

| Environment Param       | Description                                           |
| ----------------------- | ----------------------------------------------------- |
| OIDCCONFIG_ISSUERURI    | endpoint for identity provider                        |
| OIDCCONFIG_CLIENTID     | OIDC Client ID                                        |
| OIDCCONFIG_CLIENTSECRET | Client secret                                         |
| OIDCCONFIG_REDIRECTURI  | Redirect URI - application address with /callback uri |

Note that the Imixs OIDC module provides a redirect servlet with the endpoint `/callback` this is the endpoint typically used by the identity provider as the callback uri.
The client id and secret information needed here is from the Microsoft System.

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

### HTTPS

Make sure you are using HTTPS Listener in Wildfly (Port 8443) instead of the HTTP listener (Port 8080). The OpenID Provider typically did not accept a different protocol during the verification procedure. The HTTPS listener is enabled in Wildfly by default.

You can set the correct redirect URL with the environment parameter `OIDCCONFIG_REDIRECTURI`

## Connect Imixs-Office-Workflow to Microsoft Entra ID

In the Azure portal, go to the All Services page and click Microsoft Entra ID.
In the left-hand menu, select App registrations and click New Registration. You will see the Register an application page

<img src="azure-register.jpg.webp" />

Enter a name (e.g., imixs) and click the Register button. This brings you to the App registrations overview page for your application.

Click Add a Redirect URL, then click Add a Platform.

Select Web; you will see the Configure Web page

<img src="azure-register-app.jpg.webp" />

Enter the URL of your Imixs-Office-Workflow Application

Click the Configure button.

Go back to the Overview page and click Add a certificate or secret.

Select + New Client secret. This brings you to the Add a client secret

<img src="azure-add-secret.jpg.webp" />

Enter a description (e.g., Imixs-Office-Workflow) and click the Add button.

Copy the value shown on the next page and paste this value into envrionment variable `OIDCCONFIG_CLIENTSECRET`

Return to the Overview page and click on Endpoints.

Copy the value from OpenID Connect metadata document and paste it into the environment variable `OIDCCONFIG_ISSUERURI`. Be sure to remove the text /.well-known/openid-configuration.

Return to the Overview page. Copy the Application (client) ID and paste this value in the environment variable `OIDCCONFIG_CLIENTID`

Save the changes to the oidc.json file.

The connection to Microsoft Entra ID is now configured, and you can deploy Imixs-Office-Workflow and test the authentication.
When you enter the URL of your Imixs-Office-Workflow installation in your browser you'll now be redirected to the Azure login page to log in with your Azure credentials.

<img src="azure-login.jpg.webp" />
