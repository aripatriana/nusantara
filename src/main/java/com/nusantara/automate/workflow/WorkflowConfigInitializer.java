package com.nusantara.automate.workflow;

/**
 * This is implemented by any custom object to register handler and custom function
 * 
 * @author ari.patriana
 *
 */
public interface WorkflowConfigInitializer {

	public void configure(WorkflowConfig workflowConfig);
	
}
