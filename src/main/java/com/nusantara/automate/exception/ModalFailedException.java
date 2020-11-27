package com.nusantara.automate.exception;

public class ModalFailedException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Object[] object;
	
	public ModalFailedException(String message, Object... object) {
		super(message);
		this.object = object;
	}
	
	public ModalFailedException(String message) { 
		super(message);
	}
	
	public Object[] getObjects() {
		return object;
	}

}
