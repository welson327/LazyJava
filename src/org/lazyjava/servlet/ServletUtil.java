package org.lazyjava.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONException;
import org.json.JSONObject;
import org.lazyjava.utility.HttpRequestUtil;

public class ServletUtil {
	public static final String CONTENT_TYPE = "application/json;charset=UTF-8";
	
	public static String getServletRequestBody(HttpServletRequest request) throws IOException {
		StringBuilder sb = new StringBuilder();
        String s = null;
        BufferedReader br = request.getReader();
        while ((s = br.readLine()) != null) {
            sb.append(s);
        }
        // do NOT close the reader here, or you won't be able to get the post data twice
        br.reset();

        br.close();
        return sb.toString();
	}
	
	public static void utf8Encoding(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
		request.setCharacterEncoding("UTF-8");
    	
    	if (isSupportApplicationJson(request)) {
	        response.setContentType(CONTENT_TYPE);
	    } else {
	        // IE workaround
	        response.setContentType("text/plain; charset=UTF-8");
	    }
	}
	
	public static void setSessionAttribute(HttpServletRequest request, String attr, String value) {
		HttpSession session = request.getSession();
		if(session != null)
			session.setAttribute(attr, value);
	}
	
	private static boolean isSupportApplicationJson(HttpServletRequest request) {
		String header = request.getHeader("accept");
		if(header == null) // for apps
			return true;
		else {
			if (header.indexOf("application/json") != -1) {
		        return true;
		    } else {
		        // IE workaround
		        return false;
		    }
		}
	}
	
	public static JSONObject redirectPost(HttpServletRequest request, HttpServletResponse response, String url) throws JSONException, IOException {
		JSONObject req = new JSONObject(getServletRequestBody(request));
		String rslt = HttpRequestUtil.sendPost(url, req);
		return new JSONObject(rslt);
	}
	
	//=============================================================================
	// Purpose: 	forward request to another context
	// Parameters:	If you want to forward /context1/foo to /context2/foo:
	// 				context: "/context2"
	//				path: "/foo"
	// Return:
	// Remark:		http://stackoverflow.com/questions/10621580/how-to-get-the-servlet-context-from-servletrequest-in-servlet-2-5
	// Author:		welson
	//=============================================================================
	public static void forward(HttpServletRequest request, HttpServletResponse response, String context, String path) throws ServletException, IOException {
		ServletContext ctx = request.getSession().getServletContext().getContext(context);
		RequestDispatcher rd = ctx.getRequestDispatcher(path);
		rd.forward(request, response);
	}
	
	//=============================================================================
	// Purpose: 	fix non-english parameter
	// Parameters:
	// Return:
	// Remark:		http://openhome.cc/Gossip/Encoding/Servlet.html
	// Author:		welson
	//=============================================================================
	public static String getParameter(HttpServletRequest request, String key, String defaultValue) {
		String value = null;
		try {
			value = request.getParameter(key);
			if(value != null) {
				value = new String(value.getBytes("ISO-8859-1"), "UTF-8");
			} else {
				value = defaultValue;
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			value = null;
		}
		return value;
	}
	
	public static String getRequestUrl(HttpServletRequest request){
		StringBuffer requestURL = request.getRequestURL();
		if (request.getQueryString() != null) {
		    requestURL.append("?").append(request.getQueryString());
		}
		return requestURL.toString();
	}
}
