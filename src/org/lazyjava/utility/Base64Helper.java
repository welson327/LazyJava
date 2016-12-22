package org.lazyjava.utility;

import java.io.UnsupportedEncodingException;
import org.apache.commons.codec.binary.Base64;

public class Base64Helper {
	public static String encode(String text) throws UnsupportedEncodingException {
		byte[] b = Base64.encodeBase64(text.getBytes());
		return new String(b, "UTF-8");
	}
	
	public static String decode(String cipher) throws UnsupportedEncodingException {
		byte[] b = Base64.decodeBase64(cipher.getBytes());
		return new String(b, "UTF-8");
	}
	
}
