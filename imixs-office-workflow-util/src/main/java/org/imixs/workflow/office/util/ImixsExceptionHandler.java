package org.imixs.workflow.office.util;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

import org.imixs.workflow.exceptions.WorkflowException;

import jakarta.ejb.EJBException;
import jakarta.el.ELException;
import jakarta.enterprise.context.ContextException;
import jakarta.enterprise.context.NonexistentConversationException;
import jakarta.faces.FacesException;
import jakarta.faces.application.Application;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.application.ViewHandler;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.ExceptionHandler;
import jakarta.faces.context.ExceptionHandlerWrapper;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ExceptionQueuedEvent;
import jakarta.faces.view.ViewDeclarationLanguage;
import jakarta.servlet.RequestDispatcher;

/**
 * The ImixsExceptionHandler handles Imixs WorkflowExceptions as also EJB
 * Exceptions.
 * In base of a Imixs WorkflowException the ExceptionHandler adds a new
 * FacesMessage with the root cause of the problem. This kind of message
 * can be simply displayed with a
 * {@code<h:messages id="globalMessages" globalOnly="true" />} tag.
 * <p>
 * In case of an EJB Exception this ExceptionHandler redirects to the page
 * <code>error.xhml</code> page to display a user-friendly version of
 * those exceptions.
 * <p>
 * See also the book ' The Definitive Guide to JSF in Java EE' by Bauke Scholtz
 * and Arjan Tijms:
 * 
 * https://link.springer.com/book/10.1007/978-1-4842-3387-0
 * 
 */
public class ImixsExceptionHandler extends ExceptionHandlerWrapper {

    private static Logger logger = Logger.getLogger(ImixsExceptionHandler.class.getName());

    public ImixsExceptionHandler(ExceptionHandler wrapped) {
        super(wrapped);
    }

    @Override
    public void handle() {
        handleImixsWorkflowException(FacesContext.getCurrentInstance());
        getWrapped().handle();
    }

    /**
     * This method check if the root cause is an instance of Imixs
     * WorkflowException, and if so, it will add a faces message
     * 
     * @param context
     */
    public void handleImixsWorkflowException(FacesContext context) {

        Iterator<ExceptionQueuedEvent> unhandledExceptionQueuedEvents = getUnhandledExceptionQueuedEvents().iterator();
        if (context == null
                || !unhandledExceptionQueuedEvents.hasNext()) {
            return;
        }

        // Check if this is a Imixs Exception type....
        Throwable exception = unhandledExceptionQueuedEvents.next().getContext().getException();
        while (exception.getCause() != null
                && (exception instanceof FacesException
                        || exception instanceof ELException
                        || exception instanceof NonexistentConversationException)) {
            exception = exception.getCause();
        }

        // We are only interested in Imixs WorkflowExceptions and general EJBExceptions
        if (!(exception instanceof WorkflowException)
                && !(exception instanceof EJBException)
                && !(exception instanceof ContextException)) {
            // no - return to default behavior.
            return;
        }

        // in case of an EJB Exception we resolve the root cause to get the origin error
        // message and and redirect to error.xhtml.
        if (exception instanceof EJBException || exception instanceof NonexistentConversationException) {
            Throwable rootCause = findCauseUsingPlainJava(exception);
            redirectErrorPage(context, exception, rootCause);
        } else {
            // normal case add a new FacesMessage only
            createFacesMessage(context, exception);
        }

        // finally remove unhandled queued events...
        unhandledExceptionQueuedEvents.remove();
        while (unhandledExceptionQueuedEvents.hasNext()) {
            unhandledExceptionQueuedEvents.next();
            unhandledExceptionQueuedEvents.remove();
        }
    }

    /**
     * This helper method creates a new facesMessage and adds the exception meta
     * data into the FacesContext attributes.
     * 
     * @param context
     * @param exceptionMessage
     * @param exceptionName
     * @param exceptionType
     */
    private void createFacesMessage(FacesContext context, Throwable exception) {
        String exceptionType = exception.getClass().getSimpleName();
        String exceptionName = exception.getClass().getName();
        String exceptionMessage = exception.getMessage();

        // We put the exception context in the current faces context. This data can be
        // read by the error page later.
        context.getAttributes().put("exceptionMessage", exceptionMessage);
        context.getAttributes().put("exceptionName", exceptionName);
        context.getAttributes().put("exceptionType", exceptionType);

        // Build Context
        context.addMessage(null, new FacesMessage(
                FacesMessage.SEVERITY_FATAL,
                exception.toString(),
                exceptionMessage));
        context.validationFailed();

        // add a new view context
        context.getPartialViewContext()
                .getRenderIds().add("globalMessages");
    }

    /**
     * This helper method redirects to error.xhtml page
     * <p>
     * the #{requestScope['jakarta.servlet.error.request_uri']} and
     * #{requestScope['jakarta.servlet.error.exception']} variables will be set so
     * that the error page can access them. Also the UIViewRoot instance
     * representing the error page will be created with the help of the ViewHandler
     * and set in the faces context. If necessary, we can conditionally prepare the
     * view ID of the error page based on the actual root cause of the exception.
     * 
     * @param context
     * @param exception
     */
    private void redirectErrorPage(FacesContext context, Throwable exception, Throwable rootCause) {
        String exceptionMessage = rootCause.getMessage();
        String exceptionName = rootCause.getClass().getName();
        String exceptionType = rootCause.getClass().getSimpleName();

        ExternalContext external = context.getExternalContext();
        String uri = external.getRequestContextPath()
                + external.getRequestServletPath();
        Map<String, Object> requestScope = external.getRequestMap();
        requestScope.put(RequestDispatcher.ERROR_REQUEST_URI, uri);
        requestScope.put(RequestDispatcher.ERROR_EXCEPTION, exception);
        logger.info("redirectErrorPage for exception type: " + exceptionType);
        String viewId = "/error.xhtml";
        if ("OptimisticLockException".equals(exceptionType)) {
            viewId = "/error_optimisticlock.xhtml";
        }
        if ("NonexistentConversationException".equals(exceptionType)) {
            viewId = "/error_conversationexpired.xhtml";
        }

        Application application = context.getApplication();
        ViewHandler viewHandler = application.getViewHandler();
        UIViewRoot viewRoot = viewHandler.createView(context, viewId);
        context.setViewRoot(viewRoot);
        try {
            external.responseReset();
            ViewDeclarationLanguage viewDeclarationLanguage = viewHandler.getViewDeclarationLanguage(context, viewId);
            viewDeclarationLanguage.buildView(context, viewRoot);
            context.getPartialViewContext().setRenderAll(true);
            viewDeclarationLanguage.renderView(context, viewRoot);
            context.responseComplete();
        } catch (IOException e) {
            throw new FacesException(e);
        } finally {
            requestScope.remove(RequestDispatcher.ERROR_EXCEPTION);
        }
    }

    /**
     * Helper method to find the exception root cause.
     * 
     * See: https://www.baeldung.com/java-exception-root-cause
     */
    private static Throwable findCauseUsingPlainJava(Throwable throwable) {
        Objects.requireNonNull(throwable);
        Throwable rootCause = throwable;
        while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
            rootCause = rootCause.getCause();
        }
        return rootCause;
    }

}
