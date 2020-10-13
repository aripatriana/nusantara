package com.nusantara.automate.util;

import java.util.UUID;

public class IDUtils {

	public static String getRandomId() {
		return UUID.randomUUID().toString().replace("-", "");
	}
}
