package com.nusantara.automate.annotation;

public enum MapType {

	/**
	 * Inject data from data file and session
	 */
	RETENTION,
	
	
	/**
	 * Inject data from session 
	 */
	LOCAL,

	/**
	 * Inject data from data file and session, but duplicate concept with RETENTION 
	 * so this type is deprecated
	 */
	@Deprecated
	COMPOSITE;
}
