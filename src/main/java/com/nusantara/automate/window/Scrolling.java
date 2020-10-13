package com.nusantara.automate.window;

public interface Scrolling {

	public final int MAX_PIXEL_HEIGHT = 5000;
	
	public PositionPixel getPosition();
	
	public Integer getScrollHeight();
	
	public PositionPixel scrollToDown();
	
	public PositionPixel moveUp(int pixel);
	
	public PositionPixel moveDown(int pixel);
	
	public Integer getClientHeight();
	
	public Integer getClientWidth();

	public Boolean isPixelOrigin();
	
}
