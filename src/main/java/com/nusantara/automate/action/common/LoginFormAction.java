package com.nusantara.automate.action.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nusantara.automate.Actionable;
import com.nusantara.automate.WebElementWrapper;
import com.nusantara.automate.WebExchange;
import com.nusantara.automate.util.LoginInfo;
import com.nusantara.automate.util.Sleep;


/**
 * The action for access login
 * 
 * @author ari.patriana
 *
 */
public class LoginFormAction extends WebElementWrapper implements Actionable {

	Logger log = LoggerFactory.getLogger(LoginFormAction.class);
	private String memberCode;
	private String username;
	private String password;
	private String keyFile;
	private String token;
	
	public LoginFormAction(LoginInfo loginInfo) {
		this(loginInfo.getMemberCode(), loginInfo.getUsername(), loginInfo.getPassword(), loginInfo.getKeyFile());
	}
	
	public LoginFormAction(String memberCode, String username, String password, String keyFile) {
		this.memberCode = memberCode;
		this.username = username;
		this.password = password;
		this.keyFile = keyFile;
		try {
			this.token = new String(Files.readAllBytes(Paths.get(keyFile)));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setKeyFile(String keyFile) {
		this.keyFile = keyFile;
	}
	
	public void setMemberCode(String memberCode) {
		this.memberCode = memberCode;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getKeyFile() {
		return keyFile;
	}
	
	public String getMemberCode() {
		return memberCode;
	}
	
	public String getPassword() {
		return password;
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getToken() {
		return token;
	}
	
	public void setToken(String token) {
		this.token = token;
	}
	
	@Override
	public void submit(WebExchange webExchange) {
		log.info("Open Login Page");
		
		Sleep.wait(1000);
		
		findElementById("memberCode").sendKeys(getMemberCode());
		findElementByName("username").sendKeys(getUsername());
		findElementByName("password").sendKeys(getPassword());
		findElementById("keyFile").sendKeys(getKeyFile());
		
		captureFullWindow();
		
		findElementByXpath("//button[text()='Sign In']").click();		
		
		webExchange.put("username", getUsername());
		webExchange.put("memberCode", getMemberCode());
		webExchange.put("password", getPassword());
		webExchange.put("token", getToken());	
	}

	
}
