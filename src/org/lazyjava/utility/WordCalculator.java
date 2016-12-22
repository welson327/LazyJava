package org.lazyjava.utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class WordCalculator {
	//=======================================================================
	// Purpose:		Simple count words
	// Parameters:
	// Return:
	// Remark:		For any language: http://stackoverflow.com/questions/16637506/get-word-count-from-a-string-in-unicode-in-any-language
	// Author:		welson
	//=======================================================================
	public static int simpleCount(String paragraph) {
		int total = 0;

		String[] splits = paragraph.split("\\s");

		try {
			for(String section : splits) {
				int childWord = 0;
				int r = -1;
				
				if(section.length() == 0) {
					continue;
				}
				DBG(String.format("-----------> section=[%s]", section));
				
				Reader reader = new StringReader(section);
				Reader buffer = new BufferedReader(reader);
		        
				while ((r = buffer.read()) != -1) {
					//char ch = (char) r;
					String s = String.valueOf((char)r);
				
					if(IsPureChineseWord(s)) {
						++childWord;
						DBG(String.format("[%s]isChinese.", s));
					} else {
						DBG(String.format("[%s]", s));
					}
				}
				reader.close();
				buffer.close();
				
				if(childWord == 0) {
					++total;
				}
				
				total += childWord;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return total;
	}
	
	
	
	//=======================================================================
	// Purpose:		
	// Parameters:
	// Return:
	// Remark:		
	// Author:		JY
	//=======================================================================
	public static boolean IsPureChineseWord(String src) {
		
		  int b0,b1,b2;
		  int i,j;
		  byte[] tmpByte;
		  String tmpStr;
		  
		  try {
			  
			    for (j=0;j < src.length();j++) { 
			    	tmpStr = src.substring(j, j+1);
					tmpByte = tmpStr.getBytes("UTF-8");
		            if (tmpByte.length < 3) { // not a Chinese word
			           return false;
		            }
	                b0 = (tmpByte[0] < 0) ? tmpByte[0]+256:tmpByte[0];
	                b1 = (tmpByte[1] < 0) ? tmpByte[1]+256:tmpByte[1];
	                b2 = (tmpByte[2] < 0) ? tmpByte[2]+256:tmpByte[2];
	                //System.out.printf("UTF8--%d,%d,%d\n", b0,b1,b2);

	                i = ((b0- 228) * 4096) + ((b1- 184) * 64) + (b2- 128);
	                //System.out.printf("big5 to gb2312 lookup index: %d\n", i);
	                if ((i <= 20901) && (i >= 0)) {  
                     continue;
	                } else {
	    	           return false; 
	                }
			    }
		  } catch (Exception e) {
			    return false;
		  }
		  if (j == src.length()) {
			  return true;
		  } else {
		      return false;
		  }
	}
	
	
	private static void DBG(String msg) {
		//System.out.println("[WordCaculator] " + msg);
	}
}
