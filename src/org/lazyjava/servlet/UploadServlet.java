package org.lazyjava.servlet;

import java.io.File;
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
import org.lazyjava.servletfileloader.ServletFileLoader;

public class UploadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ServletUtil.utf8Encoding(request, response);
		JSONObject output = new JSONObject();

		try {
			String token = request.getParameter("token");
			JSONObject tokenInfo = TokenChecker.checkToken(token);

			// upload dir
			String account = tokenInfo.getString("account");
			String uploadDir = "/tmp";

			ServletFileLoader fileLoader = new ServletFileLoader();
			fileLoader.setMaxSize(10 * 1024 * 1024); // 20mb
			File uploadedFile = fileLoader.upload(request, uploadDir, null);
			System.out.println(String.format("<%s> try to upload %s", account, uploadedFile.getAbsolutePath()));
		
			output.put("resultCode", ServiceConstant.SUCCESS);
		} catch (ServiceException e) {
			try {
				output.put("resultCode", e.getCode());
				output.put("errMsg", e.getMessage());
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