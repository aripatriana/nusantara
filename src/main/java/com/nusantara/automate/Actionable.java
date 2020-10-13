package com.nusantara.automate;

import com.nusantara.automate.exception.FailedTransactionException;

public interface Actionable {

	public void submit(WebExchange webExchange) throws FailedTransactionException;
	
}
