package com.nusantara.automate.util;

import java.util.Map;

/**
 * The helper for simple entry
 * 
 * @author ari.patriana
 *
 * @param <K>
 * @param <V>
 */
public class SimpleEntry<K, V> implements Map.Entry<K, V>{
	    private final K key;
	    private V value;

	    public SimpleEntry(K key, V value) {
	        this.key = key;
	        this.value = value;
	    }

	    @Override
	    public K getKey() {
	        return key;
	    }

	    @Override
	    public V getValue() {
	        return value;
	    }

	    @Override
	    public V setValue(V value) {
	        V old = this.value;
	        this.value = value;
	        return old;
	    }
}
