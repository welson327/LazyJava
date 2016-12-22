package org.lazyjava.guava;

import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;

public class ExampleCacheProvider extends CacheProvider<String, String> {
	private static CacheProvider<String, String> instance = null;
	
	public static CacheProvider<String, String> getInstance() {
		if (instance == null) {
			instance = new ExampleCacheProvider();
		}
		return instance;
	}
	
	public ExampleCacheProvider() {
		//StickerListRemovalListener listener = new StickerListRemovalListener();
		CacheLoader<String, String> loader = new CacheLoader<String, String>() {
            @Override
            public String load(String uuid) throws Exception {
         	   return "";
            } 
        };
        
		maxSize = 2000;
		expiry = 12;
		
		if(expiry > 0) {
			cache = CacheBuilder.newBuilder()
					.maximumSize(maxSize)
					.expireAfterWrite(expiry, TimeUnit.HOURS)
					//.removalListener(listener)
					.build(loader);
		} else {
			cache = CacheBuilder.newBuilder()
					.maximumSize(maxSize)
					//.expireAfterAccess(10, TimeUnit.SECONDS)
					//.removalListener(listener)
					.build(loader);
		}
	}
}
