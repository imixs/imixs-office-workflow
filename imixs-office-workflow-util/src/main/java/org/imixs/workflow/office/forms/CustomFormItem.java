/*******************************************************************************
 *  Imixs Workflow 
 *  Copyright (C) 2001, 2011 Imixs Software Solutions GmbH,  
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
 *  Project: 
 *  	http://www.imixs.org
 *  	http://java.net/projects/imixs-workflow
 *  
 *  Contributors:  
 *  	Imixs Software Solutions GmbH - initial API and implementation
 *  	Ralph Soika - Software Developer
 *******************************************************************************/

package org.imixs.workflow.office.forms;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import jakarta.faces.model.SelectItem;

/**
 * This CustomFormItem provides the informations from a single item inside a
 * custom form section
 * <p>
 * The optional 'options' contains a list of select options. Example:
 * <code>SEPA|sepa_transfer;Bankeinzug/ Kreditkarte|direct_debit"</code>
 * 
 * 
 * @author rsoika
 * @version 1.0
 */
public class CustomFormItem {

    String name;
    String type;
    String label;
    boolean required;
    boolean readonly;
    boolean disabled;
    boolean hide;
    String options;
    String path; // used for custom types
    int span; // flex grid layout span

    private static Logger logger = Logger.getLogger(CustomFormItem.class.getName());


    public CustomFormItem(String name, String type, String label, boolean required, boolean readonly, boolean disabled,
            String options,
            String path, boolean hide, int span) {
        super();
        this.label = label;
        this.name = name;
        this.type = type;
        this.required = required;
        this.readonly = readonly;
        this.disabled = disabled;
        this.hide = hide;
        this.options = options;
        this.path=path;
        if ("custom".equalsIgnoreCase(type) && (path ==null || path.isEmpty()) ) {
        	logger.warning("Custom Form Item requires 'path' attribute - please check your BPMN model");
        }
        // default span = 12
        if (span <= 0 || span > 12) {
            span = 12;
        }
        this.span = span;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public boolean isHide() {
        return hide;
    }

    public void setHide(boolean hide) {
        this.hide = hide;
    }

    public int getSpan() {
        return span;
    }

    public void setSpan(int span) {
        this.span = span;
    }

    /**
     * SelectItem getter Method provides a getter method to an ArrayList of
     * <SelectItem> objects for a given options String. The options String contains
     * multiple options spearated by ; One option can be devided by a | into a label
     * and a value component. Example:
     * <p>
     * <code>
     *   SEPA|sepa_transfer;Bankeinzug/ Kreditkarte|direct_debit"
     * </code>
     * 
     * <code>
     * <f:selectItems value="#{item.selectItems}" />
     * </code>
     * 
     * @return
     * @throws Exception
     */
    public List<SelectItem> getSelectItems() throws Exception {
        ArrayList<SelectItem> selection;
        selection = new ArrayList<SelectItem>();

        // check if a value for this param is available...
        // if not return an empty list
        if (this.options == null || this.options.isEmpty()) {
            return selection;
        }

        // get value list first value from vector if size >0
        String[] valueList = options.split(";");
        for (String aValue : valueList) {
            // test if aValue has a | as an delimiter
            String sValue = aValue;
            String sName = sValue;
            if (sValue.indexOf("|") > -1) {
                sValue = sValue.substring(0, sValue.indexOf("|"));
                sName = sName.substring(sName.indexOf("|") + 1);
            }
            selection.add(new SelectItem(sName.trim(), sValue.trim()));
        }
        return selection;
    }

    
    /**
     * Optional option string.
     * <p>
     * Can contain custom parts data
     * 
     * @return
     */
    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    /**
     * optional path for custom items.
     * @return
     */
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
    
}
