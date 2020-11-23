package com.nusantara.automate.report;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;

import com.nusantara.automate.reader.TemplateReader;
import com.nusantara.automate.util.DateUtils;
import com.nusantara.automate.util.IDUtils;
import com.nusantara.automate.util.StringUtils;

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
	private static final String EL_RAWTEXT = "${rawtext}";
	private static final String EL_CHECKPOINT_FILE = "${checkpoint_file}";
	private static final String EL_TYPE_IMAGE = "${type_image}";
	private static final String EL_TYPE_RAWTEXT = "${type_rawtext}";
	private static final String EL_TYPE_CHECKPOINT = "${type_checkpoint}";
	private static final String EL_ROW = "${row}";
	private static final String EL_SESSION_ID = "${session_id}";
	private static final String EL_SESSION_DATA = "${session_data}";
	private static final String EL_META_DATA = "${meta_data}";
	private static final String EL_FAILED_DATA = "${failed_data}";
	private static final String EL_LOG_ERROR = "${log_error}";

	
	private static final String EL_DATA_HTML = "${data_html}";
	private static final String EL_SCRIPT_HTML = "${script_html}";
	private static final String EL_SNAPHSOT_HTML = "${snapshot_html}";
	private static final String EL_SCEN_HTML = "${scen_html}";
	
	private static final String FN_IF = "@if";
	private static final String FN_END_IF = "@endif";
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
		banners.add("style.css");
	}
	
	@Value("{template_dir}")
	private String templateDir;
	
	@Value("{report_dir}")
	private String reportDir;
	
	@Value("{testcase_dir}")
	private String testCaseDir;

	private String startDate;
	
	private String reportDateFolder;
	
	public ReportManager(String startTimeMilis) {
		this.startDate = DateUtils.format(new Date(Long.valueOf(startTimeMilis)), "YYYY-MM-DD HH:MM:SS");
		this.reportDateFolder =  DateUtils.format(new Date(Long.valueOf(startTimeMilis)), "yyyyMMdd_hhmmss");
	}
	
	public String getTemplate(String templateCode) {
		TemplateReader testCaseReader =  new TemplateReader(new File(StringUtils.path(templateDir,templateCode)));
		return testCaseReader.read().toString();
	}

	public void createReport() throws IOException {
		createReportHtml(getTemplate(TESTCASE_TEMPLATE));
		createScenHtml(getTemplate(SCEN_TEMPLATE));
		createDataHtml(getTemplate(DATA_TEMPLATE));
		createSnapshotHtml(getTemplate(SNAPSHOT_TEMPLATE));
		
		for (String banner : banners) {
			FileUtils.copyFile(new File(StringUtils.path(templateDir, banner)), new File(StringUtils.path(reportDir, reportDateFolder, banner)));			
		}
	}
	
	public void createReportHtml(String template) throws IOException {
		TextParser parser = new TextParser(template);
		parser.addParam(EL_TIMESTAMP, startDate);
		for (TestCaseEntry testCaseEntry :  ReportMonitor.getTestCaseEntries()) {
			Map<String, Object> loopParam = new HashMap<String, Object>();
			loopParam.put(EL_TEST_CASE_ID, testCaseEntry.getTestCaseId());
			loopParam.put(EL_NUM_SCEN, testCaseEntry.getNumOfScen());
			loopParam.put(EL_NUM_FAIL, testCaseEntry.getNumOfFailed());
			loopParam.put(EL_NUM_DATA, testCaseEntry.getNumOfData());
			loopParam.put(EL_STATUS, testCaseEntry.getStatus());
			loopParam.put(EL_SCEN_HTML, "./" + testCaseEntry.getTestCaseId() + "/" + testCaseEntry.getTestCaseId() + ".html");
			if (testCaseEntry.getStatus().equals(FAILED) || testCaseEntry.getStatus().equals(HALTED)) {
				loopParam.put(EL_CSS_FLAG, CSS_RED);
			} else {
				loopParam.put(EL_CSS_FLAG, "");
			}
			parser.addLoopParam(loopParam);
		}
		
		FileUtils.writeStringToFile(new File(StringUtils.path(reportDir, reportDateFolder, "report.html")), 
				parser.parse(), "UTF-8");
	}

	public void createScenHtml(String template) throws IOException {
		for (TestCaseEntry testCaseEntry :  ReportMonitor.getTestCaseEntries()) {
			TextParser parser = new TextParser(template);
			parser.addParam(EL_TIMESTAMP, startDate);
			parser.addParam(EL_TEST_CASE_ID, testCaseEntry.getTestCaseId());
			for (ScenEntry scenEntry : ReportMonitor.getScenEntries(testCaseEntry.getTestCaseId())) {		
				Map<String, Object> loopParam = new HashMap<String, Object>();
				loopParam.put(EL_SNAPHSOT_HTML, (ReportMonitor.getImageEntries(scenEntry.getTscanId()) != null 
						&& ReportMonitor.getImageEntries(scenEntry.getTscanId()).size() > 0 ? scenEntry.getTscanId()+ "_snapshot.html" : "#"));
				loopParam.put(EL_DATA_HTML, (ReportMonitor.getDataEntries(scenEntry.getTscanId()) != null 
						&& ReportMonitor.getDataEntries(scenEntry.getTscanId()).size() > 0 ? scenEntry.getTscanId()+ "_data.html" : "#"));
				loopParam.put(EL_SCRIPT_HTML, scenEntry.getTscanId().replace(scenEntry.getTestCaseId() + "_", "") + ".y");
				loopParam.put(EL_TSCEN_ID, scenEntry.getTscanId());
				loopParam.put(EL_NUM_DATA, scenEntry.getNumOfData());
				loopParam.put(EL_FAILED_DATA, scenEntry.getFailedRow());
				loopParam.put(EL_LOG_ERROR, (scenEntry.getErrorLog().isEmpty() ? "-" : scenEntry.getErrorLog()));
				loopParam.put(EL_STATUS, scenEntry.getStatus());
				if (scenEntry.getStatus().equals(FAILED) || scenEntry.getStatus().equals(HALTED)) {
					loopParam.put(EL_CSS_FLAG, CSS_RED);				
				} else {
					loopParam.put(EL_CSS_FLAG, "");
				}
				parser.addLoopParam(loopParam);
				
				// copy y script file
				FileUtils.copyFile(
						new File(StringUtils.path(testCaseDir, testCaseEntry.getTestCaseId(), scenEntry.getTscanId().replace(testCaseEntry.getTestCaseId() + "_", "") + ".y")),
						new File(StringUtils.path(reportDir, reportDateFolder, testCaseEntry.getTestCaseId(), scenEntry.getTscanId().replace(testCaseEntry.getTestCaseId() + "_", "") + ".y")));

			}
			
			FileUtils.writeStringToFile(new File(StringUtils.path(reportDir, reportDateFolder, testCaseEntry.getTestCaseId(), testCaseEntry.getTestCaseId() + ".html")), 
					parser.parse(), "UTF-8");
		}
	}

	public void createDataHtml(String template) throws IOException {
		for (TestCaseEntry testCaseEntry :  ReportMonitor.getTestCaseEntries()) {
			for (ScenEntry scenEntry : ReportMonitor.getScenEntries(testCaseEntry.getTestCaseId())) {	
				LinkedList<DataEntry> dataEntries = ReportMonitor.getDataEntries(scenEntry.getTscanId());
				if (dataEntries != null) {
					TextParser parser = new TextParser(template);
					parser.addParam(EL_TIMESTAMP, startDate);
					parser.addParam(EL_TEST_CASE_ID, testCaseEntry.getTestCaseId());
					parser.addParam(EL_TSCEN_ID, scenEntry.getTscanId());
					
					int no = 1;
					for (DataEntry dataEntry : dataEntries) {					
						Map<String, Object> loopParam = new HashMap<String, Object>();
						loopParam.put(EL_ROW, no);
						loopParam.put(EL_META_DATA, dataEntry.getMetaData());
						loopParam.put(EL_SESSION_ID, dataEntry.getSessionId());
						loopParam.put(EL_SESSION_DATA, dataEntry.getSessionData());
						if (dataEntry.getStatus().equals(FAILED)) {
							loopParam.put(EL_CSS_FLAG, CSS_RED);				
						} else {
							loopParam.put(EL_CSS_FLAG, "");
						}
						
						parser.addLoopParam(loopParam);
						no++;
					}

					FileUtils.writeStringToFile(new File(StringUtils.path(reportDir, reportDateFolder, testCaseEntry.getTestCaseId(), scenEntry.getTscanId()+ "_data.html")), 
							parser.parse(), "UTF-8");
				}				
			}
			
		}
	}
	
	public void createSnapshotHtml(String template) throws IOException {
		for (TestCaseEntry testCaseEntry :  ReportMonitor.getTestCaseEntries()) {
			for (ScenEntry scenEntry : ReportMonitor.getScenEntries(testCaseEntry.getTestCaseId())) {	
				LinkedList<SnapshotEntry> imageEntries = ReportMonitor.getImageEntries(scenEntry.getTscanId());
				if (imageEntries != null) {
					TextParser parser = new TextParser(template);
					parser.addParam(EL_TIMESTAMP, startDate);
					parser.addParam(EL_TEST_CASE_ID, testCaseEntry.getTestCaseId());
					parser.addParam(EL_TSCEN_ID, scenEntry.getTscanId());
					
					for (SnapshotEntry snapshotEntry : imageEntries) {				
						Map<String, Object> loopParam = new HashMap<String, Object>();
						
						if (snapshotEntry.getSnapshotAs().equals(SnapshotEntry.SNAPSHOT_AS_IMAGE)) {
							loopParam.put(EL_IMG_FILE, snapshotEntry.getImgFile());
							loopParam.put(EL_TYPE_IMAGE, true);
						} else if (snapshotEntry.getSnapshotAs().equals(SnapshotEntry.SNAPSHOT_AS_RAWTEXT)) {
							loopParam.put(EL_RAWTEXT, snapshotEntry.getRawText());
							loopParam.put(EL_TYPE_RAWTEXT, true);
						} else if(snapshotEntry.getSnapshotAs().equals(SnapshotEntry.SNAPSHOT_AS_CHECKPOINT)) {
							loopParam.put(EL_RAWTEXT, snapshotEntry.getRawText());
							loopParam.put(EL_CHECKPOINT_FILE, snapshotEntry.getImgFile());
							loopParam.put(EL_TYPE_CHECKPOINT, true);
						}
						
						if (snapshotEntry.getStatus().equals(FAILED)) {
							loopParam.put(EL_CSS_FLAG, CSS_RED);				
						} else {
							loopParam.put(EL_CSS_FLAG, "");
						}
						
						parser.addLoopParam(loopParam);
					}

					FileUtils.writeStringToFile(new File(StringUtils.path(reportDir, reportDateFolder, testCaseEntry.getTestCaseId(), scenEntry.getTscanId()+ "_snapshot.html")), 
							parser.parse(), "UTF-8");
				}				
			}
		}
	}
	
	class TextParser {
		
		String template;
		Map<String, String> param  = new HashMap<String, String>();
		List<Map<String, Object>> loopParam = new ArrayList<Map<String,Object>>();
		
		public TextParser(String template) {
			this.template = template;
		}
		
		public void addParam(String key, String value) {
			param.put(key, value);
		}
		
		public void addLoopParam(Map<String, Object> value) {
			loopParam.add(value);
		}
		
		public String parse() {
			if (template == null || template.isEmpty()) return template;
			
			template = parseLoop(template);
			
			Map<String, String> conditions = new HashMap<String, String>();
			parseIf(conditions, template);
			
			// update if
			for (Entry<String, String> condition : conditions.entrySet()) {
				for (Entry<String, String> entry : param.entrySet()) {
					if (condition.getValue().startsWith("(" + entry.getKey()+ ")")) {
						String val = condition.getValue().replace("(" + entry.getKey()+ ")","");
						template = StringUtils.replaceVar(template, condition.getKey(), val);
					}
				}
			}
			
			// update param
			for (Entry<String, String> entry : param.entrySet()) {
				template = StringUtils.replaceVar(template, entry.getKey(), entry.getValue());		
			}
			
			// remove temp
			for (String key : conditions.keySet()) {
				template = template.replace(key, "");
			}
			return template;
		}
		
		private String parseLoop(String text) {
			if (text.indexOf(FN_LOOP) <0) 
				return text;
			
			String row = text.substring(template.indexOf(FN_LOOP), (template.indexOf(FN_END_LOOP)+FN_END_LOOP.length()));
			text = text.replace(row, FN_EMBEDED_CODE);
			row = row.replace(FN_LOOP, "").replace(FN_END_LOOP, "");
			if (!row.trim().isEmpty()) {
				Map<String, String> conditions = new HashMap<String, String>();
				row = parseIf(conditions, row);
				
				StringBuffer sb = new StringBuffer();
				for (Map<String, Object> loop : loopParam) {
					String temp = row;
					
					// update if
					for (Entry<String, String> condition : conditions.entrySet()) {
						for (Entry<String, Object> entry : loop.entrySet()) {
							if (condition.getValue().startsWith("(" + entry.getKey()+ ")")) {
								String val = condition.getValue().replace("(" + entry.getKey()+ ")","");
								temp = temp.replace(condition.getKey(), val);
							}
						}
					}
					
					// update param
					for (Entry<String, Object> entry : loop.entrySet()) {
						temp = StringUtils.replaceVar(temp, entry.getKey(), entry.getValue());
					}
					
					// remove temp
					for (String key : conditions.keySet()) {
						temp = temp.replace(key, "");
					}
					sb.append(temp);
				}
				
				text = text.replace(FN_EMBEDED_CODE, sb.toString());				
			}
			
			return text;
		}
		
		private String parseIf(Map<String, String> conditions, String text) {
			while(true) {
				if (text.indexOf(FN_IF) <0) 
					break;
				String condition = text.substring(text.indexOf(FN_IF), (text.indexOf(FN_END_IF)+FN_END_IF.length()));
				String id = "if_" + IDUtils.getRandomId();
				text = text.replace(condition, id);
				condition = condition.replace(FN_IF, "").replace(FN_END_IF, "");
				conditions.put(id, condition);
			}
			return text;
		}
	}
	
	public static void main(String[] args) {
		String test = "select * from gen_Tx_ @if test @endif halo halo @if sehingga @endif lebih canggih";
		String temp = test.substring(test.indexOf("@if"), (test.indexOf("@endif")+"@endif".length()));
		System.out.println(temp);
		test = test.replace(temp, "");
		
		test = test.substring(test.indexOf("@if"), (test.indexOf("@endif")+"@endif".length()));
		System.out.println(test);
	}
}
