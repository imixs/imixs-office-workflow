# Imixs-Office-Workflow - Open ID Connect for Wildfly

This module provides a propriary implementation for a login helper with the Wildfly OIDC impelemtation.
It contains a bean reacting on login events and provides user information from the ODIC claim

## Configuration

To use the build in OIDC login module from Widlfyl must add the following `oidc.yaml` file into your /WEB-INF/ directory

```json
{
  "client-id": "imixs",
  "provider-url": "http://keycloak.imixs.local:8084/realms/imixs-office-workflow",
  "principal-attribute": "preferred_username",
  "credentials": {
    "secret": "xxxxxxxxxxxxxxxxx"
  }
}
```

**Notes:**

- Replace the params with your corresponding values!
- The Wildfly OIDC module is available with Version 29 or greater!
- No additional realm configuration is needed. Find more information [here](https://ralph.blog.imixs.com/2025/03/26/wildfly-29-oidc-bearer-token-authentication/)

## Bearer Token Login

For a programmatic login you can use the access token given by the OpenID Provider.

See the following curl example

```bash
export access_token=$(\
curl -X POST http://keycloak.imixs.local:8084/realms/imixs-office-workflow/protocol/openid-connect/token \
-H 'content-type: application/x-www-form-urlencoded' \
-d 'client_id=imixs&client_secret=xxxxxxxxxxxxxx' \
-d 'username=anna&password=anna&grant_type=password' | jq --raw-output '.access_token' \
)

curl -vL http://localhost:8080/api/model   -H "Authorization: Bearer "$access_token
```

## Build OIDC Security Modul

To build Imixs-Office-Workflow with an OpenID Connect security module build this module with the following option:

    # imixs-office-workflow/
    $ mvn clean install -Pauth-oidc-wildfly

The Widlfly OpenID Connect module is now part of the build Imixs-Office-Workflow an can be used as usual.

**Note:** You need also to disable the security domains configured in the files `WEB-INF/jobss-ejb3.xml` and `WEB-INF/jobss-web.xml`
