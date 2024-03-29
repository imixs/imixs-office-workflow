package org.imixs.workflow.office.views;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.imixs.workflow.faces.data.ViewController;
import org.imixs.workflow.faces.util.LoginController;
import org.imixs.workflow.office.config.SetupController;


@Named
@ViewScoped
public class PortletWorklistTasks extends ViewController {

	private static final long serialVersionUID = 1L;

	@Inject
	LoginController loginController;
	
	@Inject
	SetupController setupController;

	/**
	 * Initialize default behavior configured by the BASIC configuration entity.
	 */
	@Override
	@PostConstruct
	public void init() {
		super.init();
		this.setQuery("(type:\"workitem\" AND $owner:\"" + loginController.getRemoteUser() + "\")");
		this.setPageSize(setupController.getPortletSize());
		this.setSortBy(setupController.getSortBy());
		this.setSortReverse(setupController.getSortReverse());
	}


}
