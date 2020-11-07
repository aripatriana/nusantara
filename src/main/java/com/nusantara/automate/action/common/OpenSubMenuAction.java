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
import com.nusantara.automate.util.StringUtils;

/**
 * The action for open the menu trees
 * 
 * @author ari.patriana
 *
 */
public class OpenSubMenuAction extends WebElementWrapper implements Actionable {

	Logger log = LoggerFactory.getLogger(OpenSubMenuAction.class);
	OpenMenuAction prevMenu;
	String menuName;
	String menuId;
	int timeout = 3;
	
	public OpenSubMenuAction(OpenMenuAction prevMenu, String menuName, String menuId) {
		this.prevMenu = prevMenu;
		this.menuName = menuName;
		this.menuId = menuId;
	}
	
	public String getMenuId() {
		return menuId;
	}
	
	public String getMenuName() {
		return menuName;
	}
	
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	
	public int getTimeout() {
		return timeout;
	}
	
	@Override
	public void submit(WebExchange webExchange) {
		if (menuName == null) return;
		Sleep.wait(500);
		log.info("Open Sub Menu " + menuName);
		try {
			WebDriverWait wait = new WebDriverWait(getDriver(),timeout);
			WebElement webElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ul[@id='" + StringUtils.removeLastChar(menuId, "::") + "']//li//a//span[text()='" + getMenuName() + "']")));
			webElement.click();
		} catch (TimeoutException e) {
			if (prevMenu != null) {
				prevMenu.setTimeout(3);
				prevMenu.submit(webExchange);
				this.submit(webExchange);
			} else {
				getDriver().navigate().refresh();
				submit(webExchange);				
			}
		}
		
	}

}
