package org.imixs.office.ejb.security;

import java.io.FileInputStream;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;
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

import org.imixs.workflow.ItemCollection;

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

	private boolean enabled = false;
	private Properties configurationProperties = null;

	private String dnSearchFilter = null;
	private String groupSearchFilter = null;
	private String searchContext = null;
	private String[] userAttributes = null;

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
			logger.info("LDAPLookupService init");
			// load confiugration entity
			loadProperties();

			// skip if no configuration
			if (configurationProperties == null)
				return;

			// initialize ldap connection
			reset();

		} catch (Exception e) {
			logger.severe("Unable to initalize LDAPGroupLookupService");

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
		logger.fine("LDAPLookupService reset....");

		searchContext = configurationProperties.getProperty("search-context",
				"");
		dnSearchFilter = configurationProperties.getProperty(
				"dn-search-filter", "(uid=%u)");
		groupSearchFilter = configurationProperties.getProperty(
				"group-search-filter", "(member=%d)");

		// read user attributes
		String sAttributes = configurationProperties.getProperty(
				"user-attributes", "uid,SN,CN,mail");
		userAttributes = sAttributes.split(",");

		// test if ldap is enabled...
		LdapContext ldapCtx = null;
		try {
			ldapCtx = getDirContext();
			enabled = (ldapCtx != null);
		} finally {
			try {
				if (ldapCtx != null)
					ldapCtx.close();
			} catch (NamingException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * returns the default attributes for a given user
	 * 
	 * @param aUID
	 *            - user id
	 * @return ItemCollection containing the user attributes
	 */
	public ItemCollection findUser(String aUID) {

		ItemCollection user = (ItemCollection) ldapCache.get(aUID);
		if (user != null)
			return user;

		// start lookup
		LdapContext ldapCtx = null;
		try {
			logger.fine("LDAP find user: " + aUID);

			ldapCtx = getDirContext();
			return fetchUser(aUID, ldapCtx);
		} finally {
			if (ldapCtx != null)
				try {												
					ldapCtx.close();
				} catch (NamingException e) {
					e.printStackTrace();
				}
		}

	}

	/**
	 * returns all groups for a given UID
	 * 
	 * 
	 * @param aUID
	 *            - user unique id
	 * @return string array of group names
	 */
	public String[] findGroups(String aUID) {
		// test cache...
		String[] groupArrayList = (String[]) ldapCache.get(aUID + "-GROUPS");
		if (groupArrayList != null) {
			return groupArrayList;
		}

		LdapContext ldapCtx = null;
		try {
			logger.fine("LDAP find user groups for: " + aUID);
			ldapCtx = getDirContext();
			String[] groups= fetchGroups(aUID, ldapCtx);
			if (logger.getLevel().intValue() <= java.util.logging.Level.FINE
					.intValue()) {
				String groupListe = "";
				for (String aGroup : groups)
					groupListe += aGroup + " ";
				logger.fine("LDAP groups found for " + aUID + "=" + groupListe);
			}
			
			return groups;
			
		} finally {
			if (ldapCtx != null)
				try {
					ldapCtx.close();
				} catch (NamingException e) {
					e.printStackTrace();
				}
		}

	}

	/**
	 * returns the default attributes for a given user
	 * 
	 * @param aUID
	 *            - user id
	 * @return ItemCollection containing the user attributes
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private ItemCollection fetchUser(String aUID, LdapContext ldapCtx) {
		ItemCollection user = null;
		String sDN = null;
		if (!enabled)
			return new ItemCollection();

		NamingEnumeration<SearchResult> answer = null;
		try {

			SearchControls ctls = new SearchControls();
			ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			ctls.setReturningAttributes(userAttributes);

			String searchFilter = dnSearchFilter.replace("%u", aUID);
			logger.finest("LDAP search:" + searchFilter);
			answer = ldapCtx.search(searchContext, searchFilter, ctls);

			if (answer.hasMore()) {
				SearchResult entry = (SearchResult) answer.next();
				sDN = entry.getName();
				logger.finest("LDAP DN= " + sDN);

				user = new ItemCollection();
				Attributes attributes = entry.getAttributes();
				// fetch all attributes
				for (String itemName : userAttributes) {
					Attribute atr = attributes.get(itemName.trim());
					if (atr != null) {
						NamingEnumeration<?> values = atr.getAll();

						Vector valueList = new Vector();
						while (values.hasMore()) {
							valueList.add(values.next());
						}
						if (valueList.size() > 0)
							user.replaceItemValue(itemName, valueList);
					}
				}

				user.replaceItemValue("dn", sDN);
			}

			if (sDN == null) {
				// empty user entry
				sDN = aUID;

				user = new ItemCollection();

				user.replaceItemValue("dn", sDN);
			}

			// cache DN
			ldapCache.put(aUID, user);
		} catch (NamingException e) {
			logger.warning("Unable to fetch DN for: " + aUID);
			if (logger.getLevel().intValue() <= java.util.logging.Level.FINEST
					.intValue())
				e.printStackTrace();
		} finally {
			if (answer != null)
				try {
					answer.close();
				} catch (NamingException e) {

					e.printStackTrace();
				}
		}
		return user;
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
	private String[] fetchGroups(String aUID, LdapContext ldapCtx) {
		String sDN = null;
		Vector<String> vGroupList = null;
		String[] groupArrayList = null;

		if (!enabled)
			return null;

		NamingEnumeration<SearchResult> answer = null;
		try {

			vGroupList = new Vector<String>();

			String groupNamePraefix = configurationProperties
					.getProperty("group-name-praefix");

			ItemCollection user = fetchUser(aUID, ldapCtx);

			sDN = user.getItemValueString("dn");

			logger.fine("LDAP fetchGroups for: " + sDN);

			String returnedAtts[] = { "cn" };

			SearchControls ctls = new SearchControls();
			ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			ctls.setReturningAttributes(returnedAtts);

			String searchFilter = groupSearchFilter.replace("%d", sDN);
			logger.finest("LDAP search:" + searchFilter);

			answer = ldapCtx.search(searchContext, searchFilter, ctls);

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

				logger.finest("LDAP found Group= " + sGroupName);
				vGroupList.add(sGroupName);
			}

			logger.finest("LDAP found " + vGroupList.size() + " groups");

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
		} finally {
			if (answer != null)
				try {
					answer.close();
				} catch (NamingException e) {

					e.printStackTrace();
				}
		}
		return groupArrayList;
	}

	/**
	 * lookups the single attribute for a given uid
	 * 
	 * 
	 * @param aQnummer
	 * @return
	 * @throws NamingException
	 */
	private String fetchAttribute(String aUID, String sAttriubteName,
			LdapContext ldapCtx) {
		String sAttriubteValue = null;

		// test cache...
		sAttriubteValue = (String) ldapCache.get(aUID + "-" + sAttriubteName);
		if (sAttriubteValue != null)
			return sAttriubteValue;

		if (!enabled)
			return sAttriubteValue;

		NamingEnumeration<SearchResult> answer = null;
		try {

			// try to lookup....
			logger.fine("LDAP fetch attribute: " + sAttriubteName + " for "
					+ aUID);

			String returnedAtts[] = { sAttriubteName };

			SearchControls ctls = new SearchControls();
			ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			ctls.setReturningAttributes(returnedAtts);

			String searchFilter = dnSearchFilter.replace("%u", aUID);
			logger.finest("LDAP search:" + searchFilter);
			answer = ldapCtx.search(searchContext, searchFilter, ctls);

			if (answer.hasMore()) {

				SearchResult entry = (SearchResult) answer.next();
				Attributes attrs = entry.getAttributes();

				// Attribute attr = null;

				Attribute attr = attrs.get(sAttriubteName);

				if (attr != null) {
					sAttriubteValue = (String) attr.get(0);
					logger.finest("LDAP fetch attribute= " + sAttriubteValue);
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
		} finally {
			if (answer != null)
				try {
					answer.close();
				} catch (NamingException e) {

					e.printStackTrace();
				}

		}
		return sAttriubteValue;
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
	private void loadProperties() throws Exception {
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
	 * 'LdapJndiName' (DisableJndi=false) or manually if DisableJndi=true.
	 * 
	 * @see http://java.net/projects/imixs-workflow-marty/pages/Useldapgroups
	 * 
	 * @return LdapContext object
	 * @throws NamingException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private LdapContext getDirContext() {
		String ldapJndiName = null;

		LdapContext ldapCtx = null;

		// test if configuration is available
		if (configurationProperties == null) {
			return null;
		}

		// try to load dirContext...

		Context initCtx;
		try {
			initCtx = new InitialContext();

			// test if manually ldap context should be build
			String sDisabled = configurationProperties
					.getProperty("disable-jndi");
			if (sDisabled != null && "true".equals(sDisabled.toLowerCase())) {
				logger.finest("LDAPGroupLookupService setup LDAP Ctx manually.....");
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
				logger.finest("Get DirContext Manually successful! ");

			} else {
				// read GlassFish ldap_jndiName from configuration
				ldapJndiName = configurationProperties
						.getProperty("ldap-jndi-name");
				if ("".equals(ldapJndiName))
					ldapJndiName = "org.imixs.office.ldap";
				logger.finest("LDAPGroupLookupService lookup LDAP Ctx from pool '"
						+ ldapJndiName + "' .....");
				ldapCtx = (LdapContext) initCtx.lookup(ldapJndiName);

			}

			logger.fine("LDAPGroupLookupService Context initialized");

		} catch (NamingException e) {
			logger.severe("Unable to open ldap context: " + ldapJndiName);
			if (logger.getLevel().intValue() <= java.util.logging.Level.FINEST
					.intValue())
				e.printStackTrace();
		}

		return ldapCtx;
	}
}
