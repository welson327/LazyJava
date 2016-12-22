package org.lazyjava.utility;

import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonUtil {

	public static JSONObject merge(JSONObject j1, JSONObject j2) throws JSONException {
		if(j2 == null)
			return j1;
		if(j1 == null)
			return new JSONObject(j2);
		
		JSONObject output = jsondup(j1);
		Iterator iter = j2.keys();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			output.put(key, j2.get(key));
		}
		return output;
	}
	
	public static JSONObject jsondup(JSONObject input) throws JSONException {
		JSONObject clone = null;    	
		if(input != null) {
			clone = new JSONObject(input.toString());
		}
		return clone;
	}
	
	public static boolean isJSONString(String s) {
        if(s != null) {
        	return s.startsWith("{") && s.endsWith("}");
        } else {
        	return false;
        }
    }
	
	public static JSONArray concatJSONArray(JSONArray a1, JSONArray a2) throws JSONException {
		JSONArray ret = new JSONArray();
		for(int i=0; i<a1.length(); ++i) {
			ret.put(a1.get(i));
		}
		for(int i=0; i<a2.length(); ++i) {
			ret.put(a2.get(i));
		}
		return ret;
	}
 }
