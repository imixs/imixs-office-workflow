# Database Security Domain

The default security setup of Imixs-Office-Workflow provides a build-in Userdatabase and a form-based authentication. The corresponding security domain is configured in the wildfly `standalone.xml` file using the Wildfly Elytron Framework.


```xml 
    <subsystem xmlns="urn:wildfly:elytron:16.0" final-providers="combined-providers" disallowed-providers="OracleUcrypto">
        ....
        ........
        <security-domains>
            ....
            ........
            <security-domain name="imixsrealm" default-realm="imixsrealm" permission-mapper="default-permission-mapper">
                    <realm name="imixsrealm"/>
            </security-domain>
        </security-domains>
        <security-realms>
            ....
            .......
            
            <!-- Imixs -->
            <jdbc-realm name="imixsrealm">
                <principal-query sql="select PASSWORD from USERID where ID=? AND PASSWORD IS NOT NULL" data-source="office">
                    <simple-digest-mapper algorithm="simple-digest-sha-256" password-index="1" hash-encoding="hex"/>
                </principal-query>
                <principal-query sql="select GROUP_ID from USERID_USERGROUP where ID=?" data-source="office">
                    <attribute-mapping>
                        <attribute to="Roles" index="1"/>
                    </attribute-mapping>
                </principal-query>
            </jdbc-realm>
        </security-realms>
    ...
```

**Note:** You can customize the settings to you own needs and you can of course adapt the security to any other Jakarta EE application server. 

See also [LDAP Security Domain](ldap.html)

