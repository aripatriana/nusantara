package com.nusantara.automate;

import java.util.HashMap;
import java.util.Map;

public class ConfigLoader {

	private static Map<String, Object> configMap = new HashMap<String, Object>();
	
	public static Map<String, Object> getConfigMap() {
		return configMap;
	}
	
	
	public static void setConfigMap(Map<String, Object> configMap) {
		ConfigLoader.configMap = configMap;
	}
	
	public static void addConfig(String key, Object value) {
		ConfigLoader.configMap.put(key, value);
	}
	
	public static Object getConfig(String key) {
		return ConfigLoader.configMap.get(key);
	}
	
	public static void clear() {
		ConfigLoader.configMap.clear();
	}
	
	
}
