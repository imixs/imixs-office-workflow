package org.imixs.workflow.office.textadapter;

import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.engine.TextEvent;
import org.imixs.workflow.engine.plugins.AbstractPlugin;
import org.imixs.workflow.util.XMLParser;

import jakarta.ejb.Stateless;
import jakarta.enterprise.event.Observes;

/**
 * The CountryCodeAdapter replaces country codes with a user specific country
 * name
 * 
 * @author rsoika
 *
 */
@Stateless
public class CountryCodeAdapter {

    private static Logger logger = Logger.getLogger(AbstractPlugin.class.getName());

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
            if (sLocale == null || sLocale.isEmpty()) {
                logger.info("...no locale defined - default to en_EN");
                sLocale = "en_EN";
            }
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
                // we can not compute the user locale in
                // case of scheduled events!

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

}
