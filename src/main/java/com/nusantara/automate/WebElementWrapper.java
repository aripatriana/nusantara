package com.nusantara.automate;

import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;

import com.nusantara.automate.util.Sleep;

/**
 * Base class for any object that need the browser manager capabilities
 * 
 * @author ari.patriana
 *
 */
public abstract class WebElementWrapper extends DefaultBaseDriver {
	
	protected void setInputField(String id, String value) {
		findElementById(id).sendKeys(value);
		Sleep.wait(200);
	}
	
	protected void setDatepickerField(String id, String value) {
		findElementById(id).sendKeys(value);
		findElementByXpath("//td[contains(@class,'ui-datepicker-current-day')]").click();
		Sleep.wait(200);
	}
	
	protected void selectDropdown(String id, String textValue) {
		findElementByXpath("//div[contains(@id,'" + id + "')]//div//span").click();
		Sleep.wait(100);
		findElementByXpath("//ul[contains(@id,'" + id + "')]//li[text()='" + textValue + "']").click();
		Sleep.wait(200);
	}
	
	protected void clickButtonLookup(String id) {
		findElementById("buttonTo_" + id).click();
		Sleep.wait(1000);
	}
	
	protected void clickButton(WebElement webElement, String id) {
		findElementById(webElement, id).click();
		Sleep.wait(1000);
	}
	
	protected void clickButton(String id) {
		findElementById(id).click();
		Sleep.wait(1000);
	}
	
	protected void selectLookupSearch(String id, String value) {
		selectLookupSearch(id, value, 0);
	}
	
	protected void selectSimpleLookupSearch(String id, String value) {
		setInputField("textInput_" + id, value);
	}
	
	protected void selectLookupSearch(String id, String value, int index) {
		clickButtonLookup(id);
		Sleep.wait(1000);
		findElementByXpath("//div[@id='myModal_" + id + "']//div//div//div[@class='modal-body']//div//div//div[contains(@class,'search')]//input").sendKeys(value);
		Sleep.wait(3000);
		
		try {
			findElementByXpath("//input[contains(@name, '" + id + "radio') and @type='radio' and @data-index='" + index + "']").click();	
		} catch (StaleElementReferenceException e) {
			Sleep.wait(3000);	
			findElementByXpath("//input[contains(@name, '" + id + "radio') and @type='radio' and @data-index='" + index + "']").click();	
		}
		
		findElementById("buttonSave_" + id).click();
		Sleep.wait(200);
	}

	protected void clickCustomTableSearch(String modalId, String tableId, String value) {
		clickCustomTableSearch(modalId, tableId, value, 0);
	}
	
	protected void clickCustomTableSearch(String modalId, String tableId, String value, int index) {
		findElementByXpath("//div[@id='" + modalId + "']//div//div//div//div//div[contains(@class,'search')]//input").sendKeys(value);
		Sleep.wait(3000);
		
		try {
			findElementByXpath("//table[@id='" + tableId + "']//tbody//tr[@data-index='" + index + "']//td//input[@type='checkbox' and @data-index='" + index + "']").click();
		} catch (StaleElementReferenceException e) {
			Sleep.wait(3000);	
			findElementByXpath("//table[@id='" + tableId + "']//tbody//tr[@data-index='" + index + "']//td//input[@type='checkbox' and @data-index='" + index + "']").click();
		}
	}
	
	protected void clickCheckBoxTableSearch(String id) {
		clickCheckBoxTableSearch(id, 0);
	}
	
	protected void clickCheckBoxTableSearch(String id, int index) {
		findElementByXpath("//table[@id='" + id + "']/tbody/tr[@data-index='" + index +"']/td/input[@type='checkbox']").click();
		Sleep.wait(2000);
	}
	
	protected void clickCheckBoxTableSearch(String id, String query) {
		WebElement webElement = findElementByXpath("//table[@id='" + id + "']/tbody/tr[./td/text()='" + query+ "']");
		String index = webElement.getAttribute("data-index");
		clickCheckBoxTableSearch(id, Integer.valueOf(index));
	}
	
	protected void clickTableSearch(String id) {
		clickTableSearch(id, 0);
	}
	
	protected void clickTableSearch(String id, int index) {
		findElementByXpath("//table[contains(@id,'" + id + "')]//tbody//tr[@data-index='" + index + "']//td//a[@href='#inquiry']").click();
		Sleep.wait(2000);
	}
	
	protected void clickPageNo(String formId, String value) {
		findElementByXpath("//form[@id='" + formId + "']//div[@class='row']//div[not(@id)]//div[1]//div[4]//span[@class='page-list']//span//button").click();
		findElementByXpath("//form[@id='" + formId + "']//div[@class='row']//div[not(@id)]//div[1]//div[4]//span[@class='page-list']//span//ul//li//a[text()='" + value + "']").click();
	}
	
	protected void clickPageFirst(String formId) {
		WebElement webElement = findElementByXpath("//form[@id='" + formId + "']//div[@class='row']//div[not(@id)]//div[1]//div[4]//ul[@class='pagination']//li[contains(@class,'page-first')]//a");
		if (webElement.isEnabled())
			webElement.click();
	}
	
	protected void clickPageLast(String formId) {
		WebElement liElement = findElementByXpath("//form[@id='" + formId + "']//div[@class='row']//div[not(@id)]//div[1]//div[4]//ul[@class='pagination']//li[contains(@class,'page-last')]");
		if (!liElement.getAttribute("class").contains("disabled")) {
			WebElement aElement = findElementByXpath("//form[@id='" + formId + "']//div[@class='row']//div[not(@id)]//div[1]//div[4]//ul[@class='pagination']//li[contains(@class,'page-last')]//a");
			aElement.click();		
		}		
	}

	
	public String getTextById(String id) {
		return findElementById(id).getText();
	}
	
	public String getTextByName(String name) {
		return findElementByName(name).getText();
	}
	
	public String getTextByXPath(String xpath) {
		return findElementByXpath(xpath).getText();
	}
	
	public String getTextById(WebElement webElement, String id) {
		return findElementById(webElement, id).getText();
	}
	
	public String getTextByName(WebElement webElement, String name) {
		return findElementByName(webElement, name).getText();
	}
	
	public String getTextByXPath(WebElement webElement, String xpath) {
		return findElementByXpath(webElement, xpath).getText();
	}
}
