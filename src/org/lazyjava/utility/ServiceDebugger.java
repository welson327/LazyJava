package org.lazyjava.utility;


public class ServiceDebugger 
{
	public static void DBG(String tag, String msg) {
		System.out.printf("[%s] %s\n", tag, msg);
	}
	
	public static void printStringArray(String[] arr) {
		if(arr != null  &&  arr.length>0) {
			StringBuilder sb = new StringBuilder("");
			for(int i=0; i<arr.length-1; ++i) {
				sb.append(arr[i] + " ");
			}
			sb.append(arr[arr.length-1]);
			System.out.println("[" + sb.toString() + "]");
		}
	}
}
