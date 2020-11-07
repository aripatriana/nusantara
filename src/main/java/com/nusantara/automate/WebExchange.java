package com.nusantara.automate;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import com.nusantara.automate.util.MapUtils;

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
	
	// moduleid|value
	Map<String, LinkedList<Map<String, Object>>> cachedMetaData = new HashMap<String, LinkedList<Map<String,Object>>>();
	
	// moduleid|moduleid$XXX
	Map<String, LinkedList<String>> cachedMetaDataKey = new HashMap<String, LinkedList<String>>();
	
	// moduleid|session
//	Map<String, String> cachedSessionMetaData = new HashMap<String, String>();
	
	// session
	Set<String> cachedSession = new HashSet<String>();
	public static final String LOCAL_VARIABLE = "local_variable";
	public static final String ALL_LOCAL_VARIABLE = "all_local_variable";
	LinkedList<String> sessionList = new LinkedList<String>();
	Set<String> failedSessionList = new HashSet<String>();
	Map<String, Map<String, Object>> sessionHolder = new HashMap<String, Map<String,Object>>();
	String transactionId = UUID.randomUUID().toString();
	String sessionId = null;
	boolean retention = false;
	
	private void reset() {
		retention = false;
		sessionId = null;
		listMetaData.clear();
		sessionList.clear();
		failedSessionList.clear();
		sessionHolder.clear();
		cachedMetaData.clear();
		cachedMetaDataKey.clear();
		cachedSession.clear();
		holder.remove(LOCAL_VARIABLE);
		holder.remove(ALL_LOCAL_VARIABLE);
		
		renewTransactionId();
	}
	
	public void renewTransactionId() {
		// create new copy transaction id
		String tempTransactionId = UUID.randomUUID().toString();
		holder.put(tempTransactionId, holder.get(getTransactionId()));
		transactionId = tempTransactionId;
	}
	
	public void addMetadata(Map<String, Object> metadata) {
		listMetaData.add(metadata);
	}
	
	public int getMetaDataSize() {
		return listMetaData.size();
	}
	
	public void clearCachedSession() {
		cachedSession.clear();
	}

	private LinkedList<Map<String, Object>> getMetaDataByMenuId(String cahcedMenuId) {
		LinkedList<Map<String, Object>> tempListMetaData = new LinkedList<Map<String,Object>>();
		LinkedList<Map<String, Object>> bufferListMetaData = new LinkedList<Map<String,Object>>(listMetaData);
		Collections.copy(bufferListMetaData, listMetaData);
		for (Map<String, Object> map : bufferListMetaData) {
			if (map.keySet().toArray()[0].toString().toUpperCase().startsWith(cahcedMenuId+".")) {
				MapUtils.clearMapKey(cahcedMenuId + ".", map);
				tempListMetaData.add(map);
			}
		}
		return tempListMetaData;
	}
	
	/**
	 * Sesi dibentuk per row dalam 1 sheet, apabila ada sheet lain maka sesi nya akan mengikuti sesi sebelumnya
	 * row pertama pada setiap sheet cachedSession akan kosong, tujuannya untuk identifikasi apakah ada module$number
	 * jika tidak ada maka data yg digunakan adalah data pada sheet tanpa $number
	 * 
	 * @param moduleId
	 * @return
	 */
	public LinkedList<Map<String, Object>> getListMetaData(String moduleId, boolean checkNextMenuId) {
		String mainMenu = moduleId.toUpperCase();
		String cahcedMenuId = moduleId.toUpperCase();
		LinkedList<Map<String, Object>> metadata = new LinkedList<Map<String,Object>>();
		int indexMenuId = 0;
		
		// the cached session is in the empty condition in every sheet begin
		if (cachedSession.contains(getCurrentSession())) {
			// read the metadata in the next row in the existing sheet
			return cachedMetaData.get(cachedMetaDataKey.get(cahcedMenuId).getLast());	
		} else {
			cachedSession.add(getCurrentSession());
			
			// there are three conditions the code comes here
			// 1. read the first sheet, the cached metadata must be empty
			// 2. read new sheet with different sheet names, the cached metadata must be empty
			// 3. read the sheet with the same name but containing suffix $number, 
			if (cachedMetaData.containsKey(cahcedMenuId)) {
				// the third conditions, check the next suffix $number
				if (checkNextMenuId) {
					indexMenuId++;
					cahcedMenuId = mainMenu + "" + indexMenuId;
					boolean emptyCached = false;
					while(true) {
						if (cachedMetaData.containsKey(cahcedMenuId)) {
							indexMenuId++;
							cahcedMenuId = mainMenu  + "" + indexMenuId;		
						} else {
							emptyCached = true;
							break;
						}
					}
					if (!emptyCached) {
						metadata = getMetaDataByMenuId(cahcedMenuId);
						if (metadata.isEmpty()) {
							metadata = cachedMetaData.get(cachedMetaDataKey.get(mainMenu).getLast());
						} else {
							cachedMetaData.put(cahcedMenuId, metadata);
							LinkedList<String> cachedKey = cachedMetaDataKey.get(mainMenu);
							if (cachedKey == null) cachedKey = new LinkedList<String>();
							cachedKey.add(cahcedMenuId);
							cachedMetaDataKey.put(mainMenu, cachedKey);
						}
					}
				} else {
					metadata = cachedMetaData.get(cachedMetaDataKey.get(mainMenu).getLast());
				}
			} else {
				// the first and second conditions comes here
				metadata = getMetaDataByMenuId(cahcedMenuId);
				
				cachedMetaData.put(cahcedMenuId, metadata);
				LinkedList<String> cachedKey = cachedMetaDataKey.get(mainMenu);
				if (cachedKey == null) cachedKey = new LinkedList<String>();
				cachedKey.add(cahcedMenuId);
;
				cachedMetaDataKey.put(mainMenu, cachedKey);
			}
		}
		return metadata;
	}
	
	public LinkedList<Map<String, Object>> getListMetaData(String moduleId) {
		return getListMetaData(moduleId, false);
	}
	
	public Map<String, Object> getMetaData(String menuId, int index, boolean checkNextMenuId) {
		LinkedList<Map<String, Object>> tempListMetaData = getListMetaData(menuId, checkNextMenuId);
		if (tempListMetaData != null && tempListMetaData.size() > 0)
			return tempListMetaData.get(index);
		return null;
	}
	
	public Map<String, Object> getMetaData(String menuId, int index) {
		LinkedList<Map<String, Object>> tempListMetaData = getListMetaData(menuId, false);
		if (tempListMetaData != null && tempListMetaData.size() > 0) {
			return tempListMetaData.get(index);
		}
		return null;
	}
	
	public LinkedList<Map<String, Object>> getMetaData(String menuId) {
		LinkedList<Map<String, Object>> tempListMetaData = getListMetaData(menuId, false);
		return tempListMetaData;
	}
	
	public void clearMetaData() {
		listMetaData.clear();
	}
	
	public void clear() {
		reset();
	}
	
	public void remove(String key) {
		if (key.startsWith("@")) {
			String session = getCurrentSession();
			if (getCurrentSession() == null)
				throw new RuntimeException("Session is not created");
			
			Map<String, Object> localVariable = sessionHolder.get(session);
			if (localVariable != null) {
				localVariable.remove(key);
			}
		} else {
			holder.get(getTransactionId()).remove(key);
		}
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
		List<Map<String, Object>> localMap = new LinkedList<Map<String, Object>>();		
		for (Entry<String, Map<String, Object>> entry : sessionHolder.entrySet()) {
			if (!failedSessionList.contains(entry.getKey()))
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
		return createSession(999);
	}
	public String createSession(int index) {
		try {
			sessionId = sessionList.get(index);
			return sessionId;
		} catch (IndexOutOfBoundsException e) {
			// do nothing
		}
		
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

	public void addListFailedSession(Collection<String> failedSessionId) {
		failedSessionList.addAll(failedSessionId);
	}

	
	public void addFailedSession(String sessionId) {
		failedSessionList.add(sessionId);
	}
	
	public Set<String> getFailedSessionList() {
		return failedSessionList;
	}
	
	public boolean isSessionFailed(String sessionId) {
		return failedSessionList.contains(sessionId);
	}
	public boolean isRetention() {
		return retention;
	}

}
