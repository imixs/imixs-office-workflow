package org.imixs.office.ejb;

import java.io.FileInputStream;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Singleton;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

/**
 * This singelton ejb provides a cache to lookup ldap user informations
 * 
 * The bean reads its configuration from the environment configuration entity
 * The following proerties are expected:
 * 
 * ldap_jndiName - name of the external jndi ldap context (default
 * ="org.imixs.marty.ldap")
 * 
 * @version 1.0
 * @author rsoika
 * 
 */
@Singleton
public class LDAPGroupLookupService {

	// private ItemCollection configItemCollection = null;
	private Properties configurationProperties = null;

	int MAX_CACHE_SIZE = 30;
	long expiresTime = 0;
	long lastReset=0;
	private Cache cache = null; // cache holds userdata

	private DirContext ldapCtx = null;

	@Resource
	SessionContext ejbCtx;

	@EJB
	org.imixs.workflow.jee.ejb.EntityService entityService;

	private static Logger logger = Logger.getLogger("org.imixs.office");

	@PostConstruct
	void init() {
		try {
			// load confiugration entity
			loadProperties();

			// skip if no configuration
			if (configurationProperties == null)
				return;

			resetCache();

			// initialize ldapCtx...
			ldapCtx = getDirContext();
		} catch (Exception e) {
			logger.severe("Unable to initalize LDAPGroupLookupService");
			ldapCtx = null;
			e.printStackTrace();
		}
	}

	/**
	 * resets the ldap cache object
	 */
	public void resetCache() {
		// determine the cache size....
		int iCacheSize = Integer.valueOf(configurationProperties
				.getProperty("CacheSize"));
		if (iCacheSize <= 0)
			iCacheSize = MAX_CACHE_SIZE;

		// initialize cache
		cache = new Cache(iCacheSize);

		// read expires time...
		try {
			String sExpires = configurationProperties
					.getProperty("CacheExpires");
			expiresTime = Long.valueOf(sExpires);
		} catch (NumberFormatException nfe) {
			expiresTime = 0;
		}

	}

	/**
	 * lookups the single attribute for a given uid
	 * 
	 * 
	 * @param aQnummer
	 * @return
	 * @throws NamingException
	 */
	public String fetchAttribute(String aUID, String sAttriubteName)
			throws NamingException {
		String sAttriubteValue = null;

		if (ldapCtx == null)
			return sAttriubteValue;

		// test cache...
		sAttriubteValue = (String) cache.get(aUID + "-" + sAttriubteName);
		if (sAttriubteValue != null)
			return sAttriubteValue;

		// try to lookup....
		logger.info("LDAP fetch attribute: " + sAttriubteName + " for " + aUID);

		String returnedAtts[] = { sAttriubteName };

		SearchControls ctls = new SearchControls();
		ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		ctls.setReturningAttributes(returnedAtts);
		String searchfilter = "";
		searchfilter = "(uid=" + aUID + ")";

		NamingEnumeration answer = getDirContext().search("", searchfilter,
				ctls);

		if (answer.hasMore()) {

			SearchResult entry = (SearchResult) answer.next();
			Attributes attrs = entry.getAttributes();

			// Attribute attr = null;

			Attribute attr = attrs.get(sAttriubteName);

			if (attr != null) {
				sAttriubteValue = (String) attr.get(0);
				logger.info("LDAP fetch attribute= " + sAttriubteValue);
			}
		}

		// no luck.?...
		if (sAttriubteValue == null)
			sAttriubteValue = aUID;

		// cache entry
		cache.put(aUID + "-" + sAttriubteName, sAttriubteValue);

		return sAttriubteValue;
	}

	/**
	 * returns the dn for a given remote user
	 * 
	 * @param aQnummer
	 * @return
	 * @throws NamingException
	 */
	public String fetchDN(String aUID) throws NamingException {
		String sDN = null;

		if (ldapCtx == null)
			return aUID;

		// test cache...
		sDN = (String) cache.get(aUID + "-DN");
		if (sDN != null)
			return sDN;

		logger.info("LDAP fetchDN: " + aUID);

		String returnedAtts[] = { "mail" };

		SearchControls ctls = new SearchControls();
		ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		ctls.setReturningAttributes(returnedAtts);
		String searchfilter = "";
		searchfilter = "(uid=" + aUID + ")";

		NamingEnumeration answer = getDirContext().search("", searchfilter,
				ctls);

		if (answer.hasMore()) {
			SearchResult entry = (SearchResult) answer.next();
			sDN = entry.getName();
			logger.info("LDAP fetchDN= " + sDN);
		}

		if (sDN == null)
			sDN = aUID;

		// cache DN
		cache.put(aUID + "-DN", sDN);

		return sDN;
	}

