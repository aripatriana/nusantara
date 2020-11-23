package com.nusantara.automate;

import com.nusantara.automate.util.DataTypeUtils;

public class Statement {

	public static final String EQUAL = "==";
	public static final String NOT_EQUAL = "<>";

	public final static String[] MARK = new String[] {Statement.EQUAL, Statement.NOT_EQUAL};
	
	private String arg1;
	
	private Object val1;
	
	private String arg2;
	
	private Object val2;
	
	private String equality;
	
	public Statement(Statement statement) {
		this.arg1 = statement.arg1;
		this.val1 = statement.val1;
		this.arg2 = statement.arg2;
		this.val2 = statement.val2;
		this.equality = statement.equality;
	}
	
	public Statement(String arg1, String arg2, String equality) {
		this.arg1 = arg1;
		this.arg2 = arg2;
		this.equality = equality;
		if (isArg1(DataTypeUtils.TYPE_OF_ARGUMENT))
			this.val1 = arg1;
		if (isArg2(DataTypeUtils.TYPE_OF_ARGUMENT))
			this.val2 = arg2;
	}
	
	public String getArg1() {
		return arg1;
	}
	
	public boolean isArg1(int type) {
		return DataTypeUtils.checkType(arg1, type);
	}
	
	public void setArg1(String arg1) {
		this.arg1 = arg1;
	}

	public Object getVal1() {
		return val1;
	}

	public void setVal1(Object val1) {
		this.val1 = val1;
	}

	public String getArg2() {
		return arg2;
	}

	public boolean isArg2(int type) {
		return DataTypeUtils.checkType(arg2, type);
	}
	public void setArg2(String arg2) {
		this.arg2 = arg2;
	}

	public Object getVal2() {
		return val2;
	}

	public void setVal2(Object val2) {
		this.val2 = val2;
	}

	public String getEquality() {
		return equality;
	}

	public void setEquality(String equality) {
		this.equality = equality;
	}
	
	public boolean isTrue() {
		Object v1 = val1;
		Object v2 = val2;
		
		if (v1 instanceof String)
			v1 = v1.toString().replace("'", "").replace("\"", "");
		if (v2 instanceof String)
			v2 = v2.toString().replace("'", "").replace("\"", "");
		if (equality.equals(EQUAL)) {
			return v1.equals(v2);
		} 
		return !v1.equals(v2);
	}
	
	public String getStatement() {
		StringBuffer sb = new StringBuffer();
		sb.append("'");
		sb.append(arg1 + "/" + val1);
		sb.append((equality.equals(EQUAL) ? " is equal with " : " is not equal with "));
		sb.append(arg2 + "/" + val2);
		sb.append("'");
		sb.append(" is <b>" + String.valueOf(isTrue()).toUpperCase() + "<b>");
		return sb.toString();
	}

	@Override
	public String toString() {
		return "Statement [arg1=" + arg1 + ", val1=" + val1 + ", arg2=" + arg2 + ", val2=" + val2 + ", equality="
				+ equality + "]";
	}
	
}
