package com.nusantara.automate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nusantara.automate.exception.ScriptInvalidException;
import com.nusantara.automate.reader.WorkflowYReader;
import com.nusantara.automate.util.ReflectionUtils;
import com.nusantara.automate.util.SimpleEntry;
import com.nusantara.automate.workflow.WorkflowConfig;
import com.nusantara.automate.workflow.WorkflowConfigAwareness;
import com.nusantara.automate.workflow.WorkflowConfigInitializer;
import com.nusantara.automate.workflow.WorkflowEntry;

import ch.qos.logback.classic.util.ContextInitializer;

/**
 * The main class for startup all of flow process of the system
 * 
 * @author ari.patriana
 *
 */
public class RunTestApplication {

	private static final Logger log = LoggerFactory.getLogger(RunTestApplication.class);
	private static final String LOGBACK_FILE_PATH = "src/main/resources/logback.xml";
	private static final String CONFIG_FILE_PATH = "/config/config.properties";
	private static final String DRIVER_FILE_PATH = "/lib/driver/bin/chromedriver.exe";
	
	public static void run(Class<? extends RunTestWorkflow> clazz, String[] args) {
		WorkflowConfig workflowConfig = null;
		try {
			System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY, LOGBACK_FILE_PATH);
			
			String driverPathFile = DRIVER_FILE_PATH;
			String configPathFile = CONFIG_FILE_PATH;
			
			for (String arg : args) {
				if (arg.startsWith("-Ddriver.path=")) {
					driverPathFile = arg.substring("-Ddriver.path=".length(), arg.length());
				}		
				if (arg.startsWith("-Dconfig.path=")) {
					configPathFile = arg.substring("-Dconfig.path=".length(), arg.length());
				}
			}
	
			setDriver(driverPathFile);;
			
			setConfig(configPathFile);
			
			workflowConfig = new WorkflowConfig();
			setWorkflowy(workflowConfig);
			
			RunTestWorkflow workflow = (RunTestWorkflow) ReflectionUtils.instanceObject(clazz);
			
			if (workflow != null) {
				if (workflow instanceof WorkflowConfigInitializer) {
					((WorkflowConfigInitializer)workflow).configure(workflowConfig);
					verifyWorkflowy(workflowConfig);
				}
				if (workflow instanceof WorkflowConfigAwareness) {
					((WorkflowConfigAwareness) workflow).setWorkflowConfig(workflowConfig);
				}
				
				ContextLoader.setObjectWithCustom(workflow, ConfigLoader.getConfigMap());
				workflow.testWorkflow();				
			}
		} catch (Exception e) {
			log.error("ERROR ", e);
		} finally {
			try {
				FileUtils.cleanDirectory(new File(System.getProperty("user.dir") + "\\tmp"));
				if (workflowConfig != null)
					workflowConfig.clear();
				ConfigLoader.clear();
				ContextLoader.clear();
			} catch (IOException e1) {
				log.error("ERROR ", e1);
			}
		}
	}
	
	private static void setDriver(String driverPathFile) {
		if (driverPathFile != null) {
			log.info("Driver Path : " + System.getProperty("user.dir") + driverPathFile);
			
			DriverManager.setDriverPath(System.getProperty("user.dir") + driverPathFile);
		}
	}
	
	private static void setConfig(String configPathFile) throws IOException {
		Map<String, Object> systemData = new HashMap<String, Object>();
		Map<String, Object> metadata = new HashMap<String, Object>();
		systemData.put("{base_dir}", System.getProperty("user.dir"));
		systemData.put("{tmp_dir}", System.getProperty("user.dir") + "\\tmp");
		systemData.put("{log_dir}", System.getProperty("user.dir") + "\\log");
		systemData.put("{config_dir}", System.getProperty("user.dir") + "\\config");
		systemData.put("{keyfile_dir}", System.getProperty("user.dir") + "\\keyfile");
		systemData.put("{testcase_dir}", System.getProperty("user.dir") + "\\testcase");
		metadata.putAll(systemData);
		
		if (configPathFile != null) {
			log.info("Config Properties : " + System.getProperty("user.dir") + configPathFile);

			File file = new File(System.getProperty("user.dir") + configPathFile); 
			Properties prop = new Properties();
			try {
				prop.load(new FileInputStream(file));
			} catch (FileNotFoundException e) {
				log.error("ERROR ", e);
			} catch (IOException e) {
				log.error("ERROR ", e);
			}
			
			for (final String name: prop.stringPropertyNames()) {
				String value = prop.getProperty(name);
				if (value.toString().contains("{") && value.toString().contains("}")) {
					value = replaceSystemVariable(systemData, value);
				}
			    metadata.put(name, value);
			    if (name.contains("keyFile")) {
					metadata.put(name.replace("keyFile", "token"), new String(Files.readAllBytes(Paths.get(value))));
			    }
			}
			
			ConfigLoader.setConfigMap(metadata);
		}
	}
	
	private static String replaceSystemVariable(Map<String, Object> systemData, String value) {
		for (Entry<String, Object> entry : systemData.entrySet()) {
			if (value.contains(entry.getKey())) {
				value = value.replace(entry.getKey(), entry.getValue().toString());
				break;
			}
		}
		return value;
	}
	
	private static void setWorkflowy(WorkflowConfig workflowConfig) throws Exception {

		File workflowDir = new File(System.getProperty("user.dir") + "\\testcase");
		
		log.info("Load workflow files " + workflowDir.getAbsolutePath());
		
		LinkedHashMap<String , LinkedList<File>> mapFiles = new LinkedHashMap<String, LinkedList<File>>();
		searchFile(workflowDir.listFiles(), "testcase", mapFiles);
		
		for (Entry<String, LinkedList<File>> tscen : mapFiles.entrySet()) {
			int i = 0, w = 0;
			for (File file : tscen.getValue()) {
				
				if (!file.isHidden()) {
					if (file.getName().endsWith(".y")) {
						String workflowKey = tscen.getKey() + "_" + file.getName().replace(".y", "");
						log.info("Load workflowy file " + workflowKey);
						w++;
						WorkflowYReader reader = new WorkflowYReader(file);
						LinkedList<WorkflowEntry> workFlowEntries = reader.read();
						for (WorkflowEntry entry : workFlowEntries) {
							if (entry.isLoadFile()) entry.setVariable(tscen.getKey());
						}
						workflowConfig.addWorkflowEntry(workflowKey, workFlowEntries);
						workflowConfig.addWorkflowScan(tscen.getKey(), workflowKey);
					} else if (file.getName().endsWith(".xlsx")
							|| file.getName().endsWith(".xlx")) {
						log.info("Load data file " + tscen.getKey() + " -> " + file.getName());
						workflowConfig.addWorkflowData(tscen.getKey(), file);
						i++;
					}					
				}
			}			
			
			if (i == 0 || w == 0 || i > 1) {
				throw new Exception("Tscen required file incomplete");
			}
			
		}
	}
	
	private static void verifyWorkflowy(WorkflowConfig workflowConfig) throws ScriptInvalidException {
		for (LinkedList<WorkflowEntry> entryList : workflowConfig.getWorkflowEntries().values()) {
			for (WorkflowEntry entry : entryList) {
				if (entry.isActionMenu()) {
					Menu menu = workflowConfig.getMenu(entry.getVariable());
					if (menu == null) {
						throw new ScriptInvalidException("Menu not found for " + entry.getVariable());
					}
				}
				if (entry.isFunction()) {
					SimpleEntry<Class<?>, Object[]> function = workflowConfig.getFunction(entry.getVariable());
					if (function == null) {
						throw new ScriptInvalidException("Function not found for " + entry.getVariable());
					}
				}
					
			}
		}
		
	}
	
	private static void searchFile(File[] files, String dir, Map<String, LinkedList<File>> mapFiles) {
		for (File file : files) {
			if (file.isDirectory()) {
				searchFile(file.listFiles(), file.getName(), mapFiles);
			}
			
			if (file.isFile()) {
				if (file.getName().endsWith(".y") 
						|| file.getName().endsWith(".xlsx")
						|| file.getName().endsWith(".xlx")) {
					LinkedList<File> fileList = mapFiles.get(dir);
					if (fileList == null) fileList = new LinkedList<File>();
					fileList.add(file);
					mapFiles.put(dir, fileList);
				};
			}
		}
	}
	
}
