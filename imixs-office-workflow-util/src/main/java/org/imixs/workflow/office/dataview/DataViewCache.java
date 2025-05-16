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

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.imixs.workflow.ItemCollection;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;

/**
 * The DataViewCache implements a sessionScoped cache for the dataView filter.
 * The cache uses the cacheId provided in the URL - see DataViewController
 * onLoad
 * 
 * @author rsoika
 * @version 1.0
 */
@Named
@SessionScoped
public class DataViewCache implements Serializable {

    private static final long serialVersionUID = 1L;

    private Map<String, ItemCollection> dataViewStates = new ConcurrentHashMap<>();

    public void put(String key, ItemCollection data) {
        dataViewStates.put(key, data);
    }

    public ItemCollection get(String key) {
        ItemCollection result = dataViewStates.get(key);
        if (result == null) {
            // init new empty cache object!
            result = new ItemCollection();
            dataViewStates.put(key, result);
        }
        return result;
    }
}
