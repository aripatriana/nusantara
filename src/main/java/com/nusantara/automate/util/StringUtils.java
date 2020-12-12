package com.nusantara.automate.util;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.SystemUtils;

import com.nusantara.automate.exception.ScriptInvalidException;

public class StringUtils {

	public static final String SLASH_ON_LINUX = "/";
	public static final String SLASH_ON_WINDOW = "\\";
	public static final String SLASH_ON_UNKNOWN_OS = "/";
	public static final String SLASH_ON_MACOS = "/";
	
	public static String getOsSlash() {
		if (SystemUtils.IS_OS_LINUX) {
			return SLASH_ON_LINUX;
		} else if (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_MAC_OSX) {
			return SLASH_ON_MACOS;
		} else {
			return SLASH_ON_WINDOW;
		}
	}
	
	public static String path(String...args){
		return Paths.get(path(args, getOsSlash())).toString();
	}
	
	public static String path(String[] args, String slash) {
		String path = "";
		for (String arg: args) {
			if (!path.isEmpty())
				path += slash;
			if (arg.isEmpty())
				path +=slash;
			else
				path += repath(arg);;
		}
		return path;
	}
	
	public static String repath(String path) {
		String slash = getSlash(path, new String[] {SLASH_ON_LINUX, SLASH_ON_WINDOW, SLASH_ON_UNKNOWN_OS});
		if (slash != null) {
			String[] arr = path.split("\\"+slash);
			path(arr, getOsSlash());
		}
		return path;
	}
	
	public static String getSlash(String path, String...slash) {
		for (String s : slash) {
			if (path.contains(s))
				return s;
		}
		return null;
	}
	
	public static String quote(String value) {
		return "'" + value + "'";
	}
	
	public static int containsCharBackwardFollowingBy(String checked, Character findWith, Character following) {
		int i = containsCharBackward(checked, findWith);
		if (i<0)
			return checked.length();
		if (i-1<0)
			return -1;
		if (checked.charAt(i-1) != following)
			return -1;
		if (i-2<0)
			return i;
		return containsCharFollowingBy(checked.substring(0, i-2), findWith, following);
	}
	
	public static int containsCharFollowingBy(String checked, Character findWith, Character following) {
		int i = containsCharForward(checked, findWith);
		if (i<0)
			return checked.length();
		if (i+1>checked.length())
			return -1;
		if (checked.charAt(i+1) != following)
			return -1;
		if (i+2>checked.length())
			return i;
		return containsCharFollowingBy(checked.substring(i+2, checked.length()), findWith, following);
	}
	
	public static int containsCharForward(String checked, Character findWith, int index) {
		for (int i=0; i<checked.length(); i++) {
			if (i == index && checked.charAt(i) == findWith) {
				return i;
			}
		}
		return -1;
	}
	
	public static int containsCharBackward(String checked, Character findWith, int index) { 
		for (int i=checked.length()-1; i>0; i--) {
			if ((checked.length()-1)-index == i && checked.charAt(i) == findWith) {
				return i;
			}
		}
		return -1;
	}
	
	public static int containsCharForward(String checked, Character findWith) {
		for (int i=0; i<checked.length(); i++) {
			if (checked.charAt(i) == findWith) {
				return i;
			}
		}
		return -1;
	}
	
	public static int containsCharBackward(String checked, Character findWith) { 
		for (int i=checked.length()-1; i>0; i--) {
			if (checked.charAt(i) == findWith) {
				return i;
			}
		}
		return -1;
	}
	
	public static String replaceCharForward(String checked, Character findWith, String replaceWith, int index) {
		int i = containsCharForward(checked, findWith, index);
		if (i < 0) return checked;
		return checked.substring(0, i) + replaceWith + checked.substring(i+1, checked.length());
	}
	
	public static String replaceCharBackward(String checked, Character findWith, String replaceWith, int index) {
		int i = containsCharBackward(checked, findWith, index);
		if (i < 0) return checked;
		return checked.substring(0, i) + replaceWith + checked.substring(i+1, checked.length());
	}
	
	public static String replaceCharForward(String checked, Character findWith, String replaceWith) {
		int i = containsCharForward(checked, findWith);
		if (i < 0) return checked;
		return checked.substring(0, i) + replaceWith + checked.substring(i+1, checked.length());
	}
	
