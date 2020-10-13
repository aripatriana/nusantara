package com.nusantara.automate;

import org.openqa.selenium.WebElement;

import com.nusantara.automate.exception.FailedTransactionException;

/**
 * The implementation of callback that support browser manager
 * 
 * @author ari.patriana
 *
 */
public abstract class WebCallback extends WebElementWrapper implements Callback {

	private String successId;
	
	private String failedId;
	
	public WebCallback() {
	}
	
	public WebCallback(String successId, String failedId) {
		this.successId = successId;
		this.failedId = failedId;
	}
	
	public void setSuccessId(String successId) {
		this.successId = successId;
	}
	
	public String getSuccessId() {
		return successId;
	}
	
	public void setFailedId(String failedId) {
		this.failedId = failedId;
	}
	
	public String getFailedId() {
		return failedId;
	}
	
	@Override
	public void callback(WebElement webElement, WebExchange webExchange) throws FailedTransactionException {
		if (webElement.getAttribute("id").equals(successId)) {
			ok(webElement, webExchange);
		} else {
			notOk(webElement, webExchange);
		}
	}
	
	public abstract void ok(WebElement webElement, WebExchange webExchange) throws FailedTransactionException ;
	
	public void notOk(WebElement webElement, WebExchange webExchange) throws FailedTransactionException{
		captureWindow();
		clickButton("failedOk");
		throw new FailedTransactionException("Failed for transaction");
	}
}
