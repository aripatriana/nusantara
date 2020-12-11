package com.nusantara.automate;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nusantara.automate.util.Sleep;

/**
 * Base class for any object that need the browser manager capabilities
 * 
 * @author ari.patriana
 *
 */
public abstract class WebElementWrapper extends AbstractBaseDriver {

	Logger log = LoggerFactory.getLogger(WebElementWrapper.class);
	
	private static int INPUT_TIMEOUT = 3;
	
	public static final String DEFAULT_MAIN = "main";
	
	public static final String DEFAULT_MODAL = "//div[@class='modal fade modal-wide in']";
	
	public static final String DEFAULT_MODAL_CONFIRMATION = "//div[contains(@class, 'modal') and @role='dialog' and contains(@style,'block') and ./div[contains(@class, 'modal-sm')]]";
	
	public static final String DEFAULT_TOOLTIP = "//div/div[contains(@id,'tooltip') and contains(@class,'tooltip')]";
	
	protected void setFocusOn(String id) {
		try {
			WebElement element = findElementById(id);
				if (element.isEnabled() && element.isDisplayed()) {
				if("input".equals(element.getTagName())) {
					element.sendKeys("");
				} else{
					JavascriptExecutor jse = (JavascriptExecutor) getDriver();
					jse.executeScript("document.getElementById('"+id+"').focus();");
				}
			}
		} catch (TimeoutException e) {
			log.info("Element " + id + " is not found");
		}
	}
	
	protected void setInputFieldLike(String id, String value) {
		try {
			WebElement we = findElementByXpath("//input[contains(@id,'" + id + "')]",INPUT_TIMEOUT); 
			if (we.isEnabled() && we.isDisplayed()) {
				we.clear();
				we.sendKeys(value);
				Sleep.wait(200);
			} else {
				log.info("Element " + id + " is not enabled/not displayed");
			}
		} catch (TimeoutException e) {
			log.info("Element " + id + " is not found");
		}
	}
	
	protected void setInputField(String id, String value) {
		try {
			WebElement we = findElementById(id,INPUT_TIMEOUT);
			if (we.isEnabled() && we.isDisplayed()) {
				we.sendKeys(Keys.chord(Keys.CONTROL, "a"));
				we.clear();
				we.sendKeys(value);
				Sleep.wait(200);
			} else {
				log.info("Element " + id + " is not enabled/not displayed");
			}
		} catch (TimeoutException e) {
			log.info("Element " + id + " is not found");
		}
	}
	
	protected void setDatepickerField(String id, String value) {
		try {
			WebElement we = findElementById(id,INPUT_TIMEOUT);
			if (we.isEnabled() && we.isDisplayed()) {
				we.sendKeys(value);
				findElementByXpath("//td[contains(@class,'ui-datepicker-current-day')]").click();
				Sleep.wait(200);
			} else {
				log.info("Element " + id + " is not enabled/not displayed");
			}		
		} catch (TimeoutException e) {
			log.info("Element " + id + " is not found");
		}
	}
	
	protected void selectDropdown(String id, String textValue) {
		try {
			WebElement we = findElementByXpath("//span[@id='select2-" + id + "-container']",INPUT_TIMEOUT);
			if (we.isEnabled() && we.isDisplayed()) {
				we.click();
				//findElementByXpath("//div[contains(@id,'" + id + "')]//div//span").click();
				Sleep.wait(100);
				findElementByXpath("//ul[contains(@id,'" + id + "')]//li[text()='" + textValue + "']", INPUT_TIMEOUT).click();
				Sleep.wait(200);
			} else {
				log.info("Element " + id + " is not enabled/not displayed");
			}
		} catch (TimeoutException e) {
			log.info("Element " + id + " is not found");
		}
	}
	
	protected void clickButtonLookup(String id) {
		try {
			WebElement we = findElementById("buttonTo_" + id,INPUT_TIMEOUT);
			if (we.isEnabled() && we.isDisplayed()) {
				we.click();
				Sleep.wait(1000);
			} else {
				log.info("Element buttonTo_" + id + " is not enabled/not displayed");
			}
		} catch (TimeoutException e) {
			log.info("Element buttonTo_" + id + " is not found");
		}
	}
	
	protected void clickButtonLike(WebElement webElement, String id) {
		try {
			WebElement we = findElementByXpath(webElement, "//button[contains(@id,'" + id + "')]",INPUT_TIMEOUT);
			if (we.isEnabled() && we.isDisplayed()) {
				we.click();
				Sleep.wait(1000);
			} else {
				log.info("Element " + id + " is not enabled/not displayed");
			}
		} catch (TimeoutException e) {
			log.info("Element " + id + " is not found");
		}
	}
	
