package org.lazyjava.auth;

import java.util.Date;
import java.util.HashMap;

import org.apache.commons.lang.RandomStringUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class OneTimeTicket {
	public static final int SUCCESS = 0;
	public static final int FAIL 	= 1;
	public static final int EXPIRED = 2;
	
	private static HashMap<String, JSONObject> map = null;
	private static final String _TICKET_ = "ticket";
	private static final String _CREATE_TIME_ = "createTime";
	
	static {
		if(map == null) {
			map = new HashMap<String, JSONObject>();
		}
	}
	
	public String getTicket(String key, int ticketLength) throws JSONException {
		return getTicket(key, ticketLength, false);
	}
	
	public String getTicket(String key, int ticketLength, boolean isOnlyNumeric) throws JSONException {
		String ticket = generateTicket(ticketLength, isOnlyNumeric);
		JSONObject value = new JSONObject();
		value.put(_TICKET_, ticket);
		value.put(_CREATE_TIME_, new Date().getTime());
		map.put(key, value);
		return ticket;
	}
	
	//============================================================
	// Purpose:		check if ticket if leagal, and drop it after checking
	// Parameters: 	ticket: check if the ticket is own of key
	// Return:		0 if success
	// Remark:	
	// Author:		welson
	//============================================================
	public int checkTicket(String key, String ticketToCheck, int expiryInSecond) throws JSONException {
		int rslt = FAIL;
		if(map.containsKey(key)) {
			JSONObject oneTimeObj = map.get(key);
			String ticket = oneTimeObj.getString(_TICKET_);
			if(ticket.equals(ticketToCheck)) {
				if(expiryInSecond >= 0) {
					long createTime = oneTimeObj.getLong(_CREATE_TIME_);
					long now = new Date().getTime();
					if(now > createTime + expiryInSecond*1000) {
						rslt = EXPIRED;
					} else {
						rslt = SUCCESS;
					}
				} else {
					rslt = SUCCESS;
				}
				map.remove(key);
			} else {
				rslt = FAIL;
			}
		} else {
			rslt = FAIL;
		}
		return rslt;
	}
	
	public JSONObject report() throws JSONException {
		JSONObject report = new JSONObject();
		report.put("numberOfRecords", map.size());
		return report;
	}
	
	private String generateTicket(int count, boolean isOnlyNumeric) {
		if(isOnlyNumeric) {
			return RandomStringUtils.randomNumeric(count);
		} else {
			return RandomStringUtils.randomAlphanumeric(count).toUpperCase();
		}
	}
}

//class OneTimeTicketBean {
//	private String key = null;
//	private long createTime = -1;
//	private String value = null;
//	private int expiryInSeconds = -1;
//	
//	public OneTimeTicketBean(String key, int expiry) {
//		this.key = key;
//		this.expiryInSeconds = expiry;
//		this.createTime = new Date().getTime();
//	}
//	public String getValue() {
//		return value;
//	}
//}