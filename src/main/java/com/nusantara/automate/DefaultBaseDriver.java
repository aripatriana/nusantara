package com.nusantara.automate;

/**
 * Abstract class for default driver used for browser
 * 
 * @author ari.patriana
 *
 */
@Deprecated
public abstract class DefaultBaseDriver extends AbstractBaseDriver {

	public DefaultBaseDriver() {
		super(DriverManager.getDefaultDriver());
	}
}
