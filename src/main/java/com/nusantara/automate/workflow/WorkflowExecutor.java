package com.nusantara.automate.workflow;

import java.util.Map;

import org.apache.log4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.nusantara.automate.Actionable;
import com.nusantara.automate.BasicScript;
import com.nusantara.automate.ConfigLoader;
import com.nusantara.automate.ContextLoader;
import com.nusantara.automate.DriverManager;
import com.nusantara.automate.FileRetention;
import com.nusantara.automate.RunTestApplication;
import com.nusantara.automate.Statement;
import com.nusantara.automate.WebExchange;
import com.nusantara.automate.action.common.DelayAction;
import com.nusantara.automate.action.common.LoginFormAction;
import com.nusantara.automate.action.common.LogoutFormAction;
import com.nusantara.automate.action.common.ProductSelectorAction;
import com.nusantara.automate.exception.FailedTransactionException;
import com.nusantara.automate.exception.ModalFailedException;
import com.nusantara.automate.exception.ScriptInvalidException;
import com.nusantara.automate.exception.XlsSheetStyleException;
import com.nusantara.automate.function.AssertQueryAction;
import com.nusantara.automate.function.AssertStatementAction;
import com.nusantara.automate.function.ExecuteQueryAction;
import com.nusantara.automate.handler.ModalType;
import com.nusantara.automate.query.QueryEntry;
import com.nusantara.automate.reader.MultiLayerXlsFileReader;
import com.nusantara.automate.reader.QueryReader;
import com.nusantara.automate.reader.TemplateReader;
import com.nusantara.automate.report.ReportMonitor;
import com.nusantara.automate.util.LoginInfo;
import com.nusantara.automate.util.ReflectionUtils;
import com.nusantara.automate.util.SimpleEntry;
import com.nusantara.automate.util.StringUtils;

/**
 * The execution of workflow comes from here
 * 
 * @author ari.patriana
 *
 */
public class WorkflowExecutor {

	Logger log = LoggerFactory.getLogger(WorkflowExecutor.class);
	
	@Value("login.url.it")
	private String loginUrl;
	
	@Value("login.url.cm")
	private String loginUrlCm;
	
	public void execute(String scen, Workflow workflow, WorkflowConfig config) throws Exception {
		String productType = null;
		for (String workflowKey : config.getWorkflowKey(scen)) {
	        MDC.put("testcase", workflowKey);
	        
			try {
				log.info("Execute workflow " + workflowKey);
				ContextLoader.getWebExchange().put("active_workflow", workflowKey);
				if (config.getWorkflowModule(workflowKey) != null)
					ContextLoader.getWebExchange().setModules(config.getWorkflowModule(workflowKey));
				
				for (WorkflowEntry entry : config.getWorkflowEntries(workflowKey)) {
					if (entry.checkKeyword(BasicScript.LOAD_FILE)) {
						loadFile(config, entry, workflow, workflowKey);
					} else if (entry.checkKeyword(BasicScript.LOGIN)) {
						login(config, entry, workflow);
					} else if (entry.checkKeyword(BasicScript.LOGOUT)) {
						logout(config, entry, workflow);
					} else if (entry.checkKeyword(BasicScript.RELOGIN)) {
						relogin(config, entry, workflow, productType);
					} else if (entry.checkKeyword(BasicScript.SELECT_PRODUCT)) {
						productType = entry.getVariable();
						selectProduct(config, entry, workflow);
					} else if (entry.checkKeyword(BasicScript.OPEN_MENU)) {
						actionMenu(config, entry, workflow);
					} else if (entry.checkKeyword(BasicScript.EXECUTE)) {
						execute(config, entry, workflow);
					} else if (entry.checkKeyword(BasicScript.EXECUTE_QUERY)) {
						executeQuery(config, entry, workflow);
					} else if (entry.checkKeyword(BasicScript.ASSERT)) {
						asserts(config, entry, workflow);
					} else if (entry.checkKeyword(BasicScript.ASSERT_QUERY)) {
						assertQuery(config, entry, workflow);
					} else if (entry.checkKeyword(BasicScript.ASSERT_AGGREGATE)) {
						assertAggregate(config, entry, workflow, scen);
					} else if (entry.checkKeyword(BasicScript.CLEAR_SESSION)) {
						clearSession(config, entry, workflow);
					} else if (entry.checkKeyword(BasicScript.DELAY)) {
						delay(config, entry, workflow);
					}
				}
				
				ReportMonitor.completeScen(workflowKey);
			} catch (Exception e) {
				e.printStackTrace();
				log.error(e.toString());
				// scenario halted caused by exception
				ReportMonitor.scenHalted(scen, workflowKey, e.getMessage());
			} finally {
				// if exception occured in any state of the workflow, must be ensured to logout the system
				 if (workflow.getWebExchange().get("token") != null) {
					 try {
						 workflow.actionMajor(new LogoutFormAction());	 
					 } catch (Exception e) {
						 // if exception keeps stubborn then close driver
						 DriverManager.close();
					 }
				 }
					
				 MDC.remove("testcase");
			}
		}
	}
	
