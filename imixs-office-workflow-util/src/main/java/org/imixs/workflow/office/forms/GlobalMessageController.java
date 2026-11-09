package org.imixs.workflow.office.forms;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UINamingContainer;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

/**
 * Provides access to global FacesMessages (messages not bound to a component).
 * <p>
 * This is needed because EL coerces a null argument to an empty String, so
 * facesContext.getMessageList(null) cannot be called directly from a page.
 */
@Named
@RequestScoped
public class GlobalMessageController implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Returns all messages without a client id (global messages).
     * <p>
     * Each message is marked as rendered, because the page displays them via
     * ui:repeat instead of h:messages. Otherwise the runtime logs the warning
     * "FacesMessage(s) have been enqueued, but may not have been displayed".
     *
     * @return list of global messages, never null
     */
    public List<FacesMessage> getMessages() {
        List<FacesMessage> messages = FacesContext.getCurrentInstance().getMessageList(null);
        for (FacesMessage msg : messages) {
            msg.rendered();
        }
        return messages;
    }

    /**
     * Returns all messages of components located inside the naming container
     * with the given id (e.g. a f:subview wrapping a custom part). The inner
     * structure of the container does not matter.
     *
     * @param containerId the (non-qualified) id of the naming container
     * @return list of messages, never null
     */
    public List<FacesMessage> getMessagesInside(String containerId) {
        FacesContext context = FacesContext.getCurrentInstance();
        char sep = UINamingContainer.getSeparatorChar(context);
        // e.g. ":field_0_5:" - matches all client ids below this container
        String marker = sep + containerId + sep;
        List<FacesMessage> result = new ArrayList<>();
        Iterator<String> clientIds = context.getClientIdsWithMessages();
        while (clientIds.hasNext()) {
            String clientId = clientIds.next();
            // global messages have no client id
            if (clientId != null && (sep + clientId).contains(marker)) {
                for (FacesMessage msg : context.getMessageList(clientId)) {
                    // mark as displayed to avoid the 'not displayed' warning
                    msg.rendered();
                    result.add(msg);
                }
            }
        }
        return result;
    }
}