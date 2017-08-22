package org.lazyjava.db;

import java.util.List;
import java.util.Set;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisAdapter {
	protected static final int DEF_POOL_SIZE = -1; // not use pool
	
	protected JedisPool jedisPool = null;
	protected Jedis jedis = null;
	
	protected String host = "localhost";
	protected int port = 6379;
	protected int connPoolSize = DEF_POOL_SIZE;	
	private boolean usePool = false; // true if connPoolSize > 0

	protected static JedisPool createPool(String host, int port, int maxConn) {
		JedisPoolConfig config = new JedisPoolConfig();
		
		// fix cannot get resource: http://fantaxy025025.iteye.com/blog/2340096
		config.setTestOnBorrow(true);
		config.setTestOnReturn(true);
		
		if(maxConn > 0) {
			config.setMaxTotal(maxConn); // maximum active connections
			config.setMaxIdle(3);
		}
		config.setMaxWaitMillis(5000);
		
		return new JedisPool(config, host, port);
	}

	// use pool for singleton
	/*
	private static RedisAdapter instance = null;
	public static RedisAdapter getInstance() {
		if (instance == null) {
			instance = new RedisAdapter();
			instance.usePool = true;
		}
		return instance;
	}
	*/
	
	public RedisAdapter() {
	}
	
	public RedisAdapter(String host, int port) {
		this(host, port, DEF_POOL_SIZE);
	}
	public RedisAdapter(String host, int port, int connPoolSize) {
		try {
			this.host = host;
			this.port = port;
			if(connPoolSize > 0) {
				this.connPoolSize = connPoolSize;
				this.usePool = true;
			} else {
				this.connPoolSize = -1;
				this.usePool = false;
			}
			
			// fix conn bug: if someone construct object but do nothing
			//this.m = createMongoClient();
			
			if(jedis == null) {
				//jedisPool = initPool(host, port);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void finalize() {
		try{
			super.finalize();
			
			System.out.println(this.getClass().getName() + " : finalize");
			close();
		} catch(Throwable t) {
			t.printStackTrace();
		}
	}
	
	public Set<String> keys(String pattern) {
		try {
			jedis = safeGetJedisClient();
			
			if(pattern == null || pattern.length()==0) {
				pattern = "*";
			}
			Set<String> list = jedis.keys(pattern);
			return list;
		} finally {
			closeResource();
		}
	}
	
	public String flushAll() {
		try {
			jedis = safeGetJedisClient();
			return jedis.flushAll();
		} finally {
			closeResource();
		}
	}
	
	// ----------------------------------------------------
	//
	//                      String
	//
	// ----------------------------------------------------
	public String set(String key, String value) {
		try {
			jedis = safeGetJedisClient();
			return jedis.set(key, value);
		} finally {
			closeResource();
		}
	}
	public String setExpiry(String key, String value, int expiryInSec) {
		try {
			jedis = safeGetJedisClient();
			if(expiryInSec > 0) {
				return jedis.setex(key, expiryInSec, value);
			} else {
				return jedis.set(key, value);
			}
		} finally {
			closeResource();
		}
	}
	
	public String get(String key) {
		try {
			jedis = safeGetJedisClient();
			return jedis.get(key);
		} finally {
			closeResource();
		}
	}
	public boolean exists(String key) {
		try {
			jedis = safeGetJedisClient();
			return jedis.exists(key);
		} finally {
			closeResource();
		}
	}
	public long delete(String key) {
		try {
			jedis = safeGetJedisClient();
			return jedis.del(key);
		} finally {
			closeResource();
		}
	}
	public long deleteBy(String keyPattern) {
		long cnt = 0;
		try {
			Set<String> keys = keys(keyPattern);
			jedis = safeGetJedisClient();
			for (String key : keys) {
			    jedis.del(key);
			    ++cnt;
			} 
		} finally {
			closeResource();
		}
		return cnt;
	}
	public Long incr(String key, int val) {
		try {
			jedis = safeGetJedisClient();
			return jedis.incrBy(key, val);
		} finally {
			closeResource();
		}
	}
	public Long decr(String key, int val) {
		try {
			jedis = safeGetJedisClient();
			return jedis.decrBy(key, val);
		} finally {
			closeResource();
		}
	}

	
	// ----------------------------------------------------
	//
	//                      List
	//
	// ----------------------------------------------------
	public Long lpush(String key, String value) {
		try {
			jedis = safeGetJedisClient();
			return jedis.lpush(key, value);
		} finally {
			closeResource();
		}
	}
	public Long rpush(String key, String value) {
		try {
			jedis = safeGetJedisClient();
			return jedis.rpush(key, value);
		} finally {
			closeResource();
		}
	}
	public List<String> lrange(String key, int startIndex, int endIndex) {
		try {
			jedis = safeGetJedisClient();
			List<String> list = jedis.lrange(key, startIndex, endIndex);
			return list;
		} finally {
			closeResource();
		}
	}
	
	public String lpop(String key) {
		try {
			jedis = safeGetJedisClient();
			return jedis.lpop(key);
		} finally {
			closeResource();
		}
	}
	public String rpop(String key) {
		try {
			jedis = safeGetJedisClient();
			return jedis.rpop(key);
		} finally {
			closeResource();
		}
	}
	public String ltrim(String key, long start, long end) {
		try {
			jedis = safeGetJedisClient();
			return jedis.ltrim(key, start, end);
		} finally {
			closeResource();
		}
	}
	
	// ----------------------------------------------------
	//
	//                      Hashes
	//
	// ----------------------------------------------------
	public long hset(String key, String field, String value) {
		try {
			jedis = safeGetJedisClient();
			return jedis.hset(key, field, value);
		} finally {
			closeResource();
		}
	}
	public String hget(String key, String field) {
		try {
			jedis = safeGetJedisClient();
			return jedis.hget(key, field);
		} finally {
			closeResource();
		}
	}
	
	
//	public String rrange(String key, int startIndex, int endIndex) {
//		try {
//			jedis = safeGetJedisClient();
//			List<String> list = jedis.rrange(key, startIndex, endIndex);
//			return list;
//		} finally {
//			closeResource();
//		}
//	}
	
	// ----------------------------------------------------
	//
	//                      pub/sub
	//
	// ----------------------------------------------------
	public Long publish(String channel, String message) {
		try {
			jedis = safeGetJedisClient();
			return jedis.publish(channel, message);
		} finally {
			closeResource();
		}
	}
	
	// ----------------------------------------------------
	//
	//                    debug command
	//
	// ----------------------------------------------------
	public String ping() {
		try {
			jedis = safeGetJedisClient();
			return jedis.ping();
		} finally {
			closeResource();
		}
	}
	public String clientList() {
		try {
			jedis = safeGetJedisClient();
			return jedis.clientList();
		} finally {
			closeResource();
		}
	}
	public String info() {
		try {
			jedis = safeGetJedisClient();
			return jedis.info();
		} finally {
			closeResource();
		}
	}
	
	// ----------------------------------------------------
	//
	//                    config
	//
	// ----------------------------------------------------
	public List<String> configGet(String pattern) {
		try {
			jedis = safeGetJedisClient();
			return jedis.configGet(pattern);
		} finally {
			closeResource();
		}
	}
	public String configSet(String parameter, String value) {
		try {
			jedis = safeGetJedisClient();
			return jedis.configSet(parameter, value);
		} finally {
			closeResource();
		}
	}
	
	public void close() {
		try {
			closeResource();
			
			if(jedisPool != null) {
				//jedisPool.returnResource(jedis);
				//jedisPool.destroy(); // destroy keeps the connection open until timeout is reached.
				jedisPool.close();
				jedisPool = null;
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private void closeResource() {
		// Deprecated. starting from Jedis 3.0, using @see Jedis.close()
		/*
		if(jedisPool != null) {
			jedisPool.returnResource(jedis);
		}
		*/
		
		if(jedis != null) {
			jedis.close();
			jedis = null;
		}			
	}
	private Jedis safeGetJedisClient() {
		Jedis ret = null;
		try {
			if(usePool) {
				if(jedisPool == null) {
					jedisPool = createPool(host, port, connPoolSize);
				}
				ret = jedisPool.getResource();
			} else {
				ret = new Jedis(host, port);
			}
		} catch(Exception e) {
			e.printStackTrace();
			ret = new Jedis(host, port);
		}
		return ret;
	}
}
