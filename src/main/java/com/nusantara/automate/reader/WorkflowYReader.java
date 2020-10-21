package com.nusantara.automate.reader;

import java.io.File;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nusantara.automate.BasicScript;
import com.nusantara.automate.exception.ScriptInvalidException;
import com.nusantara.automate.workflow.WorkflowEntry;

/**
 * Translate the script inside y file and convert it to the WorkflowEntry that system can read
 * 
 * @author ari.patriana
 *
 */
public class WorkflowYReader {

	private static Logger log = LoggerFactory.getLogger(WorkflowYReader.class);
	
	private SimpleFileReader fileReader;
	
	public WorkflowYReader(File file) {
		fileReader = new SimpleFileReader(file);
	}
	
	public LinkedList<WorkflowEntry> read() throws ScriptInvalidException {
		LinkedList<WorkflowEntry> workflowEntries = new LinkedList<WorkflowEntry>();
		while(fileReader.iterate()) {
			String script = fileReader.read();
			if (!script.startsWith("//") && !script.isEmpty()) {
				workflowEntries.add(translate(script));				
			}
		}
		fileReader.close();
		
		return workflowEntries;
	}
	
	private WorkflowEntry translate(String script) throws ScriptInvalidException {
		WorkflowEntry workflowEntry = new WorkflowEntry();
		
		if (!script.endsWith(";")) throw new ScriptInvalidException("Missing semicolon " + script);
		script = script.replace(";", "");
		
		String variable = detectVariable(script);
		String simpleScript = script;
		if (variable != null)
			simpleScript = script.replace("\"" + variable + "\"", "");
		String[] simpleScripts = simpleScript.split("\\.");

		if (simpleScripts.length == 2) {
			String actionType = simpleScripts[1].replace("()", "");
			if (simpleScripts[1].equals(actionType)) 
				throw new ScriptInvalidException("Missing or invalid bracket for " + script);
			verifyActionType(actionType);
			workflowEntry.setActionType(actionType);
		}
		
		String basicScript = simpleScripts[0].replace("()", "");
		if (simpleScripts[0].equals(basicScript)) 
			throw new ScriptInvalidException("Missing or invalid bracket for " + script);
		setBasicScript(basicScript, workflowEntry);
		workflowEntry.setVariable(variable);
		return workflowEntry;
	}
	
	public static void main(String[] args) {
		WorkflowYReader w = new WorkflowYReader(new File("D:\\Files\\1. General\\web-remote-as-1.1\\workflow.y"));
		try {
			for (WorkflowEntry we : w.read()) {
				System.out.println(we);
			}
		} catch (ScriptInvalidException e) {
			log.error("ERROR ", e);
		}
	}
	
	private String detectVariable(String script) throws ScriptInvalidException {
		boolean findout = false;
		int startI = 0;
		int endI = 0;
		for (int i=0; i < script.length(); i++) {
			if (script.charAt(i) == '"') {
				if (!findout) {
					startI = i+1;
					findout = true;
				} else {
					if (findout && endI != 0) {
						throw new ScriptInvalidException("Invalid quote variable for " + script);
					} 
					endI = i;
				}
			}
		}
		
		if (startI >0 && endI == 0)
			throw new ScriptInvalidException("Invalid quote variable for " + script);
		
		if (startI == 0 && endI == 0) {
			return null;
		} 

		return script.substring(startI, endI);
	}
	
	public void setBasicScript(String basicScript, WorkflowEntry workflowEntry) throws ScriptInvalidException {
		if (BasicScript.LOGIN.equals(basicScript)) {
			workflowEntry.setLogin(Boolean.TRUE);
		} else if (BasicScript.LOAD_FILE.equals(basicScript)) {
			workflowEntry.setLoadFile(Boolean.TRUE);
		} else if (BasicScript.LOGOUT.equals(basicScript)) {
			workflowEntry.setLogout(Boolean.TRUE);
		} else if (BasicScript.RELOGIN.equals(basicScript)) {
			workflowEntry.setRelogin(Boolean.TRUE);
		} else if (BasicScript.OPEN_MENU.equals(basicScript)) {
			workflowEntry.setActionMenu(Boolean.TRUE);
		} else if (BasicScript.EXECUTE.equals(basicScript)) {
			workflowEntry.setFunction(Boolean.TRUE);
		} else if (BasicScript.CLEAR_SESSION.equals(basicScript)) {
			workflowEntry.setClearSession(Boolean.TRUE);
		} else if (BasicScript.SELECT_PRODUCT.equals(basicScript)) {
			workflowEntry.setSelectProduct(Boolean.TRUE);
		} else {
			throw new ScriptInvalidException("Invalid script for " + basicScript);
		}
	}
	
	private void verifyActionType(String actionType) throws ScriptInvalidException {
		if (!BasicScript.BASIC_FUNCTION.contains(actionType)) {
			throw new ScriptInvalidException("Invalid action type for " + actionType);
		}
	}
}
