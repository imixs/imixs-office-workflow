package org.imixs.workflow.office.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.engine.jpa.Document;

/**
 * The MartyConfigLoader is a singleton EJB loading the marty properties from
 * the configuration entity 'BASIC' and provides the properties to the
 * MartyConfigSource
 * <p>
 * See implementation details here:
 * https://stackoverflow.com/questions/52117613/microprofile-config-custom-configsource-using-jpa/56498321#56498321
 * 
 * @author rsoika
 * @see PropertiesConfigSource
 */
@Startup
@Singleton
public class PropertiesLoader {

	@PersistenceContext(unitName = "org.imixs.workflow.jpa")
	private EntityManager manager;

	private static Logger logger = Logger.getLogger(PropertiesLoader.class.getName());

	/**
	 * This method loads the properties after the EJB is constructed and overwrites
	 * the properties of the PropertiesConfigSource
	 */
	@PostConstruct
	void init() {
		reset();
	}

	/**
	 * This method reset the properties from the PropertiesConfigSource
	 */
	public void reset() {
		PropertiesConfigSource.properties = loadProperties();
	}

	/**
	 * This method is used to load a imixs.property file into the property
	 * Map<String,String>
	 * <p>
	 * The imixs.property file is loaded from the current threads classpath.
	 * 
	 */
	public Map<String, String> loadProperties() {
		ItemCollection basicConfig = getBasicConfigurationDocument();
		if (basicConfig != null) {
			Map<String, String> properties = new HashMap<String, String>();

			// read properties
			List<?> v = (List<?>) basicConfig.getItemValue("properties");
			if (v.size() > 0) {
				logger.fine("...Update imixs.properties");
				for (Object o : v) {
					String sProperty = (String) o;
					int ipos = sProperty.indexOf('=');
					if (ipos > 0) {
						String sKey = sProperty.substring(0, sProperty.indexOf('='));

						String sValue = sProperty.substring(sProperty.indexOf('=') + 1);

						logger.fine("Overwrite property/value: " + sKey + "=" + sValue);
						properties.put(sKey, sValue);
					}
				}
			}
			return properties;
		} else {
			return null;
		}

	}

	/**
	 * Returns the 'BASIC' configuration Document entity by using the EntityManager
	 * native.
	 * 
	 * @param query
	 *            - JPQL statement
	 * @return
	 * 
	 */
	private ItemCollection getBasicConfigurationDocument() {
		if (manager == null) {
			return null; // happens during deployment
		}
		// select all documenty by type
		String query = "SELECT document FROM Document AS document ";
		query += " WHERE document.type = 'configuration'";
		query += " ORDER BY document.created DESC";
		Query q = manager.createQuery(query);

		@SuppressWarnings("unchecked")
		Collection<Document> documentList = q.getResultList();
		if (documentList != null) {

			// filter resultset by read access
			for (Document doc : documentList) {
				// check name = "BASIC"
				ItemCollection configDocument = new ItemCollection(doc.getData());
				if (configDocument.getItemValueString("txtname").equals("BASIC")) {
					return configDocument;
				}
			}
		}
		logger.fine("BASIC configuration not found.");
		return null;
	}

}