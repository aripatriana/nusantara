package com.nusantara.automate.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class MapUtils {

	public static void clearMapKey(String removed, Map<String, Object> data) {
		Map<String, Object> temp = new LinkedHashMap<String, Object>(data);
		data.clear();
		for (Entry<String, Object> entry : temp.entrySet()) {
			data.put(entry.getKey().replace(removed, ""), entry.getValue());
		}
	}
	
	public static void concatMapKey(String concat, Map<String, Object> data) {
		Map<String, Object> temp = new LinkedHashMap<String, Object>(data);
		data.clear();
		for (Entry<String, Object> entry : temp.entrySet()) {
			data.put(concat.concat(entry.getKey()), entry.getValue());
		}
	}
	
	public static void replaceMapBracketValue(Map<String, Object> map, Map<String, Object> value) {
		for (Entry<String, Object> entry : map.entrySet()) {
			String keyVal = entry.getValue().toString();
			if (keyVal.startsWith("{")
					&& keyVal.startsWith("}")) {
				keyVal = keyVal.replace("{", "").replace("}", "");
				map.replace(entry.getKey(), value.get(keyVal));				
			}
		}
	}

}
