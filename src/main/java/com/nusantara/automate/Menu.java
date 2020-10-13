package com.nusantara.automate;

/**
 * All menu should be implemented by this class if want to be managed by the system
 * 
 * @author ari.patriana
 *
 */
public interface Menu {

	public String getMenu();
	
	public String getMenuId();
	
	public String getForm();
	
	public String getSubMenu();
	
	public String getId();
}
