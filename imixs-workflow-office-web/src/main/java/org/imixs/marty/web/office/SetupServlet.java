package org.imixs.marty.web.office;

import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.imixs.office.ejb.security.UserGroupService;

/**
 * This servlet checks configuration on startup. The servlet is configured in
 * the web.xml with the option load-on-startup=1 which means that the servlet
 * init() method is triggered after deployment.
 * 
 * @author rsoika
 * 
 */
public class SetupServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger("org.imixs.office");

	@EJB
	UserGroupService userGroupService;

	@Override
	public void init() throws ServletException {

		super.init();

		logger.info("Imixs Office Workflow - setup...");
		userGroupService.initUserIDs();

	}

}