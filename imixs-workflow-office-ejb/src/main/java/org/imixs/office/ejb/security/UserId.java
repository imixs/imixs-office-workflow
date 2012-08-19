package org.imixs.office.ejb.security;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

/**
 * User object to provide a database for username/password/groups. The user
 * object is managed by the UserDBPlugin.
 * 
 * @author rsoika
 * 
 */
@javax.persistence.Entity
public class UserId implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	private String id;
	private String password;
	private Set<UserGroup> userGroups;

	/**
	 * default constructor for JPA
	 */
	public UserId() {
		super();
	}

	/**
	 * A User will be automatically initialized with an id and password
	 */
	public UserId(String aid) {
		// Initialize time objects
		this.id = aid;
	}

	/**
	 * returns the unique identifier for the Entity.
	 * 
	 * @return universal id
	 */
	@Id
	public String getId() {
		return id;
	}

	protected void setId(String aID) {
		id = aID;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * It is important to rename the joinColum to 'ID' so the GlassFish
	 * jdbcRealm configuration will work on this colum
	 * 
	 * @return
	 */
	@JoinTable(name = "USERID_USERGROUP", joinColumns = { @JoinColumn(name = "ID") }, inverseJoinColumns = { @JoinColumn(name = "GROUP_ID") })
	@ManyToMany(cascade = CascadeType.PERSIST)
	public Set<UserGroup> getUserGroups() {
		return userGroups;
	}

	public void setUserGroups(Set<UserGroup> groups) {
		this.userGroups = groups;
	}

}
