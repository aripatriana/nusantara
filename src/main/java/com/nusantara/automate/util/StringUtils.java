package com.nusantara.automate.util;

public class StringUtils {

	public static String removeLastChar(String value, String separator) {
		String[] temp = value.split(separator);
		return removeCharIndex(value, separator, temp.length-1);
		
	}
	
	public static String removeCharIndex(String value, String separator, int index) {
		StringBuffer sb = new StringBuffer();
		String[] temp = value.split(separator);
		for (int i=0; i<temp.length; i++) {
			if (i != index) {
				if (sb.length() != 0)
					sb.append(separator);
				sb.append(temp[i]);
			}
		}
		return sb.toString();
	}

}
