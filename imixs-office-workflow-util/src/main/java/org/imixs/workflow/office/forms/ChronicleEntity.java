package org.imixs.workflow.office.forms;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.ItemCollectionComparator;

public class ChronicleEntity {

	Date date;
	String user;
	List<ItemCollection> entries;

	/**
	 * Creates a new ChronicleEntity for the given user and date.
	 * The date is normalized to minute precision via {@link #setDate(Date)},
	 * ensuring consistent behaviour regardless of the call site.
	 *
	 * @param user the user name; defaults to empty string if {@code null}
	 * @param date the timestamp; defaults to current time if {@code null}
	 */
	public ChronicleEntity(String user, Date date) {
		if (user == null) {
			// should not happen
			user = "";
		}
		this.user = user;
		setDate(date);
		entries = new ArrayList<ItemCollection>();
	}

	public Date getDate() {
		return date;
	}

	/**
	 * Sets the date of this chronicle entry.<br>
	 * The value is normalized to minute precision (seconds and milliseconds
	 * are truncated) so that {@link #equals(Object)} and {@link #hashCode()}
	 * remain consistent regardless of the date source.
	 *
	 * @param date the timestamp; defaults to current time if {@code null}
	 */
	public void setDate(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date == null ? new Date() : date);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		this.date = cal.getTime();
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public List<ItemCollection> getEntries() {
		return entries;
	}

	public void addEntry(ItemCollection entry) {
		entries.add(entry);
		// sort by date
		Collections.sort(entries, new ItemCollectionComparator("date", true));

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + date.hashCode();
		result = prime * result + ((user == null) ? 0 : user.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ChronicleEntity other = (ChronicleEntity) obj;
		if (!date.equals(other.date))
			return false;
		if (user == null) {
			if (other.user != null)
				return false;
		} else if (!user.equals(other.user))
			return false;
		return true;
	}

}
