package com.nusantara.automate.reader;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

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
			if (!tmp.toString().isBlank()) throw new ScriptInvalidException("Invalid script for " + tmp.toString().replace("#", ""));
			return scripts;
		}
		
	}
	

	public static void main(String[] args) throws ScriptInvalidException {
		WorkflowYReader y = new WorkflowYReader(new File("D:/error.txt"));
		System.out.println(y.read());
		
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
			setActionType(actionType, workflowEntry);
		}
		
		String basicScript = simpleScripts[0].replace("()", "");
		if (simpleScripts[0].equals(basicScript)) 
			throw new ScriptInvalidException("Missing or invalid bracket for " + script);
		setBasicScript(basicScript, workflowEntry);
		workflowEntry.setVariable(variable);
		return workflowEntry;
	}
	
	public static void main1(String[] args) {
		WorkflowYReader w = new WorkflowYReader(new File("D:\\Files\\1._General\\web-remote-as-1.1\\workflow.y"));
	
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
