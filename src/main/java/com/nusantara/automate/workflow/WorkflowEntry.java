package com.nusantara.automate.workflow;

public class WorkflowEntry {

	private boolean clearSession;
	private boolean loadFile;
	private boolean login;
	private boolean logout;
	private boolean relogin;
	private boolean selectProduct; 
	private boolean actionMenu;
	private boolean function;
	private String variable;
	private String actionType;
	
	public void setClearSession(boolean clearSession) {
		this.clearSession = clearSession;
	}
	
	public boolean isClearSession() {
		return clearSession;
	}
	public void setLoadFile(boolean loadFile) {
		this.loadFile = loadFile;
	}
	
	public boolean isLoadFile() {
		return loadFile;
	}
	
	public void setLogout(boolean logout) {
		this.logout = logout;
	}
	
	public boolean isLogout() {
		return logout;
	}
	public void setLogin(boolean login) {
		this.login = login;
	}
	
	public boolean isLogin() {
		return login;
	}
	
	public void setRelogin(boolean relogin) {
		this.relogin = relogin;
	}
	
	public boolean isRelogin() {
		return relogin;
	}
	
	public void setSelectProduct(boolean selectProduct) {
		this.selectProduct = selectProduct;
	}
	
	public boolean isSelectProduct() {
		return selectProduct;
	}
	
	public boolean isActionMenu() {
		return actionMenu;
	}
	
	public void setActionMenu(boolean actionMenu) {
		this.actionMenu = actionMenu;
	}
	
	public boolean isFunction() {
		return function;
	}
	
	public void setFunction(boolean function) {
		this.function = function;
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
		return "WorkflowEntry [clearSession=" + clearSession + ", loadFile=" + loadFile + ", login=" + login
				+ ", logout=" + logout + ", relogin=" + relogin + ", selectProduct=" + selectProduct + ", actionMenu="
				+ actionMenu + ", function=" + function + ", variable=" + variable + ", actionType="
				+ actionType + "]";
	}
	
	
}
