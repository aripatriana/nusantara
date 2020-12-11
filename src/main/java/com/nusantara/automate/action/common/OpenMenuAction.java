package com.nusantara.automate.action.common;

import org.openqa.selenium.TimeoutException;
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
	int timeout = 10;
	
	public OpenMenuAction(OpenMenuAction prevMenu, String menuName) {
		this.prevMenu = prevMenu;
		this.menuName = menuName;
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
		log.info("Open Menu " + menuName);
		try {
			findElementByXpath("//ul//li//a//span[text()='" + getMenuName() + "']", timeout).click();
		} catch (TimeoutException e) {
			if (prevMenu != null) {
				prevMenu.setTimeout(1);
				prevMenu.submit(webExchange);
				this.submit(webExchange);
			} else {
				findElementByXpath("//a[@title='Collapse Menu']").click();
				try {
					findElementByXpath("//ul//li//a//span[text()='" + getMenuName() + "']", 1);
				} catch (TimeoutException e1) {
					getDriver().navigate().refresh();
					submit(webExchange);	
				}			
			}
		}
		
	}

}
