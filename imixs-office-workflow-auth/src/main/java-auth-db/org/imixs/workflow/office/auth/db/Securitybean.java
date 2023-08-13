package org.imixs.workflow.office.auth.oidc;

import java.io.Serializable;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.security.enterprise.authentication.mechanism.http.FormAuthenticationMechanismDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.LoginToContinue;

/**
 * This class implements a form based authentication mechanism.
 * 
 * See also:
 * 
 * - https://www.baeldung.com/java-ee-8-security
 * 
 */
@FormAuthenticationMechanismDefinition( //
        loginToContinue = @LoginToContinue( //
                loginPage = "/login.html", //
                errorPage = "/loginerror.html") //
)
@ApplicationScoped
public class Securitybean implements Serializable {

}
