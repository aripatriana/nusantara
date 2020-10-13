package com.nusantara.automate;

import org.openqa.selenium.WebElement;

import com.nusantara.automate.exception.FailedTransactionException;

/**
 * The needed for callback
 * 
 * @author ari.patriana
 *
 */
public interface Callback  {

	public void callback(WebElement webElement, WebExchange webExchange) throws FailedTransactionException;

}