	protected void clickButton(WebElement webElement, String id) {
		try {
			WebElement we = findElementById(webElement, id,INPUT_TIMEOUT);
			if (we.isEnabled() && we.isDisplayed()) {
				we.click();
				Sleep.wait(1000);
			} else {
				log.info("Element " + id + " is not enabled/not displayed");
			}
		} catch (TimeoutException e) {
			log.info("Element " + id + " is not found");
		}
	}

	protected void clickButtonLike(String id) {
		try {
			WebElement we = findElementByXpath("//button[contains(@id,'" + id + "')]",INPUT_TIMEOUT);
			if (we.isEnabled() && we.isDisplayed()) {
				we.click();
				Sleep.wait(1000);
			} else {
				log.info("Element " + id + " is not enabled/not displayed");
			}
		} catch (TimeoutException e) {
			log.info("Element " + id + " is not found");
		}
	}
	
	protected void clickButton(String id) {
		try {
			WebElement we = findElementById(id,INPUT_TIMEOUT);
			if (we.isEnabled() && we.isDisplayed()) {
				findElementById(id).click();
				Sleep.wait(1000);
			} else {
				log.info("Element " + id + " is not enabled/not displayed");
			}
		} catch (TimeoutException e) {
			log.info("Element " + id + " is not found");
		}
	}
	
	/**
	 * Input text pada field lookup tanpa membuka dialog pencariannya
	 * @param id
	 * @param value
	 */
	protected void selectSimpleLookupSearch(String id, String value) {
		setInputField("textInput_" + id, value);
	}
	
	/**
	 * Input text pada field lookup dengan membuka dialog pencariannya
	 * @param id
	 * @param value
	 */
	protected void selectLookupSearch(String id, String value) {
		try {
			WebElement we = findElementById("buttonTo_" + id,INPUT_TIMEOUT);
			if (we.isEnabled() && we.isDisplayed()) {
				we.click();
				Sleep.wait(2000);
				
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
			} else {
				log.info("Element buttonTo_" + id + " is not enabled/not displayed");
			}
		} catch (TimeoutException e) {
			log.info("Element buttonTo_" + id + " is not found");
		}
		
	}
	
	/**
	 * Digunakan untuk mencari data pada tabel pencarian yang terdapat pada modal terpisah
	 * @param modalId
	 * @param tableId
	 * @param value
	 */
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
	
	/**
	 * Klik checkbox row pertama pada tabel pencarian
	 * @param id
	 */
	protected void clickCheckBoxTableSearch(String id) {
		clickCheckBoxTableSearch(id, 0);
	}
	
	/**
	 * Klik checkbox pada tabel pencarian sesuai dengan index yg dipassing
	 * @param id
	 * @param index
	 */
	protected void clickCheckBoxTableSearch(String id, int index) {
		findElementByXpath("//table[@id='" + id + "']/tbody/tr[@data-index='" + index +"']/td/input[@type='checkbox']").click();
		Sleep.wait(2000);
	}
	
	/**
	 * Klik checkbox pada tabel pencarian sesuain dengan query yang match
	 * @param id
	 * @param query
	 */
	protected void clickCheckBoxTableSearch(String id, String query) {
		WebElement webElement = findElementByXpath("//table[@id='" + id + "']/tbody/tr[./td/text()='" + query+ "']");
		String index = webElement.getAttribute("data-index");
		clickCheckBoxTableSearch(id, Integer.valueOf(index));
	}
	
	/**
	 * Digunakan untuk klik data row pertama pada tabel pencarian 
	 * @param id
	 */
	protected void clickTableSearch(String id) {
		clickTableSearch(id, 0);
	}
	
	/**
	 * Digunakan untuk klik data row pada tabel pencarian sesuai dengan index yg dipassing
	 * @param id
	 * @param index
	 */
	protected void clickTableSearch(String id, int index) {
		findElementByXpath("//table[contains(@id,'" + id + "')]//tbody//tr[@data-index='" + index + "']//td//a").click();
		Sleep.wait(2000);
	}
	
	
	/**
	 * Digunakan untuk klik data row pada tabel pencarian sesuai dengan query yang match
	 * @param id
	 * @param index
	 */
	protected void clickTableSearch(String id, String query) {
		WebElement webElement = findElementByXpath("//table[@id='" + id + "']/tbody/tr[./td/text()='" + query+ "']");
		String index = webElement.getAttribute("data-index");
		clickTableSearch(id, Integer.valueOf(index));
	}
	
	protected boolean searchOnTable(String id, String query) {
		try {
			findElementByXpath("//table[@id='" + id + "']/tbody/tr[./td/text()='" + query+ "']", 1);
			return true;
		} catch (Exception e) {
			// do nothing
		}
		return false;
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
