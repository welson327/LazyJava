package org.lazyjava.utility;

import java.io.File;
import java.io.IOException;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class HttpRequestUtil {
	public static String sendGet(String url) throws IOException {
		CloseableHttpResponse response = null;
		CloseableHttpClient httpclient = null;
		try {
			httpclient = HttpClients.createDefault();
			HttpGet httpGet = new HttpGet(url);
			response = httpclient.execute(httpGet);
			return response2String(response);
		} catch (IOException e) {
			throw e;
		} finally {
			if(response != null) {
				response.close();
				response = null;
			}
			if(httpclient!= null) {
				httpclient.close();
				httpclient = null;
			}
		}
	}
	
	//=========================================================================
	// Purpose:		
	// Parameters:	postData: if your input is JSON, use json.tostring()
	//				charset: Encoding postData to "UTF-8" | "BIG5" | ...
	// Return:
	// Remark:		http://www.examplesample.com/java/http-post-request-using-apache-commons
	// Author:		welson
	//=========================================================================
	public static String sendPost(String url, String postData, String charset, BasicNameValuePair[] header) throws IOException, JSONException {
		CloseableHttpResponse response = null;
		CloseableHttpClient httpclient = null;
		try {
			httpclient = HttpClients.createDefault();
			HttpPost httpPost = new HttpPost(url);
			
			// header
			if(header != null) {
				for(BasicNameValuePair pair : header) {
					httpPost.addHeader(pair.getName(), pair.getValue()); 
				}
				/* (if JSON)
				String key = null;
				Object value = null;
				Iterator<?> keys = header.keys();
				while(keys.hasNext()) {
					key = (String) keys.next();
					value = header.get(key);
					if(value instanceof String) {
						//System.out.printf("(k,v)=%s, %s\n", key, value);
						httpPost.addHeader(key, (String)value); 
					}
				}
				*/
			}
			
			// post parameters
			StringEntity se = new StringEntity(postData, charset);
			httpPost.setEntity(se);
			/*
			List<BasicNameValuePair> parameters = new ArrayList<BasicNameValuePair>();
		    parameters.add(new BasicNameValuePair("search", "jsproch"));
		    parameters.add(new BasicNameValuePair("filters", "closed"));
		    parameters.add(new BasicNameValuePair("affilfilter", "everyone"));
		    parameters.add(new BasicNameValuePair("btnG", "Search"));
			httpPost.setEntity(new UrlEncodedFormEntity(parameters));
		    */
			
			response = httpclient.execute(httpPost);
			int statusCode = response.getStatusLine().getStatusCode();
			//System.out.println("statusCode=" + statusCode);
			switch(statusCode) {
				case HttpStatus.SC_OK:
					return response2String(response);
				case HttpStatus.SC_NOT_FOUND://404
					return null;
				default:
					return null;
			}
		} catch (IOException e) {
			throw e;
		} finally {
			if(response != null) {
				response.close();
				response = null;
			}
			if(httpclient!= null) {
				httpclient.close();
				httpclient = null;
			}
		}
	}
	public static String sendPost(String url, JSONObject postData, BasicNameValuePair[] header) throws IOException, JSONException {
		return sendPost(url, (postData!=null) ? postData.toString() : "", "UTF-8", header);
	}
	
	public static String sendPost(String url, String postString, BasicNameValuePair[] header) throws IOException, JSONException {
		return sendPost(url, postString , "UTF-8", header);
	}
	
	public static String sendPost(String url, JSONObject postData) throws IOException, JSONException {
		return sendPost(url, postData, null);
	}
	
	public static String sendPostUntil(String url, JSONObject postData, long timeoutInMs) throws IOException, JSONException {
//		String rslt = null;
//		long timeout = 0;
//
//		if(timeoutInMs < 0) {
//			rslt = sendPost(url, postData, null);
//		} else {
//			while(timeout < timeoutInMs) {
//				try {
//					rslt = sendPost(url, postData, null);
//					if(rslt == null) {
//						DBG(String.format("Send %s FAIL(404 Not Found), retry after 15s ...", url));
//						sleep(15000);
//						timeout += 15000;
//						continue;
//					} else {
//						timeout = timeoutInMs + 1; // more safety break
//						break;
//					}
//				} catch(Exception e) {
//					DBG(String.format("Send %s FAIL, retry after 15s ...", url));
//					sleep(15000);
//					timeout += 15000;
//					continue;
//				}
//			}
//		}
//		return rslt;
		return sendUntil(url, postData, timeoutInMs, 15000);
	}
	
	public static String sendFileUntil(String url, File file, long timeoutInMs) throws IOException, JSONException {
		return sendUntil(url, file, timeoutInMs, 15000);
	}
	
	public static String sendFile(String url, File file) throws IOException {
		CloseableHttpResponse response = null;
		CloseableHttpClient httpclient = null;
		try {
			httpclient = HttpClients.createDefault();
			HttpPost httpPost = new HttpPost(url);
			
			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
			builder.addPart("file", new FileBody(file));
			httpPost.setEntity(builder.build());
	
			response = httpclient.execute(httpPost);
			return response2String(response);
		} catch (IOException e) {
			throw e;
		} finally {
			if(response != null) {
				response.close();
				response = null;
			}
			if(httpclient!= null) {
				httpclient.close();
				httpclient = null;
			}
		}
	}
	
	public static String sendUntil(String url, Object data, long timeoutInMs, long retryIntervalInMs) throws IOException, JSONException {
		String rslt = null;
		long timeout = 0;

		if(timeoutInMs < 0) {
			rslt = _send(url, data);
		} else {
			while(timeout < timeoutInMs) {
				try {
					rslt = _send(url, data);
					if(rslt == null) {
						DBG(String.format("Send %s FAIL(404 Not Found), retry after %d(ms) ...", url, retryIntervalInMs));
						sleep(retryIntervalInMs);
						timeout += retryIntervalInMs;
						continue;
					} else {
						timeout = timeoutInMs + 1; // more safety break
						break;
					}
				} catch(Exception e) {
					DBG(String.format("Send %s FAIL, retry after %d(ms) ...", url, retryIntervalInMs));
					sleep(retryIntervalInMs);
					timeout += retryIntervalInMs;
					continue;
				}
			}
		}
		return rslt;
	}
	private static String _send(String url, Object data) throws IOException, JSONException {
		String rslt = null;
		if(data instanceof JSONObject) {
			rslt = sendPost(url, (JSONObject)data, null);
		} else if(data instanceof File) {
			rslt = sendFile(url, (File)data);
		}
		return rslt;
	}
	
	private static String response2String(CloseableHttpResponse response) throws IOException {
		return EntityUtils.toString(response.getEntity(), "UTF-8"); 
		
		/* old
		InputStream is = null;
		try {
			is = response.getEntity().getContent();
			return StringConverter.stream2String(is);
		} catch (IOException e) {
			throw e;
		} finally {
			if(is != null) {
				is.close();
				is = null;
			}
		}*/
	}
	
	// ================================================================
	// Purpose:		
	// Parameter:
	// Return:
	// Remark:		https://wiki.apache.org/HttpComponents/HttpClientConfiguration
	// Author:		welson
	// ================================================================
//	private CloseableHttpClient getHttpClients() {
//		RequestConfig defaultRequestConfig = RequestConfig.custom()
//			    .setSocketTimeout(5000)
//			    .setConnectTimeout(5000)
//			    .setConnectionRequestTimeout(5000)
//			    .setStaleConnectionCheckEnabled(true)
//			    .build();
//		CloseableHttpClient httpclient = HttpClients.custom()
//			    .setDefaultRequestConfig(defaultRequestConfig)
//			    .build();
//		return httpclient;
//	}
	
	private static void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private static void DBG(String msg) {
		System.out.println("[HttpRequestUtil] " + msg);
	}
}
