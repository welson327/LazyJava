package org.lazyjava.utility;

import java.util.regex.Pattern;


public class EmojiUtil {
	
	// ====================================================
	// Purpose:		
	// Parameter:
	// Return:
	// Remark:		http://stackoverflow.com/questions/24840667/what-is-the-regex-to-extract-all-the-emojis-from-a-string
	// Author: 		welson
	// ====================================================
	public static boolean containsEmoji(String str) {
		//String str = "素素雞🐔";
		//String pattern = "[\uE000-\uF8FF]|\uD83C[\uDF00-\uDFFF]|\uD83D[\uDC00-\uDDFF]";
		
		final String pattern = "[\uD83C-\uDBFF\uDC00-\uDFFF]+";
		boolean isFound = Pattern.compile(pattern).matcher(str).find();
		//System.out.printf("[%s] contains emoji? => %b\n", str, isFound);
		return isFound;
	}
}
