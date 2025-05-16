/*******************************************************************************
 *  Imixs Workflow Technology
 *  Copyright (C) 2003, 2008 Imixs Software Solutions GmbH,
 *  http://www.imixs.com
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 *
 *  You can receive a copy of the GNU General Public
 *  License at http://www.gnu.org/licenses/gpl.html
 *
 *  Contributors:
 *  	Imixs Software Solutions GmbH - initial API and implementation
 *  	Ralph Soika
 *
 *******************************************************************************/
package org.imixs.workflow.office.dataview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.exceptions.AccessDeniedException;
import org.imixs.workflow.exceptions.QueryException;
import org.imixs.workflow.faces.data.AbstractDataController;
import org.imixs.workflow.faces.data.DocumentController;

import com.oracle.svm.core.annotate.Inject;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

/**
 * The DataViewDefinitionController is used to configure a dataview definition
 *
 * @author rsoika
 * @version 1.0
 */
@Named
@SessionScoped
public class DataViewDefinitionController extends AbstractDataController {

    private static final long serialVersionUID = 1L;

    protected List<ItemCollection> attributeList = null;

    @Inject
    DocumentController documentController;

    protected List<ItemCollection> dataViewDefinitions = null;

    private static Logger logger = Logger.getLogger(DataViewDefinitionController.class.getName());

    /**
     *
     *
     */
    @PostConstruct
    public void init() {

    }

    public List<ItemCollection> getDefinitions() {
        if (dataViewDefinitions == null) {
            // lazy loading
            String quuery = "(type:\"dataview\")";
            try {
                dataViewDefinitions = this.getDocumentService().find(quuery, 999, 0, "name", false);
            } catch (QueryException e) {
                logger.severe("Failed to load DataView definition list: " + e.getMessage());
            }

        }

        return dataViewDefinitions;

    }

    /**
     * Finds all dataview definitions assigned to a process by its name
     * 
     * @param name
     * @return
     */
    public List<ItemCollection> findDefinitionByProcessRef(String id) {
        List<ItemCollection> result = new ArrayList<>();
        if (id == null) {
            return result;
        }

        getDefinitions();
        for (ItemCollection def : dataViewDefinitions) {
            if (id.equals(def.getItemValueString("process.ref"))) {
                result.add(def);
            }
        }
        return result;
    }

    /**
     * Returns the current workItem. If no workitem is defined the method
     * Instantiates a empty ItemCollection.
     *
     * @return - current workItem or null if not set
     */
    public ItemCollection getData() {
        // do initialize an empty workItem here if null
        if (data == null) {
            reset();
        }
        return data;
    }

    /**
     * This method returns a unique view URI to be used zu display the View Data.
     * The uri contains a unique random ID to support caching over different browser
     * tabs
     * 
     * @return
     */
    public String getViewURI(ItemCollection dataDef) {
        return "/pages/dataviews/data.xhtml?id=" + dataDef.getUniqueID() + "&faces-redirect=true";
    }

    @Override
    public void load(String uniqueid) {

        super.load(uniqueid);
        attributeList = computeDataViewItemDefinitions(this.data);
    }

    /**
     * Returns a List of ItemCollection instances representing the view column
     * description.
     * Each column has the items:
     * 
     * name,label,format,convert
     * 
     * @param dataViewDefinition
     * @return
     * 
     */
    @SuppressWarnings("unchecked")
    public static List<ItemCollection> computeDataViewItemDefinitions(ItemCollection dataViewDefinition) {

        ArrayList<ItemCollection> result = new ArrayList<ItemCollection>();
        List<Object> mapItems = dataViewDefinition.getItemValue("dataview.items");
        for (Object mapOderItem : mapItems) {
            if (mapOderItem instanceof Map) {
                ItemCollection itemCol = new ItemCollection((Map) mapOderItem);
                // check label
                String itemLabel = itemCol.getItemValueString("item.label");
                if (itemLabel.isEmpty()) {
                    itemCol.setItemValue("item.label", itemLabel);
                }
                // check type
                String itemType = itemCol.getItemValueString("item.type");
                if (itemType.isEmpty()) {
                    itemCol.setItemValue("item.type", "xs:string");
                }
                result.add(itemCol);
            }
        }

        // if no columns are defined we create the default columns
        if (result.size() == 0) {
            ItemCollection itemCol = new ItemCollection();
            itemCol.setItemValue("item.name", "$workflowSummary");
            itemCol.setItemValue("item.label", "Name");
            itemCol.setItemValue("item.type", "xs:anyURI");
            result.add(itemCol);

            itemCol = new ItemCollection();
            itemCol.setItemValue("item.name", "$modified");
            itemCol.setItemValue("item.label", "Modified");
            itemCol.setItemValue("item.type", "xs:date");
            result.add(itemCol);
        }
        return result;
    }

