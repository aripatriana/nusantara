package com.nusantara.automate.report;

public class ScenEntry {

	private String testCaseId;
	
	private String tscanId;
	
	private int numOfData;
	
	private int failedRow;
	
	private String errorLog = "";
	
	private String status;

	public String getTestCaseId() {
		return testCaseId;
	}

	public void setTestCaseId(String testCaseId) {
		this.testCaseId = testCaseId;
	}

	public String getTscanId() {
		return tscanId;
	}

	public void setTscanId(String tscanId) {
		this.tscanId = tscanId;
	}

	public int getNumOfData() {
		return numOfData;
	}

	public void setNumOfData(int numOfData) {
		this.numOfData = numOfData;
	}

	public int getFailedRow() {
		return failedRow;
	}

	public void setFailedRow(int failedRow) {
		this.failedRow = failedRow;
	}
	
	public String getErrorLog() {
		return errorLog;
	}
	
	public void setErrorLog(String errorLog) {
		this.errorLog = errorLog;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	public void appendErrorLog(String error) {
		StringBuffer sb = new StringBuffer(this.errorLog);
		sb.append(System.lineSeparator());
		sb.append(error);
		errorLog = sb.toString();
	}

	@Override
	public String toString() {
		return "ScenEntry [testCaseId=" + testCaseId + ", tscanId=" + tscanId + ", numOfData=" + numOfData
				+ ", failedRow=" + failedRow + ", errorLog=" + errorLog + ", status=" + status + "]";
	}
}
