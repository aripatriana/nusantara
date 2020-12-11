package com.nusantara.automate;

import java.awt.Toolkit;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

/**
 * Manage the browser driver
 * 
 * @author ari.patriana
 *
 */
public class DriverManager {

	private static WebDriver wd;
	
	private static String driverPath = "D:/System/WebDriver/bin/chromedriver.exe";

	private static String headless;
	
	public static void setDriverPath(String driverPath) {
		DriverManager.driverPath = driverPath;
	}
	
	public static void setHeadlessMode(String headless) {
		DriverManager.headless = headless;
	}
	
	public static WebDriver getChromeDriver() {
		if (wd == null) {
			System.setProperty("webdriver.chrome.driver", driverPath);
			ChromeOptions option = new ChromeOptions();
			if ("true".equalsIgnoreCase(headless)) {
				option.addArguments("--headless");
				wd =  new ChromeDriver(option);
				wd.manage().window().setPosition(new Point(0,0));
				wd.manage().window().setSize(new Dimension((int)Toolkit.getDefaultToolkit().getScreenSize().getWidth(),(int)Toolkit.getDefaultToolkit().getScreenSize().getHeight()));
			} else {
				wd =  new ChromeDriver(option);
				wd.manage().window().maximize();
			}
		}
			
		return wd;
	}
	
	public static WebDriver getDefaultDriver() {
		return getChromeDriver();
	}
	
	public static void close() {
		getDefaultDriver().quit();
		wd = null;
	}
}
