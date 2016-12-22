package org.lazyjava.utility;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class NetworkUtil {
	private static final String IP_PATTERN = "\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b";
	private static final String VIRTUAL_IP_PATTERN = "(10|192\\.168|172\\.(1[6-9]|2[0-9]|3[01])).*";
	private static final String sep = ":";
	
	private static JSONObject interfaceJSON = null;
	
    private String getRandomKey() {
    	String keycode = UUID.randomUUID().toString().split("-")[0];
    	return keycode;
    }    
    
    public static JSONObject getInterfaces() throws JSONException, SocketException, UnsupportedEncodingException, UnknownHostException {
    	JSONObject outputJSON = new JSONObject();
    	JSONArray outputArray = new JSONArray();
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        
        while (interfaces.hasMoreElements()) {
            NetworkInterface ni = interfaces.nextElement();
            if (ni != null) {
            	JSONObject json = new JSONObject();
            	boolean leagalIPFound = false;
            	String hostname = "";
            	String ip = "";
            	
            	// trace IP
            	Enumeration<InetAddress> inetAddressList = ni.getInetAddresses();
            	while (inetAddressList.hasMoreElements()) {
            		leagalIPFound = false;
            		
            		InetAddress inetAddress = inetAddressList.nextElement();
            		ip = inetAddress.getHostAddress();
            		
            		if(isLegalIP(ip)) {
            			leagalIPFound = true;
            			hostname = inetAddress.getHostName();
            			System.out.printf("[hostAddr=%s][hostname=%s]\n", ip, hostname);

            			json.put("hostname", hostname);
            			json.put("ip", ip);
            			break; // break if find a reasonable IP
            		}
            	}
            	if(!leagalIPFound) {
            		// skip trace MAC if no host/ip information
            		continue;
            	}
            	
            	
            	// trace MAC
                byte[] bMac = ni.getHardwareAddress();
                if (bMac != null) {
                	String mac = toMac(bMac, ":");
					System.out.printf("[mac=%s]\n", mac);
					json.put("mac", mac);
                }
                
                System.out.println("------------------------------------------------");
                if(json.length() > 0) { // add if numbers of key of json > 0
                	outputArray.put(json);
                }
            }
        }
        outputJSON.put("interfaces", outputArray);
        interfaceJSON = outputJSON;
        
        return outputJSON;
    }

	public static String getIP() throws UnknownHostException {
		return getLocalIP();
	}
	
    public static String getMAC() throws SocketException, UnknownHostException {
    	return getLocalMAC(":");
    }
    
    public static String getLocalIP() throws UnknownHostException {
    	InetAddress ip = InetAddress.getLocalHost();
    	return ip.getHostAddress();
    }

    public static String getLocalMAC(String separator) throws SocketException, UnknownHostException {
        InetAddress ip = InetAddress.getLocalHost();
		NetworkInterface network = NetworkInterface.getByInetAddress(ip);
		byte[] mac = network.getHardwareAddress();
		return toMac(mac, separator);
	}
	
    public static String getLinuxExternalIP() {
    	String ip = "";
    	String firstLegalIP = "";
    	String ret = "";
		int i = 0;
		
		while(i <= 10) {
			ip = getEthNIP(i);
			System.out.printf("[getLinuxExternalIP()] Parse eth%d IP = %s\n", i, ip);
			if(isLegalIP(ip)  &&  !isVirutalIP(ip)) {
				ret = ip;
				break;
			}
			
			if(firstLegalIP.equals("")  &&  isLegalIP(ip)) {
				// return any legal IP if external IP not found
				// (maybe some env. is LAN)
				firstLegalIP = ip; 
				ret = ip;
			}
			++i;
		}
		return ret;
    }
    
    public static String getExternalIP() throws JSONException, SocketException, UnsupportedEncodingException, UnknownHostException {
    	JSONObject interfaces = (interfaceJSON==null) ? getInterfaces() : interfaceJSON;
    	JSONArray arr = interfaces.getJSONArray("interfaces");
    	for(int i=0; i<arr.length(); ++i) {
    		JSONObject tmp = arr.getJSONObject(i);
    		String ip = tmp.getString("ip");
    		if(isVirutalIP(ip)) {
    			continue;
    		} else {
    			return ip;
    		}
    	}
    	return null;
    }
    
	public static String getClassAIP() throws JSONException, SocketException, UnsupportedEncodingException, UnknownHostException {
		JSONObject interfaces = (interfaceJSON==null) ? getInterfaces() : interfaceJSON;
		JSONArray arr = interfaces.getJSONArray("interfaces");
		for(int i=0; i<arr.length(); ++i) {
			JSONObject tmp = arr.getJSONObject(i);
			String ip = tmp.getString("ip");
			if(ip.startsWith("10.")) {
				return ip;
			} else {
				continue;
			}
		}
		return null;
	}
	
	public static String getExternalMAC() throws JSONException, SocketException, UnsupportedEncodingException, UnknownHostException {
		JSONObject interfaces = (interfaceJSON==null) ? getInterfaces() : interfaceJSON;
		JSONArray arr = interfaces.getJSONArray("interfaces");
		for(int i=0; i<arr.length(); ++i) {
			JSONObject tmp = arr.getJSONObject(i);
			String ip = tmp.getString("ip");
			if(isVirutalIP(ip)) {
				continue;
			} else {
				return tmp.getString("mac");
			}
		}
		return null;
	}
    
    //===================================================================
    // Purpose: 	is any ip
    // Parameters: 	
    // Return:
    // Remark:		http://www.regular-expressions.info/examples.html
    // Author:		welson
    //===================================================================
    public static boolean isLegalIP(String ip) {
        if(ip.matches(IP_PATTERN))
            return true;
        else
            return false;
    }

    //===================================================================
    // Purpose: 	judge virtual ip
    // Parameters: 	
    // Return:
    // Remark:		預留給內部區網用的虛擬IP分為：
    //				class A: 10.0.0.0 – 10.255.255.255
    //				class B: 172.16.0.0 – 172.31.255.255
    //				class C: 192.168.0.0 – 192.168.255.255
    // Author:		welson
    //===================================================================
    public static boolean isVirutalIP(String ip) {
    	if(ip.startsWith("127.") || ip.startsWith("192.168.") || ip.startsWith("10.")) {
    		return true;
    	} else if(ip.startsWith("172.")) {
    		String[] elems = ip.split(".");
    		int number2 = Integer.valueOf(elems[1]);
			return (number2 >= 16 && number2 <= 31);
    	} else {
    		return false;
    	}
    }
    
    public static String getEthNIP(int n) {
    	if(n < 0) {
    		n = 0;
    	}
    	
    	String ip = "";
    	String ethN = String.format("eth%d", n);
    	String[] cmd =  new String[]{
			"/bin/sh", 
			"-c", 
			String.format("ifconfig %s | grep 'inet addr' | awk '{print $2}' | cut -d':' -f2", ethN)
		};
		
		List<String> rslt = ProcessExecutor.exec(cmd);
		if(rslt != null  &&  rslt.size() > 0) {
			ip = rslt.get(0);
		}
		return ip;
    }

    //------------------------------------------------------------------------------------------//
	private static String toMac(byte[] mac, String sep) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < mac.length; i++) {
			sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? sep : ""));		
		}
		return sb.toString();
	}
}
