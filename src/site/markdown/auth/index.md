# Security - Authentication and Authorization


Imixs-Office-Workflow supports a broad set of security mechanisms to authentication and authorizes users. You can setup security directly on the application server using the corresponding security domain configuration. For this Imixs-Office-Workflow offers a Form-Based Authentication realm named imixsrealm per default. When you build the Wildfly Docker image a database security domain is configured per default. You can change the security domain directly in the standalone.xml file in various ways. E.g. you can change the security domain from a database realm to a LDAP based realm. Another way is to implement the security with the Jakarta EE Security API 3.0. This module includes a corresponding implementation for OpenID Connect.

The following section will describe details about how to setup security in Imixs-Office-Workflow


 - [Database Security Domain](./db.html)
 - [LDAP Security Domain](./ldap.html)
 - [Keycloak Integration](./oidc/keycloak.html)