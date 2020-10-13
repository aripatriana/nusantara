package com.nusantara.automate.handler;

import com.nusantara.automate.exception.MethodNotSupportedException;
import com.nusantara.automate.workflow.Workflow;

/**
 * Base class for handle the event of form page
 * 
 * @author ari.patrana
 *
 */
public interface SubmitHandler {

	public void validate(Workflow workflow) throws MethodNotSupportedException;
	
	public void check(Workflow workflow, ModalType modalType) throws MethodNotSupportedException;
	
	public void checkMultiple(Workflow workflow) throws MethodNotSupportedException;
	
	public void reject(Workflow workflow, ModalType modalType) throws MethodNotSupportedException;
	
	public void rejectMultiple(Workflow workflow) throws MethodNotSupportedException;
	
	public void approve(Workflow workflow, ModalType modalType) throws MethodNotSupportedException;
	
	public void approveMultiple(Workflow workflow) throws MethodNotSupportedException;
	
	public void search(Workflow workflow) throws MethodNotSupportedException;
	
}
