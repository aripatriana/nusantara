package com.nusantara.automate.report;

public class ImageEntry {

	private String testCaseId;
	
	private String tscenId;
	
	private String row;
	
	private String imgFile;
	
	private String status;

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

	@Override
	public String toString() {
		return "ImageEntry [testCaseId=" + testCaseId + ", tscenId=" + tscenId + ", row=" + row + ", imgFile=" + imgFile
				+ ", status=" + status + "]";
	}
}
