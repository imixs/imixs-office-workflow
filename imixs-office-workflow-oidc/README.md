# Imixs-Office-Workflow - Open ID Connect

Imixs-Office-Workflow offers a broad set of security mechanisms to authentication and authorizes users. You can setup security directly on the application server using the corresponding security domain configuration. For this Imixs-Office-Workflow offers a Form-Based Authentication realm named `imixsrealm` per default. When you build the Wildfly Docker image a database security domain is configured per default. You can change the security domain directly in the `standalone.xml` file in various ways. E.g. you can change the security domain from a database realm to a LDAP based realm. Another way is to implement the security with the Jakarta EE Security API 3.0.  

This module includes a corresponding implementation for OpenID Connect. 

Find more details and examples [here](https://doc.office-workflow.com/auth/index.html).

## Build OIDC Security Modul 

To build Imixs-Office-Workflow with an OpenID Connect security module build this module with the following option:

    # imixs-office-workflow/
    $ mvn clean install -Pauth-oidc

The OpenID Connect module is now part of the build Imixs-Office-Workflow an can be used as usual.

**Note:** You need also to disable the security domains configured in the files `WEB-INF/jobss-ejb3.xml` and `WEB-INF/jobss-web.xml` 
