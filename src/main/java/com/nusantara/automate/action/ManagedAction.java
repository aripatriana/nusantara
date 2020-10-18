package com.nusantara.automate.action;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import com.nusantara.automate.AbstractBaseDriver;
import com.nusantara.automate.Actionable;
import com.nusantara.automate.ContextLoader;
import com.nusantara.automate.WebExchange;
import com.nusantara.automate.exception.FailedTransactionException;

/**
 * The action is managed by the session
 * @author ari.patriana
 *
 */
public class ManagedAction extends AbstractBaseDriver implements Actionable {

	private LinkedList<Actionable> actionableList = new LinkedList<Actionable>();
	private Class<?> inheritClass;
	private Map<String, Object> metadata = new HashMap<String, Object>();
	
	public ManagedAction(Class<?> inheritClass) {
		this.inheritClass = inheritClass;
	}
	
	public Class<?> getInheritClass() {
		return inheritClass;
	}
	
	public void addActionable(Actionable actionable) {
		actionableList.add(actionable);
	}
	
	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}
	
	@Override
	public void submit(WebExchange webExchange) throws FailedTransactionException {
		for (Actionable actionable : actionableList) {
			setObject(actionable, metadata);
			actionable.submit(webExchange);
		}
	}
	
	public void setObject(Actionable actionable, Map<String, Object> metadata) {
		if (ContextLoader.isPersistentSerializable(inheritClass)) {
			// execute map serializable
			if (ContextLoader.isLocalVariable(actionable)) {
				ContextLoader.setObjectLocal(actionable);
			} else if (ContextLoader.isCompositeVariable(actionable)) {
				ContextLoader.setObjectLocalWithCustom(actionable, metadata);
			} else {
				ContextLoader.setObjectWithCustom(actionable, metadata);
			}
		} else {		
			ContextLoader.setObject(actionable);
		}
	}
}
