package org.imixs.workflow.office.auth.rest;

import java.io.Serializable;
import java.security.Principal;
import java.util.logging.Logger;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

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
@Path("/auth")
@Produces({ MediaType.TEXT_PLAIN })
public class Securitystatus implements Serializable {

    private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(Securitystatus.class.getName());

    @Inject
    Principal principal;

   
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
            logger.info("Imixs-Security-Status - collecting context information... (see details in server log)");

            System.out.println("=========================================");
            if (principal != null) {
                message="  Principal name: " + principal.getName();
            } else {
                message="  Principal resolved to null!";
            }
          
        } catch (Exception e) {
            message = "Failed to resolve security status!";
            logger.warning(message);
            logger.warning(e.toString());
        }
        return message;
    }

}
