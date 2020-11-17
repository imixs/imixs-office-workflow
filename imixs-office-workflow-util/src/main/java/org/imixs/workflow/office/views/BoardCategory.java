package org.imixs.workflow.office.views;

/**
 * This class builds a category used by the BoardController
 * 
 * A board category is split into the fields WorkflowGorup, Status and TaskID
 * 
 * @author rsoika
 *
 */
public class BoardCategory {

	private static final int DEFAULT_PAGE_SIZE = 5;

	String workflowGroup;
	String workflowStatus;
	int taskID;
	int pageIndex = 0;
	int pageSize = DEFAULT_PAGE_SIZE;

	public BoardCategory(String workflowGroup, String workfowStatus, int processID, int pageSize) {
		super();
		this.workflowGroup = workflowGroup;
		this.workflowStatus = workfowStatus;
		this.taskID = processID;
		this.pageSize = pageSize;
	}

	@Override
	public String toString() {
		return workflowGroup + "/" + taskID;
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

	public int getTaskID() {
		return taskID;
	}

	public void setTaskID(int id) {
		this.taskID = id;
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
