package com.nusantara.automate.workflow;

import com.nusantara.automate.Actionable;
import com.nusantara.automate.ConfigLoader;
import com.nusantara.automate.ContextLoader;
import com.nusantara.automate.MenuAwareness;
import com.nusantara.automate.WebExchange;

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
		webExchange.putAll(ConfigLoader.getConfigMap());
		webExchange.addElements(ConfigLoader.getElementMap());
		ContextLoader.setWebExchange(webExchange);
		return new ParalelizedWorkflow(webExchange);
	}
	
	public Workflow endLoop() throws Exception {
		if (!activeLoop && webExchange.isRetention()) {
			throw new RuntimeException("Loop must be initialized");	
		}
		
		try {
			if (webExchange.getTotalMetaData() > 0) {
				webExchange.initSession(webExchange.getMetaDataSize());
				
				log.info("Total data-row " + webExchange.getTotalMetaData());
				try {
					for (Actionable actionable : actionableForLoop) {
						
						if (actionable instanceof MenuAwareness) {
							setActiveMenu(((MenuAwareness) actionable).getMenu());
						}
						
						// execute actionable if any session active, if all session failed no further process performed
						if (!(webExchange.getSessionList().size() > 0
								&& (webExchange.getSessionList().size() <= webExchange.getFailedSessionList().size()))) {
							executeActionableWithSession(actionable);						
						}
					}
				} catch (Exception e) { 
					log.info("Transaction interrupted ");
					log.error("ERROR ", e);
					throw e;
				}
			}	
		} finally {
			webExchange.setRetention(Boolean.FALSE);
			webExchange.clearMetaData();
			activeLoop = false;
		}
		
		return this;
	}
	
}
