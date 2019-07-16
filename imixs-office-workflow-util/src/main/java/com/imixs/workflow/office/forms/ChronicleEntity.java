package com.imixs.workflow.office.forms;

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
	
	List<ItemCollection> historyEntries;
	List<ItemCollection> commentEntries;
	List<ItemCollection> fileEntries;
	List<ItemCollection> versionEntries;
	List<ItemCollection> referencesEntries;
	
	
	
	
	public ChronicleEntity(String user, Date date) {
		super();
		this.user = user;
		
		
		// cut milis and seconds..
		Calendar cal=Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND, 0);
		this.date = cal.getTime();
		
		historyEntries=new ArrayList<ItemCollection>();
		commentEntries=new ArrayList<ItemCollection>();
		fileEntries=new ArrayList<ItemCollection>();
		versionEntries=new ArrayList<ItemCollection>();
		referencesEntries=new ArrayList<ItemCollection>();
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
	public List<ItemCollection> getHistoryEntries() {
		return historyEntries;
	}
	public void setHistoryEntries(List<ItemCollection> historyEntries) {
		this.historyEntries = historyEntries;
	}
	public List<ItemCollection> getCommentEntries() {
		return commentEntries;
	}
	public void setCommentEntries(List<ItemCollection> commentEntries) {
		this.commentEntries = commentEntries;
	}
	public List<ItemCollection> getFileEntries() {
		return fileEntries;
	}
	public void setFileEntries(List<ItemCollection> fileEntries) {
		this.fileEntries = fileEntries;
	}
	public List<ItemCollection> getVersionEntries() {
		return versionEntries;
	}
	public void setVersionEntries(List<ItemCollection> versionEntries) {
		this.versionEntries = versionEntries;
	}
	public List<ItemCollection> getReferencesEntries() {
		return referencesEntries;
	}
	public void setReferencesEntries(List<ItemCollection> referencesEntries) {
		this.referencesEntries = referencesEntries;
	}
	
	
	
	public List<ItemCollection> getAllEntries() {
		List<ItemCollection> result=new ArrayList<ItemCollection>();
		
		result.addAll(historyEntries);
		result.addAll(commentEntries);
		result.addAll(fileEntries);
		result.addAll(versionEntries);
		result.addAll(referencesEntries);
		
		Collections.sort(result, new ItemCollectionComparator("date", true));
		
		
		
		return result;
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
