package com.nusantara.automate.function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.nusantara.automate.Actionable;
import com.nusantara.automate.Assertion;
import com.nusantara.automate.Statement;
import com.nusantara.automate.WebExchange;
import com.nusantara.automate.exception.FailedTransactionException;
import com.nusantara.automate.query.QueryEntry;
import com.nusantara.automate.report.ReportManager;
import com.nusantara.automate.report.ReportMonitor;
import com.nusantara.automate.report.SnapshotEntry;
import com.nusantara.automate.util.DataTypeUtils;
import com.nusantara.automate.util.MapUtils;
import com.nusantara.automate.util.ReflectionUtils;
import com.nusantara.automate.util.StringUtils;

public class AssertStatementAction  implements Actionable {
	
	Logger log = LoggerFactory.getLogger(AssertStatementAction.class);
	
	@Value("active_scen")
	private String testcase;
	
	@Value("active_workflow")
	private String scen;
	
	private Statement statement;
	
	public AssertStatementAction(Statement statement) {
		this.statement = statement;
	}

	@Override
	public void submit(WebExchange webExchange) throws FailedTransactionException {
	
		List<String> variables = new ArrayList<String>();
		if (statement.isArg1(DataTypeUtils.TYPE_OF_VARIABLE))
			variables.add(statement.getArg1());
		if (statement.isArg2(DataTypeUtils.TYPE_OF_VARIABLE))
			variables.add(statement.getArg2());

		if (variables.size() > 0) {
			if (webExchange.getCountSession() == 0) {
				ReportMonitor.logError(webExchange.get("active_scen").toString(),
						webExchange.get("active_workflow").toString(), "The session is needed when executing the query using a variable, use loadFile()");
				throw new FailedTransactionException("The session is needed when executing the query using a variable, use loadFile()");
			}
			
			// distinct module
			Set<String> module = new HashSet<String>();
			for (String variable : variables) {
				if (variable.startsWith("@"+WebExchange.PREFIX_TYPE_DATA)) {
					module.add(variable.split("\\.")[1]);
				}
			}	

			for (int i=0; i<webExchange.getCountSession(); i++) {
				try {
					String sessionId = webExchange.createSession(i);
					if (!webExchange.isSessionFailed(sessionId)) {
						webExchange.setCurrentSession(sessionId);
						// log data monitor
						for (String m : module) {
							Map<String, Object> metadata = webExchange.getMetaData(m, i);
							ReportMonitor.logDataEntry(webExchange.getCurrentSession(),webExchange.get("active_scen").toString(),
									webExchange.get("active_workflow").toString(), null, metadata);	
						}
						
						assertStatement(variables, webExchange);
					}
				} catch (FailedTransactionException e) {
					webExchange.addFailedSession(webExchange.getCurrentSession());
					log.error("Failed for transaction ", e);
					ReportMonitor.logDataEntry(webExchange.getCurrentSession(),webExchange.get("active_scen").toString(),
							webExchange.get("active_workflow").toString(), null, null, 
							e.getMessage(), ReportManager.FAILED);
				}
			}
		} else { 
			try {
				assertStatement(variables, webExchange);
			} catch (FailedTransactionException e) {
				webExchange.addFailedSession(webExchange.getCurrentSession());
				log.error("Failed for transaction ", e);
				ReportMonitor.logError(webExchange.get("active_scen").toString(),
						webExchange.get("active_workflow").toString(), e.getMessage());
			}
		}
			

	}
	
	private void assertStatement(List<String> variables, WebExchange webExchange) throws FailedTransactionException {
		Assertion assertion = new Assertion();
		try {
			String[] params = new String[variables.size()];
			for (int i=0; i<countStatement(variables.toArray(params), webExchange);i++) {
				Statement statement = getStatement(i);
				if (statement.isArg1(DataTypeUtils.TYPE_OF_COLUMN)) {
					statement.setVal1("null");
				} else if (statement.isArg1(DataTypeUtils.TYPE_OF_VARIABLE)) {
					if (statement.getArg1().contains(QueryEntry.SQUARE_BRACKET)) {
						statement.setVal1(StringUtils.nvl(parseExclusiveVariable(statement.getArg1(), webExchange), "null"));
					} else {
						statement.setVal1(StringUtils.nvl(webExchange.get(statement.getArg1()),"null"));
					}
				} else {
					statement.setVal1(statement.getArg1());
				}
				if (statement.isArg2(DataTypeUtils.TYPE_OF_COLUMN)) {
					statement.setVal2("null");
				} else if (statement.isArg2(DataTypeUtils.TYPE_OF_VARIABLE)) {
					if (statement.getArg2().contains(QueryEntry.SQUARE_BRACKET)) {
						statement.setVal2(StringUtils.nvl(parseExclusiveVariable(statement.getArg2(), webExchange), "null"));
					} else {
						statement.setVal2(StringUtils.nvl(webExchange.get(statement.getArg2()), "null"));
					}
				} else {
					statement.setVal2(statement.getArg2());
				}
				assertion.addStatement(statement);
			}

			log.info("Assert " + assertion.getAssertion());
		} catch (Exception e) {
			log.error("Failed statement ", e);
			throw new FailedTransactionException(e.getMessage());
		}
		
		ReportMonitor.logSnapshotEntry(testcase, scen, webExchange.getCurrentSession(), 
				SnapshotEntry.SNAPSHOT_AS_RAWTEXT, assertion.getAssertion(), null, (assertion.isTrue() ? ReportManager.PASSED : ReportManager.FAILED));

		if (!assertion.isTrue())
			throw new FailedTransactionException("Failed assertion");	
	}
	
