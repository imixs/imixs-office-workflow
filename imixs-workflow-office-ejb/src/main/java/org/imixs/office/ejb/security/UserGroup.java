package org.imixs.office.ejb.security;

import javax.persistence.Id;

/**
 * User object to provide a database for username/password/groups. The user
 * object is managed by the UserDBPlugin.
 * 
 * @author rsoika
 * 
 */
@javax.persistence.Entity
public class UserGroup implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	private String id;
	
	
	/**
	 * default constructor for JPA
	 */
	public UserGroup() {
		super();
	}

	/**
	 * A User will be automatically initialized with an id and password
	 */
	public UserGroup(String aid) {
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

	
	
	
}
