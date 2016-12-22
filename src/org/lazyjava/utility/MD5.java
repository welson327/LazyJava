package org.lazyjava.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;

public class MD5 {

	public static String hash(String str, String separator, String algorithm){
		try {
			byte[] bytesToDigest = str.getBytes("utf-8");
			MessageDigest messageDigest = MessageDigest.getInstance(algorithm); // "MD5", "SHA-256", "WHIRLPOOL"
			messageDigest.update(bytesToDigest);
			byte[] digest = messageDigest.digest();
			
			return bytesToHex(digest, separator);
			
		} catch (Exception e) {
			//e.printStackTrace();
			return null;
		}
	}
	
	public static String hashByMD5(String str, String separator){
		return hash(str, separator, "MD5");
	}
	
	// byte array to hex
    public static String bytesToHex(byte[] b, String separator) {
        StringBuffer sb = new StringBuffer();
        String stmp = "";
        
        if(separator == null)
        	separator = ""; 
        
        for (int n = 0; n < b.length; n++) {
            stmp = (Integer.toHexString(b[n] & 0XFF));
            if (stmp.length() == 1) {
                sb.append("0").append(stmp);
            } else {
                sb.append(stmp);
            }
            if (n < b.length - 1) {
                sb.append(separator);
            }
        }
        return sb.toString().toUpperCase();
    }
    
    /**
	 * Get MD5 code of target file
	 * @param path
	 * @return
	 * @throws IOException
	 * @author Wayne
	 */
	public static String getMd5OfFile (String path) throws IOException {
		File file = new File(path);
		if( !file.exists() )
			return  null;
		
		Process execProcess = Runtime.getRuntime().exec("md5sum " + path);
		InputStreamReader runtimeStream = new InputStreamReader(execProcess.getInputStream());
		BufferedReader br = new BufferedReader(runtimeStream);
		String line = br.readLine();
		br.close();
		runtimeStream.close();
		execProcess.destroy();
		if(line != null) {
			String md5 = line.split(" ")[0];
			//this.cafeLog.postLog("Hotfix.Check.MD5", cafeLevel.DEBUG, "Check MD5: ");
			//this.cafeLog.postLog("Hotfix.Check.MD5", cafeLevel.DEBUG, path);
			//this.cafeLog.postLog("Hotfix.Check.MD5", cafeLevel.DEBUG, line.split(" ")[0]);
			return md5;
		}
		else
			return null;
	}
}
