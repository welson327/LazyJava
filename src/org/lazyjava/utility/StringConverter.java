package org.lazyjava.utility;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

import org.apache.commons.io.IOUtils;

public class StringConverter {

	public static String byte2Hex(byte[] b) {
		StringBuilder result = new StringBuilder();
		  for (int i=0; i < b.length; i++)
		    result.append(Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 ));
		  return result.toString();
	}
	
	public static String string2Hex(String plainText, String charset) throws UnsupportedEncodingException {
		  return String.format("%040x", new BigInteger(1, plainText.getBytes(charset)));
	}
	
	public static byte[] hex2Byte(String hexString) {
		byte[] bytes = new byte[hexString.length() / 2];
		for (int i = 0; i < bytes.length; i++)
			bytes[i] = (byte) Integer.parseInt(hexString.substring(2 * i, 2 * i + 2), 16);
		return bytes;
	}
	
	public static String hex2String(String hexString) throws NumberFormatException {
	    StringBuilder result = new StringBuilder();
	    for (int i=0 ; i<hexString.length() ; i+=2)
	    	result.append((char) Integer.parseInt(hexString.substring(i, i + 2), 16));
	    return result.toString();
	}
	
	//=========================================================================
	// Purpose:		InputStream to string
	// Parameters:
	// Return:
	// Remark:		in order to be independent of other class
	//				http://stackoverflow.com/questions/309424/read-convert-an-inputstream-to-a-string
	// Author:	
	//=========================================================================
	public static String stream2String(InputStream is) throws IOException {
		String encoding = "UTF-8";
    	return IOUtils.toString(is, encoding);
		/*
		StringWriter writer = new StringWriter();
		IOUtils.copy(is, writer, encoding);
		return writer.toString();    
		*/
		
		
    	
    	/*
		BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
	    StringBuilder sb = new StringBuilder();
	    String line = null;
    	while ((line = reader.readLine()) != null){
    		sb.append(line);
		}
    	return sb.toString();
    	*/
		
		
		
		
		/*
			//
	        // To convert the InputStream to String we use the
	        // Reader.read(char[] buffer) method. We iterate until the
	        // Reader return -1 which means there's no more data to
	        // read. We use the StringWriter class to produce the string.
	        //
	        if (is != null) {
	            Writer writer = new StringWriter();
	 
	            char[] buffer = new char[1024];
	            try {
	                Reader reader = new BufferedReader(
	                        new InputStreamReader(is, "UTF-8"));
	                int n;
	                while ((n = reader.read(buffer)) != -1) {
	                    writer.write(buffer, 0, n);
	                }
	            } finally {
	                is.close();
	            }
	            return writer.toString();
	        } else {        
	            return "";
	        }
        */
	}
	
	public static InputStream string2stream(String s) throws UnsupportedEncodingException {
		InputStream is = new ByteArrayInputStream(s.getBytes("UTF-8"));
		return is;
	}
}
