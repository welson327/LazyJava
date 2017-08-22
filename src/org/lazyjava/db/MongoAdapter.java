package org.lazyjava.db;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lazyjava.common.ServiceConstant;

import com.mongodb.AggregationOptions;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.Cursor;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoException;
import com.mongodb.ServerAddress;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;

/*
 * Ref: https://docs.mongodb.com/ecosystem/drivers/java/
 */
public class MongoAdapter {
	// tag
	protected final String _QUERYRESULT_ = "queryResult";
	
	private static MongoClientOptions mongoClientOptions = null;
	private String host = null;
	private int port = -1;
	
	protected MongoClient m = null;
	protected DB db = null;
	private DBCollection coll = null;
	private boolean autoCloseConnection = true;
	
	static {
		//http://api.mongodb.org/java/2.6/com/mongodb/MongoOptions.html
		mongoClientOptions = MongoClientOptions.builder()
			//.autoConnectRetry(true)   // driver v3.3.0 is drop
            //.connectionsPerHost(100)
			//.connectTimeout(5000) 	// v2.6: 0 is default and infinite
			.cursorFinalizerEnabled(false) // fix: "MongoCleaner" waiting on condition (at com.mongodb.Mongo$CursorCleanerThread.run(Mongo.java:773))
            //.maxWaitTime(5000) 		// v2.6: Default is 120,000
            //.socketTimeout(5000)  	// v2.6: 0 is default and infinite
            //.threadsAllowedToBlockForConnectionMultiplier(5000)
            .build();
	}
	
	public MongoAdapter() {
		this(true);
	}
	
	public MongoAdapter(boolean autoClose) {
		this(null, -1, autoClose);
	}
	
