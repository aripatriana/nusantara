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
import com.nusantara.automate.exception.FailedTransactionException;
import com.nusantara.automate.exception.ModalFailedException;
import com.nusantara.automate.util.Sleep;

/**
 * The action for open the form page
 * 
 * @author ari.patriana
 *
 */
public class OpenFormAction extends WebElementWrapper implements Actionable, MenuAwareness {

	Logger log = LoggerFactory.getLogger(OpenFormAction.class);
	private Actionable prevMenu;
	private String menuId;
	private String form;
	private Menu menu;
	int timeout = 1;
	
	public OpenFormAction(Actionable prevMenu, String menuId, String form) {
		this.prevMenu = prevMenu;
		this.menuId = menuId;
		this.form = form;
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
	public void setMenu(Menu menu) {
		this.menu = menu;
	}
	
	@Override
	public void submit(WebExchange webExchange) {
		log.info("Open Form " + form);
		Sleep.wait(500);
		try {
			WebDriverWait wait = new WebDriverWait(getDriver(),timeout);
			WebElement webElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ul[@id='" + getMenuId() + "']//li//a[./span[text()='" + getForm() + "']]")));
			webElement.click();
			
			Sleep.wait(3000);
		} catch (TimeoutException e) {
			if (prevMenu != null) {
				if (prevMenu instanceof OpenMenuAction 
						|| prevMenu instanceof OpenSubMenuAction) {
					try {
						prevMenu.submit(webExchange);
					} catch (FailedTransactionException | ModalFailedException e1) {
						// do nothing
					}
				}
				this.submit(webExchange);
			} else {
				getDriver().navigate().refresh();
				submit(webExchange);				
			}
		}			
	}

}