    /**
     * creates an empty new dataview definition
     */
    public void create() {
        data = new ItemCollection();
        data.setType("dataview");
    }

    /**
     * This method saves the current document.
     *
     * @throws AccessDeniedException - if user has insufficient access rights.
     */
    public void save() throws AccessDeniedException {
        // save definition ...
        data.setType("dataview");
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context
                .getExternalContext();
        String sUser = externalContext.getRemoteUser();
        data.replaceItemValue("$editor", sUser);

        // implode attributes
        if (attributeList != null) {
            List<Map> mapItemList = new ArrayList<Map>();
            logger.fine("Convert attribute items into Map...");
            for (ItemCollection attrItem : attributeList) {
                ItemCollection ding = (ItemCollection) attrItem.clone();
                mapItemList.add(ding.getAllItems());
            }
            data.replaceItemValue("dataview.items", mapItemList);
        }
        data = this.getDocumentService().save(data);

        // reset view
        dataViewDefinitions = null;

    }

    public String openTestView() {
        this.save();
        return "/pages/dataviews/data.xhtml?id=" + data.getUniqueID() + "&faces-redirect=true";
    }

    public List<ItemCollection> getAttributeList() {
        if (attributeList == null) {
            attributeList = new ArrayList<>();
        }
        return attributeList;
    }

    /**
     * Adds a new attribute object.
     */
    public void addAttribute() {
        if (attributeList == null) {
            attributeList = new ArrayList<ItemCollection>();
        }
        ItemCollection source = new ItemCollection();
        attributeList.add(source);
    }

    /**
     * Removes an attribute item from the list
     *
     * @param name - name of attribute
     */
    public void removeAttribute(String name) {
        if (name != null && attributeList != null) {
            for (ItemCollection item : attributeList) {
                if (name.equals(item.getItemValueString("item.name"))) {
                    attributeList.remove(item);
                    break;
                }
            }
        }
    }

    /**
     * Moves an attribute item up in the list
     *
     * List<ItemCollection> attributeList
     *
     * @param name - name of attribute
     */
    public void moveAttributeUp(String name) {
        if (name != null && attributeList != null) {
            logger.info("move up: " + name);
            for (int i = 0; i < attributeList.size(); i++) {
                ItemCollection item = attributeList.get(i);
                if (name.equals(item.getItemValueString("item.name"))) {
                    // Check if it's not already at the top
                    if (i > 0) {
                        // Swap with previous element
                        Collections.swap(attributeList, i, i - 1);
                    }
                    break; // Exit loop once found and processed
                }
            }
        }
    }

    /**
     * Moves an attribute item up down the list
     *
     * List<ItemCollection> attributeList
     *
     * @param name - name of attribute
     */
    public void moveAttributeDown(String name) {
        if (name != null && attributeList != null) {
            logger.info("move down: " + name);
            for (int i = 0; i < attributeList.size(); i++) {
                ItemCollection item = attributeList.get(i);
                if (name.equals(item
                        .getItemValueString("item.name"))) {
                    // Check if it's not already at the bottom
                    if (i < attributeList.size() - 1) {
                        // Swap with next element
                        Collections.swap(attributeList, i, i + 1);
                    }
                    break; // Exit loop once found and processed
                }
            }
        }
    }

}
