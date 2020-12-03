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
public abstract class WebElementWrapper extends AbstractBaseDriver {

	public static final String DEFAULT_MAIN = "main";
	
	public static final String DEFAULT_MODAL = "//div[@class='modal fade modal-wide in']";
	
	public static final String DEFAULT_MODAL_CONFIRMATION = "//div[contains(@class, 'modal') and @role='dialog' and contains(@style,'block') and ./div[contains(@class, 'modal-sm')]]";
	
	public static final String DEFAULT_TOOLTIP = "//div/div[contains(@id,'tooltip') and contains(@class,'tooltip')]";
	
	protected void setInputFieldLike(String id, String value) {
		findElementByXpath("//input[contains(@id,'" + id + "')]").clear();
		findElementByXpath("//input[contains(@id,'" + id + "')]").sendKeys(value);
		Sleep.wait(200);
	}
	
	protected void setInputField(String id, String value) {
		findElementById(id).clear();
		findElementById(id).sendKeys(value);
		Sleep.wait(200);
	}
	
	protected void setDatepickerField(String id, String value) {
		findElementById(id).sendKeys(value);
		findElementByXpath("//td[contains(@class,'ui-datepicker-current-day')]").click();
		Sleep.wait(200);
	}
	
	protected void selectDropdown(String id, String textValue) {
		findElementByXpath("//span[@id='select2-" + id + "-container']").click();;
//		findElementByXpath("//div[contains(@id,'" + id + "')]//div//span").click();
		Sleep.wait(100);
		findElementByXpath("//ul[contains(@id,'" + id + "')]//li[text()='" + textValue + "']").click();
		Sleep.wait(200);
	}
	
	protected void clickButtonLookup(String id) {
		findElementById("buttonTo_" + id).click();
		Sleep.wait(1000);
	}
	
	protected void clickButtonLike(WebElement webElement, String id) {
		findElementByXpath(webElement, "//button[contains(@id,'" + id + "')]").click();
		Sleep.wait(1000);
	}
	
	protected void clickButton(WebElement webElement, String id) {
		findElementById(webElement, id).click();
		Sleep.wait(1000);
	}

	protected void clickButtonLike(String id) {
		findElementByXpath("//button[contains(@id,'" + id + "')]").click();
		Sleep.wait(1000);
	}
	
	protected void clickButton(String id) {
		findElementById(id).click();
		Sleep.wait(1000);
	}
	
	protected void selectSimpleLookupSearch(String id, String value) {
		setInputField("textInput_" + id, value);
	}
	
	protected void selectLookupSearch(String id, String value) {
		clickButtonLookup(id);
		Sleep.wait(1000);
		findElementByXpath("//div[@id='myModal_" + id + "']//div//div//div[@class='modal-body']//div//div//div[contains(@class,'search')]//input").sendKeys(value);
		Sleep.wait(1000);
		
		int index = 0;
		
		try {
			WebElement webElement = findElementByXpath("//table[contains(@id, '" + id + "')]//tbody//tr[./td[2]/text()='" + value + "']");
			index = Integer.valueOf(webElement.getAttribute("data-index"));
			
			findElementByXpath("//input[contains(@name, '" + id + "radio') and @type='radio' and @data-index='" + index + "']").click();	
		} catch (StaleElementReferenceException e) {
			Sleep.wait(3000);	
			findElementByXpath("//input[contains(@name, '" + id + "radio') and @type='radio' and @data-index='" + index + "']").click();	
		}
		
		findElementById("buttonSave_" + id).click();
		Sleep.wait(200);
	}
	
	protected void clickCustomTableSearch(String modalId, String tableId, String value) {
		findElementByXpath("//div[@id='" + modalId + "']//div//div//div//div//div[contains(@class,'search')]//input").sendKeys(value);
		Sleep.wait(3000);
		
		int index = 0;
		try {
			WebElement webElemenet = findElementByXpath("//table[contains(@id,'" + tableId + "')]//tbody//tr[./td/text()='" + value +"']");
			index = Integer.valueOf(webElemenet.getAttribute("data-index"));
			
			findElementByXpath("//table[contains(@id,'" + tableId + "')]//tbody//tr[@data-index='" + index + "']//td//input[@type='checkbox' and @data-index='" + index + "']").click();
		} catch (StaleElementReferenceException e) {
			Sleep.wait(3000);	
			findElementByXpath("//table[contains(@id,'" + tableId + "')]//tbody//tr[@data-index='" + index + "']//td//input[@type='checkbox' and @data-index='" + index + "']").click();
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
		findElementByXpath("//table[contains(@id,'" + id + "')]//tbody//tr[@data-index='" + index + "']//td//a").click();
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

	public String getTextByIdLike(String id) {
		return findElementByXpath("//p[contains(@id,'" + id + "')]").getText();
	}

	public String getTextDropdownLike(String id) {
		return findElementByXpath("//div[contains(@id,'" + id + "') and not(contains(@style,'none'))]//span[contains(@id,'" + id + "')]").getText();		
	}
	
	public String getTextDropdown(String id) {
		return findElementByXpath("//div[@id='detailInstructionTypeFull' and not(contains(@style,'none'))]//span[@id='detailInstructionTypeFull']").getText();		
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

	public WebElement getModalConfirmationElement(int timeout) {
		return findElementByXpath(DEFAULT_MODAL_CONFIRMATION, timeout);
	}
	
	public WebElement getModalConfirmationElement() {
		return findElementByXpath(DEFAULT_MODAL_CONFIRMATION);
	}

	public WebElement getModalElement() {
		return findElementByXpath(DEFAULT_MODAL);
	}
	
	public WebElement getModalElement(int timeout) {
		return findElementByXpath(DEFAULT_MODAL, timeout);
	}
	
	public WebElement getTooltipElement() {
		return findElementByXpath(DEFAULT_TOOLTIP);
	}

	public String getModalConfirmationId() {
		return getModalConfirmationElement().getAttribute("id");
	}
	
	public String getModalConfirmationId(int timeout) {
		return getModalConfirmationElement(timeout).getAttribute("id");
	}
	
	public String getModalId(int timeout) {
		return getModalElement(timeout).getAttribute("id");
	}
	
	public String getModalId() {
		return getModalElement().getAttribute("id");
	}
	
	
}
