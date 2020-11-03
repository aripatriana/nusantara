package com.nusantara.automate.workflow;

import java.util.Map;

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
import com.nusantara.automate.action.common.LoginFormAction;
import com.nusantara.automate.action.common.LogoutFormAction;
import com.nusantara.automate.action.common.ProductSelectorAction;
import com.nusantara.automate.exception.XlsSheetStyleException;
import com.nusantara.automate.handler.ModalType;
import com.nusantara.automate.reader.MultiLayerXlsFileReader;
import com.nusantara.automate.report.ReportMonitor;
import com.nusantara.automate.util.LoginInfo;
import com.nusantara.automate.util.ReflectionUtils;
import com.nusantara.automate.util.SimpleEntry;

/**
 * The execution of workflow comes from here
 * 
 * @author ari.patriana
 *
 */
public class WorkflowExecutor {

	Logger log = LoggerFactory.getLogger(WorkflowExecutor.class);
	
	@Value("login.url")
	private String loginUrl;
	
	public void execute(String scen, Workflow workflow, WorkflowConfig config) throws Exception {
		String productType = null;
		for (String workflowKey : config.getWorkflowKey(scen)) {
			
			try {
				log.info("Execute workflow " + workflowKey);
				ContextLoader.getWebExchange().put("active_workflow", workflowKey);
				
				for (WorkflowEntry entry : config.getWorkflowEntries(workflowKey)) {
					if (entry.isLoadFile()) {
						 try {
							FileRetention retention = new FileRetention(new MultiLayerXlsFileReader(config.getWorkflowData(entry.getVariable())));
							workflow
							 	.load(retention)
								.loop();
							
							ReportMonitor.getScenEntry(workflowKey).setNumOfData(retention.getSize());
						} catch (XlsSheetStyleException e) {
							throw new Exception(e);
						}
					} else if (entry.isLogin()) {
						 workflow
							.openPage(loginUrl)
							.action(new LoginFormAction(getLoginInfo(entry.getVariable())));
						 if (workflow.getWebExchange().get("token") == null)
							 throw new Exception("Workflow halted caused by login failed");
					} else if (entry.isLogout()) {
						workflow.action(new LogoutFormAction());
					} else if (entry.isRelogin()) {
						workflow
							.action(new LogoutFormAction())
							.action(new LoginFormAction(getLoginInfo(entry.getVariable())))
							.action(new ProductSelectorAction(productType));
						 if (workflow.getWebExchange().get("token") == null)
							 throw new Exception("Workflow halted caused by login failed");
					} else if (entry.isSelectProduct()) {
						productType = entry.getVariable();
						workflow
							.action(new ProductSelectorAction(entry.getVariable()));
					} else if (entry.isActionMenu()) {
						
						Class<?> clazz = config.getHandler(entry.getVariable());
						
						Object handler = ReflectionUtils.instanceObject(clazz);
						ContextLoader.setObject(handler);
					
						workflow
							.openMenu(config.getMenu(entry.getVariable()));
					
						workflow.scopedAction();
						if (BasicScript.VALIDATE.equals(entry.getActionType())) {
							ReflectionUtils.invokeMethod(handler, BasicScript.VALIDATE, Workflow.class, workflow);
						} else if (BasicScript.SEARCH.equals(entry.getActionType())) {
							ReflectionUtils.invokeMethod(handler, BasicScript.SEARCH, Workflow.class, workflow);
						} else if (BasicScript.CHECK.equals(entry.getActionType())) {
							ReflectionUtils.invokeMethod(handler, BasicScript.CHECK, new Class[] {Workflow.class, ModalType.class}, new Object[] {workflow, ModalType.MAIN});
						} else if (BasicScript.APPROVE.equals(entry.getActionType())) {
							ReflectionUtils.invokeMethod(handler, BasicScript.APPROVE, new Class[] {Workflow.class, ModalType.class}, new Object[] {workflow, ModalType.MAIN});
						} else if (BasicScript.REJECT.equals(entry.getActionType())) {
							ReflectionUtils.invokeMethod(handler, BasicScript.REJECT, new Class[] {Workflow.class, ModalType.class}, new Object[] {workflow, ModalType.MAIN});
						} else if (BasicScript.REJECT_DETAIL.equals(entry.getActionType())) {
							ReflectionUtils.invokeMethod(handler, BasicScript.REJECT, new Class[] {Workflow.class, ModalType.class}, new Object[] {workflow, ModalType.DETAIL});
						} else if (BasicScript.MULTIPLE_REJECT.equals(entry.getActionType())) {
							ReflectionUtils.invokeMethod(handler, BasicScript.MULTIPLE_REJECT, Workflow.class, workflow);
						} else if (BasicScript.APPROVE_DETAIL.equals(entry.getActionType())) {
							ReflectionUtils.invokeMethod(handler, BasicScript.APPROVE, new Class[] {Workflow.class, ModalType.class}, new Object[] {workflow, ModalType.DETAIL});
						} else if (BasicScript.MULTIPLE_APPROVE.equals(entry.getActionType())) {
							ReflectionUtils.invokeMethod(handler, BasicScript.MULTIPLE_APPROVE, Workflow.class, workflow);
						} else if (BasicScript.CHECK_DETAIL.equals(entry.getActionType())) {
							ReflectionUtils.invokeMethod(handler, BasicScript.CHECK, new Class[] {Workflow.class, ModalType.class}, new Object[] {workflow, ModalType.DETAIL});
						} else if (BasicScript.MULTIPLE_CHECK.equals(entry.getActionType())) {
							ReflectionUtils.invokeMethod(handler, BasicScript.MULTIPLE_CHECK, Workflow.class, workflow);
						}
						workflow.resetScopedAction();
					} else if (entry.isFunction()) {
						SimpleEntry<Class<?>, Object[]> function = config.getFunction(entry.getVariable());
						Object object = ReflectionUtils.instanceObject(function.getKey(), function.getValue());
						ContextLoader.setObject(object);
						workflow
							.action((Actionable) object);
					} else if (entry.isClearSession()) {
						try {
							workflow.endLoop();	
						} catch (Exception e) {
							throw e;
						} finally {
							workflow.clearSession();							
						}
					}
				}
				
				ReportMonitor.completeScen(workflowKey);
			} catch (Exception e) {
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
					 
			}
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

