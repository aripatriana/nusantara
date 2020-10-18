package com.nusantara.automate;

import java.io.IOException;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;

import com.google.common.base.Function;
import com.nusantara.automate.window.WindowScreen;

/**
 * Base class for control and access to element of browser page
 * 
 * @author ari.patriana
 *
 */
public abstract class AbstractBaseDriver {

	private final int TIMEOUT_IN_SECOND = 15;
	
	@Value("active_scen")
	private String scen;
	
	@Value("active_workflow")
	private String workflow;
	
	WebDriver wd;
	WindowScreen ws;
	
	public AbstractBaseDriver() {
		this(DriverManager.getDefaultDriver());
	}
	
	public AbstractBaseDriver(WebDriver wd) {
		this.wd = wd;
		this.ws = new WindowScreen(wd);
	}
	
	private void setPreCapture() {
		ws.setTargetFolder(scen);
		ws.setPrefixFileName(workflow);
	}
	
	public void captureFullWindow() {
		try {
			setPreCapture();
			ws.capture(WindowScreen.CAPTURE_FULL_WINDOW);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void captureWindow() {
		try {
			setPreCapture();
			ws.capture(WindowScreen.CAPTURE_CURRENT_WINDOW);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void captureFullModal(String elementId) {
		try {
			setPreCapture();
			ws.capture(WindowScreen.CAPTURE_FULL_WINDOW, elementId);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public WebDriver getDriver() {
		return wd;
	}
	
	protected WebElement findElementById(String id) {
		return findElementById(id, TIMEOUT_IN_SECOND);
	}
	
	protected WebElement findElementById(String id, int timeout) {
		WebDriverWait wait = new WebDriverWait(getDriver(),timeout);
		return wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(id)));
	}
	
	protected WebElement findElementByXpath(String xpath) {
		return findElementByXpath(xpath, TIMEOUT_IN_SECOND);
	}
	
	protected WebElement findElementByXpath(String xpath, int timeout) {
		WebDriverWait wait = new WebDriverWait(getDriver(),timeout);
		return wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
	}
	
	protected WebElement findElementByName(String name) {
		return findElementByName(name, TIMEOUT_IN_SECOND);
	}
	
	protected WebElement findElementByName(String name, int timeout) {
		WebDriverWait wait = new WebDriverWait(getDriver(),timeout);
		return wait.until(ExpectedConditions.visibilityOfElementLocated(By.name(name)));
	}
	
	protected WebElement findElementByClassName(String className) {
		return findElementByClassName(className, TIMEOUT_IN_SECOND);
	}
	
	protected WebElement findElementByClassName(String className, int timeout) {
		WebDriverWait wait = new WebDriverWait(getDriver(),timeout);
		return wait.until(ExpectedConditions.visibilityOfElementLocated(By.className(className)));
	}
	
	
	protected WebElement findElementById(WebElement webElement, final String id) {
		return findElementById(webElement, id, TIMEOUT_IN_SECOND);
	}
	
	protected WebElement findElementById(WebElement webElement, final String id, int timeout) {
		WebElementWait wait = new WebElementWait(webElement, timeout);
	    return wait.until(new Function<WebElement, WebElement>() {
               public WebElement apply(WebElement d) {
                   return d.findElement(By.id(id));
               }
        });
	}
	
	protected WebElement findElementByXpath(WebElement webElement, final String xpath) {
		return findElementByXpath(webElement, xpath, TIMEOUT_IN_SECOND);
	}
	
	protected WebElement findElementByXpath(WebElement webElement, final String xpath, int timeout) {
		WebElementWait wait = new WebElementWait(webElement, timeout);
		return wait.until(new Function<WebElement, WebElement>() {
            public WebElement apply(WebElement d) {
                return d.findElement(By.xpath(xpath));
            }
		});
	}
	
	protected WebElement findElementByName(WebElement webElement, final String name) {
		return findElementByName(webElement, name, TIMEOUT_IN_SECOND);
	}
	
	protected WebElement findElementByName(WebElement webElement, final String name, int timeout) {
		WebElementWait wait = new WebElementWait(webElement, timeout);
		return wait.until(new Function<WebElement, WebElement>() {
            public WebElement apply(WebElement d) {
                return d.findElement(By.name(name));
            }
		});
	}
	
	protected WebElement findElementByClassName(WebElement webElement, final String className) {
		return findElementByClassName(webElement, className, TIMEOUT_IN_SECOND);
	}
	
	protected WebElement findElementByClassName(WebElement webElement, final String className, int timeout) {
		WebElementWait wait = new WebElementWait(webElement, timeout);
		return wait.until(new Function<WebElement, WebElement>() {
            public WebElement apply(WebElement d) {
                return d.findElement(By.className(className));
            }
		});
	}

	
}
