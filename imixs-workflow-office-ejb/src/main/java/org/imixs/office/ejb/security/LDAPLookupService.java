package org.imixs.office.ejb.security;

import java.io.FileInputStream;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

/**
 * This EJB provides a ldap lookup service for user informations
 * 
 * The bean reads its configuration from the configuration property file located
 * in the glassfish domains config folder
 * (GLASSFISH_DOMAIN/config/imixs-office-ldap.properties).
 * 
 * 
 * 
 * @version 1.0
 * @author rsoika
 * 
 */
@Stateless
@Local
public class LDAPLookupService {

	private boolean enabled=false;
	private Properties configurationProperties = null;

	private String dnSearchFilter = null;
	private String groupSearchFilter = null;
	private String searchContext = null;

	private LdapContext ldapCtx = null;

	@Resource
	SessionContext ejbCtx;

	@EJB
	org.imixs.workflow.jee.ejb.EntityService entityService;

	@EJB
	LDAPCache ldapCache;

	private static Logger logger = Logger.getLogger("org.imixs.office");

	@PostConstruct
	void init() {
		try {
			// load confiugration entity
			loadProperties();

			// skip if no configuration
			if (configurationProperties == null)
				return;

			// initialize ldap connection
			reset();

		} catch (Exception e) {
			logger.severe("Unable to initalize LDAPGroupLookupService");
			ldapCtx = null;
			e.printStackTrace();
		}
	}

	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * resets the config params....
	 * 
	 * 
	 */
	public void reset() {
		// determine the cache size....
		logger.fine("LDAPLookupService reinitializing settings....");

		searchContext = configurationProperties.getProperty("search-context",
				"");
		dnSearchFilter = configurationProperties.getProperty(
				"dn-search-filter", "(uid=%u)");
		groupSearchFilter = configurationProperties.getProperty(
				"group-search-filter", "(member=%d)");

		// re-initialize context
		if (ldapCtx!=null) {
			try {
				ldapCtx.close();
			} catch (NamingException e) {
				e.printStackTrace();
			}
			ldapCtx=null;
		}
		enabled=(getDirContext()!=null);
	}

	/**
	 * lookups the single attribute for a given uid
	 * 
	 * 
	 * @param aQnummer
	 * @return
	 * @throws NamingException
	 */
	public String fetchAttribute(String aUID, String sAttriubteName) {
		String sAttriubteValue = null;

		// test cache...
		sAttriubteValue = (String) ldapCache.get(aUID + "-" + sAttriubteName);
		if (sAttriubteValue != null)
			return sAttriubteValue;

		if (getDirContext() == null)
			return sAttriubteValue;
		
		try {
			// try to lookup....
			logger.fine("LDAP fetch attribute: " + sAttriubteName + " for "
					+ aUID);

			String returnedAtts[] = { sAttriubteName };

			SearchControls ctls = new SearchControls();
			ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			ctls.setReturningAttributes(returnedAtts);

			String searchFilter = dnSearchFilter.replace("%u", aUID);
			logger.fine("LDAP search:" + searchFilter);
			NamingEnumeration<SearchResult> answer = getDirContext().search(
					searchContext, searchFilter, ctls);

			if (answer.hasMore()) {

				SearchResult entry = (SearchResult) answer.next();
				Attributes attrs = entry.getAttributes();

				// Attribute attr = null;

				Attribute attr = attrs.get(sAttriubteName);

				if (attr != null) {
					sAttriubteValue = (String) attr.get(0);
					logger.fine("LDAP fetch attribute= " + sAttriubteValue);
				}
			}

			// no luck.?...
			if (sAttriubteValue == null)
				sAttriubteValue = aUID;

			// cache entry
			ldapCache.put(aUID + "-" + sAttriubteName, sAttriubteValue);
		} catch (NamingException e) {
			logger.warning("Unable to fetch attribute '" + sAttriubteName
					+ "' for: " + aUID);
			if (logger.getLevel().intValue() <= java.util.logging.Level.FINEST
					.intValue())
				e.printStackTrace();
		}
		return sAttriubteValue;
	}

	/**
	 * returns the dn for a given remote user
	 * 
	 * @param aQnummer
	 * @return
	 * @throws NamingException
	 */
	public String fetchDN(String aUID) {
		String sDN = null;
	
		// test cache...
		sDN = (String) ldapCache.get(aUID + "-DN");
		if (sDN != null)
			return sDN;
		
		if (getDirContext() == null)
			return aUID;

		try {
			logger.fine("LDAP fetchDN: " + aUID);

			String returnedAtts[] = { "mail" };

			SearchControls ctls = new SearchControls();
			ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			ctls.setReturningAttributes(returnedAtts);

			String searchFilter = dnSearchFilter.replace("%u", aUID);
			logger.fine("LDAP search:" + searchFilter);
			NamingEnumeration<SearchResult> answer = getDirContext().search(
					searchContext, searchFilter, ctls);

			if (answer.hasMore()) {
				SearchResult entry = (SearchResult) answer.next();
				sDN = entry.getName();
				logger.fine("LDAP fetchDN= " + sDN);
			}

			if (sDN == null)
				sDN = aUID;

			// cache DN
			ldapCache.put(aUID + "-DN", sDN);
		} catch (NamingException e) {
			logger.warning("Unable to fetch DN for: " + aUID);
			if (logger.getLevel().intValue() <= java.util.logging.Level.FINEST
					.intValue())
				e.printStackTrace();
		}
		return sDN;
	}

