package org.lazyjava.utility;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;


public class URLUtil {
	public static void saveAs(URL url, String dstFilePath) throws IOException {
		byte[] b = new byte[2048];
		int length = -1;

		// mkdir
		String dirPath = dstFilePath.substring(0, dstFilePath.lastIndexOf(File.separator));
		File dir = new File(dirPath);
		if(!dir.exists()) {
			System.out.println("[URLUtil] mkdir: " + dirPath);
			dir.mkdirs();
		}
		
		InputStream is = url.openStream();
		OutputStream fos = new FileOutputStream(dstFilePath); // dir path must be exist
		while ((length = is.read(b)) != -1) {
			fos.write(b, 0, length);
		}

		is.close();
		fos.close();
	}
	
	// ================================================================
	// Purpose: 	cURL address
	// Parameters:	charset: "UTF-8", "BIG5"
	// Return:		
	// Remark:		http://stackoverflow.com/questions/2586975/how-to-use-curl-in-java
	// Author:
	// ================================================================
	public static String getText(String urlAddress, String charset) throws IOException {
		URL url = new URL(urlAddress);
		BufferedReader br = null;
		String line = null;
		StringBuilder sb = new StringBuilder();
		
		try {
			br = new BufferedReader(new InputStreamReader(url.openStream(), charset));
			while((line = br.readLine()) != null) {
				sb.append(line);
			}
		} finally {
			if (br != null) { 
				br.close(); 
				br = null;
			}
		}
		return sb.toString();
	}
	
	public static String getText(String urlAddress) throws IOException {
		URL url = new URL(urlAddress);
		BufferedReader br = null;
		String line = null;
		StringBuilder sb = new StringBuilder();
		
		try {
			br = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
			while((line = br.readLine()) != null) {
				sb.append(line);
			}
		} finally {
			if (br != null) { 
				br.close(); 
				br = null;
			}
		}
		return sb.toString();
	}
	
	// ================================================================
	// Purpose: 	more faster than getText()
	// Parameters:	
	// Return:		
	// Remark:		(1) http://stackoverflow.com/questions/2586975/how-to-use-curl-in-java
	//				(2) Cannot fix non-utf8 encoding
	// Author:
	// ================================================================
	public static String getText2(String urlAddress, String charset) throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedInputStream bis = null;
		int chunksize = 4096;
        byte[] chunk = new byte[chunksize];
        int count = 0;
        try {    
            URL url = new URL(urlAddress);
            bis = new BufferedInputStream(url.openStream());
            while ((count = bis.read(chunk, 0, chunksize)) != -1) {
                sb.append(new String(chunk, charset));
            }
        } finally {
        	if(bis != null) {
        		 bis.close();
        		 bis = null;
        	}
        }
        return sb.toString();
	}
	
	public static String getText(String urlAddress, boolean useHTMLCharset) throws IOException {
		if(useHTMLCharset) {
			StringBuilder sb = new StringBuilder();
			BufferedReader br = null;
			try {
				String line = null;
				URL url = new URL(urlAddress);
				URLConnection conn = url.openConnection();
				conn.connect();
				String contentType = conn.getContentType();
				String charset = "UTF-8";
				if(contentType != null) {
					int pos = contentType.toLowerCase().lastIndexOf("charset=");
					if(pos >= 0) {
						charset = contentType.substring(pos+8);
					}
				}
				br = new BufferedReader(new InputStreamReader(conn.getInputStream(), charset));
				while((line = br.readLine()) != null) {
					sb.append(line);
				}
			} finally {
				if (br != null) { 
					br.close(); 
					br = null;
				}
			}
			return sb.toString();
		} else {
			return getText(urlAddress, "UTF-8");
		}
	}
	
	public static String getCharset(URL url) throws IOException {
		URLConnection conn = url.openConnection();
		conn.connect();
		String contentType = conn.getContentType();
		String charset = contentType.substring(contentType.toLowerCase().lastIndexOf("charset=")+8);
		//unicode = encod.substring(1,encod.length());
		//System.out.printf("contentType=%s, charset=%s\n", contentType, charset);
		return charset;
	}
	
	// ================================================================
	// Purpose: 	
	// Parameters:	
	// Return:		
	// Remark:		ConnectException: file not exist
	// Author:
	// ================================================================
	public static int getResponseCode(URL url) throws ConnectException, IOException {
		int code = -1;
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	    conn.setRequestMethod("HEAD"); //Set request to header to reduce load as Subirkumarsao said.
	    code = conn.getResponseCode();
		conn.disconnect();
		return code;
	}
	
	// ================================================================
	// Purpose: 	Check if url is exist
	// Parameters:	
	// Return:		
	// Remark:		http://stackoverflow.com/questions/10551813/check-if-url-is-valid-or-exists-in-java
	// Author:
	// ================================================================
	public static boolean exists(URL url) throws ConnectException, IOException {
		return (getResponseCode(url) == HttpURLConnection.HTTP_OK);
	}
	
	public static HashMap<String, String> getQueryParameters(String urlAddress) {
		HashMap<String, String> map = null;
		try {
			map = new HashMap<String, String>();
			
			URL url = new URL(urlAddress);
			String q = url.getQuery();
			
			if(q != null) {
				String[] kvPairs = q.split("&");
				for(String elem : kvPairs) {
					String[] kv = elem.split("=");
					if(kv.length == 2) {
						map.put(kv[0], kv[1]);
					} else {
						map.put(kv[0], "");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}
	
	public static HashMap<String, String> parseQueryString(String queryStr) {
		String urlAddr = "http://foo.com.tw?" + queryStr;
		return getQueryParameters(urlAddr);
	}
	
	public static boolean isLegalUrl(String url) {
		return url.matches("^(https?|ftp)://.{3,}/.+");
	}
	
	public static String asciiString(String urlAddress) {
		try {
			URL url = new URL(urlAddress);
			URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
			return uri.toASCIIString();
		} catch (Exception e) {
			e.printStackTrace();
			return urlAddress;
		} 
	}
}
