package com.nusantara.automate.report;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.nusantara.automate.reader.TemplateReader;
import com.nusantara.automate.util.DateUtils;

public class ReportManager {

	private static final String TESTCASE_TEMPLATE = "testcase.template";
	private static final String SCEN_TEMPLATE = "scen.template";
	private static final String DATA_TEMPLATE = "data.template";
	private static final String SNAPSHOT_TEMPLATE = "snapshot.template";
	
	private static final String EL_TEST_CASE_ID = "${testcase_id}";
	private static final String EL_NUM_SCEN = "${num_of_scen}";
	private static final String EL_NUM_FAIL = "${num_of_failed}";
	private static final String EL_NUM_DATA = "${num_of_data}";
	private static final String EL_STATUS = "${status}";
	private static final String EL_TIMESTAMP = "${timestamp}";
	private static final String EL_CSS_FLAG = "${css_flag}";
	private static final String EL_TSCEN_ID = "${tscen_id}";
	private static final String EL_IMG_FILE = "${img_file}";
	private static final String EL_ROW = "${row}";
	private static final String EL_SESSION_ID = "${session_id}";
	private static final String EL_SESSION_DATA = "${session_data}";
	private static final String EL_META_DATA = "${meta_data}";
	private static final String EL_FAILED_DATA = "${failed_data}";
	private static final String EL_LOG_ERROR = "${log_error}";

	
	private static final String EL_DATA_HTML = "${data_html}";
	private static final String EL_SNAPHSOT_HTML = "${snapshot_html}";
	private static final String EL_SCEN_HTML = "${scen_html}";
	
	private static final String FN_LOOP = "@loop";
	private static final String FN_END_LOOP = "@endloop";
	private static final String FN_EMBEDED_CODE = "@embeded-code";
	
    
	private static final String CSS_RED = "failed";
	private static final String CSS_GREEN = "success";
	
	public static final String PASSED = "PASSED";
	public static final String FAILED = "FAILED";
	public static final String HALTED = "HALTED";
	public static final String INPROGRESS = "INPROGRESS";

	private static final List<String> banners = new ArrayList<String>();
	static {
		banners.add("logo.png");
		banners.add("banner_bg.png");
	}
	
	private String templateDir;
	
	private String reportDir;

	private String startDate;
	
	private String reportDateFolder;
	
	public ReportManager(String templateDir, String reportDir, String startTimeMilis) {
		this.templateDir = templateDir;
		this.reportDir = reportDir;
		this.startDate = DateUtils.format(new Date(Long.valueOf(startTimeMilis)), "YYYY-MM-DD HH:MM:SS");
		this.reportDateFolder =  DateUtils.format(new Date(Long.valueOf(startTimeMilis)), "yyyyMMdd_hhmmss");
	}
	
	public String getTemplate(String templateCode) {
		TemplateReader testCaseReader =  new TemplateReader(new File(templateDir + "\\" + templateCode));
		return testCaseReader.read().toString();
	}

	public void createReport() throws IOException {
		Map<String, String> templates = new HashMap<String, String>();
		templates.put(TESTCASE_TEMPLATE, getTemplate(TESTCASE_TEMPLATE));
		templates.put(SCEN_TEMPLATE, getTemplate(SCEN_TEMPLATE));
		templates.put(DATA_TEMPLATE, getTemplate(DATA_TEMPLATE));
		templates.put(SNAPSHOT_TEMPLATE, getTemplate(SNAPSHOT_TEMPLATE));
	
		String htmlFilename = reportDir + "\\" + reportDateFolder + "\\report.html";
		createReportHtml(new File(htmlFilename), templates);
		for (String banner : banners) {
			FileUtils.copyFile(new File(templateDir + "\\" + banner), new File(reportDir + "\\" + reportDateFolder + "\\" + banner));			
		}
	}
	
