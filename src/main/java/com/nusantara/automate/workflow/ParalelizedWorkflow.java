package com.nusantara.automate.workflow;

import java.util.Map.Entry;

import com.nusantara.automate.Actionable;
import com.nusantara.automate.ConfigLoader;
import com.nusantara.automate.ContextLoader;
import com.nusantara.automate.MenuAwareness;
import com.nusantara.automate.WebExchange;
import com.nusantara.automate.action.common.LogoutFormAction;

/**
 * Workflow that supports for session operation
 * 
 * @author ari.patriana
 *
 */
public class ParalelizedWorkflow extends Workflow {

	
	public ParalelizedWorkflow(WebExchange webExchange) {
		super(webExchange);
	}
	
	public static Workflow configure() {
		WebExchange webExchange = new WebExchange();
		for (Entry<String, Object> config : ConfigLoader.getConfigMap().entrySet()) {
			webExchange.put(config.getKey(), config.getValue());
		}
		ContextLoader.setWebExchange(webExchange);
		return new ParalelizedWorkflow(webExchange);
	}
	
	public Workflow endLoop() {
		if (!activeLoop) {
			throw new RuntimeException("Loop must be initialized");	
		}
		
		if (webExchange.getMetaDataSize() > 0) {
			log.info("Total data-row " + webExchange.getMetaDataSize());
			try {
				for (Actionable actionable : actionableForLoop) {
					
					if (actionable instanceof MenuAwareness) {
						activeMenu = ((MenuAwareness) actionable).getMenu();
					}
					
					// execute common action		
					// cek klo sesi gagal semua maka logout saja yg diproses
					if (webExchange.getSessionList().size() > 0
							&& (webExchange.getSessionList().size() <= webExchange.getFailedSessionList().size())) {
						if (actionable instanceof LogoutFormAction 
								&& (webExchange.get("token") != null || !webExchange.get("token").toString().isEmpty())) {
							ContextLoader.setObject(actionable);
							executeSafeActionable(actionable);	
						}
					}  else {
						executeActionableWithSession(actionable);						
					}
				}
			} catch (Exception e) { 
				log.info("Transaction interrupted ");
				e.printStackTrace();
			}	
		}
			
		webExchange.setRetention(Boolean.FALSE);
		webExchange.clearMetaData();
		activeLoop = false;
		return this;
	}

	
}