	public MongoAdapter(String host, int port, boolean autoClose) {
		try {
			this.host = host;
			this.port = port;
			this.autoCloseConnection = autoClose;
			// fix conn bug: if someone construct object but do nothing
			//this.m = createMongoClient();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getHost() {
		return (host != null) ? host : "localhost"; 
	}
	public int getPort() {
		return (port >= 0) ? port : 27017; 
	}
	private MongoClient createMongoClient() throws UnknownHostException {
		return new MongoClient(new ServerAddress(getHost(), getPort()), mongoClientOptions);
		//return new MongoClient(getHost(), getPort());
		
		// fix: com.mongodb.MongoException$Network: can't call something
		//m = new Mongo(new ServerAddress(getHost(), getPort()), mongoOptions); 
	}

	
	public Set<String> getCollections(String dbName) {
		DB db = m.getDB(dbName);
		Set<String> collections = db.getCollectionNames();
		/*
		for (String collectionName : collections) {
			System.out.println(collectionName);
		}*/
		return collections;
	}
	
	public JSONObject findById(String dbName, String collectionName, String _id) throws JSONException, MongoException {
		return findById(dbName, collectionName, _id, null);
	}
	
	public JSONObject findById(String dbName, String collectionName, String _id, JSONObject fields) throws JSONException, MongoException {
		JSONObject rslt = null;
		try {
			m = safeCreateMongo();
			db = m.getDB(dbName);
			coll = db.getCollection(collectionName);	
			DBObject searchById = new BasicDBObject("_id", new ObjectId(_id));
			DBObject keys = (fields==null) ? new BasicDBObject() : (BasicDBObject) JSON.parse(fields.toString());
			DBObject found = coll.findOne(searchById, keys);
			rslt = (found!=null) ? new JSONObject(found.toString()) : null;
		} catch (IllegalArgumentException e) { 
			// IllegalArgumentException: invalid ObjectId [1478F6EF0DD000001-Z]
			rslt = null;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} finally {
			internalClose();
		}
		return rslt;
	}
	
	public JSONObject findByIds(String dbName, String collectionName, String[] _ids, JSONObject fields) throws JSONException, MongoException {
		JSONObject output = null;
		DBCursor cursor = null;
		try {
			m = safeCreateMongo();
			db = m.getDB(dbName);
			coll = db.getCollection(collectionName);
			
			// query by: db.collection.find({_id:{$in:["value1", "value2", ...]}})
			BasicDBList inArray = new BasicDBList();
			for(String _id : _ids) {
				inArray.add(new ObjectId(_id));
			}
			DBObject query = new BasicDBObject("_id", new BasicDBObject("$in", inArray));
			DBObject keys = (fields==null) ? new BasicDBObject() : (BasicDBObject) JSON.parse(fields.toString());
			cursor = coll.find(query, keys);
			
			JSONArray array = new JSONArray();
			while (cursor.hasNext()) {
				BasicDBObject bo = (BasicDBObject) cursor.next();
				JSONObject json = new JSONObject(bo.toString());
				array.put(json);
			} 
			
			output = new JSONObject();
			output.put(_QUERYRESULT_, array);
			output.put("totalCount", array.length());
			output.put("returnCount", array.length());
		} catch (IllegalArgumentException e) { 
			// IllegalArgumentException: invalid ObjectId [1478F6EF0DD000001-Z]
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} finally {
			if(cursor != null) {
				cursor.close();
				cursor = null;
			}
			internalClose();
		}
		return output;
	}
	
	public JSONObject find(String dbName, String collectionName, JSONObject input) throws JSONException, MongoException {
		return find(dbName, collectionName, input, null, -1, -1);
	}
	
	public JSONObject find(String dbName, String collectionName, JSONObject input, JSONObject fields) throws JSONException, MongoException {
		return find(dbName, collectionName, input, null, fields, -1, -1);
	}
	
	public JSONObject find(String dbName, String collectionName, JSONObject input, JSONObject orderBy, int skip, int limit) throws JSONException, MongoException {
		return find(dbName, collectionName, input, orderBy, null, skip, limit);
	}
	
	public JSONObject findFirst(String dbName, String collectionName, JSONObject input, JSONObject orderBy) throws JSONException, MongoException {
		JSONArray rslt = find(dbName, collectionName, input, orderBy, null, -1, -1).getJSONArray(_QUERYRESULT_);
		if(rslt.length() > 0) {
			return rslt.getJSONObject(0);
		} else {
			return null;
		}
	}
	
    /* ===============================================================
	 * Purpose:		
	 * Parameter:	orderBy: Mongo command: sort(), null for none
	 * 				skip,limit: Mongo command: .skip(), .limit(). -1 for none
	 * Return:
	 * Remark:		only key(String)/value(String)
	 * Author:		welson
	 * =============================================================== */	
	
	public JSONObject find(
			String dbName, 
			String collectionName, 
			BasicDBObject query, 
			JSONObject orderBy, 
			JSONObject fields, 
			int skip, 
			int limit) throws JSONException, MongoException {
		JSONObject output = new JSONObject();
		JSONArray array = new JSONArray();
		int totalCount = 0;
		DBCursor cursor = null;
		
		try {
			m = safeCreateMongo();
			db = m.getDB(dbName);
			coll = db.getCollection(collectionName);	

//			BasicDBObject query = (BasicDBObject) JSON.parse(input.toString()); //new BasicDBObject();
			BasicDBObject keys = (fields==null) ? new BasicDBObject() : (BasicDBObject) JSON.parse(fields.toString());
			if(orderBy == null)
				cursor = coll.find(query, keys);
			else {
				BasicDBObject sort = (BasicDBObject) JSON.parse(orderBy.toString());
				cursor = coll.find(query, keys).sort(sort);
			}
			
			totalCount = cursor.count();
			
			// range
			if(skip >= 0 && limit >= 0)
				cursor.skip(skip).limit(limit);
			else if(skip >= 0 && limit < 0)
				cursor.skip(skip);
			else if(skip < 0 && limit >= 0)
				cursor.limit(limit);
			
			while (cursor.hasNext()) {
				BasicDBObject bo = (BasicDBObject) cursor.next();
				JSONObject json = new JSONObject(bo.toString());
				
				/*
				// binding query result data
				Set<String> keySet = bo.keySet();
				Iterator iter = keySet.iterator();
				
				while (iter.hasNext()) {
					String key = (String) iter.next();
					json.put(key, bo.getString(key)); // only string
				}*/
				array.put(json);
			} 
			

			output.put(_QUERYRESULT_, array);
			output.put("totalCount", totalCount); // total count without skip/limit
			output.put("returnCount", array.length());
		} catch(UnknownHostException e) {
			e.printStackTrace();
		} finally {
			if(cursor != null) {
				cursor.close();
				cursor = null;
			}
			internalClose();
		}
		
		return output;
	}
	
	public JSONObject find(
							String dbName, 
							String collectionName, 
							JSONObject input, 
							JSONObject orderBy, 
							JSONObject fields, 
							int skip, 
							int limit) throws JSONException, MongoException {
		JSONObject output = new JSONObject();
		JSONArray array = new JSONArray();
		int totalCount = 0;
		DBCursor cursor = null;
		
		try {
			m = safeCreateMongo();
			db = m.getDB(dbName);
			coll = db.getCollection(collectionName);	

			BasicDBObject query = (BasicDBObject) JSON.parse(input.toString()); //new BasicDBObject();
			BasicDBObject keys = (fields==null) ? new BasicDBObject() : (BasicDBObject) JSON.parse(fields.toString());
			if(orderBy == null)
				cursor = coll.find(query, keys);
			else {
				BasicDBObject sort = (BasicDBObject) JSON.parse(orderBy.toString());
				cursor = coll.find(query, keys).sort(sort);
			}
			
			totalCount = cursor.count();
			
			// range
			if(skip >= 0 && limit >= 0)
				cursor.skip(skip).limit(limit);
			else if(skip >= 0 && limit < 0)
				cursor.skip(skip);
			else if(skip < 0 && limit >= 0)
				cursor.limit(limit);
			
			while (cursor.hasNext()) {
				BasicDBObject bo = (BasicDBObject) cursor.next();
				JSONObject json = new JSONObject(bo.toString());
				
				/*
				// binding query result data
				Set<String> keySet = bo.keySet();
				Iterator iter = keySet.iterator();
				
				while (iter.hasNext()) {
					String key = (String) iter.next();
					json.put(key, bo.getString(key)); // only string
				}*/
				array.put(json);
			} 
			

			output.put(_QUERYRESULT_, array);
			output.put("totalCount", totalCount); // total count without skip/limit
			output.put("returnCount", array.length());
		} catch(UnknownHostException e) {
			e.printStackTrace();
		} finally {
			if(cursor != null) {
				cursor.close();
				cursor = null;
			}
			internalClose();
		}
		
		return output;
	}
	
	public long findLength(String dbName, String collectionName, JSONObject input) throws JSONException, MongoException {
		int totalCount = 0;
		DBCursor cursor = null;
		try {
			m = safeCreateMongo();
			db = m.getDB(dbName);
			coll = db.getCollection(collectionName);	

			BasicDBObject query = (BasicDBObject) JSON.parse(input.toString()); //new BasicDBObject();
			cursor = coll.find(query);
			totalCount = cursor.count();
		} catch(UnknownHostException e) {
			e.printStackTrace();
		} finally {
			if(cursor != null) {
				cursor.close();
				cursor = null;
			}
			internalClose();
		}
		return totalCount;
	}
	
	public JSONObject insert(String dbName, String collectionName, JSONObject input) throws JSONException, MongoException {
		JSONObject rslt = null;
		try{
			BasicDBObject bo = toBasicDBObject(input);
			
			// override mongo ObjectId
			if(input.has("_id")) {
				bo.put("_id", new ObjectId(input.getString("_id")));
			}
		
			m = safeCreateMongo();
			db = m.getDB(dbName);
			coll = db.getCollection(collectionName);	
			WriteResult insertRslt = coll.insert(bo);
			String mongoId = ((ObjectId) bo.get("_id")).toString();
			
			rslt = new JSONObject();
			rslt.put("_id", mongoId);
			rslt.put("bookId", mongoId);
			rslt.put("data", insertRslt.toString());
			rslt.put("statuscode", ServiceConstant.SUCCESS);
		} catch(UnknownHostException e) {
			e.printStackTrace();
			rslt = new JSONObject();
			rslt.put("statuscode", ServiceConstant.FAIL);
		} finally {
			internalClose();
		}
		
		return rslt;
	}
	
    /* ===============================================================
	 * Purpose:		
	 * Parameter:	operation: "$set" for partial update, "$inc" for increate a count, ...
	 * 						   null for totally update a doc 
	 * Return:
	 * Remark:		only key(String)/value(String)
	 * Author:		welson
	 * =============================================================== */	
	public JSONObject update(String dbName, String collectionName, String operation,
	           JSONObject queryJSON, JSONObject setJSON, boolean upsert, boolean multi) throws JSONException, MongoException {

		JSONObject output = new JSONObject();
		int n = 0;
		
		try {
			m = safeCreateMongo();
			db = m.getDB(dbName);
			coll = db.getCollection(collectionName);	
			
			// query
			BasicDBObject query = toBasicDBObject(queryJSON);
			
			// set
			BasicDBObject setValue = new BasicDBObject();
			if(operation != null) {
				setValue = new BasicDBObject();
				BasicDBObject setContent = toBasicDBObject(setJSON);
				setValue.put(operation, setContent);
			} else {
				setValue = toBasicDBObject(setJSON);
			}
		
			WriteResult rslt = coll.update(query, setValue, upsert, multi);
			n = rslt.getN(); // WriteResult will destroy after close connection
			
			output.put("resultCode", ServiceConstant.SUCCESS);
		} catch(UnknownHostException e) {
			e.printStackTrace();
			output.put("resultCode", ServiceConstant.FAIL);
		} finally {
			internalClose();
		}
		
		output.put("n", n); // number of documents affected
		return output;
	}	
	public JSONObject update(String dbName, String collectionName, String operation,
			           JSONObject queryJSON, JSONObject setJSON) throws JSONException, MongoException {
		return update(dbName, collectionName, operation, queryJSON, setJSON, false, false);
	}
	public JSONObject updateById(String dbName, String collectionName, String _id, JSONObject _setter, boolean upsert) throws JSONException, MongoException {
		JSONObject output = new JSONObject();
		int n = 0;
		
		try {
			m = safeCreateMongo();
			db = m.getDB(dbName);
			coll = db.getCollection(collectionName);	
			
			// query
			BasicDBObject query = new BasicDBObject("_id", new ObjectId(_id));
			
			// setter
			BasicDBObject setter = toBasicDBObject(_setter);
		
			WriteResult rslt = coll.update(query, setter, upsert, false);
			n = rslt.getN(); // WriteResult will destroy after close connection
			
			output.put("resultCode", ServiceConstant.SUCCESS);
		} catch(UnknownHostException e) {
			e.printStackTrace();
			output.put("resultCode", ServiceConstant.FAIL);
		} finally {
			internalClose();
		}
		
		output.put("n", n); // number of documents affected
		return output;
	}
	
	public JSONObject delete(String dbName, String collectionName, JSONObject input) throws JSONException, MongoException {
		JSONObject rslt = null;
		WriteResult rmRslt = null;
		
		try {
			rslt = new JSONObject();
			m = safeCreateMongo();
			db = m.getDB(dbName);
			coll = db.getCollection(collectionName);	
	
			BasicDBObject bo = (BasicDBObject) JSON.parse(input.toString());
			rmRslt = coll.remove(bo);
			rslt.put("resultCode", ServiceConstant.SUCCESS);
			rslt.put("statuscode", ServiceConstant.SUCCESS);
		} catch (Exception e) {
			rslt.put("resultCode", ServiceConstant.TRANSACTION_FAIL);
			rslt.put("statuscode", ServiceConstant.TRANSACTION_FAIL);
			rslt.put("message", rmRslt.getError());
		} finally {
			internalClose();
		}
		
		return rslt;
	}
	
	public boolean deleteById(String dbName, String collectionName, String _id) throws JSONException, MongoException {
		boolean rslt = false;
		try {
			m = safeCreateMongo();
			db = m.getDB(dbName);
			coll = db.getCollection(collectionName);	
			DBObject searchById = new BasicDBObject("_id", new ObjectId(_id));
			WriteResult r = coll.remove(searchById);
			
			rslt = (r!=null) ? true : false;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} finally {
			internalClose();
		}
		return rslt;
	}
	
    /* ===============================================================
	 * Purpose:		
	 * Parameter:	_fields: fields to group by
	 * 				_cond: filter condition (ex: xxx:{$gt:0})
	 * 				_initial: initial value of the aggregation result (ex: {total:0})
	 * 				_reduce: specifies an $reduce Javascript function
	 * Return:
	 * Remark:		
	 * Author:		welson
	 * =============================================================== */
	public JSONArray group(String dbName, 
							String collectionName, 
							JSONObject _fields, 
							JSONObject _cond,
							JSONObject _initial,
							String _reduce) throws JSONException, MongoException {
		JSONArray ret = null;
		try {
			m = safeCreateMongo();
			db = m.getDB(dbName);
			coll = db.getCollection(collectionName);
			
			DBObject key = (DBObject) JSON.parse(_fields.toString());
			DBObject cond = (DBObject) JSON.parse(_cond.toString());
			DBObject initial = (DBObject) JSON.parse(_initial.toString());
			String reduce = (_reduce!=null && _reduce.length()>0) ? _reduce : "function(curr, result){}";
			DBObject rslt = coll.group(key, cond, initial, reduce);
			ret = (rslt!=null) ? new JSONArray(rslt.toString()) : new JSONArray();
		} catch(UnknownHostException e) {
			e.printStackTrace();
		} finally {
			internalClose();
		}
		return ret;
	}
	
	public JSONObject aggregate(String dbName, String collectionName, List<JSONObject> _pipeline) throws JSONException, MongoException{
		JSONObject output = new JSONObject();
		JSONArray array = new JSONArray();
		Cursor cursor = null;
		try {
			m = safeCreateMongo();
			db = m.getDB(dbName);
			coll = db.getCollection(collectionName);
	
			List<DBObject> pipeline = new ArrayList<DBObject>();
			for(JSONObject j : _pipeline) {
				DBObject query = (DBObject) JSON.parse(j.toString()); //new BasicDBObject();
				pipeline.add(query);
			}
			
			AggregationOptions aggregationOptions = AggregationOptions.builder()
			        .batchSize(100)
			        .outputMode(AggregationOptions.OutputMode.CURSOR)
			        .allowDiskUse(true)
			        .build();
			cursor = coll.aggregate(pipeline, aggregationOptions);
			
			while(cursor.hasNext()) {
				BasicDBObject bo = (BasicDBObject) cursor.next();
				JSONObject json = new JSONObject(bo.toString());
				array.put(json);
			}
			output.put(_QUERYRESULT_, array);
		
			/*
			BasicDBObject match = new BasicDBObject("$match", new BasicDBObject("status", 1));
			BasicDBObject group = new BasicDBObject(
			    "$group", new BasicDBObject("_id", null).append(
			        "total", new BasicDBObject( "$sum", "$key1" )
			    )
			);
			DBCollection collection = db.getCollection(collectionName);
			AggregationOutput output = collection.aggregate(match, group);
			*/
		} catch(UnknownHostException e) {
			e.printStackTrace();
		} finally {
			if(cursor != null) {
				cursor.close();
				cursor = null;
			}
			internalClose();
		}
		
		return output;
	}
	
	public void close() {
		if(m != null) {
			m.close();
			m = null;
		}
	}
	
	public void internalClose() {
		if(autoCloseConnection  &&  m != null) {
			m.close();
			m = null;
		}
	}
	
	private MongoClient safeCreateMongo() throws UnknownHostException {
		if(m == null) {
			m = createMongoClient();
		}
		return m;
	}
	
	protected BasicDBObject toBasicDBObject(JSONObject queryJSON) throws JSONException {
		DBObject bo = (DBObject) JSON.parse(queryJSON.toString());
		return (BasicDBObject) bo;
	}
	
	public MongoClientOptions getOption() {
		return mongoClientOptions;
	}
}
