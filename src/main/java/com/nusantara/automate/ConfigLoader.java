package com.nusantara.automate;

import java.util.HashMap;
import java.util.Map;

/**
 * Hold the configuration properties
 * 
 * @author ari.patriana
 *
 */
public class ConfigLoader {

	private static Map<String, Object> configMap = new HashMap<String, Object>();
	private static Map<String, Map<String, Object>> loginMap = new HashMap<String, Map<String, Object>>();
	private static Map<String, Map<String, Object>> elementMap = new HashMap<String, Map<String,Object>>();
	
	public static Map<String, Object> getConfigMap() {
		return configMap;
	}
	
	public static void setConfigMap(Map<String, Object> configMap) {
		ConfigLoader.configMap = configMap;
	}
	
	public static void addConfig(String key, Object value) {
		ConfigLoader.configMap.put(key, value);
	}
	
	public static void setLoginInfo(Map<String, Map<String, Object>> loginMap) {
		ConfigLoader.loginMap = loginMap;
	}
	
	public static void addLoginInfo(String key, Map<String, Object> value) {
		ConfigLoader.configMap.put(key, value);
	}

	public static Map<String, Object> getLoginInfo(String key) {
		return ConfigLoader.loginMap.get(key);
	}
	
	public static Map<String, Map<String, Object>> getLoginInfo() {
		return ConfigLoader.loginMap;
	}
	
	public static Object getConfig(String key) {
		return ConfigLoader.configMap.get(key);
	}
	
	public static void clear() {
		ConfigLoader.configMap.clear();
	}
	
	public static Map<String, Map<String, Object>> getElementMap() {
		return elementMap;
	}
	
	public static void setElementMap(String key, Map<String, Object> elements) {
		elementMap.put(key, elements);
	}
	
	
}
