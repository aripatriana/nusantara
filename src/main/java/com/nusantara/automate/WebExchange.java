package com.nusantara.automate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.nusantara.automate.util.MapUtils;

import java.util.UUID;

/**
 * This object like the memory that hold the object which can be accessed for all implementation of actionable class
 * and this support for the operation of session
 * 
 * @author ari.patriana
 *
 */
public class WebExchange {

	Map<String, Map<String, Object>> holder = new HashMap<String, Map<String,Object>>();
	LinkedList<Map<String, Object>> listMetaData = new LinkedList<Map<String,Object>>();
	Map<String, LinkedList<Map<String, Object>>> cachedMetaData = new HashMap<String, LinkedList<Map<String,Object>>>();
	Map<String, LinkedList<String>> cachedMetaDataKey = new HashMap<String, LinkedList<String>>();
	List<String> cachedSession = new ArrayList<String>();
	public static final String LOCAL_VARIABLE = "local_variable";
	public static final String ALL_LOCAL_VARIABLE = "all_local_variable";
	LinkedList<String> sessionList = new LinkedList<String>();
	LinkedList<String> failedSessionList = new LinkedList<String>();
	Map<String, Map<String, Object>> sessionHolder = new HashMap<String, Map<String,Object>>();
	String transactionId = UUID.randomUUID().toString();
	String sessionId = null;
	boolean retention = false;
	
	public void addMetadata(Map<String, Object> metadata) {
		listMetaData.add(metadata);
	}

	public LinkedList<Map<String, Object>> getListMetaData(String menuId) {
		String mainMenu = menuId.split("\\.")[0].toUpperCase();
		String cahcedMenuId = mainMenu;
		boolean emptyCached = false;
		int indexMenuId = 0;
		if (cachedSession.contains(getCurrentSession())) {
			if (cachedMetaDataKey.containsKey(cahcedMenuId)) {
				return cachedMetaData.get(cachedMetaDataKey.get(cahcedMenuId).getLast());	
			} else {
				emptyCached = true;
			}
		} else {
			if (cachedMetaData.containsKey(cahcedMenuId)) {
				indexMenuId++;
				cahcedMenuId = menuId.toUpperCase() + "" + indexMenuId;
				while(true) {
					if (cachedMetaData.containsKey(cahcedMenuId)) {
						indexMenuId++;
						cahcedMenuId = mainMenu  + "" + indexMenuId;		
					} else {
						emptyCached = true;
						break;
					}
				}
			} else {
				emptyCached = true;
			}
		}
		
		if (emptyCached) {
			LinkedList<Map<String, Object>> tempListMetaData = new LinkedList<Map<String,Object>>();
			LinkedList<Map<String, Object>> bufferListMetaData = new LinkedList<Map<String,Object>>(listMetaData);
			for (Map<String, Object> map : bufferListMetaData) {
				if (map.keySet().toArray()[0].toString().toUpperCase().startsWith(cahcedMenuId+".")) {
					MapUtils.clearMapKey(cahcedMenuId + ".", map);;
					tempListMetaData.add(map);
				}
			}
			if (!tempListMetaData.isEmpty()) {
				cachedSession.add(getCurrentSession());
				cachedMetaData.put(cahcedMenuId, tempListMetaData);
				LinkedList<String> cachedKey = cachedMetaDataKey.get(menuId);
				if (cachedKey == null) cachedKey = new LinkedList<String>();
				cachedKey.add(cahcedMenuId);
				cachedMetaDataKey.put(mainMenu, cachedKey);
				return tempListMetaData;	
			}
		}
		
		return listMetaData;
	}
	
	public Map<String, Object> getMetaData(String menuId, int index) {
		LinkedList<Map<String, Object>> tempListMetaData = getListMetaData(menuId);
		return tempListMetaData.get(index);
	}
	
	public void clearMetaData() {
		listMetaData.clear();
	}
	
	public void clear() {
		clearMetaData();
		holder.clear();
	}
	
	public void put(String key, Object value) {
		if (key.startsWith("@")) {
			String session = getCurrentSession();
			if (getCurrentSession() == null)
				throw new RuntimeException("Session is not created");
			
			Map<String, Object> localVariable = sessionHolder.get(session);
			if (localVariable == null) {
				localVariable = new HashMap<String, Object>();
			}
			localVariable.put(key.replace("@", ""), value);
			sessionHolder.put(session, localVariable);
		} else {
			Map<String, Object> map = new HashMap<String, Object>();
			if (holder.containsKey(getTransactionId())) {
				map = holder.get(getTransactionId());
			}
			map.put(key, value);
			holder.put(getTransactionId(), map);
		}
	}
	
	public Map<String, Object> getAll() {
		return holder.get(getTransactionId());
	}
	
	public Object get(String key) {
		if (key.startsWith("@")) {
			if (getCurrentSession() == null) 
				throw new RuntimeException("Session is not created");
			return getLocalVariable(key.replace("@", ""));
		}
		Map<String, Object> data = holder.get(getTransactionId());
		if (data == null) return data;
		return holder.get(getTransactionId()).get(key);
	}
	
	public List<Map<String, Object>> getAllListLocalMap() {
		List<Map<String, Object>> localMap = new ArrayList<Map<String, Object>>();		
		for (Entry<String, Map<String, Object>> entry : sessionHolder.entrySet()) {
			localMap.add(entry.getValue());
		}
		return localMap;
	}
	
	public Map<String, Object> getLocalMap() {
		return getLocalMap(getCurrentSession());
	}
	
	public LinkedList<String> getSessionList() {
		return sessionList;
	}
	
	public Map<String, Map<String, Object>> getSessionHolder() {
		return sessionHolder;
	}
	
	public Map<String, Object> getLocalMap(String session) {
		if (sessionHolder.get(session) == null)
			return new HashMap<String, Object>();
		return sessionHolder.get(session);
	}
	
	public Object getLocalVariable(String key) {
		return getLocalMap().get(key);
	}

	public Object getLocalVariable(String session, String key) {
		return getLocalMap(session).get(key);
	}

	public void setCurrentSession(String sessionId) {
		this.sessionId = sessionId;
	}
	
	public String createSession() {
		sessionId = UUID.randomUUID().toString();
		sessionList.add(sessionId);
		return sessionId;
	}
	
	public String getCurrentSession() {
		return sessionId;
	}
	
	public String getTransactionId() {
		return transactionId;
	}
	
	public void setRetention(boolean retention) {
		this.retention = retention;
	}
	
	public void addFailedSession(String sessionId) {
		failedSessionList.add(sessionId);
	}
	
	public LinkedList<String> getFailedSessionList() {
		return failedSessionList;
	}
	
	public boolean isSessionFailed(String sessionId) {
		return failedSessionList.contains(sessionId);
	}
	public boolean isRetention() {
		return retention;
	}

}
