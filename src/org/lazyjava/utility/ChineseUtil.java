package org.lazyjava.utility;

import java.util.regex.Pattern;

public class ChineseUtil {
	private static final int MAX = 100;
	
	public static boolean containsChinese(String s) {
		String pattern = "\\p{InCJKUnifiedIdeographs}{1,}"; 
		return Pattern.compile(pattern).matcher(s).find();
		//return word.matches(pattern);
	}
	public static boolean isTraditional(String word) {
		return ZHConverter.getInstance(ZHConverter.TRADITIONAL).contains(word);
	}
	public static boolean isSimplified(String word) {
		return ZHConverter.getInstance(ZHConverter.SIMPLIFIED).contains(word);
	}
	public static boolean isTraditionalArticle(String s) {
		int len = s.length() > MAX ? MAX : s.length();
		StringBuilder sb = new StringBuilder(s.substring(0, len));
		String ch;
		int iCHT = 0, iCHS = 0;
		
		for(int i=0; i<len; ++i) {
			ch = sb.substring(i, i+1);
			if(containsChinese(ch)) {
				if(isTraditional(ch)) {
					++iCHT;
				} else {
					++iCHS;
				}
			}
		}
		return (iCHT >= iCHS); // include 0==0
	}
	public static boolean isSimplifiedArticle(String s) {
		return !isTraditionalArticle(s);
	}
}
