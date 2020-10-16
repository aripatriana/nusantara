package com.nusantara.automate.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nusantara.automate.ContextLoader;
import com.nusantara.automate.RunTestWorkflow;

/**
 * Default implementation of RunTestWorkflow
 * 
 * @author ari.patriana
 *
 */
public class RunTestWorkflowExecutable implements RunTestWorkflow, WorkflowConfigAwareness {

	Logger log = LoggerFactory.getLogger(RunTestWorkflowExecutable.class);
	
	WorkflowConfig workflowConfig;
	
	@Override
	public void setWorkflowConfig(WorkflowConfig workflowConfig) {
		this.workflowConfig = workflowConfig;
	}
	
	@Override
	public void testWorkflow() {
		for (String scen : workflowConfig.getWorkflowScens()) {
			try {
				Workflow workflow = ParalelizedWorkflow.configure();
				ContextLoader.getWebExchange().put("active_scen", scen);
				
				WorkflowExecutor executor = new WorkflowExecutor();
				ContextLoader.setObject(executor);
		
				executor.execute(scen, workflow, workflowConfig);
			} catch (Exception e) {
				log.error("FATAL error " + e.getMessage());
				e.printStackTrace();
			}			
		}
	}
}
