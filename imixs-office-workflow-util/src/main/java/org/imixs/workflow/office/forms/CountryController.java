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
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

import org.imixs.marty.profile.UserController;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.engine.TextEvent;
import org.imixs.workflow.util.XMLParser;

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

    /**
     * Observer method for CDI TextEvetns to convert a country code into
     * getDisplayCountry
     * <p>
     * Example: {@code<countryname>company.country</countryname>}
     * <p>
     * Optional a locale can be provided to specify the target language Example:
     * {@code<countryname locale="de_DE">company.country</countryname>}
     * 
     * @param event
     */
    @SuppressWarnings("unchecked")
    public void onEvent(@Observes TextEvent event) {
        String text = event.getText();
        ItemCollection documentContext = event.getDocument();

        List<String> tagList = XMLParser.findTags(text, "countryname");
        logger.finest(tagList.size() + " tags found");
        // test if a <value> tag exists...
        for (String tag : tagList) {

            // next we check if the start tag contains a 'locale' attribute
            Locale locale = null;
            String sLocale = XMLParser.findAttribute(tag, "locale");
            String separator = XMLParser.findAttribute(tag, "separator");
            if (sLocale != null && !sLocale.isEmpty()) {
                // split locale
                StringTokenizer stLocale = new StringTokenizer(sLocale, "_");
                if (stLocale.countTokens() == 1) {
                    // only language variant
                    String sLang = stLocale.nextToken();
                    String sCount = sLang.toUpperCase();
                    locale = new Locale(sLang, sCount);
                } else {
                    // language and country
                    String sLang = stLocale.nextToken();
                    String sCount = stLocale.nextToken();
                    locale = new Locale(sLang, sCount);
                }
            } else {
                // get user default locale
                locale = userController.getLocale();
            }

            // extract Item name containing the country code
            String sItemName = XMLParser.findTagValue(tag, "countryname");
            String country = "";

            if (separator == null || separator.isEmpty()) {
                String countryCode = documentContext.getItemValueString(sItemName);

                Locale countryLocale = new Locale("", countryCode);
                // get the country name
                if (countryLocale != null) {
                    country = countryLocale.getDisplayCountry(locale);
                }
            } else {
                List<String> countryCodes = documentContext.getItemValue(sItemName);
                for (String countryCode : countryCodes) {
                    Locale countryLocale = new Locale("", countryCode);
                    // get the country name
                    country = country + countryLocale.getDisplayCountry(locale);
                    country = country + separator;
                }
                // cut last separator
                if (country.endsWith(separator)) {
                    country = country.substring(0, country.length() - separator.length());
                }
            }

            // now replace the tag with the result string
            int iStartPos = text.indexOf(tag);
            int iEndPos = text.indexOf(tag) + tag.length();

            text = text.substring(0, iStartPos) + country + text.substring(iEndPos);
        }

        event.setText(text);
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
