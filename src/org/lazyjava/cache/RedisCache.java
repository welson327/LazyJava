package org.lazyjava.cache;

import java.util.Set;
import org.lazyjava.db.RedisAdapter;
import redis.clients.jedis.exceptions.JedisConnectionException;


abstract public class RedisCache {
	
	private RedisAdapter redisAdapter = null;
	
	protected String namespace = "";
	protected int expiryInSec = -1;
		
	public RedisCache(RedisAdapter ra) {
		this(ra, "", -1);
	}
	public RedisCache(RedisAdapter ra, String namespace) {
		this(ra, namespace, -1);
	}
	public RedisCache(RedisAdapter ra, String namespace, int expiryInSec) {
		if(namespace != null) {
			this.namespace = namespace;
		}
		if(expiryInSec > 0) {
			this.expiryInSec = expiryInSec;
		}
		this.redisAdapter = ra;
	}
	
	@Override
	public void finalize() {
		try {
			super.finalize();
			disconnect();
		} catch(Throwable t) {
			t.printStackTrace();
		}
	}
	
	public Set<String> keys(String pattern) {
		return redisAdapter.keys(nsKey(pattern));
	}
	public boolean exists(String key) {
		return redisAdapter.exists(nsKey(key));
	}
	public Object get(String key) {
		Object value = null;
		try {
			value = redisAdapter.get(nsKey(key));
			if(value == null) {
				value = load(key); // for more readable, NOT use namespace for load()
			}
		} catch (JedisConnectionException e) {
			long ts = System.currentTimeMillis();
			System.out.println("JedisConnectionException: Get " + key + " exception !!! ts=" + ts + ", please check stack log.");
			e.printStackTrace();
			System.out.println("JedisConnectionException: Get " + key + " exception !!! ts=" + ts + ". --- <END> ---");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return value;
	}
	public String set(String key, String value) {
		return redisAdapter.set(nsKey(key), value);
	}
	public String set(String key, String value, int expiryInSec) {
		return redisAdapter.setExpiry(nsKey(key), value, expiryInSec);
	}
	public long invalidate(String key) {
		return redisAdapter.delete(nsKey(key));
	}
	public void disconnect() {
		redisAdapter.close();
	}

	abstract protected Object load(String key) throws Exception;

	private String nsKey(String key) {
		if(namespace == null || namespace.length()==0) {
			return key;
		} else {
			return namespace + ":" + key;
		}
	}
}
