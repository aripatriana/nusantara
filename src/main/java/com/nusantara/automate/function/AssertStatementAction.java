package com.nusantara.automate.function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.nusantara.automate.Actionable;
import com.nusantara.automate.Assertion;
import com.nusantara.automate.Statement;
import com.nusantara.automate.WebExchange;
import com.nusantara.automate.exception.FailedTransactionException;
import com.nusantara.automate.report.ReportManager;
import com.nusantara.automate.report.ReportMonitor;
import com.nusantara.automate.report.SnapshotEntry;
import com.nusantara.automate.util.DataTypeUtils;
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
		try {
			assertStatement(webExchange);
		} catch (FailedTransactionException e) {
			webExchange.addFailedSession(webExchange.getCurrentSession());
			ReportMonitor.logError(webExchange.get("active_scen").toString(),
					webExchange.get("active_workflow").toString(), e.getMessage());
		}
	}
	
	private void assertStatement(WebExchange webExchange) throws FailedTransactionException {
		Assertion assertion = new Assertion();
		if (statement.isArg1(DataTypeUtils.TYPE_OF_COLUMN)) {
			statement.setVal1("");
		} else if (statement.isArg1(DataTypeUtils.TYPE_OF_VARIABLE)) {
			statement.setVal1(StringUtils.nvl(webExchange.get(statement.getArg1()),"null"));
		} else {
			statement.setVal1(statement.getArg1());
		}
		if (statement.isArg2(DataTypeUtils.TYPE_OF_COLUMN)) {
			statement.setVal2("");
		} else if (statement.isArg2(DataTypeUtils.TYPE_OF_VARIABLE)) {
			statement.setVal2(StringUtils.nvl(webExchange.get(statement.getArg2()), "null"));
		} else {
			statement.setVal2(statement.getArg2());
		}
		assertion.addStatement(statement);

		log.info("Assert " + assertion.getAssertion());
		
		SnapshotEntry entry = new SnapshotEntry();
		entry.setTscenId(scen);
		entry.setTestCaseId(testcase);
		entry.setSnapshotAs(SnapshotEntry.SNAPSHOT_AS_RAWTEXT);
		entry.setRawText(assertion.getAssertion());
		entry.setStatus((assertion.isTrue() ? ReportManager.PASSED : ReportManager.FAILED));
		
		ReportMonitor.logSnapshotEntry(entry);

		if (!assertion.isTrue())
			throw new FailedTransactionException("Failed assertion", entry);	
	}
}
