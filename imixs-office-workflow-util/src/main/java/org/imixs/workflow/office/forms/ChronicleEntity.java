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

	public ChronicleEntity(String user, Date date) {
		super();
		if (date == null) {
			// should not happen
			date = new Date();
		}
		if (user == null) {
			// should not happen
			user = "";
		}
		this.user = user;

		// cut milis and seconds..
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.MILLISECOND, 0);
		//cal.set(Calendar.SECOND, 0);
		this.date = cal.getTime();
		entries = new ArrayList<ItemCollection>();
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
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
		result = prime * result + ((date == null) ? 0 : date.hashCode());
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
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		if (user == null) {
			if (other.user != null)
				return false;
		} else if (!user.equals(other.user))
			return false;
		return true;
	}

}