	private void relogin(WorkflowConfig wc, WorkflowEntry we, Workflow workflow, String productType) throws Exception {
		String prefix = LoginInfo.parsePrefixVariable(we.getVariable());
		final String loginUrl = ("it".equals(prefix)) ? this.loginUrl : this.loginUrlCm;		
		workflow
			.action(new LogoutFormAction())
			.action(new Actionable() {
				
				@Override
				public void submit(WebExchange webExchange) throws FailedTransactionException, ModalFailedException {
					workflow.openPage(loginUrl);
					
				}
			})
			.action(new LoginFormAction(getLoginInfo(LoginInfo.parseVariable(we.getVariable()))))
			.action(new ProductSelectorAction(productType));
	}
	
	private void logout(WorkflowConfig wc, WorkflowEntry we, Workflow workflow) {
		workflow.action(new LogoutFormAction());
	}
	
	private void login(WorkflowConfig wc, WorkflowEntry we, Workflow workflow) throws Exception {
		String prefix = LoginInfo.parsePrefixVariable(we.getVariable());
		final String loginUrl = ("it".equals(prefix)) ? this.loginUrl : this.loginUrlCm;	
		
		 workflow
			.openPage(loginUrl)
			.action(new LoginFormAction(getLoginInfo(LoginInfo.parseVariable(we.getVariable()))));
	}
	
	private void clearSession(WorkflowConfig wc, WorkflowEntry we, Workflow workflow) throws Exception {
		try {
			workflow.endLoop();	
		} catch (Exception e) {
			throw e;
		} finally {
			workflow.clearSession();							
		}
	}
	
	private void selectProduct(WorkflowConfig wc, WorkflowEntry we, Workflow workflow) {
		workflow
			.action(new ProductSelectorAction(we.getVariable()));
	}
	
	private void delay(WorkflowConfig wc, WorkflowEntry we, Workflow workflow) {
		workflow
			.action(new DelayAction(we.getVariable()));
	}
	
	private void loadFile(WorkflowConfig wc, WorkflowEntry we, Workflow workflow, String workflowKey) throws Exception {
		 try {
			FileRetention retention = new FileRetention(new MultiLayerXlsFileReader(wc.getWorkflowData(we.getVariable())));
			workflow
			 	.load(retention)
				.loop();
		} catch (XlsSheetStyleException e) {
			throw new Exception(e);
		}
	}
	
