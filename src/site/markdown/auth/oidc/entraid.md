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

```
      OIDCCONFIG_ISSUERURI: "https://login.microsoftonline.com/xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx/v2.0"
      OIDCCONFIG_CLIENTID: "<YOUR-MICROSOFT-CLIENTID>"
      OIDCCONFIG_CLIENTSECRET: "<YOUR-CLIENT-SCRET>"
```

The data needed here is from the Microsoft System.

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
