package org.lazyjava.auth;

import java.util.Date;

import org.json.JSONObject;
import org.lazyjava.common.ServiceConstant;
import org.lazyjava.common.ServiceException;

public class TokenChecker {
	private static TokenProvider tokeProvider = new TokenProvider();
	
	public static JSONObject checkToken(String accessToken) throws ServiceException {
		try {
			if(accessToken == null) {
				throw new ServiceException(ServiceConstant.TOKEN_IS_NULL);
			} else {
				JSONObject tokenInfo = tokeProvider.parseToken(accessToken);
				int parseResult = tokenInfo.getInt("parseResult");
				//String account = tokenInfo.getString("account");
				//String imUuid = tokenInfo.getString("imUuid");
				if(parseResult == ServiceConstant.SUCCESS) {
					return tokenInfo;
				} else {
					throw new ServiceException(ServiceConstant.TOKEN_MISMATCH);
				}
			}
		} catch (ServiceException e) {
			throw new ServiceException(e.getCode(), e.getMessage());
		} catch (Exception e) {
			throw new ServiceException(ServiceConstant.TOKEN_MISMATCH, "unknown error!");
		}
	}
	
	public static JSONObject checkTokenWithExpiry(String accessToken, long expiryInSeconds) throws ServiceException {
		try {
    		JSONObject tokenInfo = checkToken(accessToken);
    		int parseResult = tokenInfo.getInt("parseResult");
    		if(parseResult == ServiceConstant.SUCCESS) {
				Long ts = tokenInfo.getLong("timestamp");
				if(isExpired(ts, expiryInSeconds)) {
					throw new ServiceException(ServiceConstant.TOKEN_EXPIRED);
				} 
    			return tokenInfo;
    		} else
    			throw new ServiceException(ServiceConstant.TOKEN_MISMATCH);
		} catch (Exception e) {
			throw new ServiceException(ServiceConstant.TOKEN_MISMATCH);
		}
    }
	
    private static boolean isExpired(long loginTime, long expiryInSeconds) {
    	long curr = new Date().getTime();
    	return (curr > loginTime + expiryInSeconds*1000);
    }
}
