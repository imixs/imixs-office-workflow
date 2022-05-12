package org.imixs.workflow.office.views;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.imixs.marty.team.TeamService;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.ItemCollectionComparator;
import org.imixs.workflow.engine.DocumentService;
import org.imixs.workflow.exceptions.QueryException;
import org.imixs.workflow.faces.util.LoginController;

@Named
@ViewScoped
public class RolesAndRightsViewController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    LoginController loginController;

    @Inject
    private DocumentService documentService;
    @EJB
    protected TeamService teamService;

        private List<ItemCollection> profiles;

        private List<ItemCollection> spaces;
        private List<ItemCollection> processList;


    public RolesAndRightsViewController() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * PostContruct event - loads the imixs.properties.
     * @throws QueryException 
     */
    @PostConstruct
    void init() throws QueryException {
        String _query = "(type:\"profile\") AND ($taskid:[210 TO 299]) ";
        profiles = documentService.find(_query, 9999, 0);
        Collections.sort(profiles, new ItemCollectionComparator("txtusername", true));
        
        
        _query = "type:\"space\"";
        spaces = documentService.find(_query, 9999, 0);
        Collections.sort(spaces, new ItemCollectionComparator("name", true));
        
        _query = "type:\"process\"";
        processList = documentService.find(_query, 9999, 0);
        Collections.sort(processList, new ItemCollectionComparator("name", true));
    }

    
    /**
     * Returns a list of all active profiles
     * 
     * @return
     * @throws QueryException 
     */
    public List<ItemCollection> getProfiles(){       
        return profiles;
    }

    public List<ItemCollection> getSpaces() {
        return spaces;
    }

    public List<ItemCollection> getProcessList() {
        return processList;
    }

}
