package com.nusantara.automate;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;

public class Session {

	// session
	private LinkedList<String> sessionList = new LinkedList<String>();
	private Set<String> failedSessionList = new HashSet<String>();
	private Map<String, Map<String, Object>> sessionHolder = new LinkedHashMap<String, Map<String,Object>>();
	private String sessionId = null;
	
	public void setCurrentSessionByIndex(int index) {
		this.sessionId = sessionList.get(index);
	}
	
	public void setCurrentSession(String sessionId) {
		this.sessionId = sessionId;
	}
	
	public String getCurrentSession() {
		return sessionId;
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
	
	public void put(String key, Object value) {
		String session = getCurrentSession();
		if (getCurrentSession() == null)
			throw new RuntimeException("Session is not created");
		
		Map<String, Object> localVariable = sessionHolder.get(session);
		if (localVariable == null) {
			localVariable = new LinkedHashMap<String, Object>();
		}
		key = setPrefixDataIfNotExist(key.replace("@", ""));
		localVariable.put(key, value);
		sessionHolder.put(session, localVariable);
	}
		
	public Object get(String key) {
		if (getCurrentSession() == null) 
			throw new RuntimeException("Session is not created");
		key = setPrefixDataIfNotExist(key.replace("@", ""));
		return getMap(getCurrentSession()).get(key);
	}
	
	public void remove(String key) {
		String session = getCurrentSession();
		if (getCurrentSession() == null)
			throw new RuntimeException("Session is not created");
		
		Map<String, Object> localVariable = sessionHolder.get(session);
		if (localVariable != null) {
			localVariable.remove(key);
		}
	}
	
	public Map<String, Object> getMap(String session) {
		if (sessionHolder.get(session) == null)
			return new HashMap<String, Object>();
		return sessionHolder.get(session);
	}
	
	public LinkedList<String> getSessionList() {
		return sessionList;
	}
	
	public Map<String, Map<String, Object>> getSessionHolder() {
		return sessionHolder;
	}
	
	public List<Map<String, Object>> getAllListLocalMap() {
		List<Map<String, Object>> localMap = new LinkedList<Map<String, Object>>();		
		for (Entry<String, Map<String, Object>> entry : sessionHolder.entrySet()) {
			if (!failedSessionList.contains(entry.getKey()))
				localMap.add(getMap(entry.getKey()));
		}
		return localMap;
	}
	
	private String setPrefixDataIfNotExist(String key) {
		if (key.startsWith(WebExchange.PREFIX_TYPE_SYSTEM)
			|| key.startsWith(WebExchange.PREFIX_TYPE_ELEMENT)
			|| key.startsWith(WebExchange.PREFIX_TYPE_DATA)) {
			return key;
		}
		return WebExchange.PREFIX_TYPE_SYSTEM + "." + key;
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
	
	public int countSize() {
		return sessionList.size();
	}
	
	public void clearSession() {
		sessionId = null;
		sessionList.clear();
		failedSessionList.clear();
		sessionHolder.clear();
	}
}
