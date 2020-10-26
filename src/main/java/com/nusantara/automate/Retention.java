package com.nusantara.automate;

/**
 * Perform all operation related to fetching data from other data file
 * 
 * @author ari.patriana
 *
 */
public interface Retention {

	public void perform(WebExchange webExchange);
	
	public int getSize();
	
}
