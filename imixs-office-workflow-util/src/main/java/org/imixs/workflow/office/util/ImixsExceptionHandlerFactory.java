package org.imixs.workflow.office.util;

import jakarta.faces.context.ExceptionHandler;
import jakarta.faces.context.ExceptionHandlerFactory;

/**
 * This is the custom exception handler factory class that is responsible for
 * creating the instances of the ImixsExceptionHandler class.
 * 
 * The factory need to be declared in the faces-config.xml file
 * 
 * <pre>{@code
 *   <factory>
      <exception-handler-factory>
        org.imixs.workflow.office.util.ImixsExceptionHandlerFactory
      </exception-handler-factory>
    </factory>
 * }</pre>
 */
public class ImixsExceptionHandlerFactory extends ExceptionHandlerFactory {

    public ImixsExceptionHandlerFactory(ExceptionHandlerFactory wrapped) {
        super(wrapped);
    }

    @Override
    public ExceptionHandler getExceptionHandler() {
        ExceptionHandler parentHandler = getWrapped().getExceptionHandler();
        return new ImixsExceptionHandler(parentHandler);
    }

}