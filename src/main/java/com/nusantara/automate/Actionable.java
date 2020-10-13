package com.nusantara.automate;

import com.nusantara.automate.exception.FailedTransactionException;

/**
 * The implementation of logic workflow should be implemented through this class
 * 
 * @author ari.patriana
 *
 */
public interface Actionable {

	public void submit(WebExchange webExchange) throws FailedTransactionException;
	
}
