package org.lazyjava.db;

import java.util.List;
import java.util.Set;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisAdapter {
	//protected final String _QUERYRESULT_ = "queryResult";
	protected JedisPool jedisPool = null;
	
	protected String host = "localhost";
	protected int port = 6379;
	
	private String namespace = "";
	protected Jedis jedis = null;
	private boolean autoCloseConnection = true;
	private boolean useConnPool = true;
/*
	private static MongoClientOptions mongoClientOptions = null;
	static {
		//http://api.mongodb.org/java/2.6/com/mongodb/MongoOptions.html
		mongoClientOptions = MongoClientOptions.builder()
			.autoConnectRetry(true)
            //.connectionsPerHost(100)
			//.connectTimeout(5000) 	// v2.6: 0 is default and infinite
			.cursorFinalizerEnabled(false) // fix: "MongoCleaner" waiting on condition (at com.mongodb.Mongo$CursorCleanerThread.run(Mongo.java:773))
            //.maxWaitTime(5000) 	// v2.6: Default is 120,000
            //.socketTimeout(5000)  	// v2.6: 0 is default and infinite
            //.threadsAllowedToBlockForConnectionMultiplier(5000)
            .build();
	}
	

	private static MongoClient createMongoClient() throws UnknownHostException {
		return new MongoClient(new ServerAddress(getHost(), getPort()), mongoClientOptions);
		//return new MongoClient(getHost(), getPort());
		
		// fix: com.mongodb.MongoException$Network: can't call something
		//m = new Mongo(new ServerAddress(getHost(), getPort()), mongoOptions); 
	}
*/
	protected static JedisPool initPool(String host, int port) {
		JedisPoolConfig config = new JedisPoolConfig();
		//config.setMaxTotal(maxTotal); // maximum active connections
		return new JedisPool(config, host, port);
	}
	
	private static RedisAdapter instance = null;
	public static RedisAdapter getInstance() {
		if (instance == null) {
			instance = new RedisAdapter();
		}
		return instance;
	}
	
	public RedisAdapter() {
		this("", true);
	}
	
	public RedisAdapter(String namespace, boolean autoClose) {
		try {
			this.namespace = namespace;
			this.autoCloseConnection = autoClose;
			// fix conn bug: if someone construct object but do nothing
			//this.m = createMongoClient();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public RedisAdapter(String host, int port, String namespace, boolean autoClose, boolean usePool) {
		try {
			this.host = host;
			this.port = port;
			this.namespace = namespace;
			this.autoCloseConnection = autoClose;
			this.useConnPool = usePool;
			// fix conn bug: if someone construct object but do nothing
			//this.m = createMongoClient();
			
			if(usePool && jedis==null) {
				//jedisPool = initPool(host, port);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getHost() {
		return this.host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public void setPort(int port) {
		this.port = port;		
	}
	
	
	public Set<String> keys(String pattern) {
		jedis = safeGetJedisClient();
		
		if(pattern == null || pattern.length()==0) {
			pattern = "*";
		}
		Set<String> list = jedis.keys(pattern);
		return list;
	}
	
	public String flushAll() {
		return jedis.flushAll();
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
			safeClose();
		}
	}
	public String setExpiry(String key, String value, int expiryInSec) {
		try {
			jedis = safeGetJedisClient();
			return jedis.setex(key, expiryInSec, value);
		} finally {
			safeClose();
		}
	}
	
	public String get(String key) {
		try {
			jedis = safeGetJedisClient();
			return jedis.get(key);
		} finally {
			safeClose();
		}
	}
	public boolean exists(String key) {
		try {
			jedis = safeGetJedisClient();
			return jedis.exists(key);
		} finally {
			safeClose();
		}
	}
	public long delete(String key) {
		try {
			jedis = safeGetJedisClient();
			return jedis.del(key);
		} finally {
			safeClose();
		}
	}
	public long deleteBy(String keyPattern) {
		long cnt = 0;
		try {
			Set<String> keys = keys(keyPattern);
			for (String key : keys) {
			    jedis.del(key);
			    ++cnt;
			} 
		} finally {
			safeClose();
		}
		return cnt;
	}
	public Long incr(String key, int val) {
		try {
			jedis = safeGetJedisClient();
			return jedis.incrBy(key, val);
		} finally {
			safeClose();
		}
	}
	public Long decr(String key, int val) {
		try {
			jedis = safeGetJedisClient();
			return jedis.decrBy(key, val);
		} finally {
			safeClose();
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
			safeClose();
		}
	}
	public Long rpush(String key, String value) {
		try {
			jedis = safeGetJedisClient();
			return jedis.rpush(key, value);
		} finally {
			safeClose();
		}
	}
	public List<String> lrange(String key, int startIndex, int endIndex) {
		try {
			jedis = safeGetJedisClient();
			List<String> list = jedis.lrange(key, startIndex, endIndex);
			return list;
		} finally {
			safeClose();
		}
	}
	
	public String lpop(String key) {
		try {
			jedis = safeGetJedisClient();
			return jedis.lpop(key);
		} finally {
			safeClose();
		}
	}
	public String rpop(String key) {
		try {
			jedis = safeGetJedisClient();
			return jedis.rpop(key);
		} finally {
			safeClose();
		}
	}
	public String ltrim(String key, long start, long end) {
		try {
			jedis = safeGetJedisClient();
			return jedis.ltrim(key, start, end);
		} finally {
			safeClose();
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
			safeClose();
		}
	}
	public String hget(String key, String field) {
		try {
			jedis = safeGetJedisClient();
			return jedis.hget(key, field);
		} finally {
			safeClose();
		}
	}
	
	
//	public String rrange(String key, int startIndex, int endIndex) {
//		try {
//			jedis = safeGetJedisClient();
//			List<String> list = jedis.rrange(key, startIndex, endIndex);
//			return list;
//		} finally {
//			safeClose();
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
			safeClose();
		}
	}

	
	public void close() {
		if(jedis != null) {
			jedis.close();
			jedis = null;
		}
	}

	private String nsKey(String key) {
		if(namespace == null || namespace.length()==0) {
			return key;
		} else {
			return namespace + ":" + key;
		}
	}
	private void safeClose() {
		if(jedis != null && this.autoCloseConnection) {
			jedis.close();
			jedis = null;
		}
	}
	private Jedis safeGetJedisClient() {
		if(jedis == null) {
			jedis = cretaeJedisClient();
		}
		return jedis;
	}
	private Jedis cretaeJedisClient() {
		/*
		if(namespace == null || namespace.length()==0) {			
			return new NamespaceJedis(host);
		} else {
			return new NamespaceJedis(host);
		}
		*/
		
//		String sHost = String.format("%s:%d", host, port);
		
		if(jedisPool != null) {
			return jedisPool.getResource();
		} else {
			return new Jedis(host);
		}
	}
	

}
