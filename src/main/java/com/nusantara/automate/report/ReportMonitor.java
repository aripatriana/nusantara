package com.nusantara.automate.report;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ReportMonitor {

	
	private static LinkedList<TestCaseEntry> testCaseEntries = new LinkedList<TestCaseEntry>();
	private static Map<String, LinkedList<ScenEntry>> scenEntries = new HashMap<String, LinkedList<ScenEntry>>();  
	private static Map<String, ScenEntry> scenEntriesByTscenId = new HashMap<String, ScenEntry>();
	private static LinkedHashMap<String, DataEntry> dataEntryBySessionId = new LinkedHashMap<String, DataEntry>();
	private static Map<String, LinkedList<DataEntry>> dataEntries = new HashMap<String, LinkedList<DataEntry>>();
	private static Map<String, LinkedList<ImageEntry>> imageEntries = new HashMap<String, LinkedList<ImageEntry>>();
	
	public static void addTestCaseEntry(TestCaseEntry testCaseEntry, LinkedList<ScenEntry> scenEntryList) {
		for (ScenEntry scenEntry : scenEntryList) {
			scenEntriesByTscenId.put(scenEntry.getTscanId(), scenEntry);			
		}
		testCaseEntries.add(testCaseEntry);
		scenEntries.put(testCaseEntry.getTestCaseId(), scenEntryList);
	}
	
	public static ScenEntry getScenEntry(String tscenId) {
		return scenEntriesByTscenId.get(tscenId);
	}
	
	public static TestCaseEntry getTestCaseEntry(String testCaseId) {
		for (TestCaseEntry testCase : testCaseEntries) {
			if (testCase.getTestCaseId().equals(testCaseId)) {
				return testCase;
			}
		}
		return null;
	}
	
	public static ScenEntry getScenEntry(String testCaseId, String scenId) {
		for (ScenEntry scenEntry : scenEntries.get(testCaseId)) {
			if (scenEntry.getTscanId().equals(scenId)) {
				return scenEntry;
			}
		}
		return null;
	}
	
	public static void logDataEntry(List<String> sessionId, String testCaseId, String tscenId, Map<String, Object> sessionData, Map<String, Object> rawdata) {
		for (String session : sessionId) {
			logDataEntry(session, testCaseId, tscenId, sessionData, rawdata, null, ReportManager.PASSED);			
		}
	}
	
	public static void logDataEntry(String sessionId, String testCaseId, String tscenId, Map<String, Object> sessionData, Map<String, Object> rawdata) {
		logDataEntry(sessionId, testCaseId, tscenId, sessionData, rawdata, null, ReportManager.PASSED);
	}

	public static void logDataEntry(List<String> sessionId, String testCaseId, String tscenId, Map<String, Object> sessionData, Map<String, Object> rawdata, String errorLog, String status) {
		for (String session : sessionId) {
			logDataEntry(session, testCaseId, tscenId, sessionData, rawdata, errorLog, status);
		}
	}
	
	public static void logDataEntry(String sessionId, String testCaseId, String tscenId, Map<String, Object> sessionData, Map<String, Object> rawdata, String errorLog, String status) {
		DataEntry dataEntry = new DataEntry();
		dataEntry.addMetaData(rawdata);
		dataEntry.setScenId(tscenId);
		dataEntry.setSessionId(sessionId);
		dataEntry.setSessionData(sessionData);
		dataEntry.setStatus(status);
		dataEntry.appendErrorLog(errorLog);
		dataEntry.setTestCaseId(testCaseId);
		logDataEntry(sessionId, dataEntry);
	}
	
	public static void logDataEntry(String sessionId, DataEntry dataEntry) {
		DataEntry data = dataEntryBySessionId.get(sessionId);
		if (data == null) {
			LinkedList<DataEntry> dataEntryList = dataEntries.get(dataEntry.getScenId());
			if (dataEntryList == null)  dataEntryList = new LinkedList<DataEntry>();
			dataEntryList.add(dataEntry);
			
			dataEntries.put(dataEntry.getScenId(), dataEntryList);
			dataEntryBySessionId.put(sessionId, dataEntry);
		} else {
			if (data.equals(dataEntry)) {
				data.setStatus(dataEntry.getStatus());
				data.appendErrorLog(dataEntry.getErrorLog());
				if (dataEntry.getSessionData() != null)
					data.setSessionData(dataEntry.getSessionData());
				if(dataEntry.getMetaData().size() > 0)
					if (!data.checkMetaData(dataEntry.getMetaData().get(0))) {
						data.addAllMetaData(dataEntry.getMetaData());
					}
			}
		}
	}
	
	public static void logImageEntry(String testCaseId, String scenId, String filePath, String status) {
		ImageEntry imageEntry = new ImageEntry();
		imageEntry.setTscenId(scenId);
		imageEntry.setTestCaseId(testCaseId);
		imageEntry.setImgFile(filePath);
		imageEntry.setStatus(status);
		logImageEntry(imageEntry);
		
	}
	public static void logImageEntry(ImageEntry imageEntry) {
		LinkedList<ImageEntry> imageEntryTemp = imageEntries.get(imageEntry.getTscenId());
		if (imageEntryTemp == null) imageEntryTemp = new LinkedList<ImageEntry>();
		imageEntryTemp.add(imageEntry);
		imageEntries.put(imageEntry.getTscenId(), imageEntryTemp);
	}
	

	public static LinkedList<TestCaseEntry> getTestCaseEntries() {
		return testCaseEntries;
	}
	
	public static LinkedList<ScenEntry> getScenEntries(String testCaseId) {
		return scenEntries.get(testCaseId);
	}
	
	public static LinkedList<DataEntry> getDataEntries(String tscenId) {
		return dataEntries.get(tscenId);
	}
	
	
	public static LinkedList<ImageEntry> getImageEntries(String tscenId) {
		return imageEntries.get(tscenId);
	}
	
	public static void completeTestCase(String testCaseId) {
		boolean testCaseFailed = false;
		boolean testCaseHalted = false;
		int numfailed = 0;
		int numdata = 0;
		LinkedList<ScenEntry> scenEntryList = scenEntries.get(testCaseId);
		if (scenEntryList != null) {
			for (ScenEntry scen : scenEntries.get(testCaseId)) {
				if (scen.getStatus().equals(ReportManager.FAILED)
						|| scen.getStatus().equals(ReportManager.HALTED)) {
					numfailed++;
					testCaseFailed = true;
				}
				if (scen.getStatus().equals(ReportManager.HALTED)) {
					testCaseHalted = true;
				}
				numdata = numdata + scen.getNumOfData();
			}			
		}
		
		TestCaseEntry testCase = getTestCaseEntry(testCaseId);
		testCase.setNumOfFailed(numfailed);
		testCase.setNumOfData(numdata);
		if (testCaseFailed)
			if (testCaseHalted)
				testCase.setStatus(ReportManager.HALTED);
			else
				testCase.setStatus(ReportManager.FAILED);
		else
			testCase.setStatus(ReportManager.PASSED);
	}
	
	public static void completeScen(String tscenId) {
		ScenEntry scen = scenEntriesByTscenId.get(tscenId);
		if (scen != null) {
			StringBuffer sb = new StringBuffer();
			int numfailed = 0;
			if (scen.getStatus().equals(ReportManager.INPROGRESS)) {
				boolean scenFailed = false;
				if (dataEntries.get(tscenId) != null) {
					for (DataEntry dataEntry : dataEntries.get(tscenId)) {
						if (dataEntry.getStatus().equals(ReportManager.FAILED)
								||dataEntry.getStatus().equals(ReportManager.HALTED)) {
							numfailed++;
							scenFailed=true;
							sb.append(dataEntry.getErrorLog());
							sb.append(System.lineSeparator());
						}
					}					
				}
				
				if (imageEntries.get(tscenId) != null) {
					for (ImageEntry imageEntry : imageEntries.get(tscenId)) {
						if (imageEntry.getStatus().equals(ReportManager.FAILED)
								|| imageEntry.getStatus().equals(ReportManager.HALTED)) {
							scenFailed=true;
							break;
						}
					}					
				}
				
				scen.setFailedRow(numfailed);
				scen.appendErrorLog(sb.toString());
				if (scenFailed) {
					scen.setStatus(ReportManager.FAILED);
				} else {
					scen.setStatus(ReportManager.PASSED);
				}			
			}
		}
	}
	
	public static void scenHalted(String testCaseId, String tscenId, String errorLog) {
		ScenEntry scen = scenEntriesByTscenId.get(tscenId);
		scen.appendErrorLog(errorLog);
		scen.setFailedRow(scen.getNumOfData());
		scen.setStatus(ReportManager.HALTED);
		
		LinkedList<DataEntry> dataEntryList = dataEntries.get(tscenId);
		if (dataEntryList != null) {
			for (DataEntry dataEntry : dataEntryList) {
				if (dataEntry.getStatus().equals(ReportManager.INPROGRESS))
					dataEntry.setStatus(ReportManager.HALTED);
			}
		}
		
		LinkedList<ImageEntry> imageEntryList = imageEntries.get(tscenId);
		if (imageEntryList != null) {
			for (ImageEntry imageEntry : imageEntryList) {
				if (imageEntry.getStatus().equals(ReportManager.INPROGRESS))
					imageEntry.setStatus(ReportManager.HALTED);
			}
		}
	}
	
	public static void testCaseHalted(String testCaseId, String errorLog) {
		boolean setErrorLog = false;
		int numfailed = 0;
		int numdata = 0;
		LinkedList<ScenEntry> scenEntryList = scenEntries.get(testCaseId);
		if (scenEntryList != null) {
			for (ScenEntry scenEntry : scenEntries.get(testCaseId)) {
				if (scenEntry.getStatus().equals(ReportManager.FAILED)) numfailed++;
				if (scenEntry.getStatus().equals(ReportManager.INPROGRESS)) {
					scenEntry.setStatus(ReportManager.HALTED);
					if (!setErrorLog)
						scenEntry.setErrorLog(errorLog);
					scenEntry.setFailedRow(scenEntry.getNumOfData());
					setErrorLog = true;
					numfailed++;
				}
				numdata = numdata + scenEntry.getNumOfData();
			}
		}
		
		TestCaseEntry testCase = getTestCaseEntry(testCaseId);
		testCase.setNumOfData(numdata);
		testCase.setNumOfFailed(numfailed);
		testCase.setStatus(ReportManager.HALTED);
	}
	
	public static void main(String[] args) {
		TestCaseEntry t =new TestCaseEntry();
		t.setTestCaseId("T1");
		
		ScenEntry s = new ScenEntry();
		s.setTscanId("S1");
		
		LinkedList<ScenEntry> en = new LinkedList<ScenEntry>();
		en.add(s);
		
		ReportMonitor.addTestCaseEntry(t, en);
		
		ReportMonitor.scenEntriesByTscenId.get("S1").setErrorLog("ERROR");
		System.out.println(ReportMonitor.scenEntries.get("T1").get(0).getErrorLog());
	}
	
	
}