	private void actionMenu(WorkflowConfig wc, WorkflowEntry we, Workflow workflow) {
		Class<?> clazz = wc.getHandler(we.getVariable());
		
		Object handler = ReflectionUtils.instanceObject(clazz);
		ContextLoader.setObject(handler);
	
		workflow.openMenu(wc.getMenu(we.getVariable()));
	
		workflow.scopedAction();
		if (BasicScript.VALIDATE.equals(we.getActionType())) {
			ReflectionUtils.invokeMethod(handler, BasicScript.VALIDATE, Workflow.class, workflow);
		} else if (BasicScript.SEARCH.equals(we.getActionType())) {
			ReflectionUtils.invokeMethod(handler, BasicScript.SEARCH, Workflow.class, workflow);
		} else if (BasicScript.CHECK.equals(we.getActionType())) {
			ReflectionUtils.invokeMethod(handler, BasicScript.CHECK, new Class[] {Workflow.class, ModalType.class}, new Object[] {workflow, ModalType.MAIN});
		} else if (BasicScript.APPROVE.equals(we.getActionType())) {
			ReflectionUtils.invokeMethod(handler, BasicScript.APPROVE, new Class[] {Workflow.class, ModalType.class}, new Object[] {workflow, ModalType.MAIN});
		} else if (BasicScript.REJECT.equals(we.getActionType())) {
			ReflectionUtils.invokeMethod(handler, BasicScript.REJECT, new Class[] {Workflow.class, ModalType.class}, new Object[] {workflow, ModalType.MAIN});
		} else if (BasicScript.REJECT_DETAIL.equals(we.getActionType())) {
			ReflectionUtils.invokeMethod(handler, BasicScript.REJECT, new Class[] {Workflow.class, ModalType.class}, new Object[] {workflow, ModalType.DETAIL});
		} else if (BasicScript.MULTIPLE_REJECT.equals(we.getActionType())) {
			ReflectionUtils.invokeMethod(handler, BasicScript.MULTIPLE_REJECT, Workflow.class, workflow);
		} else if (BasicScript.APPROVE_DETAIL.equals(we.getActionType())) {
			ReflectionUtils.invokeMethod(handler, BasicScript.APPROVE, new Class[] {Workflow.class, ModalType.class}, new Object[] {workflow, ModalType.DETAIL});
		} else if (BasicScript.MULTIPLE_APPROVE.equals(we.getActionType())) {
			ReflectionUtils.invokeMethod(handler, BasicScript.MULTIPLE_APPROVE, Workflow.class, workflow);
		} else if (BasicScript.CHECK_DETAIL.equals(we.getActionType())) {
			ReflectionUtils.invokeMethod(handler, BasicScript.CHECK, new Class[] {Workflow.class, ModalType.class}, new Object[] {workflow, ModalType.DETAIL});
		} else if (BasicScript.MULTIPLE_CHECK.equals(we.getActionType())) {
			ReflectionUtils.invokeMethod(handler, BasicScript.MULTIPLE_CHECK, Workflow.class, workflow);
		}
		workflow.resetScopedAction();
	}
	
	private void execute(WorkflowConfig wc, WorkflowEntry we, Workflow workflow) {
		SimpleEntry<Class<?>, Object[]> function = wc.getFunction(we.getVariable());
		Object object = ReflectionUtils.instanceObject(function.getKey(), function.getValue());
		ContextLoader.setObject(object);
		workflow.action((Actionable) object);
	}
	
	private void executeQuery(WorkflowConfig wc, WorkflowEntry we, Workflow workflow) throws ScriptInvalidException {
		try {
			QueryReader qr = new QueryReader(we.getVariable());
			QueryEntry qe = qr.read();
			ExecuteQueryAction actionable = new ExecuteQueryAction(qe);
			workflow.action(actionable);
		} catch (ScriptInvalidException e) {
			throw e;
		}
	}
	
	private void asserts(WorkflowConfig wc, WorkflowEntry we, Workflow workflow) throws ScriptInvalidException {
		try {
			String[] result = StringUtils.parseStatement(we.getVariable(), Statement.MARK);
			AssertStatementAction actionable = new AssertStatementAction(new Statement(result[0], result[1], result[2]));
			workflow.action(actionable);
		} catch (ScriptInvalidException e) {
			throw e;
		}
	}
	
	private void assertQuery(WorkflowConfig wc, WorkflowEntry we, Workflow workflow) throws ScriptInvalidException {
		try {
			QueryReader qr = new QueryReader(we.getVariable());
			QueryEntry qe = qr.read();
			AssertQueryAction actionable = new AssertQueryAction(qe);
			workflow.action(actionable);
		} catch (ScriptInvalidException e) {
			throw e;
		}
	}
	
	private void assertAggregate(WorkflowConfig wc, WorkflowEntry we, Workflow workflow, String testCase) throws ScriptInvalidException {
		try {			
			TemplateReader tr = new TemplateReader(wc.getWorkflowQuery(testCase, we.getVariable()));
			QueryReader qr = new QueryReader(tr.read().toString());
			QueryEntry qe = qr.read();
			AssertQueryAction actionable = new AssertQueryAction(qe);
			workflow.action(actionable);
		} catch (ScriptInvalidException e) {
			throw e;
		}
	}
	
	public LoginInfo getLoginInfo(String variable) {
		Map<String, Object> loginUser = ConfigLoader.getLoginInfo(variable);
		return new LoginInfo(loginUser.get(variable + "." + RunTestApplication.PREFIX_MEMBER_CODE).toString(), 
				loginUser.get(variable + "." + RunTestApplication.PREFIX_USERNAME).toString(), 
				loginUser.get(variable + "." + RunTestApplication.PREFIX_PASSWORD).toString(), 
				loginUser.get(variable + "." + RunTestApplication.PREFIX_KEYFILE).toString());
	}
}

