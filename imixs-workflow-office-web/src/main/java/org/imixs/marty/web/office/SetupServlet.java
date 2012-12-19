package org.imixs.marty.web.office;

import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.imixs.office.ejb.security.UserGroupService;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.jee.ejb.WorkflowService;

/**
 * This servlet checks configuration and scheduler on startup. The servlet is
 * configured in the web.xml with the option load-on-startup=1 which means that
 * the servlet init() method is triggered after deployment.
 * 
 * @author rsoika
 * 
 */
public class SetupServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger("org.imixs.office");

	@EJB
	org.imixs.marty.ejb.WorkflowSchedulerService workflowSchedulerService;

	@EJB
	UserGroupService userGroupService;

	@Override
	public void init() throws ServletException {

		super.init();
		logger.info("Imixs Office Workflow - setup...");

		// init userIDs for user db
		try {
			if (userGroupService != null) {
				
				userGroupService.initUserIDs();
			}

		} catch (Exception e) {
			logger.warning("SetupServlet - unable to initUserIds "
					+ e.getMessage());
		}

		// try to start the scheduler service
		try {
			ItemCollection configItemCollection = workflowSchedulerService
					.findConfiguration();

			if (configItemCollection.getItemValueBoolean("_enabled"))
				workflowSchedulerService.start();
		} catch (Exception e) {
			logger.warning("SetupServlet - unable to start scheduler service "
					+ e.getMessage());
		}

	}

}