	private Statement getStatement(int index) {
		Statement statement = new Statement(this.statement);
		
		if (statement.getArg1().contains(QueryEntry.ROUND_BRACKET)) {
			statement.setArg1(statement.getArg1().replace(QueryEntry.ROUND_BRACKET, "[" +  index + "]"));
		}
		if (statement.getArg2().contains(QueryEntry.ROUND_BRACKET)) {
			statement.setArg2(statement.getArg2().replace(QueryEntry.ROUND_BRACKET, "[" +  index + "]"));
		}
		
		return statement;
	}
	
	@SuppressWarnings("unchecked")
	private String parseExclusiveVariable(String argument, WebExchange webExchange) throws Exception {
		String result = null;
		Map<String, List<String>> squared = new HashMap<String, List<String>>();
		if(argument.contains(QueryEntry.SQUARE_BRACKET)) {
			String[] s = argument.split("\\" + QueryEntry.SQUARE_BRACKET);
			if (s.length > 2)
				throw new Exception("Not valid argument for " + argument);
			List<String> r = squared.get(s[0]);
			if (r == null) r = new ArrayList<String>();
			if (s.length == 2)
				r.add(s[1].replace(".","").trim());
			squared.put(s[0], r);
		}
		
		// []
		if (!squared.isEmpty()) {
			for (Entry<String, List<String>> e : squared.entrySet()) {
				Object o = webExchange.get(e.getKey()+QueryEntry.SQUARE_BRACKET);
				if (o != null && !ReflectionUtils.checkAssignableFrom(o.getClass(), List.class))
					throw new Exception("Argument value is not a list for " + e.getKey()+QueryEntry.SQUARE_BRACKET);
				
				if (o != null) {
					List<Object> l = (List<Object>) o;
					if (e.getValue() != null && !e.getValue().isEmpty()) {
						for (String v : e.getValue()) {
							List<Object> values = MapUtils.mapAsList((List<Map<String, Object>>) (List<?>)l, v);
							result = MapUtils.listAsString(values, ",");
						}
					} else {
						result = MapUtils.listAsString(l, ",");
					}
				}
			}
		}
		
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private int countStatement(String[] params, WebExchange webExchange) throws Exception {
		Map<String, List<String>> rounded = new HashMap<String, List<String>>();
		int count = 1;
		for (String p : params) {
			if (p.contains(QueryEntry.ROUND_BRACKET)) {
				String[] s = p.split("\\(\\)");
				if (s.length > 2)
					throw new Exception("Not valid argument");
				List<String> r = rounded.get(s[0]);
				if (r == null) r = new ArrayList<String>();
				if (s.length == 2)
					r.add(s[1].replace(".","").trim());
				rounded.put(s[0], r);
			}
		}
		
		// ()
		if (!rounded.isEmpty()) {
			int[] size = new int[rounded.size()];
			int i = 0;

			// get size
			for (String k : rounded.keySet()) {
				Object o = webExchange.get(k+QueryEntry.SQUARE_BRACKET);
				if (o != null && !ReflectionUtils.checkAssignableFrom(o.getClass(), List.class))
					throw new Exception("Argument value is not a list");
				if (o != null) {
					List<Object> l = (List<Object>) o;
					size[i] = l.size();
				}
				i++;
			}
			
			// compare size
			if (i > 1) {
				int total = IntStream.of(size).sum();
				int average = total / i;
				if (average != size[0])
					throw new Exception("Size of argument is not match");
			}

			count = size[0];
		}
		
		return count;
	}
}
