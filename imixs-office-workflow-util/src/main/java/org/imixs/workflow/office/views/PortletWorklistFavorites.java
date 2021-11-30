package org.imixs.workflow.office.views;

import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.imixs.marty.profile.UserController;
import org.imixs.workflow.faces.data.ViewController;
import org.imixs.workflow.faces.util.LoginController;
import org.imixs.workflow.office.config.SetupController;

@Named
@ViewScoped
public class PortletWorklistFavorites extends ViewController {

    private static final long serialVersionUID = 1L;

    public static final String LINK_PROPERTY = "$workitemref";
    public static final String LINK_PROPERTY_DEPRECATED = "txtworkitemref";

    private int mode = 1; // 1=favorites | 2=creator

    @Inject
    protected UserController userController;

    @Inject
    LoginController loginController;

    @Inject
    SetupController setupController;
    
   @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(PortletWorklistFavorites.class.getName());

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

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

        // Getting a cookie value for mytasks mode.....
        try {
            String value = getCookie("imixs.office.document.mytasks.mode");
            if (value != null) {
                mode = Integer.parseInt(value);
            }
        } catch (NumberFormatException e) {
            // no op
        }
    }

    /**
     * Generate custom query based on the user profile
     */
    @SuppressWarnings("unchecked")
    @Override
    public String getQuery() {
        
        if (userController.getWorkitem() == null) {
            return null;
        }

      

        String sQuery = "";

        // favorites?
        if (mode == 1) {
            List<String> favorites = null;
            // get favorite ids from profile
            // support deprecated ref field
            if (!userController.getWorkitem().hasItem(LINK_PROPERTY)
                    && userController.getWorkitem().hasItem(LINK_PROPERTY_DEPRECATED)) {
                favorites = userController.getWorkitem().getItemValue(LINK_PROPERTY_DEPRECATED);
            } else {
                favorites = userController.getWorkitem().getItemValue(LINK_PROPERTY);
            }

            if (favorites == null || favorites.size() == 0) {
                return null;
            }
            sQuery = "(type:\"workitem\" OR type:\"workitemarchive\") ";

            // create IN list
            sQuery += " ( ";
            for (String aID : favorites) {
                sQuery += "$uniqueid:\"" + aID + "\" OR ";
            }
            // cut last ,
            sQuery = sQuery.substring(0, sQuery.length() - 3);
            sQuery += " ) ";

        } else {
            // TODO
            // should be participant
            // sQuery="(type:\"workitem\" AND $participants:\"" +
            // loginController.getRemoteUser() + "\")";
            sQuery = "(type:\"workitem\" AND $creator:\"" + loginController.getRemoteUser() + "\")";

        }

        return sQuery;
    }

    

    public String getCookie(String name) {

        FacesContext facesContext = FacesContext.getCurrentInstance();

        HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
        Cookie cookie = null;

        Cookie[] userCookies = request.getCookies();
        if (userCookies != null && userCookies.length > 0) {
            for (int i = 0; i < userCookies.length; i++) {
                if (userCookies[i].getName().equals(name)) {
                    cookie = userCookies[i];
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
