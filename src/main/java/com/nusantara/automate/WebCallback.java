package com.nusantara.automate;

import org.openqa.selenium.WebElement;

import com.nusantara.automate.exception.FailedTransactionException;
import com.nusantara.automate.exception.ModalFailedException;

/**
 * The implementation of callback that support browser manager
 * 
 * @author ari.patriana
 *
 */
public abstract class WebCallback extends WebElementWrapper implements Callback {

	private String successId;
	
	private String[] failedId;
	
	public WebCallback() {
	}
	
	public WebCallback(String successId, String[] failedId) {
		this.successId = successId;
		this.failedId = failedId;
	}
	
	public void setSuccessId(String successId) {
		this.successId = successId;
	}
	
	public String getSuccessId() {
		return successId;
	}
	
	public void setFailedId(String[] failedId) {
		this.failedId = failedId;
	}
	
	public String[] getFailedId() {
		return failedId;
	}
	
	@Override
	public void callback(WebElement webElement, WebExchange webExchange) throws FailedTransactionException, ModalFailedException {
		if (webElement.getAttribute("id") != null && webElement.getAttribute("id").equals(successId)) {
			webExchange.put("@response_modal", "success");
			ok(webElement, webExchange);
		} else {
			webExchange.put("@response_modal", "failed");
			notOk(webElement, webExchange);
		}
	}
	
	public abstract void ok(WebElement webElement, WebExchange webExchange) throws FailedTransactionException ;
	
	public void notOk(WebElement webElement, WebExchange webExchange) throws FailedTransactionException, ModalFailedException{
		try {
			if (findElementById(webElement, "failedOk",1) != null) {
				captureWindow();
				clickButton(webElement, "failedOk");
				try {
					captureFullModal(getModalId());			
				} catch (Exception e) {
					captureFullWindow();
				}				
			}
		} catch (Exception e) {
			try {
				captureFullModal(getModalId());			
			} catch (Exception e1) {
				captureFullWindow();
			}	
		}
		throw new ModalFailedException("Modal failed");
	}
}
