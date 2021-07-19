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
package org.imixs.workflow.office.forms;

import java.io.Serializable;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

import org.imixs.marty.profile.UserController;

/**
 * The CountryController provides select lists for countries.
 * 
 * @see workitems/parts/country.xhtml
 * @author rsoika,gheinle
 */
@Named
@RequestScoped
public class CountryController implements Serializable {

	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(CountryController.class.getName());

	@Inject
	UserController userController;

	/**
	 * Retuns a list of select items with all country codes and country display
	 * names
	 * 
	 * @return
	 */
	public List<SelectItem> getCountriesSelectItems() {

		ArrayList<SelectItem> selection;
		selection = new ArrayList<SelectItem>();

		String[] locales = Locale.getISOCountries();
		logger.fine("...found " + locales.length + " countries");

		for (String countryCode : locales) {
			Locale locale = new Locale("", countryCode);
			String code = locale.getCountry();
			String country = locale.getDisplayCountry(userController.getLocale());
			selection.add(new SelectItem(code, country + " (" + code + ")"));
		}

		// now we sort by the user locale
		Collections.sort(selection, selectItemComparator);

		return selection;

	}

	// Sort countries by display Country
	Comparator<SelectItem> selectItemComparator = new Comparator<SelectItem>() {
		@Override
		public int compare(SelectItem s1, SelectItem s2) {
			Collator collator = Collator.getInstance(userController.getLocale());
			collator.setStrength(Collator.SECONDARY); // non case sensitive
			return collator.compare(s1.getLabel(), s2.getLabel());
		}
	};

}