	public void createReportHtml(File file, Map<String, String> templates) throws IOException {
		String template = templates.get(TESTCASE_TEMPLATE);
		if (template == null || template.isEmpty()) return;
		
		String row = template.substring(template.indexOf(FN_LOOP), (template.indexOf(FN_END_LOOP)+FN_END_LOOP.length()));
		template = template.replace(row, FN_EMBEDED_CODE);
		row = row.replace(FN_LOOP, "").replace(FN_END_LOOP, "");
		
		StringBuffer sb = new StringBuffer();
		for (TestCaseEntry testCaseEntry :  ReportMonitor.getTestCaseEntries()) {
			// create report.html
			String rowIdx = row;
			rowIdx = replaceVar(rowIdx, EL_TEST_CASE_ID, testCaseEntry.getTestCaseId());
			rowIdx = replaceVar(rowIdx, EL_NUM_SCEN, testCaseEntry.getNumOfScen());
			rowIdx = replaceVar(rowIdx, EL_NUM_FAIL, testCaseEntry.getNumOfFailed());
			rowIdx = replaceVar(rowIdx, EL_NUM_DATA, testCaseEntry.getNumOfData());
			rowIdx = replaceVar(rowIdx, EL_STATUS, testCaseEntry.getStatus());
			rowIdx = replaceVar(rowIdx, EL_SCEN_HTML, "./" + testCaseEntry.getTestCaseId() + "/" + testCaseEntry.getTestCaseId() + ".html");
			if (testCaseEntry.getStatus().equals(FAILED) || testCaseEntry.getStatus().equals(HALTED)) {
				rowIdx = replaceVar(rowIdx, EL_CSS_FLAG, CSS_RED);				
			} else {
				rowIdx = replaceVar(rowIdx, EL_CSS_FLAG, "");
			}
			sb.append(rowIdx);
			
			String filename = reportDir + "\\" + reportDateFolder + "\\" + testCaseEntry.getTestCaseId() + "\\" + testCaseEntry.getTestCaseId() + ".html";
			createScenHtml(new File(filename), templates, testCaseEntry.getTestCaseId());
		}

		template = template.replace(EL_TIMESTAMP, startDate);
		String completedText = template.replace(FN_EMBEDED_CODE, sb.toString());
		FileUtils.writeStringToFile(file, completedText, "UTF-8");
	}
 	
	
	public void createScenHtml(File file, Map<String, String> templates, String testCaseId) throws IOException {
		String template = templates.get(SCEN_TEMPLATE);
		if (template == null || template.isEmpty()) return;
		
		String row = template.substring(template.indexOf(FN_LOOP), (template.indexOf(FN_END_LOOP)+FN_END_LOOP.length()));
		template = template.replace(row, FN_EMBEDED_CODE);
		row = row.replace(FN_LOOP, "").replace(FN_END_LOOP, "");
		
		StringBuffer sb = new StringBuffer();
		for (ScenEntry scenEntry : ReportMonitor.getScenEntries(testCaseId)) {					
			// create data.html
			String rowIdx = row;
			rowIdx = replaceVar(rowIdx, EL_SNAPHSOT_HTML, (ReportMonitor.getImageEntries(scenEntry.getTscanId()) != null 
					&& ReportMonitor.getImageEntries(scenEntry.getTscanId()).size() > 0 ? scenEntry.getTscanId()+ "_snapshot.html" : "#"));
			rowIdx = replaceVar(rowIdx, EL_DATA_HTML, (ReportMonitor.getDataEntries(scenEntry.getTscanId()) != null 
					&& ReportMonitor.getDataEntries(scenEntry.getTscanId()).size() > 0 ? scenEntry.getTscanId()+ "_data.html" : "#"));
			rowIdx = replaceVar(rowIdx, EL_TSCEN_ID, scenEntry.getTscanId());
			rowIdx = replaceVar(rowIdx, EL_NUM_DATA, scenEntry.getNumOfData());
			rowIdx = replaceVar(rowIdx, EL_FAILED_DATA, scenEntry.getFailedRow());
			rowIdx = replaceVar(rowIdx, EL_LOG_ERROR, scenEntry.getErrorLog());
			rowIdx = replaceVar(rowIdx, EL_STATUS, scenEntry.getStatus());
			
			if (scenEntry.getStatus().equals(FAILED) || scenEntry.getStatus().equals(HALTED)) {
				rowIdx = replaceVar(rowIdx, EL_CSS_FLAG, CSS_RED);				
			} else {
				rowIdx = replaceVar(rowIdx, EL_CSS_FLAG, "");
			}
			sb.append(rowIdx);
			
			String dataFilename = reportDir + "\\" + reportDateFolder + "\\" + testCaseId + "\\"+ scenEntry.getTscanId()+ "_data.html";
			createDataHtml(new File(dataFilename), templates, scenEntry.getTscanId(), testCaseId);
			
			String snapshotFilename = reportDir + "\\" + reportDateFolder + "\\" + testCaseId + "\\"+ scenEntry.getTscanId()+ "_snapshot.html";
			createSnapshotHtml(new File(snapshotFilename), templates, scenEntry.getTscanId(), testCaseId);
		}
		
		template = template.replace(EL_TIMESTAMP, startDate);
		template = template.replace(EL_TEST_CASE_ID, testCaseId);
		String completedText = template.replace(FN_EMBEDED_CODE, sb.toString());
		FileUtils.writeStringToFile(file, completedText, "UTF-8");
	}
	
