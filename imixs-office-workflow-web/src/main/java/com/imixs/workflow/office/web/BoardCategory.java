package com.imixs.workflow.office.web;

/**
 * This class builds a category used by the BoardController
 * 
 * A board category is split into the fields WorkflowGorup, Status and ProcessID
 * 
 * @author rsoika
 *
 */
public class BoardCategory {
	
	private static final int DEFAULT_PAGE_SIZE = 5;

	String workflowGroup;
	String workflowStatus;
	int processID;
	int pageIndex=0;
	int pageSize=DEFAULT_PAGE_SIZE;
	
	public BoardCategory(String workflowGroup, String workfowStatus, int processID, int pageSize) {
		super();
		this.workflowGroup = workflowGroup;
		this.workflowStatus = workfowStatus;
		this.processID = processID;
		this.pageSize = pageSize;
	}
	
	

	@Override
	public String toString() {
		return workflowGroup + "/" + processID;
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return this.toString().equals(obj.toString());
	}

	public String getWorkflowGroup() {
		return workflowGroup;
	}

	public void setWorkflowGroup(String workflowGroup) {
		this.workflowGroup = workflowGroup;
	}

	public String getWorkflowStatus() {
		return workflowStatus;
	}

	public void setWorkflowStatus(String workflowStatus) {
		this.workflowStatus = workflowStatus;
	}

	public int getProcessID() {
		return processID;
	}

	public void setProcessID(int processID) {
		this.processID = processID;
	}



	public int getPageIndex() {
		return pageIndex;
	}



	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
	}



	public int getPageSize() {
		return pageSize;
	}



	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	
	

}
