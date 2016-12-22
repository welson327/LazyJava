package org.lazyjava.cache;

import org.lazyjava.db.RedisAdapter;

public class RedisCache {
//	private static RedisCache instance = null;
	
	private RedisAdapter redisAdapter = null;
	
//	public static RedisCache getInstance() {
//		if(instance == null) {
//			instance = new RedisCache();
//		}
//		return instance;
//	}
	

	public RedisCache(String host, int port, String namespace) {
		if(namespace == null) {
			namespace = "";
		}
		this.redisAdapter = new RedisAdapter(host, port, namespace, true, true);
	}
	
	public Object get(String key) {
		Object value = null;
		try {
			value = redisAdapter.get(key);
			if(value == null) {
				value = load(key);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return value;
	}
	public String set(String key, String value, int expiryInSec) {
		return redisAdapter.setExpiry(key, value, expiryInSec);
	}
	
	public Object load(String key) throws Exception {
		return null;
	};
	
	public long invalidate(String key) {
		return redisAdapter.delete(key);
	}
}
