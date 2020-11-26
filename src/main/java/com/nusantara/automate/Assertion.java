package com.nusantara.automate;

import java.util.LinkedList;
import java.util.List;

public class Assertion {

	private String query;
	
	private String result;
	
	private List<Statement> statements = new LinkedList<Statement>();

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public List<Statement> getStatements() {
		return statements;
	}

	public void setStatements(List<Statement> statements) {
		this.statements = statements;
	}

	public void addStatement(Statement statement) {
		statements.add(statement);
	}
	
	public boolean isTrue() {
		for (Statement s : statements) {
			if (!s.isTrue()) 
				return false;
		}
		return true;
	}
	
	public String getAssertion() {
		StringBuffer sb = new StringBuffer();
		if (query != null) sb.append("<b>Query</b>").append("<br>").append(getQuery()).append("<br><br>");
		if (query != null) sb.append("<b>Result</b>").append("<br>").append(getResult()).append("<br><br>");
		if (statements.size() > 0) sb.append("<b>Assert</b>").append("<br>");		
		for (Statement s : statements) {
			sb.append("&raquo; " + s.getStatement()).append("<br>");
		}
		return sb.toString();
	}
	
	@Override
	public String toString() {
		return "Assertion [query=" + query + ", result=" + result + ", statements=" + statements + "]";
	}
	
	
}
