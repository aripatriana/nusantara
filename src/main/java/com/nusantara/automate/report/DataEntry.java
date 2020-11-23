package com.nusantara.automate.report;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class DataEntry {

	private String testCaseId;
	
	private String tscenId;
	
	private String row;
	
	private String sessionId;
	
	private Map<String, Object> sessionData = new HashMap<String, Object>();
	
	private String errorLog = "";
	
	private LinkedList<Map<String, Object>> metaData = new LinkedList<Map<String, Object>>();
	
	private String status;

	public String getTestCaseId() {
		return testCaseId;
	}

	public void setTestCaseId(String testCaseId) {
		this.testCaseId = testCaseId;
	}

	public String getScenId() {
		return tscenId;
	}

	public void setScenId(String scenId) {
		this.tscenId = scenId;
	}

	public String getRow() {
		return row;
	}

	public void setRow(String row) {
		this.row = row;
	}

	public boolean checkMetaData(Map<String, Object> metadata) {
		if (metadata != null) {
			for (Map<String, Object> temp : metaData) {
				if (temp.equals(metadata)) return true;
			}
		}
		return false;
	}
	
	public LinkedList<Map<String, Object>> getMetaData() {
		return metaData;
	}
	
	public void addMetaData(Map<String, Object> metaData) {
		if (metaData == null) return;
		this.metaData.add(metaData);
	}
	
	public void addAllMetaData(LinkedList<Map<String, Object>> metadataList) {
		if (metadataList == null) return;
		this.metaData.addAll(metadataList);
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public void setErrorLog(String errorLog) {
		this.errorLog = errorLog;
	}
	
	public String getErrorLog() {
		return errorLog;
	}
	
	public String getSessionId() {
		return sessionId;
	}
	
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	
	public Map<String, Object> getSessionData() {
		return sessionData;
	}
	
	public void setSessionData(Map<String, Object> sessionData) {
		this.sessionData = sessionData;
	}
	
	public void appendErrorLog(String error) {
		if (error == null) return;
		StringBuffer sb = new StringBuffer(this.errorLog);
		sb.append(System.lineSeparator());
		sb.append(error);
		errorLog = sb.toString();
	}

	@Override
	public String toString() {
		return "DataEntry [testCaseId=" + testCaseId + ", tscenId=" + tscenId + ", row=" + row + ", metaData=" + metaData
				+ ", status=" + status + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((testCaseId == null) ? 0 : testCaseId.hashCode());
		result = prime * result + ((tscenId == null) ? 0 : tscenId.hashCode());
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
		DataEntry other = (DataEntry) obj;
		if (testCaseId == null) {
			if (other.testCaseId != null)
				return false;
		} else if (!testCaseId.equals(other.testCaseId))
			return false;
		if (tscenId == null) {
			if (other.tscenId != null)
				return false;
		} else if (!tscenId.equals(other.tscenId))
			return false;
		return true;
	}
	
}
