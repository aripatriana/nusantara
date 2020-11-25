package com.nusantara.automate.reader;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nusantara.automate.BasicScript;
import com.nusantara.automate.exception.ScriptInvalidException;
import com.nusantara.automate.util.StringUtils;
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
	private TextCompiler compiler;
	
	public WorkflowYReader(File file) {
		fileReader = new SimpleFileReader(file);
		compiler = new TextCompiler();
	}
	
	public static class TextCompiler {
	
		private List<String> scripts = new LinkedList<String>();
		private StringBuffer tmp = new StringBuffer();
		
		public void compile(String rawText) throws ScriptInvalidException {
			rawText = rawText.trim();
			tmp.append(rawText);
			if (rawText.endsWith(";")) {
				scripts.add(check(tmp.toString()));
				tmp = new StringBuffer();
			} else if (rawText.endsWith("\"") || rawText.endsWith("+")) {
				tmp.append("#");
			} else {
				throw new ScriptInvalidException("Invalid script for " + rawText);
			}
		}
		
		private String check(String script) throws ScriptInvalidException {
			if (!script.contains("#")) return script;
			
			String checked = script.replace(" ", "").replace("#", "");
			checked = checked.replace("\"+\"","");
			checked = StringUtils.replaceCharForward(checked, '"', "");
			checked = StringUtils.replaceCharBackward(checked, '"', "");
			
			if (StringUtils.containsCharForward(checked, '"') > 0)
				throw new ScriptInvalidException("Invalid script for " + script.replace("#", ""));

			String[] parsed = script.split("#");
			StringBuffer sb = new StringBuffer();
			for (int i=0; i<parsed.length; i++) {
				String temp = parsed[i].trim();
				if (i==0) {
					temp = StringUtils.replaceCharBackward(temp, '+', "", 0).trim();
					temp = StringUtils.replaceCharBackward(temp, '"', "", 0);
				} else if (i==parsed.length-1) {
					temp = StringUtils.replaceCharForward(temp, '+', "", 0).trim();
					temp = StringUtils.replaceCharForward(temp, '"', "", 0);
				} else {
					temp = StringUtils.replaceCharForward(temp, '+', "", 0).trim();
					temp = StringUtils.replaceCharForward(temp, '"', "", 0);
					temp = StringUtils.replaceCharBackward(temp, '+', "", 0).trim();
					temp = StringUtils.replaceCharBackward(temp, '"', "", 0);
				}
				sb.append(temp);
			}
			return sb.toString();
		}

		public List<String> getScripts() throws ScriptInvalidException {
			if (!tmp.toString().isEmpty()) throw new ScriptInvalidException("Invalid script for " + tmp.toString().replace("#", ""));
			return scripts;
		}
		
	}
	
	public LinkedList<WorkflowEntry> read() throws ScriptInvalidException {
		while(fileReader.iterate()) {
			String script = fileReader.read();
			if (!script.startsWith("//") && !script.isEmpty()) {
				compiler.compile(script);				
			}
		}
		fileReader.close();
		
		LinkedList<WorkflowEntry> workflowEntries = new LinkedList<WorkflowEntry>();
		for (String script : compiler.getScripts()) {
			workflowEntries.add(translate(script));			
		}
		return workflowEntries;
	}
	
	private WorkflowEntry translate(String script) throws ScriptInvalidException {
		WorkflowEntry workflowEntry = new WorkflowEntry();
		
		// check semicolon
		if (!script.endsWith(";")) throw new ScriptInvalidException("Missing semicolon " + script);
		script = script.replace(";", "");
		
		// check variable
		String variable = detectVariable(script);
		checkVariable(variable);
		workflowEntry.setVariable(variable);
		
		// check script
		String simpleScript = script;
		if (variable != null)
			simpleScript = script.replace("\"" + variable + "\"", "");
		String[] simpleScripts = simpleScript.split("\\.");

		if (simpleScripts.length == 2) {
			String actionType = simpleScripts[1].replace("()", "");
			if (simpleScripts[1].equals(actionType)) 
				throw new ScriptInvalidException("Missing or invalid bracket for " + script);
			setActionType(actionType, workflowEntry);
		}
		
		String basicScript = simpleScripts[0].replace("()", "");
		if (simpleScripts[0].equals(basicScript)) 
			throw new ScriptInvalidException("Missing or invalid bracket for " + script);
		setBasicScript(basicScript, workflowEntry);
	
		return workflowEntry;
	}
	
	private void checkVariable(String script) throws ScriptInvalidException {
		if (script == null)
			return;
		Map<String, Integer> counter = new HashMap<String, Integer>(); 
		for (int i=0; i<script.length(); i++) {
			if (script.charAt(i) == '(')
				counter.put("c", (counter.get("c") == null ? 1 : counter.get("c") + 1));
			if (script.charAt(i) == ')')
				counter.put("c", (counter.get("c") == null ? -1 : counter.get("c") - 1));
			if (script.charAt(i) == '\'')
				counter.put("sq", (counter.get("sq") == null ? -1 : counter.get("sq") + 1));
		}
		if (counter.get("c") != null && (counter.get("c") != 0))
			throw new ScriptInvalidException("Missing or invalid bracket script for " + script);
		if (counter.get("sq") != null && counter.get("sq") % 2 != 0)
			throw new ScriptInvalidException("Invalid single quote for " + script);
	}
	
	private String detectVariable(String script) throws ScriptInvalidException {
		boolean findout = false;
		int start = 0;
		int end = 0;
		for (int i=0; i < script.length(); i++) {
			if (script.charAt(i) == '"') {
				if (!findout) {
					start = i+1;
					findout = true;
				} else {
					if (findout && end != 0) {
						throw new ScriptInvalidException("Invalid quote variable for " + script);
					} 
					end = i;
				}
			}
		}
		
		if (start >0 && end == 0)
			throw new ScriptInvalidException("Invalid quote variable for " + script);
		
		if (start == 0 && end == 0) {
			return null;
		} 

		return script.substring(start, end);
	}
	
	public void setBasicScript(String basicScript, WorkflowEntry workflowEntry) throws ScriptInvalidException {
		workflowEntry.setKeyword(basicScript);
		if (!workflowEntry.checkKeywords(BasicScript.BASIC_SCRIPT))
			throw new ScriptInvalidException("Invalid script for " + basicScript);
	}
	
	private void setActionType(String actionType, WorkflowEntry workflowEntry) throws ScriptInvalidException {
		workflowEntry.setActionType(actionType);
		if (!workflowEntry.checkActionTypes(BasicScript.BASIC_FUNCTION))
			throw new ScriptInvalidException("Invalid action type for " + actionType);
	}
}
