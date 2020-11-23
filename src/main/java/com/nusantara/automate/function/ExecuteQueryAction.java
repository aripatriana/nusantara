package com.nusantara.automate.function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nusantara.automate.Actionable;
import com.nusantara.automate.DBConnection;
import com.nusantara.automate.WebExchange;
import com.nusantara.automate.exception.FailedTransactionException;
import com.nusantara.automate.query.QueryEntry;
import com.nusantara.automate.report.ReportMonitor;


public class ExecuteQueryAction implements Actionable {

	Logger log = LoggerFactory.getLogger(ExecuteQueryAction.class);
	
	private QueryEntry qe;
	
	public ExecuteQueryAction(QueryEntry qe) {
		this.qe = qe;
	}
	
	@Override
	public void submit(WebExchange webExchange) throws FailedTransactionException {
		log.info("Query -> " + qe.getQuery());
		
		boolean executeBatch = false;
		for (String param : qe.getParameters()) {
			if (param.startsWith("@" + WebExchange.PREFIX_TYPE_DATA))
				executeBatch = true;
		}
		
		if (executeBatch) {
			for (int i=0; i<webExchange.getCountSession(); i++) {
				webExchange.setCurrentSession(i);
				try {
					executeQuery(webExchange);			
				} catch (FailedTransactionException e) {
					webExchange.addFailedSession(webExchange.getCurrentSession());
					ReportMonitor.logError(webExchange.get("active_scen").toString(),
							webExchange.get("active_workflow").toString(), e.getMessage());
				}
			}				
		} else {
			try {
				executeQuery(webExchange);
			} catch (FailedTransactionException e) {
				webExchange.addFailedSession(webExchange.getCurrentSession());
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
			}	
		} catch (Exception e) {
			throw new FailedTransactionException("Failed execute query " + e.getMessage());
		}
	}
}
