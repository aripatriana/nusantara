package com.nusantara.automate.report;

public class TestCaseEntry {

	private String testCaseId;
	
	private int numOfScen;
	
	private int numOfFailed;
	
	private int numOfData;
	
	private String status;

	public String getTestCaseId() {
		return testCaseId;
	}
	
	public void setTestCaseId(String testCaseId) {
		this.testCaseId = testCaseId;
	}

	public int getNumOfScen() {
		return numOfScen;
	}

	public void setNumOfScen(int numOfScen) {
		this.numOfScen = numOfScen;
	}

	public int getNumOfFailed() {
		return numOfFailed;
	}

	public void setNumOfFailed(int numOfFailed) {
		this.numOfFailed = numOfFailed;
	}

	public int getNumOfData() {
		return numOfData;
	}

	public void setNumOfData(int numOfData) {
		this.numOfData = numOfData;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "TesetCaseEntry [testCaseId=" + testCaseId + ", numOfScen=" + numOfScen + ", numOfFailed=" + numOfFailed
				+ ", numOfData=" + numOfData + ", status=" + status + "]";
	}
	
	
}
