package com.nusantara.automate.workflow;

import com.nusantara.automate.ContextLoader;
import com.nusantara.automate.RunTestWorkflow;

/**
 * Default implementation of RunTestWorkflow
 * 
 * @author ari.patriana
 *
 */
public class RunTestWorkflowExecutable implements RunTestWorkflow, WorkflowConfigAwareness {

	WorkflowConfig workflowConfig;
	
	@Override
	public void setWorkflowConfig(WorkflowConfig workflowConfig) {
		this.workflowConfig = workflowConfig;
	}
	
	@Override
	public void testWorkflow() {
		for (String scen : workflowConfig.getWorkflowScens()) {
			Workflow workflow = ParalelizedWorkflow.configure();
			ContextLoader.getWebExchange().put("active_scen", scen);
			
			WorkflowExecutor executor = new WorkflowExecutor();
			ContextLoader.setObject(executor);
			executor.execute(scen, workflow, workflowConfig);			
		}
	}
}
