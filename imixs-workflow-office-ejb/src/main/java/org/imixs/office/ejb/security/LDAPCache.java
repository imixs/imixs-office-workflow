package org.imixs.office.ejb.security;

import java.io.FileInputStream;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;

/**
 * This singleton ejb provides a cache to lookup ldap user informations. The
 * cache is used by the LDAPGroupLookupService EJB.
 * 
 * The bean reads its configuration from the configuration property file located
 * in the glassfish domains config folder
 * (GLASSFISH_DOMAIN/config/imixs-office-ldap.properties).
 * 
 * cache-size = maximum number of entries
 * 
 * cache-expires = milliseconds after the cache is discarded
 * 
 * The cache-size should be set to the value of minimum concurrent user
 * sessions. cache-expires specifies the expire time of the cache in
 * milliseconds.
 * 
 * 
 * @version 1.0
 * @author rsoika
 * 
 */
@Singleton
public class LDAPCache {

	// private ItemCollection configItemCollection = null;
	private Properties configurationProperties = null;

	int DEFAULT_CACHE_SIZE = 30;
	int DEFAULT_EXPIRES_TIME = 60000;
	long expiresTime = 0;
	long lastReset = 0;
	private Cache cache = null; // cache holds userdata

	private static Logger logger = Logger.getLogger("org.imixs.office");

	@PostConstruct
	void init() {
		try {
			// load confiugration entity
			loadProperties();
			resetCache();
		} catch (Exception e) {
			logger.severe("LDAPCache unable to initalize LDAPCache");
			e.printStackTrace();
		}
	}

	/**
	 * resets the ldap cache object and reads the config params....
	 * 
	 * 
	 */
	public void resetCache() {
		// determine the cache size....
		logger.fine("LDAPCache resetCache - initalizing settings....");
		int iCacheSize = DEFAULT_CACHE_SIZE;
		try {
			iCacheSize = Integer.valueOf(configurationProperties
					.getProperty("cache-size"));
		} catch (NumberFormatException nfe) {
			iCacheSize = DEFAULT_CACHE_SIZE;
		}
		if (iCacheSize <= 0)
			iCacheSize = DEFAULT_CACHE_SIZE;

		// initialize cache
		cache = new Cache(iCacheSize);

		// read expires time...
		try {
			expiresTime = DEFAULT_EXPIRES_TIME;
			String sExpires = configurationProperties
					.getProperty("cache-expires");
			expiresTime = Long.valueOf(sExpires);
		} catch (NumberFormatException nfe) {
			expiresTime = DEFAULT_EXPIRES_TIME;
		}
		if (expiresTime <= 0)
			expiresTime = DEFAULT_EXPIRES_TIME;
		
		
		lastReset = System.currentTimeMillis();

	}

	public Object get(String key) {
		// test if cache is expired
		if (expiresTime > 0) {
			Long now = System.currentTimeMillis();
			if ((now - lastReset) > expiresTime) {
				logger.fine("LDAPCache Cache expired!");
				resetCache();				
			}
		}
		return cache.get(key);
	}
	
	/** 
	 * returns true if the key is contained in the cache.
	 * 
	 */
	public boolean contains(String key) {
		return cache.containsKey(key);
	}

	public void put(String key, Object value) {
		cache.put(key, value);
	}

	/**
	 * loads a imixs-ldap.property file
	 * 
	 * (located at domains/domain1/config/imixs-office-ldap.properties)
	 * 
	 * 
	 * @return
	 * @throws Exception
	 */
	public void loadProperties() throws Exception {
		// try loading imixs-search properties
		configurationProperties = new Properties();
		try {
			FileInputStream fis = new FileInputStream(
					"imixs-office-ldap.properties");
			configurationProperties.load(fis);
			fis.close();
		} catch (Exception ep) {
			// no properties found
			logger.severe("LDAPCache imixs-ldap.properties not found");
			configurationProperties = null;
		}
	}

	/**
	 * Cache implementation to hold userData objects
	 * 
	 * @author rsoika
	 * 
	 */
	class Cache extends LinkedHashMap<String, Object> implements Serializable {
		private static final long serialVersionUID = 1L;
		private final int capacity;

		public Cache(int capacity) {
			super(capacity + 1, 1.1f, true);
			this.capacity = capacity;
		}

		protected boolean removeEldestEntry(Entry<String, Object> eldest) {
			return size() > capacity;
		}
	}
}
