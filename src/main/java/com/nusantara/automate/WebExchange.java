package com.nusantara.automate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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

	public static final String PREFIX_TYPE_DATA = "data";
	public static final String PREFIX_TYPE_ELEMENT = "element";
	public static final String PREFIX_TYPE_SYSTEM = "system";

	public static final String LOCAL_VARIABLE = "local_variable";
	public static final String ALL_LOCAL_VARIABLE = "all_local_variable";
	
	private Map<String, Map<String, Object>> holder = new HashMap<String, Map<String,Object>>();
	private LinkedList<Map<String, Object>> listMetaData = new LinkedList<Map<String,Object>>();
	private Map<String, Map<String, String>> elements = new HashMap<String, Map<String,String>>();
	private Set<String> modules = new LinkedHashSet<String>();	
	
	// moduleid|value
	private Map<String, LinkedList<Map<String, Object>>> cachedMetaData = new HashMap<String, LinkedList<Map<String,Object>>>();
	
	// moduleid|moduleid$XXX
	private Map<String, LinkedList<String>> cachedMetaDataKey = new HashMap<String, LinkedList<String>>();
	
	// moduleid|session
	//Map<String, String> cachedSessionMetaData = new HashMap<String, String>();
	
	// session
	private Set<String> cachedSession = new HashSet<String>();
	private String transactionId = UUID.randomUUID().toString();
	private boolean retention = false;
	private Session session = new Session();

	
	public void setSession(Session session) {
		this.session = session;
	}
	
	public Session getSession() {
		return session;
	}
	
	public void setModules(Set<String> modules) {
		this.modules = modules;
	}
	
	public Set<String> getModules() {
		return modules;
	}
	
	public void addModule(String module) {
		this.modules.add(module);
	}
	
	public void initSession(int count) {
		if (count == 0) count++;
		for (int index=0; index<count; index++) {
			createSession(index);
		}
		
		for (String moduleId : modules) {
			int i = 0;
			for (Map<String, Object> metadata : getListMetaData(moduleId)) {
				createSession(i);
				putToSession(WebExchange.PREFIX_TYPE_DATA, moduleId, metadata);
				i++;
			}
		}
	}
	
	public void renewTransactionId() {
		// create new copy transaction id
		String tempTransactionId = UUID.randomUUID().toString();
		holder.put(tempTransactionId, holder.get(getTransactionId()));
		transactionId = tempTransactionId;
	}
	
	public void addMetadata(Map<String, Object> metadata) {
		listMetaData.add(metadata);
		String[] keys = metadata.keySet().iterator().next().split("\\.");
		modules.add(keys[0].toLowerCase());
	}
	
	public int getTotalMetaData() {
		return listMetaData.size();
	}
	
	public int getMetaDataSize() {
		return getMetaDataSize(modules.iterator().next());
	}
	
	public int getMetaDataSize(String moduleId) {
		int size = 0;
		for (Map<String, Object> data : listMetaData) {
			
			Object moduleName = data.get(moduleId + "."+ "module_name");
			if (moduleName != null) {
				if (moduleName.toString().equals(moduleId))
					size++;
			}
		}
		return size;
	}
	public void clearCachedSession() {
		cachedSession.clear();
	}

	private LinkedList<Map<String, Object>> getMetaDataByMenuId(String cahcedMenuId) {
		LinkedList<Map<String, Object>> tempListMetaData = new LinkedList<Map<String,Object>>();
		LinkedList<Map<String, Object>> bufferListMetaData = new LinkedList<Map<String,Object>>();
		MapUtils.copyKeepOriginal(bufferListMetaData, listMetaData);
		for (Map<String, Object> map : bufferListMetaData) {
			if (map.keySet().toArray()[0].toString().toLowerCase().startsWith(cahcedMenuId+".")) {
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
		String mainMenu = moduleId.toLowerCase();
		String cahcedMenuId = moduleId.toLowerCase();
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
					if (emptyCached) {
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
	
	public Map<String, Object> getMetaData(String moduleId, int index, boolean checkNextMenuId) {
		LinkedList<Map<String, Object>> tempListMetaData = getListMetaData(moduleId, checkNextMenuId);
		if (tempListMetaData != null && tempListMetaData.size() > 0)
			return tempListMetaData.get(index);
		return null;
	}
	
	public Map<String, Object> getMetaData(String moduleId, int index) {
		LinkedList<Map<String, Object>> tempListMetaData = getListMetaData(moduleId, false);
		if (tempListMetaData != null && tempListMetaData.size() > 0) {
			return tempListMetaData.get(index);
		}
		return null;
	}
	
	public void clearMetaData() {
		listMetaData.clear();
	}
	
	public void clear() {
		reset();
	}
	
	public void remove(String key) {
		if (key.startsWith("@")) {
			getSession().remove(key);
		} else {
			holder.get(getTransactionId()).remove(key);
		}
	}
	
	public void put(String key, Object value) {
		if (key.startsWith("@")) {
			getSession().put(key, value);
		} else {
			Map<String, Object> map = new HashMap<String, Object>();
			if (holder.containsKey(getTransactionId())) {
				map = holder.get(getTransactionId());
			}
			map.put(key, value);
			holder.put(getTransactionId(), map);
		}
	}
	
	public void putAll(Map<String, Object> data) {
		for (Entry<String, Object> e : data.entrySet()) {
			put(e.getKey(), e.getValue());
		}
	}
	
	@SuppressWarnings("unchecked")
	public void putToSession(String prefix, String moduleId, Map<String, Object> metadata) {
		MapUtils.keyLowercase((Map<String, Object>)metadata);
		for (Entry<String, Object> entry : metadata.entrySet()) {
			if (entry.getValue() instanceof List) {
				List<Object> subMetadataList = (List<Object>) entry.getValue();
				int i = 0;
				for (Object subMetadata : subMetadataList) {
					if (subMetadata instanceof Map) {
						
						for (Entry<String, Object> subEntry : ((Map<String, Object>)subMetadata).entrySet()) {
							if (subEntry.getKey().contains(moduleId)) {
								put("@" + prefix + "." + entry.getKey().concat("[" + i + "]") + "." + subEntry.getKey(),
										subEntry.getValue());						
							} else {
								put("@" + prefix + "." + moduleId + "." + entry.getKey().concat("[" + i + "]") + "." + subEntry.getKey(),
										subEntry.getValue());													
							}
						}
					} else {
						if (entry.getKey().contains(moduleId)) {
							put("@" + prefix + "." + entry.getKey().concat("[" + i + "]") ,
									subMetadata);	
						} else {
							put("@" + prefix + "." + moduleId + "." + entry.getKey().concat("[" + i + "]") ,
									subMetadata);	
						}
					}
					i++;
				}
				
				if (entry.getKey().contains(moduleId)) {
					put("@" + prefix + "." + entry.getKey().concat("[]"),
							entry.getValue());
				} else {
					put("@" + prefix + "." + moduleId + "." + entry.getKey().concat("[]"),
							entry.getValue());							
				}
			} else {
				if (entry.getKey().contains(moduleId)) {
					put("@" + prefix +  "." + entry.getKey() , entry.getValue());				
				} else {
					put("@" + prefix +  "." + moduleId + "." + entry.getKey() , entry.getValue());									
				}
			}
		}
	}
	
	public static void main(String[] args) {
		Map<String, Object> t = new LinkedHashMap<String, Object>();
		t.put("contract", "REPO-XXX");
		t.put("purchase", "REPO");
		t.put("securityType", "STOCK");
		
		List<Map<String, Object>> l = new LinkedList<Map<String,Object>>();
		Map<String, Object> tl1 = new LinkedHashMap<String, Object>();
		tl1.put("instrument_code", "TLKM");
		tl1.put("harga", "1000");
		
		Map<String, Object> tl2 = new LinkedHashMap<String, Object>();
		tl2.put("instrument_code", "ASII");
		tl2.put("harga", "1500");
		
		l.add(tl1);
		l.add(tl2);
		
		t.put("instrument", l);
		
		List<String> a = new ArrayList<String>();
		a.add("HALOO");
		a.add("TESTHELOO");
		t.put("test", a);
		
		WebExchange w = new WebExchange();
		w.createSession();
//		
//		w.putAllToSession(PREFIX_TYPE_DATA,"order-repo", t);
//		w.put("@test", "hallooo");
//		
//		for (Entry<String, Object> e : w.sessionHolder.get(w.getCurrentSession()).entrySet()) {
//			System.out.println(e.getKey() + " -> " + e.getValue());
//		}
//		
//		System.out.println(w.get("@test"));
	}

	
	public Map<String, Object> getAll() {
		return holder.get(getTransactionId());
	}
	
	public Object get(String key) {
		if (key.startsWith("@")) {
			return getSession().get(key);
		}
		Map<String, Object> data = holder.get(getTransactionId());
		if (data == null) return data;
		return holder.get(getTransactionId()).get(key);
	}
	
	public List<Map<String, Object>> getAllListLocalMap() {
		return getSession().getAllListLocalMap();
	}
	
	public Map<String, Object> getLocalMap() {
		return getLocalMap(getCurrentSession());
	}
	
	public LinkedList<String> getSessionList() {
		return getSession().getSessionList();
	}
	
	public Map<String, Map<String, Object>> getSessionHolder() {
		return getSession().getSessionHolder();
	}
	
	public Map<String, Object> getLocalMap(String session) {
		return getSession().getMap(session);
	}
	
	public Object getLocalVariable(String key) {
		return getLocalMap().get(key);
	}

	public Object getLocalVariable(String session, String key) {
		return getLocalMap(session).get(key);
	}

	public void setCurrentSession(int index) {
		getSession().setCurrentSessionByIndex(index);
	}

	public void setCurrentSession(String sessionId) {
		getSession().setCurrentSession(sessionId);
	}
	
	public String createSession() {
		return createSession(999);
	}
	
	public String createSession(int index) {
		return getSession().createSession(index);
	}
	
	public String getCurrentSession() {
		return getSession().getCurrentSession();
	}
	
	public int getCountSession() {
		return getSession().countSize();
	}
		
	public String getTransactionId() {
		return transactionId;
	}
	
	public void setRetention(boolean retention) {
		this.retention = retention;
	}

	public void addListFailedSession(Collection<String> failedSessionId) {
		getSession().addListFailedSession(failedSessionId);
	}

	
	public void addFailedSession(String sessionId) {
		getSession().addFailedSession(sessionId);
	}
	
	public Set<String> getFailedSessionList() {
		return getSession().getFailedSessionList();
	}
	
	public boolean isSessionFailed(String sessionId) {
		return getSession().isSessionFailed(sessionId);
	}
	
	public boolean isRetention() {
		return retention;
	}
	
	public void addElement(String moduleId, String key, String value) {
		Map<String, String> map = elements.get(moduleId);
		if (map == null)
			map = new HashMap<String, String>();
		map.put(key, value);
	}
	
	public Map<String, String> getElements(String moduleId) {
		return elements.get(moduleId);
	}
	
	public Map<String, Map<String, String>> getElements() {
		return elements;
	}
	
	public void addElement(String moduleId, Map<String, String> data) {
		Map<String, String> map = elements.get(moduleId);
		if (map == null)
			map = new HashMap<String, String>();
		map.putAll(data);
	}
	
	public void addElements(Map<String, Map<String, Object>> elements) {
		elements.putAll(elements);
	}
	
	private void reset() {
		retention = false;
		listMetaData.clear();
		cachedMetaData.clear();
		cachedMetaDataKey.clear();
		cachedSession.clear();
		holder.remove(LOCAL_VARIABLE);
		holder.remove(ALL_LOCAL_VARIABLE);
		getSession().clearSession();
		renewTransactionId();
	}

}
