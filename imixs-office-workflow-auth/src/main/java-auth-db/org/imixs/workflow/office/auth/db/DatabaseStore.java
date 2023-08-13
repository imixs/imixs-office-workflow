package org.imixs.workflow.office.auth.db;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.security.enterprise.identitystore.DatabaseIdentityStoreDefinition;
import jakarta.security.enterprise.identitystore.PasswordHash;

/**
 * 
 * 
 * <jdbc-realm name="imixsrealm">
 * <principal-query sql="select PASSWORD from USERID where ID=? AND PASSWORD IS
 * NOT NULL" data-source="office">
 * <simple-digest-mapper algorithm="simple-digest-sha-256" password-index="1"
 * hash-encoding="hex"/>
 * </principal-query>
 * <principal-query sql="select GROUP_ID from USERID_USERGROUP where ID=?"
 * data-source="office">
 * <attribute-mapping>
 * <attribute to="Roles" index="1"/>
 * </attribute-mapping>
 * </principal-query>
 * </jdbc-realm>
 * 
 */
@DatabaseIdentityStoreDefinition( //
        dataSourceLookup = "java:comp/env/jdbc/securityDS", //
        callerQuery = "select PASSWORD from USERID where ID=? AND PASSWORD IS NOT NULL", //
        groupsQuery = "select GROUP_ID from USERID_USERGROUP where ID=?", //
        priority = 30, //
        hashAlgorithm = PasswordHash.class)
@ApplicationScoped
public class DatabaseStore {
}
