package com.nusantara.automate.exception;

public class FailedTransactionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Object[] object;
	
	public FailedTransactionException(String message, Object... object) {
		super(message);
		this.object = object;
	}
	
	public FailedTransactionException(String message) { 
		super(message);
	}
	
	public Object[] getObjects() {
		return object;
	}

}
