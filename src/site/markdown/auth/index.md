# Security - Authentication and Authorization


Imixs-Office-Workflow supports a broad set of security mechanisms to authentication and authorizes users. You can setup security directly on the application server using the corresponding security domain configuration. For this Imixs-Office-Workflow offers a Form-Based Authentication realm named imixsrealm per default. When you build the Wildfly Docker image a database security domain is configured per default. You can change the security domain directly in the standalone.xml file in various ways. E.g. you can change the security domain from a database realm to a LDAP based realm. Another way is to implement the security with the Jakarta EE Security API 3.0. This module includes a corresponding implementation for OpenID Connect.

The following section will describe details about how to setup security in Imixs-Office-Workflow

 - [Database Security Domain](./db.html)
 - [LDAP Security Domain](./ldap.html)
 - [Keycloak Integration](./oidc/keycloak.html)


## User Profile Patterns

Imixs-Office-Workflow supports different input formats for the user login name. This is helpful to avoid different spellings of a login name. This feature is supported by the Plugin:

```java 
org.imixs.marty.profile.ProfilePlugin
```

The plugin should be added to the system workflow model including the Profile Workflow.

### Regex Pattern for UserID

During a Login or when you create new user in the user database, the plugin verifies the **userId** and email address for input patterns and
validates for duplicates. The input pattern for the userID can be defined by the imixs property `security.userid.input.pattern`

The default value is `^[A-Za-z0-9.@\\-\\w]+` which allows normal characters and numbers

To extend the pattern so the for example also special characters are allowed you can use the following regex:

`security.userid.input.pattern=^[\p{L}0-9.@\\-]+`

to set this param in the Environment variables of your server use the UNIX style for parameter names:

```
DEFAULT_USERID_PATTERN=^[\p{L}0-9.@\\-]+
```

### Special Characters in Login Name:

**Note:** Depending on the server it may be necessary to configure also the encoding of the servlet container. For example for Wildfly the servlet-container parameter in the undertow subsystem need to be edited. And the `default-encoding` attribute with the value `UTF-8`:


```xml
....
            <servlet-container name="default" default-encoding="UTF-8">
                <jsp-config />
                <websockets />
            </servlet-container>
...
``` 

### Regex Pattern for E-Mail Address

Also the E-Mail address is validated by the ProfilePlugin. by teh following pattern:

```
EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\.[-A-Za-z0-9]+)*(\.[A-Za-z]{2,})$
```

You can adapt this pattern if needed.

### Upper and Lowercase Input

The login page of Imixs-Office-Workflow lowercases all inputs for the userid by default. This is controlled by the property `security.userid.input.mode`. The following options are supported:

 - lowercase - userid input will be automatically lowercased
 - uppercase - userid input will be automatically uppercased
 - none - userid input will not be changed

You can change this setting by the imixs.properties or in the environment variables (take care about the unix style for environment variable names!)


### Unique E-Mail Address

Per default a E-Mailaddress in the Imixs Profiles need to be unique. This feature can be disabled by the Imixs property `security.email.unique` by setting its value to `false`.

### Profile Image Width

User can set a profile image in Imixs-Office-Workflow. The width of the image is automatically set to a max with of 600px. This can be changed by the imixs property `profile.image.maxwith`

