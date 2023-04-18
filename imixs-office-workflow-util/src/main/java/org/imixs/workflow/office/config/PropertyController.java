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

package org.imixs.workflow.office.config;

import java.io.Serializable;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.eclipse.microprofile.config.Config;

/**
 * This PropertyController provides access to the Microprofile config api.
 * <p>
 * The PropertyController can be used in a JSF page to access a specific property by name:
 * <p>
 * <code>
 *   #{propertyController.getProperty('my-property')}
 *   #{propertyController.getProperty('another-property','default value')}
 * </code>
 * 
 * @author rsoika
 */
@Named
@ApplicationScoped
public class PropertyController implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject Config config;

	/**
	 * Returns a property value
	 * 
	 * @param key - the property key
	 * @return property value
	 */
	public String getProperty(String key) {
		return config.getOptionalValue(key, String.class)
                .orElse("");
	}
	
	/**
     * Returns a property value
     * 
     * @param key - the property key
     * @param defaultValue - optional default value if property not defined
     * @return property value
     */
    public String getProperty(String key,String defaultValue) {
        return config.getOptionalValue(key, String.class)
                .orElse(defaultValue);
    }
}
