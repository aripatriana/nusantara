package com.nusantara.automate.action.common;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nusantara.automate.Actionable;
import com.nusantara.automate.Menu;
import com.nusantara.automate.MenuAwareness;
import com.nusantara.automate.WebElementWrapper;
import com.nusantara.automate.WebExchange;
import com.nusantara.automate.util.Sleep;

/**
 * The action for open the form page
 * 
 * @author ari.patriana
 *
 */
public class OpenFormAction extends WebElementWrapper implements Actionable, MenuAwareness {

	Logger log = LoggerFactory.getLogger(OpenFormAction.class);
	private OpenMenuAction prevMenu;
	private String menuId;
	private String form;
	private Menu menu;
	
	public OpenFormAction(OpenMenuAction prevMenu, String menuId, String form) {
		this.prevMenu = prevMenu;
		this.menuId = menuId;
		this.form = form;
	}
	
	@Override
	public void setMenu(Menu menu) {
		this.menu = menu;
	}
	
	public String getMenuId() {
		return menuId;
	}
	
	public String getForm() {
		return form;
	}
	
	public Menu getMenu() {
		return menu;
	}
	
	@Override
	public void submit(WebExchange webExchange) {
		log.info("Open Form " + form);
		Sleep.wait(500);
		try {
			WebDriverWait wait = new WebDriverWait(getDriver(),3);
			WebElement webElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ul[@id='" + getMenuId() + "']//li//a[./span[text()='" + getForm() + "']]")));
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