	/**
	 * returns all groups where the remote user is member of
	 * 
	 * The method checks the expires time and rest the cache if the cache time
	 * is expired
	 * 
	 * @param aQnummer
	 * @return
	 * @throws NamingException
	 */
	public String[] fetchGroups(String aUID) {
		String sDN = null;
		Vector<String> vGroupList = null;
		String[] groupArrayList = null;
	
		// test cache...
		groupArrayList = (String[]) ldapCache.get(aUID + "-GROUPS");
		if (groupArrayList != null) {
			logger.finest("LDAP get groups from cache for '" + aUID + "'  ("
					+ groupArrayList.length + ")");

			return groupArrayList;
		}
		
		if (getDirContext() == null)
			return null;


		try {
			vGroupList = new Vector<String>();

			String groupNamePraefix = configurationProperties
					.getProperty("group-name-praefix");

			sDN = fetchDN(aUID);

			logger.fine("LDAP fetchGroups for: " + sDN);

			String returnedAtts[] = { "cn" };

			SearchControls ctls = new SearchControls();
			ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			ctls.setReturningAttributes(returnedAtts);

			String searchFilter = groupSearchFilter.replace("%d", sDN);
			logger.fine("LDAP search:" + searchFilter);

			NamingEnumeration<SearchResult> answer = getDirContext().search(
					searchContext, searchFilter, ctls);

			while (answer.hasMore()) {
				SearchResult entry = (SearchResult) answer.next();
				String sGroupName = entry.getName();

				// it is not possible to ask for the attribute cn - maybe a
				// domino
				// problem so we take the name....
				/*
				 * Attributes attrs = entry.getAttributes(); Attribute attr =
				 * attrs.get("cn"); if (attr != null) sGroupName = (String)
				 * attr.get(0);
				 */
				sGroupName = sGroupName.substring(3);
				if (sGroupName.indexOf(',') > -1)
					sGroupName = sGroupName.substring(0,
							sGroupName.indexOf(','));

				// test groupname praefix..
				if (groupNamePraefix != null && !"".equals(groupNamePraefix)
						&& !sGroupName.startsWith(groupNamePraefix))
					continue;

				logger.fine("LDAP found Group= " + sGroupName);
				vGroupList.add(sGroupName);
			}

			logger.fine("LDAP found " + vGroupList.size() + " groups");

			groupArrayList = new String[vGroupList.size()];
			vGroupList.toArray(groupArrayList);
			// cache DN
			ldapCache.put(aUID + "-GROUPS", groupArrayList);

			logger.finest("LDAP put groups into cache for '" + aUID + "'");

		} catch (NamingException e) {
			logger.warning("Unable to fetch groups for: " + aUID);
			if (logger.getLevel().intValue() <= java.util.logging.Level.FINEST
					.intValue())
				e.printStackTrace();
		}
		return groupArrayList;
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

			// prop.load(Thread.currentThread().getContextClassLoader()
			// .getResource("imixs-ldap.properties").openStream());
		} catch (Exception ep) {
			// no properties found
			logger.severe("imixs-ldap.properties not found");
			configurationProperties = null;
		}
	}

	/**
	 * This method lookups the ldap context either from a Jndi name
	 * 'LdapJndiName' (DisableJndi=false) or manually if DisableJndi=true. If a
	 * manually ldap context should be setup then the following properties need
	 * to be spezified:
	 * 
	 * 
	 * @return
	 * @throws NamingException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private LdapContext getDirContext() {
		String ldapJndiName = null;

		if (ldapCtx != null) {
			logger.finest("GetDirContext - ldapCtx still exists - try to reconnect...");
			try {
				// force a reconnect....
				ldapCtx.reconnect(null);
				logger.finest("reconnect successfull");
				return ldapCtx;
			} catch (NamingException e) {
				logger.severe("Unable to reconnect ldapContext");
				e.printStackTrace();
				ldapCtx=null;
			}
			
		}

		// test if configuration is available
		if (configurationProperties == null) {
			ldapCtx = null;
			return ldapCtx;
		}

		// try to load dirContext...

		Context initCtx;
		try {
			initCtx = new InitialContext();

			// test if manually ldap context should be build
			String sDisabled = configurationProperties
					.getProperty("disable-jndi");
			if (sDisabled != null && "true".equals(sDisabled.toLowerCase())) {
				logger.info("LDAPGroupLookupService setup LDAP Ctx manually.....");
				Hashtable env = new Hashtable();
				env.put("java.naming.factory.initial", configurationProperties
						.getProperty("java.naming.factory.initial",
								"com.sun.jndi.ldap.LdapCtxFactory"));
				env.put("java.naming.security.authentication",
						configurationProperties
								.getProperty(
										"java.naming.security.authentication",
										"simple"));
				env.put("java.naming.security.principal",
						configurationProperties
								.getProperty("java.naming.security.principal"));
				env.put("java.naming.security.credentials",
						configurationProperties
								.getProperty("java.naming.security.credentials"));
				env.put("java.naming.provider.url", configurationProperties
						.getProperty("java.naming.provider.url"));

				ldapCtx = new InitialLdapContext(env, null);
				logger.info("Get DirContext Manually successful! ");

			} else {
				// read GlassFish ldap_jndiName from configuration
				ldapJndiName = configurationProperties
						.getProperty("ldap-jndi-name");
				if ("".equals(ldapJndiName))
					ldapJndiName = "org.imixs.office.ldap";
				logger.info("LDAPGroupLookupService setup LDAP Ctx from pool '"
						+ ldapJndiName + "' .....");
				ldapCtx = (LdapContext) initCtx.lookup(ldapJndiName);
			}

			logger.info("LDAP Ctx initialized!");

		} catch (NamingException e) {
			logger.severe("Unable to open ldap context: " + ldapJndiName);
			if (logger.getLevel().intValue() <= java.util.logging.Level.FINEST
					.intValue())
				e.printStackTrace();
		}

		return ldapCtx;
	}
}
