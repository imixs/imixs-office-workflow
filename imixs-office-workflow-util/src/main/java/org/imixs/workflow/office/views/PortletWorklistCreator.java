package org.imixs.workflow.office.views;

import org.imixs.workflow.faces.data.ViewController;
import org.imixs.workflow.faces.util.LoginController;
import org.imixs.workflow.office.config.SetupController;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@ViewScoped
public class PortletWorklistCreator extends ViewController {

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
		this.setQuery("(type:\"workitem\" AND $creator:\"" + loginController.getRemoteUser() + "\")");
		this.setPageSize(setupController.getPortletSize());
		this.setSortBy(setupController.getSortBy());
		this.setSortReverse(setupController.getSortReverse());
	}

}
