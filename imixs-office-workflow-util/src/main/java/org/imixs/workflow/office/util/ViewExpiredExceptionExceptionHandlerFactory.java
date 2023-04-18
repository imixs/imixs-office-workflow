package org.imixs.workflow.office.util;

import jakarta.faces.context.ExceptionHandler;
import jakarta.faces.context.ExceptionHandlerFactory;

/**
 * This class is used to handle expired sessions. In case a session was expired the 
 * handler caught the ViewExpiredException and redirects into a new page.
 *
 * This class expects a jsf page called 'sessionexpired.xhtml' in the web root
 * context!
 * 
 * 
 * @see ed burns ' dealing_gracefully_with_viewexpiredexception'
 * 
 *      https://www.nofluffjuststuff.com/blog/ed_burns/2009/09/
 *      dealing_gracefully_with_viewexpiredexception_in_jsf2
 * 
 * @author rsoika
 * 
 */
public class ViewExpiredExceptionExceptionHandlerFactory extends
		ExceptionHandlerFactory {
	private ExceptionHandlerFactory parent;

	public ViewExpiredExceptionExceptionHandlerFactory(
			ExceptionHandlerFactory parent) {
		this.parent = parent;
	}

	@Override
	public ExceptionHandler getExceptionHandler() {
		ExceptionHandler result = parent.getExceptionHandler();
		result = new ViewExpiredExceptionExceptionHandler(result);

		return result;
	}
}
