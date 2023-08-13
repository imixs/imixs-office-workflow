package org.imixs.workflow.office.auth;

import java.io.Serializable;
import java.security.Principal;
import java.util.logging.Logger;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.security.enterprise.authentication.mechanism.http.OpenIdAuthenticationMechanismDefinition;
import jakarta.security.enterprise.identitystore.openid.OpenIdContext;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * This class implements a OpenIdAuthenticationMechanismDefinition for
 * Authentication against an OpenID Provider (e.g. Keycloak).
 * is an OpenID Connect Default implementation
 * 
 * See also:
 * 
 * - https://auth0.com/blog/jakarta-ee-oidc/
 * - https://blogs.nologin.es/rickyepoderi/
 * 
 */
@RequestScoped
@Path("/oidc")
@Produces({ MediaType.TEXT_PLAIN })
@OpenIdAuthenticationMechanismDefinition( //
        clientId = "${oidcConfig.clientId}", //
        clientSecret = "${oidcConfig.clientSecret}", //
        redirectURI = "${baseURL}/callback", //
        providerURI = "${oidcConfig.issuerUri}" //
)
public class Securitybean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(Securitybean.class.getName());

    @Inject
    Principal principal;

    @Inject
    private OpenIdContext context;

    /**
     * Debug endpoint prints session details into the server log
     * 
     * @return
     */
    @GET
    @Produces("text/plain")
    public String sessionInfoAuth() {
        String message = "";
        try {
            logger.info("Imixs-Security-OIDC - collecting context information... (see details in server log)");

            System.out.println("=========================================");
            if (principal != null) {
                System.out.println("  Principal name: " + principal.getName());
            } else {
                System.out.println("  Principal resolved to null!");
            }
            // Here's the unique subject identifier within the issuer
            if (context == null) {
                message = "Failed to resolve OpenIdContext!";
            } else {
                System.out.println("  Subject = " + context.getSubject());
                System.out.println("  Access token = " + context.getAccessToken());
                System.out.println("  ID token = " + context.getIdentityToken());
                System.out.println("  Claims json = " + context.getClaimsJson());
                System.out.println("=========================================");
                message = "Imixs-Security-OIDC ==> OK \n" + //
                        "User Principal      ==> " + principal.getName()
                        + "\n\nSession details are available on server log";
            }
        } catch (Exception e) {
            message = "Failed to resolve OpenIdContext!";
            logger.warning(message);
            logger.warning(e.toString());
        }
        return message;
    }

}
