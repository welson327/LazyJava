package org.lazyjava.utility;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.commons.lang.RandomStringUtils;


public class ServiceStringUtil {
	
	//---------------------------------------------------------//
	public static String escapeHtml(String s) {
		//s = StringEscapeUtils.escapeHtml(content); // damage to SEO
		s = s.replaceAll("&", "&amp;");
		s = s.replaceAll("\"", "&quot;");
		s = s.replaceAll("'", "&#39;");
		s = s.replaceAll("<", "&lt;");
		s = s.replaceAll(">", "&gt;");
		return s;
	}
	
	public static String[] parseKeyword(String keyword) {
		String[] keywords = null;
		if(keyword!=null  &&  keyword.length()>0) {
			// cht/chs
			//String keywordCht = ZHConverter.getInstance(ZHConverter.TRADITIONAL).convert(keyword);
			//String keywordChs = ZHConverter.getInstance(ZHConverter.SIMPLIFIED).convert(keyword);
			String keywordCht = ChineseConvert.translate2Traditional(keyword);
			String keywordChs = ChineseConvert.translate2Simplfied(keyword);
			if(!keywordCht.equals(keywordChs)) {
				keyword = keywordCht + " " + keywordChs;
			} 
			keywords = ServiceStringUtil.splitKeyword(keyword, true);
		}
		return keywords;
	}
	public static String[] splitKeyword(String keyword, boolean dropCollision) {
		String[] keywords = null;
		if(keyword != null) {
			// (1) trim
			// (2) replace multi-space with single space
			keyword = keyword.trim().replaceAll("\\s+", " ");
			keywords = keyword.split(" ");
			
			if(dropCollision) {
				ArrayList<String> list = new ArrayList<String>();
				for(String s : keywords) {
					if(!list.contains(s)) {
						list.add(s);
					}
				}
				keywords = list.toArray(new String[list.size()]);
			}
		}
		return keywords;
	}
	
	//---------------------------------------------------------//
	public static String reverse(String str) {
		return new StringBuilder(str).reverse().toString();
	}
	
	public static String reverse(String str, int index1, int index2) {
		char[] arr = str.toCharArray();
		char tmp = arr[index1];
		arr[index1] = arr[index2];
		arr[index2] = tmp;
		return new String(arr);
	}
	
	//---------------------------------------------------------//
	protected String encode(String str) {
		String newStr = revise(str);
		return reverse(StringConverter.byte2Hex(newStr.getBytes()));
	}
	
	protected String decode(String str) throws Exception {
		String oriStr = StringConverter.hex2String(reverse(str));
		return unrevise(oriStr);
	}
	
	//---------------------------------------------------------//
	private final int RANDOM_STR_LEN = 3;
	protected String revise(String str) {
		return RandomStringUtils.randomAlphanumeric(RANDOM_STR_LEN) + 
			   str + 
			   RandomStringUtils.randomAlphanumeric(RANDOM_STR_LEN);
	}
	
	protected String unrevise(String str) {
		if(str.length() > RANDOM_STR_LEN)
			return str.substring(RANDOM_STR_LEN, str.length()-RANDOM_STR_LEN);
		else
			return "";
	}
	
	//---------------------------------------------------------//
	public String encrypt(String str) throws UnsupportedEncodingException {
		String encodeStr = encode(str);
		return Base64Helper.encode(encodeStr);
	}
	
	public String decrypt(String cipher) throws UnsupportedEncodingException, NumberFormatException, Exception {
		String textplain = Base64Helper.decode(cipher);
		return decode(textplain);
	}
	
	//---------------------------------------------------------//
	public boolean isVisible(String s) {
		// return false if '[\t\n ]' and without any '[\w]'
		return !s.trim().isEmpty(); 
	}
	
	//---------------------------------------------------------//
	public void test(String pwd) {
		try {
			String cipher = encrypt(pwd);
			String decrypdPwd = decrypt(cipher);
			
			System.out.printf("Pwd=%s, revisedPwd=%s, cipher=%s, Decode(pwd)=%s\n", pwd, encode(pwd), cipher, decrypdPwd);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
