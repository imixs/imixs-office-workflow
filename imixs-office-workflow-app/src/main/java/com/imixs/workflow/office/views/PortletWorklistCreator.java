package com.imixs.workflow.office.views;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.imixs.workflow.faces.data.ViewController;
import org.imixs.workflow.faces.util.LoginController;


@Named
@ViewScoped
public class PortletWorklistCreator extends ViewController {

	private static final long serialVersionUID = 1L;
	
	@Inject
	LoginController loginController;
	
	
	@Override
	@PostConstruct
	public void init() {
		super.init();
		this.setQuery("(type:\"workitem\" AND namcreator:\"" + loginController.getRemoteUser() + "\")");
		this.setSortBy("$modified");
		this.setSortReverse(true);
		this.setPageSize(5);
	}


}