	/**
	 * returns all groups where the remote user is member of
	 * 
	 * The method checks the expires time and rest the cache if the cache time is expired
	 * 
	 * @param aQnummer
	 * @return
	 * @throws NamingException
	 */
	public String[] fetchGroups(String aUID) throws NamingException {
		String sDN = null;
		Vector<String> vGroupList = null;
		String[] groupArrayList = null;

		if (ldapCtx == null)
			return null;
		
		
		// test if cache is expired
		if (expiresTime>0) {
			Long now=System.currentTimeMillis();
			if ((now-lastReset) > expiresTime) {
				resetCache();
				lastReset=now;
				logger.info("LDAP Cache expired!");
			}
		}

		// test cache...
		groupArrayList = (String[]) cache.get(aUID + "-GROUPS");
		if (groupArrayList != null)
			return groupArrayList;

		vGroupList = new Vector<String>();

		String groupNamePraefix = configurationProperties
				.getProperty("GroupNamePraefix");

		sDN = fetchDN(aUID);

		logger.info("LDAP fetchGroups for: " + sDN);

		String returnedAtts[] = { "cn" };

		SearchControls ctls = new SearchControls();
		ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		ctls.setReturningAttributes(returnedAtts);
		String searchfilter = "";
		searchfilter = "(member=" + sDN + ")";

		NamingEnumeration answer = getDirContext().search("", searchfilter,
				ctls);

		while (answer.hasMore()) {
			SearchResult entry = (SearchResult) answer.next();
			String sGroupName = entry.getName();

			// it is not possible to ask for the attribute cn - maybe a domino
			// problem so we take the name....
			/*
			 * Attributes attrs = entry.getAttributes(); Attribute attr =
			 * attrs.get("cn"); if (attr != null) sGroupName = (String)
			 * attr.get(0);
			 */
			sGroupName = sGroupName.substring(3);
			if (sGroupName.indexOf(',') > -1)
				sGroupName = sGroupName.substring(0, sGroupName.indexOf(','));

			// test groupname praefix..
			if (groupNamePraefix != null && !"".equals(groupNamePraefix)
					&& !sGroupName.startsWith(groupNamePraefix))
				continue;

			logger.info("LDAP found Group= " + sGroupName);
			vGroupList.add(sGroupName);
		}

		groupArrayList = new String[vGroupList.size()];
		vGroupList.toArray(groupArrayList);
		// cache DN
		cache.put(aUID + "-GROUPS", groupArrayList);

		return groupArrayList;
	}

	/**
	 * ermittelnt den Directory context com.bmw.directory.gd
	 * 
	 * @return
	 * @throws NamingException
	 */
	private DirContext getDirContext() throws NamingException {
		String ldapJndiName = null;

		// test if configuration is available
		if (configurationProperties == null) {
			ldapCtx = null;
			return ldapCtx;
		}

		// try to load dirContext...
		if (ldapCtx == null) {
			Context initCtx;
			try {
				initCtx = new InitialContext();

				// read ldap_jndiName from configuration
				ldapJndiName = configurationProperties
						.getProperty("LdapJndiName");

				if ("".equals(ldapJndiName))
					ldapJndiName = "org.imixs.office.ldap";

				ldapCtx = (DirContext) initCtx.lookup(ldapJndiName);
			} catch (NamingException e) {
				logger.severe("Unable to open ldap context: " + ldapJndiName);
				throw e;
			}
			logger.info("LDAP Ctx initialized!");
		}
		return ldapCtx;
	}

	/**
	 * loads a imixs-ldap.property file
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

			// prop.load(Thread.currentThread().getContextClassLoader()
			// .getResource("imixs-ldap.properties").openStream());
		} catch (Exception ep) {
			// no properties found
			logger.severe("imixs-ldap.properties not found");
			configurationProperties = null;
		}
	}

	/**
	 * Cache implementation to hold userData objects
	 * 
	 * @author rsoika
	 * 
	 */
	class Cache extends LinkedHashMap {
		private final int capacity;

		public Cache(int capacity) {
			super(capacity + 1, 1.1f, true);
			this.capacity = capacity;
		}

		protected boolean removeEldestEntry(Entry eldest) {
			return size() > capacity;
		}
	}
}
