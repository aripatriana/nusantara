package com.nusantara.automate.function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.nusantara.automate.Actionable;
import com.nusantara.automate.Assertion;
import com.nusantara.automate.DBConnection;
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

public class AssertQueryAction implements Actionable {

	Logger log = LoggerFactory.getLogger(AssertQueryAction.class);
	
	@Value("active_scen")
	private String testcase;
	
	@Value("active_workflow")
	private String scen;
	
	private QueryEntry qe;
	
	public AssertQueryAction(QueryEntry qe) {
		this.qe = qe;
	}
	
	@Override
	public void submit(WebExchange webExchange) throws FailedTransactionException {
		log.info("Query -> " + qe.getQuery());
	
		boolean executeBatch = false;
		for (String param : qe.getParameters()) {
			if (DataTypeUtils.checkType(param, DataTypeUtils.TYPE_OF_VARIABLE))
				executeBatch = true;
		}
		
		if (executeBatch) {
			// distinct module
			Set<String> module = new HashSet<String>();
			for (String variable : qe.getVariables()) {
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
							webExchange.setCurrentSession(i);
							Map<String, Object> metadata = webExchange.getMetaData(m, i);
							ReportMonitor.logDataEntry(webExchange.getCurrentSession(),webExchange.get("active_scen").toString(),
									webExchange.get("active_workflow").toString(), null, metadata);
						}
						
						assertQuery(webExchange);
					}
				} catch (FailedTransactionException e) {
					webExchange.addFailedSession(webExchange.getCurrentSession());

					ReportMonitor.logDataEntry(webExchange.getCurrentSession(),webExchange.get("active_scen").toString(),
							webExchange.get("active_workflow").toString(), null, null, 
							e.getMessage(), ReportManager.FAILED);
				}
			}
		} else {
			try {
				assertQuery(webExchange);
			} catch (FailedTransactionException e) {
				webExchange.addFailedSession(webExchange.getCurrentSession());
				ReportMonitor.logError(webExchange.get("active_scen").toString(),
						webExchange.get("active_workflow").toString(), e.getMessage());
			}
		}
	}
	
	private void assertQuery(WebExchange webExchange) throws FailedTransactionException {
		String[] columns = new String[qe.getColumns().size()];
		columns = qe.getColumns().toArray(columns);
		
		List<String[]> result = new ArrayList<String[]>();
		List<Assertion> asserts = new LinkedList<Assertion>();
		try {
			int i = 0;
			for (String query : qe.getParsedQuery(webExchange)) {

				log.info("Execute Query -> " + query);
				result = DBConnection.selectSimpleQuery(query, columns);
			
				if (result.size() ==0)
					result.add(new String[columns.length]);
					
				Assertion assertion = new Assertion();
				assertion.setQuery(query);
				assertion.setResult(StringUtils.asStringTableHtml(columns, result));
				for (String[] res : result) {
					Map<String, String> resultMap = MapUtils.copyAsMap(columns, res, String.class, String.class);
					for (Statement state : qe.getStatements(i).values()) {
						Statement statement = new Statement(state);
						if (statement.getEquality() != null) {
							if (statement.isArg1(DataTypeUtils.TYPE_OF_COLUMN)) {
								statement.setVal1(resultMap.get(statement.getArg1()));
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
								statement.setVal2(resultMap.get(statement.getArg2()));
							} else if (statement.isArg2(DataTypeUtils.TYPE_OF_VARIABLE)) {
								if (statement.getArg2().contains(QueryEntry.SQUARE_BRACKET)) {
									statement.setVal2(StringUtils.nvl(parseExclusiveVariable(statement.getArg2(), webExchange), "null"));
								} else {
									statement.setVal2(StringUtils.nvl(webExchange.get(statement.getArg2()),"null"));
								}
							} else {
								statement.setVal2(statement.getArg2());
							}
								
							assertion.addStatement(statement);
						}
					}
					i++;
				}
				asserts.add(assertion);
			}
		} catch (Exception e) {
			log.error("Failed execute query ", e);
			throw new FailedTransactionException(e.getMessage());
		}
		
		String rawText = "";
		boolean status = true;
		for (Assertion e : asserts) {
			if (!rawText.isEmpty())
				rawText += "<br><br>";
			rawText += e.getAssertion();
			if (status) status = e.isTrue();
		}
		
		ReportMonitor.logSnapshotEntry(testcase, scen, webExchange.getCurrentSession(), 
				SnapshotEntry.SNAPSHOT_AS_RAWTEXT, rawText, null, (status ? ReportManager.PASSED : ReportManager.FAILED));
		
		if (!status)
			throw new FailedTransactionException("Failed assertion");
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
}
