package com.nusantara.automate.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

public class MapUtils {

	public static <K, V> boolean checkAllNull(Map<K, V> map) {
		boolean nulls = true;
		for (Map.Entry<K, V> e : map.entrySet()) {
			if (e.getValue() != null)
				nulls = false;
		}
		return nulls;
	}
	
	public static <T> T findEquals(List<T> list, String key) {
		for (T t : list) {
			if (t.equals(key))
				return t;
		}
		return null;
	}
	
	public static <T> void removeEquals(List<T> list, String key) {
		while (list.contains(key)) {
			for(int i=0; i<list.size(); i++) {
				if (list.get(i).equals(key)) {
					list.remove(i);
					break;
				}
			}
		}
	}
	
	public static <T> List<T> combineValueAsList(Collection<? extends List<T>> mapValueList) {
		List<T> list = new LinkedList<T>();
		for(List<T> d : mapValueList) {
			list.addAll(d);
		}
		return list;
	}
	
	public static String listAsString(List<Object> list, String separator) {
		String result = "";
		for (Object o : list) {
			if (!result.isEmpty()) result += separator;
			result+="'" + o.toString() + "'";
		}
		return result;
	}
	
	public static <T> List<List<T>> arrayAsList(List<T[]> list) {
		List<List<T>> data = new ArrayList<List<T>>();
		for (T[] arr : list) {
			data.add(Arrays.asList(arr));
		}
		return data;
	}
	
	public static <K, V> Map<K, V> copyAsMap(K[] key, V[] value, Class<K> k, Class<V> v) {
		Map<K, V> map = new HashMap<K, V>();
		
		for (int i=0; i<key.length; i++) {
			map.put(key[i], value[i]);
		}
		return map;
	}
	
	public static List<Object> mapAsList(List<Map<String, Object>> data, String key) {
		List<Object> list = new ArrayList<Object>();
		for (Map<String, Object> d : data) {
			list.add(d.get(key));
		}
		return list;
	}
	
	public static int findMaxList(Map<String, List<String>> map) {
		int i=0;
		for (Entry<String, List<String>> e : map.entrySet()) {
			if (e.getValue().size() > i)
				i=e.getValue().size();
		}
		return i;
	}
	
	/**
	 * {instrument_code=[TLKM, ASII]
	 * price=[100, 200]}
	 *
	 * [{instrument_code='TLKM', price=100}, 
	 * [{instrument_code='ASII', price=200},
	 * 
	 * @param map
	 * @return
	 */
	public static List<Map<String, String>> transpose(Map<String, List<String>> map) {
		List<Map<String, String>> transpose = new LinkedList<Map<String,String>>();
		int arrSize = findMaxList(map);
		for (int i =0; i<arrSize; i++) {
			Map<String, String> val = new LinkedHashMap<String, String>();
			for (Entry<String, List<String>> entry : map.entrySet()) {
				try {
					val.put(entry.getKey(), entry.getValue().get(i));
				} catch (IndexOutOfBoundsException e) {				
					val.put(entry.getKey(), "");
				}
			}
			transpose.add(val);
		}
		return transpose;
	}
	
	public static List<Object> matrixAsList(LinkedHashMap<Integer, LinkedHashMap<String, Object>> data, String key) {
		List<Object> list = new LinkedList<Object>();
		for (Map<String, Object> d : data.values()) {
			list.add(d.get(key));
		}
		return list;
	}
	
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

	public static void copyKeepOriginal(List<Map<String, Object>> dest, List<Map<String, Object>> src) {
		ListIterator<Map<String, Object>> di=dest.listIterator();
        ListIterator<Map<String, Object>> si=src.listIterator();
        for (int i=0; i<src.size(); i++) {
            di.add(new HashMap<String, Object>(si.next()));
        }
	}

	@SuppressWarnings("unchecked")
	public static void keyLowercase(Map<String, Object> map) {
		Map<String, Object> copied = new LinkedHashMap<String, Object>();
		for (Entry<String, Object> e : map.entrySet()) {
			if (e.getValue() instanceof List) {
				keyLowercase((List<Map<String, Object>>) e.getValue());
				copied.put(e.getKey().toLowerCase(), e.getValue());
			} else {
				copied.put(e.getKey().toLowerCase(), e.getValue());				
			}
		}
		map.clear();
		map.putAll(copied);
	}

	@SuppressWarnings("unchecked")
	public static void keyLowercase(List<?> list) {
		for (Object o : list) {
			if (o instanceof Map) {
				Map<String, Object> map = (Map<String, Object>) o;
				Map<String, Object> copied = new LinkedHashMap<String, Object>();
				for (Entry<String, Object> e : map.entrySet()) {
					copied.put(e.getKey().toLowerCase(), e.getValue());
				}
				map.clear();
				map.putAll(copied);
			}
		}
	}
	
	public static <K, V> void copyValueNotNull(Map<K, V> src, Map<K, V> dest) {
		for (Entry<K, V> e : src.entrySet()) {
			if (e.getValue() != null) {
				dest.put(e.getKey(), e.getValue());
			}
		}
	}
	
	public static <K, V> void copyStartWith(Map<K, V> src, Map<K, V> dest, String key) {
		for (Entry<K, V> e : src.entrySet()) {
			if (e.getKey().toString().startsWith(key)) {
				dest.put(e.getKey(), e.getValue());
			}
		}
	}
	
	public static int sumValue(Map<?, Integer> map) {
		int result = 0;
		for (Integer i : map.values()) {
			result += i;
		}
		return result;
	}
	
	public static <K, V> void removeMapIfOnlyContains(Map<K, V> data, String regex) {
		Map<K, V> newData  = new HashMap<K, V>(data);
		for (Entry<K, V> e : data.entrySet()) {
			if (e.getValue().toString().length()==1
					&& regex.contains(e.getValue().toString())) {
				newData.remove(e.getKey());
			}	
		}
		data.clear();
		data.putAll(newData);
	}
}
