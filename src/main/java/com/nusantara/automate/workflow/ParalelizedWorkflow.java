package com.nusantara.automate.workflow;

import java.util.Map.Entry;

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
		
		if (webExchange.getListMetaData(getActiveMenu().getId()) != null) {
			log.info("Total data-row " + webExchange.getListMetaData(getActiveMenu().getId()).size());
			try {
				for (Actionable actionable : actionableForLoop) {
					
					if (actionable instanceof MenuAwareness) {
						activeMenu = ((MenuAwareness) actionable).getMenu();
					}
					executeActionableWithSession(actionable);
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