	private void createDataHtml(File file, Map<String, String> templates, String tscanId, String testCaseId) throws IOException {

		LinkedList<DataEntry> dataEntries = ReportMonitor.getDataEntries(tscanId);
		if (dataEntries != null) {
			String template = templates.get(DATA_TEMPLATE);
			if (template == null || template.isEmpty()) return;
			
			String row = template.substring(template.indexOf(FN_LOOP), (template.indexOf(FN_END_LOOP)+FN_END_LOOP.length()));
			template = template.replace(row, FN_EMBEDED_CODE);
			row = row.replace(FN_LOOP, "").replace(FN_END_LOOP, "");
			
			StringBuffer sb = new StringBuffer();
			
			int no = 1;
			for (DataEntry dataEntry : dataEntries) {					
				// create data.html
				String rowIdx = row;
				rowIdx = replaceVar(rowIdx, EL_ROW, no);
				rowIdx = replaceVar(rowIdx, EL_META_DATA, dataEntry.getMetaData());
				rowIdx = replaceVar(rowIdx, EL_SESSION_ID, dataEntry.getSessionId());
				rowIdx = replaceVar(rowIdx, EL_SESSION_DATA, dataEntry.getSessionData());
				if (dataEntry.getStatus().equals(FAILED)) {
					rowIdx = replaceVar(rowIdx, EL_CSS_FLAG, CSS_RED);				
				} else {
					rowIdx = replaceVar(rowIdx, EL_CSS_FLAG, "");
				}
				sb.append(rowIdx);
				no++;
			}
		
			template = template.replace(EL_TIMESTAMP, startDate);
			template = template.replace(EL_TEST_CASE_ID, testCaseId);
			template = template.replace(EL_TSCEN_ID, tscanId);
			String completedText = template.replace(FN_EMBEDED_CODE, sb.toString());
			FileUtils.writeStringToFile(file, completedText, "UTF-8");
		}
	}
	
	private void createSnapshotHtml(File file, Map<String, String> templates, String tscanId, String testCaseId) throws IOException {
		LinkedList<ImageEntry> imageEntries = ReportMonitor.getImageEntries(tscanId);
		
		if (imageEntries != null) {
			String template = templates.get(SNAPSHOT_TEMPLATE);
			if (template == null || template.isEmpty()) return;
			
			String row = template.substring(template.indexOf(FN_LOOP), (template.indexOf(FN_END_LOOP)+FN_END_LOOP.length()));
			template = template.replace(row, FN_EMBEDED_CODE);
			row = row.replace(FN_LOOP, "").replace(FN_END_LOOP, "");
			
			StringBuffer sb = new StringBuffer();
			for (ImageEntry imageEntry : imageEntries) {					
				// create data.html
				String rowIdx = row;
				rowIdx = replaceVar(rowIdx, EL_IMG_FILE, imageEntry.getImgFile());
				if (imageEntry.getStatus().equals(FAILED)) {
					rowIdx = replaceVar(rowIdx, EL_CSS_FLAG, CSS_RED);				
				} else {
					rowIdx = replaceVar(rowIdx, EL_CSS_FLAG, "");
				}
				
				sb.append(rowIdx);
			}
			
			template = template.replace(EL_TIMESTAMP, startDate);
			template = template.replace(EL_TEST_CASE_ID, testCaseId);
			template = template.replace(EL_TSCEN_ID, tscanId);
			String completedText = template.replace(FN_EMBEDED_CODE, sb.toString());
			FileUtils.writeStringToFile(file, completedText, "UTF-8");
		}
	}

	private String replaceVar(String text, String var, List<?> value) {
		String val = "";
		for (Object o : value) {
			val = val + "#" + removeBracket(o.toString());

		}
		return text.replace(var, val);
	}

	
	private String replaceVar(String text, String var, Object value) {
		return text.replace(var, removeBracket(value.toString()));
	}
	
	private String removeBracket(String val) {
		return val.replace("[", "").replace("]", "").replace("{", "").replace("}", "");
	}
}
