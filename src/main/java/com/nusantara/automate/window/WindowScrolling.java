package com.nusantara.automate.window;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.openqa.selenium.JavascriptExecutor;

/**
 * This represent of window scrolling in browser page
 * 
 * @author ari.patriana
 *
 */
public class WindowScrolling implements Scrolling {
	
	private JavascriptExecutor js;
	
	public WindowScrolling(JavascriptExecutor js) {
		this.js = js;
	}
	
	public PositionPixel getPosition() {
		return new PositionPixel (toInt(js.executeScript("return window.pageXOffset").toString()), 
				toInt(js.executeScript("return window.pageYOffset").toString()));
	}
	public Integer toInt(String s) {
		return new BigDecimal(s).setScale(BigDecimal.ROUND_DOWN, RoundingMode.DOWN).intValue();
	}
	
	public Integer getScrollHeight() {
		return toInt(js.executeScript("return document.body.scrollHeight").toString());
	}
	public PositionPixel scrollToDown() {
		js.executeScript("window.scrollBy(0," + MAX_PIXEL_HEIGHT + ")");
		return new PositionPixel (0, toInt(js.executeScript("return window.pageYOffset").toString()));
	}
	
	public PositionPixel moveUp(int pixel) {
		js.executeScript("window.scrollBy(0," + (-pixel) + ")");
		return new PositionPixel (0, toInt(js.executeScript("return window.pageYOffset").toString()));
	}
	
	public PositionPixel moveDown(int pixel) {
		js.executeScript("window.scrollBy(0," + pixel + ")");
		return new PositionPixel (0, toInt(js.executeScript("return window.pageYOffset").toString()));
	}
	
	public Integer getClientHeight() {
		return toInt(js.executeScript("return document.documentElement.clientHeight").toString());
	}
	
	public Integer getClientWidth() {
		return toInt(js.executeScript("return document.documentElement.clientWidth").toString());
	}

	
	public Boolean isPixelOrigin() {
		PositionPixel origin = new PositionPixel(0, 0);
		PositionPixel current = new PositionPixel(0, toInt(js.executeScript("return window.pageYOffset").toString()));
		return origin.equals(current);
	}
}
