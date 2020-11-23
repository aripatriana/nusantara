package com.nusantara.automate.workflow;

import java.util.List;

/**
 * This class represent a line of script inside y file
 * 
 * @author ari.patriana
 *
 */
public class WorkflowEntry {

	private String keyword;
	private String variable;
	private String actionType;

	public boolean checkKeyword(String basicScript) {
		return keyword.equals(basicScript);
	}
	
	public boolean checkKeywords(List<String> basicScripts) {
		if (keyword == null) return false;
		return basicScripts.contains(keyword);
	}
	
	public boolean checkActionTypes(List<String> actionTypes) {
		if (actionType == null) return false;
		return actionTypes.contains(actionType);
	}
	
	public String getKeyword() {
		return keyword;
	}
	
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	
	public String getVariable() {
		return variable;
	}
	
	public void setVariable(String variable) {
		this.variable = variable;
	}
	
	public String getActionType() {
		return actionType;
	}
	
	public void setActionType(String actionType) {
		this.actionType = actionType;
	}

	@Override
	public String toString() {
		return "WorkflowEntry [keyword=" + keyword + ", variable=" + variable + ", actionType=" + actionType + "]";
	}
}
