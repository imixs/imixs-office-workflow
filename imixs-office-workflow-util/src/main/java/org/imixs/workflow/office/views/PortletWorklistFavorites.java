package org.imixs.workflow.office.views;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.imixs.marty.config.SetupController;
import org.imixs.marty.profile.UserController;
import org.imixs.workflow.faces.data.ViewController;


@Named
@ViewScoped
public class PortletWorklistFavorites extends ViewController {

	private static final long serialVersionUID = 1L;
	
	public static final String LINK_PROPERTY = "$workitemref";
	public static final String LINK_PROPERTY_DEPRECATED = "txtworkitemref";

	
	@Inject
	protected UserController userController;

	@Inject
	SetupController setupController;

	/**
	 * Initialize default behavior configured by the BASIC configuration entity.
	 */
	@PostConstruct
	@Override
	public void init() {
		super.init();
		this.setPageSize(setupController.getPortletSize());
		this.setSortBy(setupController.getSortBy());
		this.setSortReverse(setupController.getSortReverse());
	}

	/**
	 * Generate custom query based on the user profile
	 */
	@SuppressWarnings("unchecked")
	@Override
	public String getQuery() {
		List<String> favorites = null;
		if (userController.getWorkitem() == null) {
			return null;
		}

		// get favorite ids from profile
		  //  support deprecated ref field
        if (!userController.getWorkitem().hasItem(LINK_PROPERTY) 
                 && userController.getWorkitem().hasItem(LINK_PROPERTY_DEPRECATED)) {
            favorites= userController.getWorkitem().getItemValue(LINK_PROPERTY_DEPRECATED);
        } else {
            favorites= userController.getWorkitem().getItemValue(LINK_PROPERTY);
        }
        
		
		if (favorites == null || favorites.size() == 0) {
			return null;
		}

		String sQuery = "(type:\"workitem\" OR type:\"workitemarchive\") ";

		// create IN list
		sQuery += " ( ";
		for (String aID : favorites) {
			sQuery += "$uniqueid:\"" + aID + "\" OR ";
		}
		// cut last ,
		sQuery = sQuery.substring(0, sQuery.length() - 3);
		sQuery += " ) ";

		return sQuery;
	}

}
