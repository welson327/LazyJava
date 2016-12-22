package org.lazyjava.auth;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;
import org.lazyjava.account.AccountBean;
import org.lazyjava.common.ServiceConstant;
import org.lazyjava.utility.AESProvider;

public class TokenProvider {
	protected static final String _ACCOUNT_ = "account";
	protected static final String _CHECKCODE_ = "checkcode";
	protected static final String _UID_ = "uid";
	protected static final String _TIMESTAMP_ = "timestamp";
	
//	private static final String _LEVEL_ = "level";
	
	private static final String aesKey = "PA2R-R0OT-1IJ4SH"; // must 16 bytes
	
	protected AESProvider aes = null;
	
	public TokenProvider() {
		aes = new AESProvider();
		aes.setKey(aesKey);
	}
	
	public String getToken(AccountBean b) throws Exception {
		long ts = new Date().getTime();
		String account = b.account;
		String uid = b.uid;
		String plainText = String.format("%s:%d:%s:%d", account, getCheckCode(account), uid, ts);
		return aes.encrypt2HexString(plainText);
	}
	
//	private String getInfo(AccountBean b) {
//		boolean isAuthenticated = true;
//		return String.format("%b,0", isAuthenticated);
//	}
	
	public JSONObject parseToken(String token) throws JSONException {
		String[] keys = {_ACCOUNT_, _CHECKCODE_, _UID_, _TIMESTAMP_};
		JSONObject json = new JSONObject();
		String account = null;
		
		try {
			String info = aes.decryptHex(token);
			String[] values = info.split(":");
			for(int i=0; i<keys.length; ++i) {
				if(values != null) {
					String key = keys[i];
					if(key.equals(_ACCOUNT_)) {
						json.put(keys[i], values[i]);
						account = values[i];
					} else if(key.equals(_CHECKCODE_)) {
						if(getCheckCode(account) != Integer.valueOf(values[i])) {
							throw new Exception("CheckCode mismatch!");
						}
					} else if(key.equals(_UID_)) {
						json.put(keys[i], values[i]);
					} else if(key.equals(_TIMESTAMP_)) {
						json.put(keys[i], Long.valueOf(values[i]));
					} else {	
						json.put(keys[i], values[i]);
					}
					//System.out.println(">>> debug : key="+keys[i] + ", value="+values[i]);
				} else {
					json.put(keys[i], "");
				}
			}
			json.put("parseResult", (values!=null) ? ServiceConstant.SUCCESS : ServiceConstant.FAIL);
		} catch (Exception e) {
			//e.printStackTrace();
			for(int i=0; i<keys.length; ++i) {
				json.put(keys[i], "");
			}
			json.put("parseResult", ServiceConstant.FAIL);
			json.put("errMsg", e.getMessage());
		}
		
		return json;
	}
	
	private int getCheckCode(String account) {
		int c0Value = 0;
		int len = 0;
		if(account != null  &&  account.length()>0) {			
			c0Value = (int) Character.valueOf(account.charAt(0));
			len = account.length(); 
		}
		return len * c0Value + 967;
	}
	
//	public void test(String account) {
//		try {
//			String token = getToken(account);
//			System.out.printf("account=%s, token=%s\n", account, token);
//			System.out.printf("parseToken=%s\n\n", parseToken(token));
//		} catch(Exception e) {
//			e.printStackTrace();
//		}
//	}
}
