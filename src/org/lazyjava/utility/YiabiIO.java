package org.lazyjava.utility;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import org.lazyjava.common.ServiceConstant;

public class YiabiIO {
    public static int remoteAccess(String accessUrl, String output) throws IOException {
    	try {
    		URL url = new URL(accessUrl);
    		
    		if(!URLUtil.exists(url)) {
    			if(accessUrl.endsWith(".txt")) {
    				System.out.printf(">> %s NOT exists!!\n", accessUrl);
    			}
    			return ServiceConstant.FILE_NOT_FOUND;
    			//throw new ServiceException(ServiceConstant.FILE_NOT_FOUND);
    		}
    		
    		DBG(String.format("Download %s as %s\n", accessUrl, output));
    		URLUtil.saveAs(url, output);
    	} catch(FileNotFoundException e) {
    		// output dir not created
    		return ServiceConstant.FILE_NOT_FOUND;
    	} catch(IOException e) {
    		// accessUrl file not found
    		throw e;
    	}
    	return ServiceConstant.SUCCESS;
    }
    
    private static void DBG(String msg) {
    	System.out.println("[YiabiIO] " + msg);
    }
}
