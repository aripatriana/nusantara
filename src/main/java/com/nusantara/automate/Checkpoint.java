package com.nusantara.automate;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Value;

import com.nusantara.automate.io.FileIO;
import com.nusantara.automate.report.ReportManager;
import com.nusantara.automate.report.ReportMonitor;
import com.nusantara.automate.report.SnapshotEntry;
import com.nusantara.automate.util.DateUtils;
import com.nusantara.automate.util.IDUtils;
import com.nusantara.automate.util.MapUtils;

public class Checkpoint {

	@Value("active_scen")
	private String testcase;
	
	@Value("active_workflow")
	private String scen;
	
	@Value("active_module_id")
	private String moduleId;
	
	@Value("active_menu_id")
	private String menuId;
	
	@Value("{tmp_dir}")
	private String tmpDir;
	
	@Value("{report_dir}")
	private String reportDir;
	
	@Value("start_time_milis")
	private String startTimeMilis;
	
	public Checkpoint() {
	}
	
	public void takeElements(WebDriver wd, WebExchange we) {
		Map<String, String> elements = we.getElements(moduleId);
		Map<String, Object> values = new HashMap<String, Object>();
		for (Entry<String, String> entry : elements.entrySet()) {
			values.put(entry.getKey(), wd.findElement(By.xpath(entry.getValue())).getText());
		}
		
		putToSession(we, values);
	}
	
	public void takeElements(WebElement wl, WebExchange we) {
		Map<String, String> elements = we.getElements(moduleId);
		Map<String, Object> values = new HashMap<String, Object>();
		for (Entry<String, String> entry : elements.entrySet()) {
			values.put(entry.getKey(), wl.findElement(By.xpath(entry.getValue())).getText());
		}
		
		putToSession(we, values);
	}
	
	private void putToSession(WebExchange we, Map<String, Object> values) {
		String filename = getOutputFile();
		we.putToSession(WebExchange.PREFIX_TYPE_ELEMENT, moduleId, values);
		MapUtils.clearMapKey("@" + WebExchange.PREFIX_TYPE_ELEMENT + "." + moduleId + ".", values);
		FileIO.write(filename, values);
		
		ReportMonitor.logSnapshotEntry(testcase, scen, SnapshotEntry.SNAPSHOT_AS_CHECKPOINT, values.toString(), filename, ReportManager.PASSED);
	}
	
	private String getOutputFile() {
		String filename = menuId.replace(".", "_") + "_"+ IDUtils.getRandomId() + ".txt";
		String fullpath = reportDir + "\\" + DateUtils.format(Long.valueOf(startTimeMilis)) + "\\" + testcase + "\\" + scen.replace(testcase + "_", "") + "\\" + filename;
		return fullpath;
	}

}