	public static String replaceCharBackward(String checked, Character findWith, String replaceWith) {
		int i = containsCharBackward(checked, findWith);
		if (i < 0) return checked;
		return checked.substring(0, i) + replaceWith + checked.substring(i+1, checked.length());
	}
	
	
	public static String removeLastChar(String value, String separator) {
		if (!value.contains(separator)) return value;
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
	
	public static String[] parseStatement(String statement, String[] separator) throws ScriptInvalidException {
		for (String s : separator) {
			if (statement.contains(s)) {
				String[] sh = statement.split(s);
				if (sh.length >  2)
					throw new ScriptInvalidException("Script not valid for " + statement);
				return new String[] {sh[0], sh[1], s};				
			}
		}
		if (StringUtils.containsCharFollowingBy(statement, '=', '=') == -1)
			throw new ScriptInvalidException("Missing equation of equality == for " + statement);
		if (StringUtils.containsCharFollowingBy(statement, '<', '>') == -1
			|| statement.contains(">"))
			throw new ScriptInvalidException("Missing equation of inequality <> for " + statement);
		throw new ScriptInvalidException("Script not valid for " + statement);
	}
	
	public static String[] parseStatement(String statement, String separator) throws ScriptInvalidException {
		String[] sh = statement.split(separator);
		if (sh.length > 2)
			throw new ScriptInvalidException("Script not valid for " + statement);
		return trimArray(sh);
	}
	
	public static String concatIfNotEmpty(String text, String concat) {
		if (!text.isEmpty()) 
			text += ",";
		return text;
	}
	
	public static String[] trimArray(String[] arg) {
		String[] temp = new String[arg.length];
		for (int i=0; i<arg.length; i++) {
			temp[i] = arg[i].trim();
		}
		return temp;
	}
	public static String findContains(String data, String[] key) {
		for (int i=0; i<key.length; i++) {
			if (data.contains(key[i]))
				return key[i];
		}
		return null;
	}
	
	public static String findContains(String[] data, String key) {
		for (int i=0; i<data.length; i++) {
			if (data[i].contains(key))
				return data[i];
		}
		return null;
	}
	
	public static boolean endsWith(String val, String[] endsWith) {
		for (String ew : endsWith) {
			if (val.endsWith(ew))
				return true;
		}
		return false;
	}


	public static String replaceVar(String text, String var, List<?> value) {
		if (value == null) return text;
		String val = "";
		for (Object o : value) {
			if (!val.isEmpty()) val = val + ",";
			val = val + "[" + removeCurlBracket(String.valueOf(o)) + "]";

		}
		return text.replace(var, val);
	}

	public static String asStringTableHtml(String[] columns, List<String[]> list) {
		StringBuffer sb = new StringBuffer();
		sb.append("<table>").append("<tr>");
		for (String column : columns) {
			sb.append("<th>").append(column).append("</th>");
		}
		sb.append("</tr>");
		
		for (String[] values : list) {
			sb.append("<tr>");
			for (String value : values) {
				sb.append("<td>");
				sb.append(value);
				sb.append("</td>");
			}
			sb.append("</tr>");			
		}
		sb.append("</table>");
		return sb.toString();
	}
	
	public static <T> String asString(List<T> list, String spices) {
		StringBuffer sb = new StringBuffer();
		for (T t : list) {
			if (!sb.toString().isEmpty())
				sb.append(spices);
			sb.append(t);
		}
		return sb.toString();
	}
	
	public static String replaceVar(String text, String var, Object value) {
		if (value == null) return text;
		return text.replace(var, removeCurlBracket(value.toString()));
	}
	
	public static String removeCurlBracket(String val) {
		return val.replace("{", "").replace("}", "");
	}
	
	public static String replaceById(String text, String[] var, Map<String, String> ids) {
		for (String bracket : var) {
			String id = IDUtils.getRandomId();
			text = text.replace(bracket, id);
			ids.put(id, bracket);
		}
		return text;
	}
	
	public static Object nvl(Object obj, String val) {
		if (obj == null) return val;
		return obj;
	}
	
	public static Object nvl(Object obj) {
		return nvl(obj, "");
	}
	
	public static String substringUntil(String text, char[] vars) {
		for (int i=0; i<text.length(); i++) {
			for (char var : vars) {
				if (text.charAt(i) == var)
					return text.substring(0, i);
			}
		}
		return text;
	}
	
	public static String substringUntil(String text, String[] vars) {
		for (int i=0; i<text.length(); i++) {
			for (String var : vars) {
				String[] s = text.split(var);
				if (s.length > 1) {
					return s[0];
				}
			}
		}
		return null;
	}
	
	public static boolean match(String text, String[] criteria) {
		if (text == null) return false;
		if (criteria == null || criteria.length == 0) return false;
		
		for (String c : criteria) {
			if (text.equals(c)) {
				return true;
			}
		}
		return false;
	}
}
