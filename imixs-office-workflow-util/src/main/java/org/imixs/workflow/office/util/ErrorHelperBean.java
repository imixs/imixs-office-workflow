package org.imixs.workflow.office.util;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;

/**
 * CDI Bean for handling and extracting meaningful error information from
 * servlet exceptions.
 * 
 * <p>
 * This bean provides utilities for JSF error pages to display user-friendly
 * error messages
 * and detailed debugging information. It automatically traverses exception
 * chains to find
 * the most relevant error messages, prioritizing business exceptions over
 * technical wrapper exceptions.
 * </p>
 * 
 * <p>
 * <strong>Usage in JSF pages:</strong>
 * </p>
 * 
 * <pre>
 * &lt;h:outputText value="#{errorHelperBean.rootCauseMessage}" /&gt;
 * &lt;h:outputText value="#{errorHelperBean.fullStackTrace}" /&gt;
 * </pre>
 * 
 * <p>
 * <strong>Example Exception Chain Handling:</strong>
 * </p>
 * 
 * <pre>
 * ObserverException
 *   └─ Caused by: SomeWrapperException  
 *       └─ Caused by: ModelException: "Invalid expression: #{!controller.method()}"
 * 
 * Result: Returns "Invalid expression: #{!controller.method()}" as the root cause message
 * </pre>
 * 
 * @author rsoika
 * @since 1.0
 * @see jakarta.servlet.http.HttpServletRequest
 * @see jakarta.faces.context.FacesContext
 */
@Named
@RequestScoped
public class ErrorHelperBean {

    /**
     * Retrieves the root cause message from the exception stack
     */
    public String getRootCauseMessage() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context == null) {
            return null;
        }

        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        Throwable exception = (Throwable) request.getAttribute("jakarta.servlet.error.exception");

        if (exception == null) {
            return null;
        }

        return findRootCauseMessage(exception);
    }

    /**
     * Recursively searches for the deepest exception with a meaningful message
     */
    private String findRootCauseMessage(Throwable throwable) {
        if (throwable == null) {
            return null;
        }

        // Traverse the exception stack from bottom to top
        Throwable rootCause = getRootCause(throwable);

        // Work from root cause upwards to find the best message
        Throwable current = rootCause;
        String bestMessage = null;

        while (current != null) {
            String message = current.getMessage();
            if (message != null && !message.trim().isEmpty()) {
                // Prioritize ModelException or other business exceptions
                if (current.getClass().getSimpleName().contains("ModelException") ||
                        current.getClass().getSimpleName().contains("BusinessException") ||
                        current.getClass().getSimpleName().contains("ValidationException")) {
                    return message;
                }
                bestMessage = message;
            }

            // Move one level up
            Throwable parent = findParent(throwable, current);
            current = parent;
        }

        return bestMessage != null ? bestMessage : "Unknown error";
    }

    private Throwable getRootCause(Throwable throwable) {
        Throwable cause = throwable;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause;
    }

    private Throwable findParent(Throwable root, Throwable target) {
        if (root == target) {
            return null;
        }

        Throwable current = root;
        while (current != null) {
            if (current.getCause() == target) {
                return current;
            }
            current = current.getCause();
        }
        return null;
    }

    /**
     * Returns the exception type of the root cause
     */
    public String getRootCauseType() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context == null) {
            return null;
        }

        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        Throwable exception = (Throwable) request.getAttribute("jakarta.servlet.error.exception");

        if (exception == null) {
            return null;
        }

        Throwable rootCause = getRootCause(exception);
        return rootCause.getClass().getSimpleName();
    }

    /**
     * Returns the full stack trace as formatted string
     */
    public String getFullStackTrace() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context == null) {
            return null;
        }

        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        Throwable exception = (Throwable) request.getAttribute("jakarta.servlet.error.exception");

        if (exception == null) {
            return null;
        }

        return getStackTraceAsString(exception);
    }

    /**
     * Returns a detailed exception chain with all causes
     */
    public String getExceptionChain() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context == null) {
            return null;
        }

        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        Throwable exception = (Throwable) request.getAttribute("jakarta.servlet.error.exception");

        if (exception == null) {
            return null;
        }

        StringBuilder chain = new StringBuilder();
        Throwable current = exception;
        int level = 0;

        while (current != null) {
            chain.append("Level ").append(level).append(": ")
                    .append(current.getClass().getName());

            if (current.getMessage() != null) {
                chain.append("\n  Message: ").append(current.getMessage());
            }

            chain.append("\n  Location: ");
            StackTraceElement[] elements = current.getStackTrace();
            if (elements.length > 0) {
                StackTraceElement firstElement = elements[0];
                chain.append(firstElement.getClassName())
                        .append(".").append(firstElement.getMethodName())
                        .append("(").append(firstElement.getFileName())
                        .append(":").append(firstElement.getLineNumber()).append(")");
            }

            chain.append("\n\n");
            current = current.getCause();
            level++;
        }

        return chain.toString();
    }

    /**
     * Gets additional servlet error attributes for debugging
     */
    public String getServletErrorInfo() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context == null) {
            return null;
        }

        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        StringBuilder info = new StringBuilder();

        // Collect all servlet error attributes
        String requestUri = (String) request.getAttribute("jakarta.servlet.error.request_uri");
        String servletName = (String) request.getAttribute("jakarta.servlet.error.servlet_name");
        Integer statusCode = (Integer) request.getAttribute("jakarta.servlet.error.status_code");
        String message = (String) request.getAttribute("jakarta.servlet.error.message");

        if (requestUri != null) {
            info.append("Request URI: ").append(requestUri).append("\n");
        }
        if (servletName != null) {
            info.append("Servlet Name: ").append(servletName).append("\n");
        }
        if (statusCode != null) {
            info.append("Status Code: ").append(statusCode).append("\n");
        }
        if (message != null) {
            info.append("Error Message: ").append(message).append("\n");
        }

        info.append("Timestamp: ").append(new java.util.Date()).append("\n");
        info.append("Session ID: ").append(request.getSession().getId()).append("\n");
        info.append("Remote Address: ").append(request.getRemoteAddr()).append("\n");
        info.append("User Agent: ").append(request.getHeader("User-Agent")).append("\n");

        return info.toString();
    }

    /**
     * Helper method to convert stack trace to string
     */
    private String getStackTraceAsString(Throwable throwable) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }
}
