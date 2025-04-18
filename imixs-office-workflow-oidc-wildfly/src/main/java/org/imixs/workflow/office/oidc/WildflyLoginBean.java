package org.imixs.workflow.office.oidc;

import java.io.Serializable;
import java.security.Principal;
import java.util.logging.Logger;

import org.imixs.marty.profile.ProfileEvent;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.exceptions.AccessDeniedException;
import org.wildfly.security.http.oidc.IDToken;
import org.wildfly.security.http.oidc.OidcSecurityContext;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.event.Observes;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * The WildflyLoginBean implements an observer pattern for the Imixs-Marty
 * Profile event and completes profile information from the Wildfly OIDC module
 * 
 * To activate this mechanism add the following imixs property:
 * 
 * profile.login.event=10
 * 
 * 
 * 
 */
@RequestScoped
@Path("/oidc")
@Produces({ MediaType.TEXT_PLAIN })

public class WildflyLoginBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(WildflyLoginBean.class.getName());

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
            logger.info("Imixs-Security-OIDC - collecting context information... (see details in server log)");

            System.out.println("=========================================");
            if (principal != null) {
                System.out.println("  Principal name: " + principal.getName());
            } else {
                System.out.println("  Principal resolved to null!");
            }
            // Here's the unique subject identifier within the issuer
            FacesContext facesContext = FacesContext.getCurrentInstance();
            HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
            if (request == null) {
                message = "Failed to resolve OpenIdContext!";
            } else {
                OidcSecurityContext oidcSecurityContext = (OidcSecurityContext) request
                        .getAttribute(OidcSecurityContext.class.getName());
                IDToken idToken = oidcSecurityContext.getIDToken();

                if (idToken == null) {
                    message = " unable to resolve IDToken!";

                } else {

                    System.out.println("  Subject = " + idToken.getSubject());
                    System.out.println("  Access token = " + idToken.getAccessTokenHash());
                    System.out.println("  ID token = " + idToken.toString());
                    System.out.println("  Claims json = " + idToken.getClaimNames());
                    System.out.println("=========================================");
                    message = "Imixs-Security-OIDC ==> OK \n" + //
                            "User Principal      ==> " + principal.getName()
                            + "\n\nSession details are available on server log";
                }
            }
        } catch (Exception e) {
            message = "Failed to resolve OpenIdContext!";
            logger.warning(message);
            logger.warning(e.toString());
        }
        return message;
    }

    /**
     * ProfileEvent listener to update a new profile with the user attributes
     * provided by the OpenID provider.
     * 
     * @param workflowEvent
     * @throws AccessDeniedException
     */
    public void onProfileEvent(@Observes ProfileEvent profileEvent) throws AccessDeniedException {

        int eventType = profileEvent.getEventType();
        ItemCollection profile = profileEvent.getProfile();
        if (profile == null) {
            return;
        }

        if (ProfileEvent.ON_PROFILE_CREATE == eventType || ProfileEvent.ON_PROFILE_LOGIN == eventType) {
            logger.info("├── Processing Wildfly OIDC login...");
            FacesContext facesContext = FacesContext.getCurrentInstance();
            HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
            if (request == null) {
                logger.warning("│   ├── unable to resolve http request!");
                return;
            }

            // org.wildfly.security.http.oidc.
            OidcSecurityContext oidcSecurityContext = (OidcSecurityContext) request
                    .getAttribute(OidcSecurityContext.class.getName());
            IDToken idToken = oidcSecurityContext.getIDToken();

            if (idToken == null) {
                logger.warning("│   ├── unable to resolve IDToken!");
                return;
            }

            String userName = "" + idToken.getName();
            String email = "" + idToken.getEmail();
            logger.info("│   ├── PreferredUsername=" + userName);
            logger.info("│   ├── Email=" + email);

            if (!email.equals(profile.getItemValueString("txtemail"))
                    || !userName.equals(profile.getItemValueString("txtusername"))) {
                logger.info("│   ├── update profile data...");
                profile.setItemValue("txtemail", email);
                profile.setItemValue("txtusername", userName);
            } else {
                logger.info("│   ├── profile already up-to-date.");
            }
        }

    }
}
