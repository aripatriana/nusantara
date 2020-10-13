package com.nusantara.automate;

/**
 * Used to inject menu
 * 
 * @author ari.patriana
 *
 */
public interface MenuAwareness {

	public void setMenu(Menu menu);
	
	public Menu getMenu();
}
