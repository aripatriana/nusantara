package com.nusantara.automate;

public abstract class DefaultBaseDriver extends AbstractBaseDriver {

	public DefaultBaseDriver() {
		super(DriverManager.getDefaultDriver());
	}
}
