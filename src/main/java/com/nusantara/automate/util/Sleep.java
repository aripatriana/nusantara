package com.nusantara.automate.util;

public class Sleep {

	public static void wait(int milis) {
		try {
			Thread.sleep(milis);
		} catch (InterruptedException e) {
		}
	}
}
