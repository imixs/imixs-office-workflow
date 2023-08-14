# LDAP Security Domain

You can switch form the default Database Security Domain to a LDAP realm by changing the the corresponding security domain  in the wildfly `standalone.xml` file using the Wildfly Elytron Framework. The concrete configuration depends on your LDAP server. See the following example. 


```xml 
    <subsystem xmlns="urn:wildfly:elytron:16.0" final-providers="combined-providers" disallowed-providers="OracleUcrypto">
        ....
        ........
        <security-domains>
            ...
            ......
            <!--Imixs domain   -->
            <security-domain name="imixsrealm" default-realm="cached-ldap" permission-mapper="default-permission-mapper">
                <realm name="cached-ldap"/>
            </security-domain>	
        </security-domains>
        <security-realms>
            ...
            <!-- LDAP realm -->
            <ldap-realm name="ldap-realm" dir-context="ldap-connection" direct-verification="true">
                <identity-mapping rdn-identifier="sAMAccountName" use-recursive-search="false" search-base-dn="${env.LDAP_SEARCH_BASE_DN}" >
                    <attribute-mapping>
                        <attribute from="CN" to="Roles" filter="(member={1})" filter-base-dn="${env.LDAP_FILTER_BASE_DN}"/>
                    </attribute-mapping>
                </identity-mapping>
            </ldap-realm>
            <caching-realm  name="cached-ldap" realm="ldap-realm"/>					       
        </security-realms>

        <!-- LDAP Dir Contexts -->
        <dir-contexts>
            <dir-context name="ldap-connection" url="${env.LDAP_CONNECTION}" principal="${env.LDAP_PRINCIPAL}">
                <credential-reference clear-text="${env.LDAP_PASSWORD}"/>
            </dir-context>
        </dir-contexts>                
    ...
```


For debugging it can be helpfull to setup up the log level of the wildfly security module within the logging section of the wildfly `standalone.xml` file:

```xml 
    ...
        <subsystem xmlns="urn:jboss:domain:logging:8.0">
            <console-handler name="CONSOLE">
                <level name="DEBUG"/>
            ....
            .....
            <!-- LDAP Logging -->
            <logger category="org.wildfly.security">
                <level name="DEBUG"/>
            </logger>
    ...
```


**Note:** You can customize the settings to you own needs and you can of course adapt the security to any other Jakarta EE application server. 


## LDAP Configuration

For additional configuraition details you can create a `ldap.properties` files

```
ldap.jndi-name=
ldap.disable-jndi=true
ldap.search-context=DC=intern,DC=foo,DC=de
ldap.dn-search-filter=(sAMAccountName=%u)
ldap.search-filter-phrase=(& (objectClass=user) (mail=*) (|(name=?*)(mail=?*)(cn=?*)(sn=?*)))
ldap.user-attributes=sAMAccountName|txtname,mail|txtemail,name|txtUserName,initials|txtInitials
ldap.group-search-filter=(member=%d)
# Cache (1h)
ldap.cache-expires=3600000
ldap.cache-size=100
# JNDI Provider
java.naming.provider.url=ldap://foo-ldap:389
java.naming.security.principal=CN=bind_user,CN=users,DC=intern,DC=foo,DC=de
java.naming.security.credentials=xxxx
```

This additional configuration file can be mapped directly into the wildfly server using a Docker volume:

```
      # Custom LDAP Properties
      - ./ldap.properties:/opt/jboss/wildfly/ldap.properties
```


Find for information here: 

 - https://www.imixs.org/marty/concepts/ldap.html



See also [Database Security Domain](db.html)


