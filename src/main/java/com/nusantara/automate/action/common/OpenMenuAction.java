package com.nusantara.automate.action.common;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nusantara.automate.Actionable;
import com.nusantara.automate.WebElementWrapper;
import com.nusantara.automate.WebExchange;
import com.nusantara.automate.util.Sleep;

/**
 * The action for open the menu trees
 * 
 * @author ari.patriana
 *
 */
public class OpenMenuAction extends WebElementWrapper implements Actionable {

	Logger log = LoggerFactory.getLogger(OpenMenuAction.class);
	OpenMenuAction prevMenu;
	String menuName;
	
	public OpenMenuAction(OpenMenuAction prevMenu, String menuName) {
		this.prevMenu = prevMenu;
		this.menuName = menuName;
	}
	
	public String getMenuName() {
		return menuName;
	}
	
	@Override
	public void submit(WebExchange webExchange) {
		log.info("Open Menu " + menuName);
		
		Sleep.wait(500);
		try {
			WebDriverWait wait = new WebDriverWait(getDriver(),5);
			WebElement webElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ul//li//a//span[text()='" + getMenuName() + "']")));
			webElement.click();
		} catch (TimeoutException e) {
			if (prevMenu != null) {
				prevMenu.submit(webExchange);
				this.submit(webExchange);
			} else {
				getDriver().navigate().refresh();
				submit(webExchange);				
			}
		}
		
	}

}
