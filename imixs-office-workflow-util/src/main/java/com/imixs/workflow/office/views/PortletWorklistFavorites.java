package com.imixs.workflow.office.views;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.imixs.marty.profile.FavoritesViewController;


@Named
@ViewScoped
public class PortletWorklistFavorites extends FavoritesViewController {

	private static final long serialVersionUID = 1L;
	
	@Override
	@PostConstruct
	public void init() {
		super.init();
		this.setPageSize(5);
	}


}
