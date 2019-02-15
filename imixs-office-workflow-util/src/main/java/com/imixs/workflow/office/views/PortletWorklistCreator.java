package com.imixs.workflow.office.views;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.imixs.marty.config.SetupController;
import org.imixs.workflow.faces.data.ViewController;
import org.imixs.workflow.faces.util.LoginController;

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
		this.setQuery("(type:\"workitem\" AND namcreator:\"" + loginController.getRemoteUser() + "\")");
		this.setPageSize(setupController.getPortletSize());
		this.setSortBy(setupController.getSortBy());
		this.setSortReverse(setupController.getSortReverse());
	}

}
