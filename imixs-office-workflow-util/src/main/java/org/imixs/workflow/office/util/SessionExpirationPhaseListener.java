package org.imixs.workflow.office.util;

import java.util.logging.Logger;

import jakarta.faces.application.ConfigurableNavigationHandler;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PhaseEvent;
import jakarta.faces.event.PhaseId;
import jakarta.faces.event.PhaseListener;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This helper Class is used to detect expired sessions during a ajax request.
 * See also here: https://developer.jboss.org/docs/DOC-48457
 * 
 * @author rsoika
 *
 */
public class SessionExpirationPhaseListener implements PhaseListener {

    private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(SessionExpirationPhaseListener.class.getName());


    @Override
    public PhaseId getPhaseId() {
        return PhaseId.RESTORE_VIEW;
    }

    @Override
    public void beforePhase(PhaseEvent event) {
    }

    /**
     * react on ajax request only
     */
    @Override
    public void afterPhase(PhaseEvent event) {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest httpRequest = (HttpServletRequest) context.getExternalContext().getRequest();
        if (httpRequest.getRequestedSessionId() != null && !httpRequest.isRequestedSessionIdValid()) {
            String facesRequestHeader = httpRequest.getHeader("Faces-Request");
            boolean isAjaxRequest = facesRequestHeader != null && facesRequestHeader.equals("partial/ajax");
            
            // navigate to sessionexpired page only for ajax requests
            if (isAjaxRequest) {
                logger.finest("...ajax session expired!");
                ConfigurableNavigationHandler handler = (ConfigurableNavigationHandler) context.getApplication().getNavigationHandler();
                handler.performNavigation("sessionexpired");
            }
        }
    }
}