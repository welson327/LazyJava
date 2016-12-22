package org.lazyjava.db;

import org.json.JSONObject;

public interface IServiceDBAdapter {
	// row data
	public JSONObject find(String dbName, String collectionName, JSONObject input) throws Exception;
	public JSONObject insert(String dbName, String collectionName, JSONObject input) throws Exception;
	public JSONObject update(String dbName, String collectionName, String operation, JSONObject queryJSON, JSONObject setJSON) throws Exception;
	public JSONObject delete(String dbName, String collectionName, JSONObject input) throws Exception;
	
	// db
	//public JSONObject createDatabase(String dbName, String collectionName);
	//public JSONObject dropDatabase(String dbName, String collectionName);
}
