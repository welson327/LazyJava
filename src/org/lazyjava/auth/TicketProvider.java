package org.lazyjava.auth;

import java.util.Date;

import org.json.JSONObject;
import org.lazyjava.common.ServiceConstant;
import org.lazyjava.common.ServiceException;
import org.lazyjava.utility.TEA;

public class TicketProvider {
	private static final String DES_SYMM_KEY = "forgetpassword-WKMFRPWB-9T2487JK-MRX3F47B-2014-yIAbi";
	private static final String CHECK_STR = "juSvVPIfxjO246Ks";
	private static final int CHECK_CODE = 2970;
	//private DESProvider des = null;
	private TEA tea = null;
	
	public TicketProvider() {
		//des = new DESProvider();
		//des.setKey(DES_SYMM_KEY);
		
		tea = new TEA(DES_SYMM_KEY.getBytes());
	}
	
	public String getTicket(String account) throws Exception {
		JSONObject info = new JSONObject();
		info.put("email", account);
		info.put("len", account.length());
		info.put("checkCode", CHECK_CODE);
		info.put("checkStr", CHECK_STR);
		info.put("requestTime", new Date().getTime());
		return tea.encrypt2HexString(info.toString());
	}
	
	public JSONObject parseTicket(String ticket, int expiryInMinutes) throws ServiceException {
		try {
			String dec = tea.decryptHex(ticket);
			JSONObject json = new JSONObject(dec);
			String email = json.getString("email");
			String checkStr = json.getString("checkStr");
			int checkCode = json.getInt("checkCode");
			long requestTime = json.getLong("requestTime");
			long currTime = new Date().getTime();

			// check expiry
			if((expiryInMinutes > 0)  &&  (currTime > requestTime + expiryInMinutes*60*1000)) {
				System.out.printf("[parseTicket] checkCode err! (%s: %d->%d)", email, requestTime, currTime);
				throw new ServiceException(ServiceConstant.TOKEN_EXPIRED);
			}
			
			// check data
			if(email.length() != json.getInt("len")) {
				System.out.printf("[parseTicket] email length err! (%s)", email);
				throw new ServiceException(ServiceConstant.TOKEN_MISMATCH);
			}
			if(!checkStr.equals(CHECK_STR)) {
				System.out.printf("[parseTicket] checkStr err! (%s)", email);
				throw new ServiceException(ServiceConstant.TOKEN_MISMATCH);
			} 
			if(checkCode != CHECK_CODE) {
				System.out.printf("[parseTicket] checkCode err! (%s)", email);
				throw new ServiceException(ServiceConstant.TOKEN_MISMATCH);
			} 
			
			json.put("parseResult", ServiceConstant.SUCCESS);
			return json;
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(ServiceConstant.TOKEN_MISMATCH);
		}
	}
}
