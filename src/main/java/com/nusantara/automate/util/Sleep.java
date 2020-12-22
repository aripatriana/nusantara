package com.nusantara.automate.util;

import java.util.concurrent.TimeoutException;

public class Sleep {

	public static void wait(int milis) {
		try {
			Thread.sleep(milis);
			
			if (timeout != null) {
				counter += milis;
			}
		} catch (InterruptedException e) {
		}
	}
	
	private static Integer timeout = null;
	private static Integer counter = null;
	public static void setTimeout(int milis) {
		Sleep.timeout = milis;
		Sleep.counter = 0;
	}
	
	public static void throwIfTimeout() throws TimeoutException {
		if (counter>=timeout) {
			int c = Sleep.counter;
			Sleep.timeout = null;
			Sleep.counter = null;
			throw new TimeoutException("Timeout reached for " + (c/1000) + " seconds");
		}
	}
}
