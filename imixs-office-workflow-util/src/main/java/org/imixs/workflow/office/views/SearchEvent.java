/*  
 *  Imixs-Workflow 
 *  
 *  Copyright (C) 2001-2020 Imixs Software Solutions GmbH,  
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
 *      https://www.imixs.org
 *      https://github.com/imixs/imixs-workflow
 *  
 *  Contributors:  
 *      Imixs Software Solutions GmbH - Project Management
 *      Ralph Soika - Software Developer
 */

package org.imixs.workflow.office.views;

import org.imixs.workflow.ItemCollection;

/**
 * The SearchEvent provides a CDI observer pattern. The SearchEvent is fired
 * by the SearchController. An event Observer can react on event to extend the current search query in a custom way.
 * 
 * The ProfileEvent defines the following event types:
 * <ul>
 * <li>ON_QUERY - send during the search query creation
 * </ul>
 * 
 * @author Ralph Soika
 * @version 1.0
 * @see org.imixs.marty.ejb.ProfileService
 */
public class SearchEvent {

    public static final int ON_QUERY = 1;

    private int eventType;
    private ItemCollection searchFilter;
    private String query = null;

    /**
     * Creates a profile event based on a existing Profile ItemCollection
     * 
     * @param query    - userid
     * @param searchFilter   - optional profile ItemCollection
     * @param eventType
     */
    public SearchEvent(ItemCollection searchFilter, int eventType) {
        this.eventType = eventType;
        this.searchFilter = searchFilter;
    }

    public int getEventType() {
        return eventType;
    }

    public ItemCollection getSearchFilter() {
        return searchFilter;
    }

    public void setSearchFilter(ItemCollection filter) {
        this.searchFilter = filter;
    }

    public String getQuery() {
        if (query==null) {
            query="";
        }
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

}
