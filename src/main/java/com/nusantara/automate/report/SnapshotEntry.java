package com.nusantara.automate.report;

public class SnapshotEntry {

	public final static String SNAPSHOT_AS_IMAGE = "snapshot_as_image";
	public final static String SNAPSHOT_AS_RAWTEXT = "snapshot_as_rawtext";
	public final static String SNAPSHOT_AS_CHECKPOINT = "snapshot_as_checkpoint";
	
	private String testCaseId;
	
	private String tscenId;
	
	private String sessionId;
	
	private String row;
	
	private String snapshotAs;
	
	private String rawText;
	
	private String imgFile;
	
	private String status;

	public SnapshotEntry() {
		this.snapshotAs = SNAPSHOT_AS_IMAGE;
	}
	
	public String getSessionId() {
		return sessionId;
	}
	
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	
	public String getTestCaseId() {
		return testCaseId;
	}

	public void setTestCaseId(String testCaseId) {
		this.testCaseId = testCaseId;
	}

	public String getTscenId() {
		return tscenId;
	}

	public void setTscenId(String tscenId) {
		this.tscenId = tscenId;
	}

	public String getRow() {
		return row;
	}

	public void setRow(String row) {
		this.row = row;
	}

	public String getImgFile() {
		return imgFile;
	}

	public void setImgFile(String imgFile) {
		this.imgFile = imgFile;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getSnapshotAs() {
		return snapshotAs;
	}

	public void setSnapshotAs(String snapshotAs) {
		this.snapshotAs = snapshotAs;
	}

	public String getRawText() {
		return rawText;
	}

	public void setRawText(String rawText) {
		this.rawText = rawText;
	}

	@Override
	public String toString() {
		return "SnapshotEntry [testCaseId=" + testCaseId + ", tscenId=" + tscenId + ", sessionId=" + sessionId
				+ ", row=" + row + ", snapshotAs=" + snapshotAs + ", rawText=" + rawText + ", imgFile=" + imgFile
				+ ", status=" + status + "]";
	}
}
