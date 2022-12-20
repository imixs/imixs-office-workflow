package org.imixs.workflow.office.views;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.imixs.workflow.faces.data.ViewController;
import org.imixs.workflow.faces.util.LoginController;


@Named
@ViewScoped
public class SpaceViewController extends ViewController {

	private static final long serialVersionUID = 1L;

	@Inject
	LoginController loginController;

    @Inject
    @ConfigProperty(name = "admin.view.entries.max", defaultValue = "999")
    transient int adminViewPageSize;

	@Override
	@PostConstruct
	public void init() {
		super.init();
		this.setQuery("(type:space*)");
		this.setSortBy("name");
		this.setSortReverse(false);
		this.setPageSize(adminViewPageSize);
		this.setLoadStubs(false);
	}


}
