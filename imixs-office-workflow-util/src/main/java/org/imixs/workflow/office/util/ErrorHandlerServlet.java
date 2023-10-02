package org.imixs.workflow.office.util;

import java.io.IOException;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This Servlet is used to catch servlet exceptions and redirect the user to
 * user-friendly page showing the exception details. The servlet simply fills
 * the ErrorHandlerData object and redirects to the errorhandler.xhtml page.
 * 
 * The servlet is mapped in the web.xml file:
 * 
 * <pre>{@code
 * <error-page> 
 *       <exception-type>java.lang.Exception</exception-type> 
 *       <location>/errorHandler</location> 
 * </error-page>
 * }</pre>
 * 
 * 
 * 
 * See also: https://www.baeldung.com/servlet-exceptions
 * https://www.digitalocean.com/community/tutorials/servlet-exception-and-error-handling-example-tutorial
 * 
 */
@WebServlet(urlPatterns = "/errorHandler")
public class ErrorHandlerServlet extends HttpServlet {

    @Inject
    private ErrorHandlerData errorHandlerData;

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        processError(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        processError(request, response);
    }

    private void processError(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        // Analyze the servlet exception
        Throwable throwable = (Throwable) request
                .getAttribute("jakarta.servlet.error.exception");
        Integer statusCode = (Integer) request
                .getAttribute("jakarta.servlet.error.status_code");
        String servletName = (String) request
                .getAttribute("jakarta.servlet.error.servlet_name");
        if (servletName == null) {
            servletName = "Unknown";
        }
        String requestUri = (String) request
                .getAttribute("jakarta.servlet.error.request_uri");
        if (requestUri == null) {
            requestUri = "Unknown";
        }

        // try {
        if (throwable != null) {
            errorHandlerData.setMessage(throwable.getMessage());
            errorHandlerData.setException(throwable.getClass().getName());
        } else {
            errorHandlerData.setMessage("Unknown");

        }
        errorHandlerData.setUri(requestUri);
        errorHandlerData.setStatusCode(statusCode);

        response.sendRedirect(request.getContextPath() + "/errorhandler.xhtml");

    }

}