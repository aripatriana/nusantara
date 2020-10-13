package com.nusantara.automate.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.nusantara.automate.Actionable;
import com.nusantara.automate.BasicScript;
import com.nusantara.automate.ContextLoader;
import com.nusantara.automate.FileRetention;
import com.nusantara.automate.action.common.LoginFormAction;
import com.nusantara.automate.action.common.LogoutFormAction;
import com.nusantara.automate.action.common.ProductSelectorAction;
import com.nusantara.automate.handler.ModalType;
import com.nusantara.automate.reader.MadnessXlsFileReader;
import com.nusantara.automate.util.LoginInfo;
import com.nusantara.automate.util.ReflectionUtils;
import com.nusantara.automate.util.SimpleEntry;
import com.nusantara.automate.util.Sleep;

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
	
	@Value("user.create.memberCode")
	private String memberCodeCreate;
	@Value("user.create.username")
	private String usernameCreate;
	@Value("user.create.password")
	private String passwordCreate;
	@Value("user.create.keyFile")
	private String keyFileCreate;
	
	@Value("user.check.memberCode")
	private String memberCodeCheck;
	@Value("user.check.username")
	private String usernameCheck;
	@Value("user.check.password")
	private String passwordCheck;
	@Value("user.check.keyFile")
	private String keyFileCheck;
	
	@Value("user.approve.memberCode")
	private String memberCodeApprove;
	@Value("user.approve.username")
	private String usernameApprove;
	@Value("user.approve.password")
	private String passwordApprove;
	@Value("user.approve.keyFile")
	private String keyFileApprove;
	
	
	public void execute(String scen, Workflow workflow, WorkflowConfig config) {
		String productType = null;
		for (String workflowKey : config.getWorkflowKey(scen)) {
			log.info("Execute workflow " + workflowKey);
			ContextLoader.getWebExchange().put("active_workflow", workflowKey);
			
			for (WorkflowEntry entry : config.getWorkflowEntries(workflowKey)) {
				if (entry.isLoadFile()) {
					 workflow
					 	.load(new FileRetention(new MadnessXlsFileReader(config.getWorkflowData(entry.getVariable()))))
						.loop();
				} else if (entry.isLogin()) {
					 workflow
						.openPage(loginUrl)
						.action(new LoginFormAction(getLoginInfo(entry.getVariable())));
				} else if (entry.isLogout()) {
					workflow.action(new LogoutFormAction());
				} else if (entry.isRelogin()) {
					workflow
						.action(new LogoutFormAction())
						.action(new LoginFormAction(getLoginInfo(entry.getVariable())))
						.action(new ProductSelectorAction(productType));
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
						ReflectionUtils.invokeMethod(handler, BasicScript.REJECT_DETAIL, new Class[] {Workflow.class, ModalType.class}, new Object[] {workflow, ModalType.DETAIL});
					} else if (BasicScript.MULTIPLE_REJECT.equals(entry.getActionType())) {
						ReflectionUtils.invokeMethod(handler, BasicScript.MULTIPLE_REJECT, Workflow.class, workflow);
					} else if (BasicScript.APPROVE_DETAIL.equals(entry.getActionType())) {
						ReflectionUtils.invokeMethod(handler, BasicScript.APPROVE_DETAIL, new Class[] {Workflow.class, ModalType.class}, new Object[] {workflow, ModalType.DETAIL});
					} else if (BasicScript.MULTIPLE_APPROVE.equals(entry.getActionType())) {
						ReflectionUtils.invokeMethod(handler, BasicScript.MULTIPLE_APPROVE, Workflow.class, workflow);
					} else if (BasicScript.CHECK_DETAIL.equals(entry.getActionType())) {
						ReflectionUtils.invokeMethod(handler, BasicScript.CHECK_DETAIL, new Class[] {Workflow.class, ModalType.class}, new Object[] {workflow, ModalType.DETAIL});
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
						// do nothing
					}
					workflow.clearSession();
				}
			}
			
			Sleep.wait(1000);
		}
	}
	
	public LoginInfo getLoginInfo(String variable) {
		if ("user.create".equals(variable)) {
			return new LoginInfo(memberCodeCreate, usernameCreate, passwordCreate, keyFileCreate);
		} else if ("user.check".equals(variable)) {
			return new LoginInfo(memberCodeCheck, usernameCheck, passwordCheck, keyFileCheck);
		} else if ("user.approve".equals(variable)) {
			return new LoginInfo(memberCodeApprove, usernameApprove, passwordApprove, keyFileApprove);
		}
		return null;
	}
	
	public static void main(String[] args) {
		test(new String[] {"A"});	
		testarg(new String[] {"A"});
	}
	
	private static void test(String[] s) {
		System.out.println(s[0]);
	}
	
	private static void testarg(String...s) {
		System.out.println(s[0]);
	}
}

