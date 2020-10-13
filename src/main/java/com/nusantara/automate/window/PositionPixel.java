package com.nusantara.automate.window;

public class PositionPixel {

	private int x;
	private int y;
	
	public PositionPixel(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	@Override
	public boolean equals(Object obj) {
		 if (this == obj) {
			 return true;
         }
         if (obj == null || getClass() != obj.getClass()) {
        	 return false;
         }
         PositionPixel pixel = (PositionPixel) obj;
         return x == pixel.x &&  y == pixel.y;
	}

	@Override
	public String toString() {
		return "PositionPixel [x=" + x + ", y=" + y + "]";
	}
}
