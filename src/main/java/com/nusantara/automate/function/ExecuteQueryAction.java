package com.nusantara.automate.function;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.nusantara.automate.Actionable;
import com.nusantara.automate.DBConnection;
import com.nusantara.automate.WebExchange;
import com.nusantara.automate.exception.FailedTransactionException;
import com.nusantara.automate.query.QueryEntry;
import com.nusantara.automate.report.ReportManager;
import com.nusantara.automate.report.ReportMonitor;
import com.nusantara.automate.report.SnapshotEntry;


public class ExecuteQueryAction implements Actionable {

	Logger log = LoggerFactory.getLogger(ExecuteQueryAction.class);
	
	private QueryEntry qe;
	
	@Value("active_scen")
	private String testcase;
	
	@Value("active_workflow")
	private String scen;
	
	public ExecuteQueryAction(QueryEntry qe) {
		this.qe = qe;
	}
	
	@Override
	public void submit(WebExchange webExchange) throws FailedTransactionException {
		log.info("Query -> " + qe.getQuery());
		
		if (qe.getVariables() != null && qe.getVariables().size() > 0) {
			if (webExchange.getCountSession() == 0) {
				ReportMonitor.logError(webExchange.get("active_scen").toString(),
						webExchange.get("active_workflow").toString(), "The session is needed when executing the query using a variable, use loadFile()");
				throw new FailedTransactionException("The session is needed when executing the query using a variable, use loadFile()");
			}
			
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
						
						executeQuery(webExchange);		
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
				executeQuery(webExchange);
			} catch (FailedTransactionException e) {
				webExchange.addFailedSession(webExchange.getCurrentSession());
				log.error("Failed for transaction ", e);
				ReportMonitor.logError(webExchange.get("active_scen").toString(),
						webExchange.get("active_workflow").toString(), e.getMessage());
			}
		}
	}
	
	private void executeQuery(WebExchange webExchange) throws FailedTransactionException {
		try {
			for (String query : qe.getParsedQuery(webExchange)) {
				log.info("Execute Query -> " + query);
				DBConnection.executeUpdate(query);
				ReportMonitor.logSnapshotEntry(testcase, scen, webExchange.getCurrentSession(), 
						SnapshotEntry.SNAPSHOT_AS_RAWTEXT, query, null, ReportManager.PASSED);
			}	
		} catch (Exception e) {
			throw new FailedTransactionException("Failed execute query " + e.getMessage());
		}
	}
}
