package org.imixs.workflow.office.util;

import java.util.Iterator;
import java.util.Objects;

import jakarta.faces.FacesException;
import jakarta.faces.application.NavigationHandler;
import jakarta.faces.context.ExceptionHandler;
import jakarta.faces.context.ExceptionHandlerWrapper;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.Flash;
import jakarta.faces.event.ExceptionQueuedEvent;
import jakarta.faces.event.ExceptionQueuedEventContext;
import jakarta.servlet.http.HttpServletRequest;

public class ImixsExceptionHandler extends ExceptionHandlerWrapper {

  public ImixsExceptionHandler(ExceptionHandler wrapped) {
    super(wrapped);
  }

  @Override
  public void handle() throws FacesException {
    Iterator iterator = getUnhandledExceptionQueuedEvents().iterator();

    while (iterator.hasNext()) {
      ExceptionQueuedEvent event = (ExceptionQueuedEvent) iterator.next();
      ExceptionQueuedEventContext context = (ExceptionQueuedEventContext) event.getSource();

      Throwable throwable = context.getException();

      throwable = findCauseUsingPlainJava(throwable);

      FacesContext fc = FacesContext.getCurrentInstance();

      try {
        Flash flash = fc.getExternalContext().getFlash();

        // Put the exception in the flash scope to be displayed in the error
        // page if necessary ...
        flash.put("message", throwable.getMessage());

        ExternalContext externalContext = fc.getExternalContext();

        flash.put("type", throwable.getClass().getSimpleName());
        flash.put("exception", throwable.getClass().getName());
        if (externalContext != null) {
          HttpServletRequest hrequest = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext()
              .getRequest();
          String uri = hrequest.getRequestURI();
          flash.put("uri", uri);
        }

        NavigationHandler navigationHandler = fc.getApplication().getNavigationHandler();
        navigationHandler.handleNavigation(fc, null, "/errorhandler.xhtml?faces-redirect=true");
        fc.renderResponse();
      } finally {
        iterator.remove();
      }
    }

    // Let the parent handle the rest
    getWrapped().handle();
  }

  /**
   * Helper method to find the exception root cause.
   * 
   * See: https://www.baeldung.com/java-exception-root-cause
   */
  public static Throwable findCauseUsingPlainJava(Throwable throwable) {
    Objects.requireNonNull(throwable);
    Throwable rootCause = throwable;
    while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
      System.out.println("cause: " + rootCause.getCause().getMessage());
      rootCause = rootCause.getCause();
    }
    return rootCause;
  }

}