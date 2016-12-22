package org.lazyjava.account;

import org.json.JSONException;
import org.json.JSONObject;

public class AccountBean {
	public String uid = "";
	public String account = "";	
	public long createTime = 0;

	public AccountBean() {
	}

	public AccountBean(JSONObject input) throws JSONException {
		this.account = input.has("account") ? input.getString("account") : "";
		this.createTime = input.has("createTime") ? input.getLong("createTime") : 0;
		this.uid = input.has("uid") ? input.getString("uid") : "";
	}

	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("uid", uid);
		json.put("account", account);
		json.put("createTime", createTime);
		return json;
	}
}
