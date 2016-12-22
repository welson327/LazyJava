package org.lazyjava.guava;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.LoadingCache;

//===================================================================
// Purpose: 	Use google guava-cache
// Parameters:
// Return:
// Remark:		http://docs.guava-libraries.googlecode.com/git/javadoc/com/google/common/cache/LoadingCache.html
// Author:		welson
//===================================================================
public class CacheProvider<K,V> {
	//public static final int _DB_ = 0;
	//public static final int _FS_ = 1;
	private static CacheProvider instance = null;

	protected LoadingCache<K,V> cache = null;
	
	protected long maxSize = 1000;
	protected int expiry = 60;

	private Set<K> loadingKeys = null;
	private boolean isLoading = false;
	private K loadingKey = null;
	
	public static CacheProvider getInstance(){
		if (instance == null) {
			instance = new CacheProvider();
		}
		return instance;
	}
	
	public CacheProvider() {
		// to be override
		
		loadingKeys = new HashSet<K>();
	}
	
	
	public V get(K key) throws ExecutionException {
		loadingKeys.add(key);
		V value = cache.get(key);
		loadingKeys.remove(key);
		
		return value;
	}
	public void set(K key, V value) {
		// https://github.com/google/guava/wiki/CachesExplained
		if(cache != null) {
			cache.put(key, value); // override prev entries
		}
	}
	public void setMap(Map<? extends K, ? extends V> map) {
		// https://github.com/google/guava/wiki/CachesExplained
		if(cache != null) {
			cache.putAll(map);
		}
	}
	
	public boolean contains(K key) {
		if(cache != null) {
			Set<K> keySet = cache.asMap().keySet();
			return keySet.contains(key);
		} else {
			return false;
		}
	}
	
	public void clear() {
		cache.invalidateAll();
		//cache.cleanUp();
	}
	
	public void invalidate(K key) {
		int time = 0;
		while(loadingKeys.contains(key)) {
			//System.out.printf("key(%s) isLoading ...\n", key.toString());
			time += 100;
			sleep(100);
			if(time > 30000) {
				time = 0;
				break;
			}
		}
		cache.invalidate(key);
	}
	
	public long size() {
		return cache.size();
	}
	
	public long maxSize() {
		return this.maxSize;
	}
	
	private static void sleep(long ms) {
		try {Thread.sleep(ms);} catch (InterruptedException e) {}
	}
}
