package org.lazyjava.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.lazyjava.auth.TokenChecker;
import org.lazyjava.common.ServiceConstant;
import org.lazyjava.common.ServiceException;

public class PostServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	ServletUtil.utf8Encoding(request, response);
    	JSONObject output = new JSONObject();
    	
    	try {
    		JSONObject req = new JSONObject(ServletUtil.getServletRequestBody(request));
    		
    		// token
    		String token = req.getString("token");
    		JSONObject tokenInfo = TokenChecker.checkToken(token);
    		String uid = tokenInfo.getString("uid");
    		String account = tokenInfo.getString("account");
    		
    		// post data
    		String postData = req.getString("postData");
    		System.out.println("uid=" + uid + ", postData=" + postData);    		
    		
    		output.put("resultCode", ServiceConstant.SUCCESS);
			output.put("data", postData);
        } catch (ServiceException e) {
			try {
				output.put("resultCode", e.getCode());
				output.put("errMessage", e.getMessage());
			} catch (JSONException e1) {}
		} catch (Exception e) {
			e.printStackTrace();
			try {
				output.put("resultCode", ServiceConstant.FAIL);
			} catch (JSONException e1) {}
		}
    	
    	PrintWriter out = null;
		out = response.getWriter();
		out.print(output);
		out.flush();
		out.close();
    }
}