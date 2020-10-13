package com.nusantara.automate;

import java.util.ArrayList;
import java.util.List;

/**
 * Basic script that represented in y file
 * @author ari.patriana
 *
 */
public class BasicScript {

	public final static String LOGIN = "login";
	public final static String RELOGIN = "relogin";
	public final static String LOGOUT = "logout";
	public final static String LOAD_FILE = "loadFile";
	public final static String OPEN_MENU = "openMenu";
	public final static String EXECUTE = "execute";
	public final static String CLEAR_SESSION = "clearSession";
	public final static String SELECT_PRODUCT = "selectProduct";
	
	
	public final static String REJECT = "reject";
	public final static String REJECT_DETAIL = "rejectDetail";
	public final static String MULTIPLE_REJECT = "rejectMultiple";
	public final static String APPROVE = "approve";
	public final static String APPROVE_DETAIL = "approveDetail";
	public final static String MULTIPLE_APPROVE = "approveMultiple";
	public final static String VALIDATE = "validate";
	public final static String UPLOAD = "upload";
	public final static String CHECK = "check";
	public final static String CHECK_DETAIL = "checkDetail";
	public final static String MULTIPLE_CHECK = "checkMultiple";
	public final static String SEARCH = "search";
	

	public final static List<String> BASIC_FUNCTION = new ArrayList<String>();
	public final static List<String> BASIC_SCRIPT = new ArrayList<String>();
	
	static {
		BASIC_FUNCTION.add(REJECT);
		BASIC_FUNCTION.add(REJECT_DETAIL);
		BASIC_FUNCTION.add(MULTIPLE_REJECT);
		BASIC_FUNCTION.add(APPROVE);
		BASIC_FUNCTION.add(APPROVE_DETAIL);
		BASIC_FUNCTION.add(MULTIPLE_APPROVE);
		BASIC_FUNCTION.add(VALIDATE);
		BASIC_FUNCTION.add(UPLOAD);
		BASIC_FUNCTION.add(CHECK);
		BASIC_FUNCTION.add(CHECK_DETAIL);
		BASIC_FUNCTION.add(MULTIPLE_CHECK);
		BASIC_FUNCTION.add(SEARCH);
		
		BASIC_SCRIPT.add(LOGIN);
		BASIC_SCRIPT.add(RELOGIN);
		BASIC_SCRIPT.add(LOGOUT);
		BASIC_SCRIPT.add(LOAD_FILE);
		BASIC_SCRIPT.add(OPEN_MENU);
		BASIC_SCRIPT.add(EXECUTE);
		BASIC_SCRIPT.add(CLEAR_SESSION);
		BASIC_SCRIPT.add(SELECT_PRODUCT);
	}
}
