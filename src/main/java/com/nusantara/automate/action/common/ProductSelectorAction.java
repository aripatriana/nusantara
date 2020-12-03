package com.nusantara.automate.action.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nusantara.automate.Actionable;
import com.nusantara.automate.WebElementWrapper;
import com.nusantara.automate.WebExchange;
import com.nusantara.automate.exception.FailedTransactionException;
import com.nusantara.automate.util.Sleep;

/**
 * The action for select the product choosen
 *  
 * @author ari.patriana
 *
 */
public class ProductSelectorAction extends WebElementWrapper implements Actionable {

	Logger log = LoggerFactory.getLogger(ProductSelectorAction.class);
	
	private String productType;
	
	public ProductSelectorAction(String productType) {
		this.productType = productType;
	}
	
	public void setProductType(String productType) {
		this.productType = productType;
	}
	
	public String getProductType() {
		return productType;
	}
	
	@Override
	public void submit(WebExchange webExchange) throws FailedTransactionException {

		 if (webExchange.get("token") == null)
			 throw new FailedTransactionException("Workflow halted caused by login failed");
		 
		log.info("Open Product " + getProductType());
		
		Sleep.wait(1000);
		findElementByClassName("divProductTypeSelector").click();
		findElementByXpath("//*[contains(@href,'" + getProductType() + "')]").click();
	}

}
