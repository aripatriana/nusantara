package com.nusantara.automate.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
	
	public final static String FORMAT_DATE_TIME = "yyyyMMdd_hhmmss";
	
	public static String format(long milis) {
		return format(new Date(Long.valueOf(milis)), DateUtils.FORMAT_DATE_TIME);
	}
	
	public static String format(Date date, String format) {
		DateFormat df = new SimpleDateFormat(format);
		return df.format(date);
	}
	
	public static Date parse(String date, String format) {
		DateFormat df = new SimpleDateFormat(format);
		try {
			return df.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
}
