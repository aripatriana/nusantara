package com.nusantara.automate.action.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nusantara.automate.Actionable;
import com.nusantara.automate.WebExchange;
import com.nusantara.automate.exception.FailedTransactionException;
import com.nusantara.automate.util.Sleep;


/**
 * The action for delay process
 * 
 * @author ari.patriana
 *
 */
public class DelayAction implements Actionable {

	Logger log = LoggerFactory.getLogger(DelayAction.class);
	
	private int delay = 0;
	
	public DelayAction(String delay) {
		try {
			this.delay = Integer.valueOf(delay);
		} catch (Exception e) {
			this.delay = 0;
		}
	}
	
	@Override
	public void submit(WebExchange webExchange) throws FailedTransactionException {
		log.info("Delay for " + delay + "s");
		Sleep.wait(delay*1000);
	}

	
}
