package org.lazyjava.utility;

public class BitUtil {
	public static String toBinary(long value) {
		return Long.toBinaryString(value);
	}
	
	public static long toValue(String binaryStr) throws NumberFormatException {
		return Long.valueOf(binaryStr, 2);
	}
}
