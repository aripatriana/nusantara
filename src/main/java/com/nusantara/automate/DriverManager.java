package com.nusantara.automate;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

/**
 * Manage the browser driver
 * 
 * @author ari.patriana
 *
 */
public class DriverManager {

	private static WebDriver wd;
	
	private static String driverPath = "D:/System/WebDriver/bin/chromedriver.exe";

	public static void setDriverPath(String driverPath) {
		DriverManager.driverPath = driverPath;
	}
	
	public static WebDriver getChromeDriver() {
		if (wd == null) {
			System.setProperty("webdriver.chrome.driver", driverPath);
			wd =  new ChromeDriver();
			wd.manage().window().maximize();
		}
			
		return wd;
	}
	
	public static WebDriver getDefaultDriver() {
		return getChromeDriver();
	}
}
