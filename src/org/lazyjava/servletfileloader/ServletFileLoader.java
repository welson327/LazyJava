package org.lazyjava.servletfileloader;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase.FileSizeLimitExceededException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.bson.types.ObjectId;

public class ServletFileLoader {
	
	private static final int BUFSIZE = 4096;
	private long permittedSize = -1;
	
    /* =======================================================
     * Purpose:
     * Parameter:	dir: dir to upload
     * Return:		outputName
     * Remark:		
     * Author:
     * ======================================================= */ 	
    public File upload(HttpServletRequest request, String uploadedFolderPath, String outputName) throws Exception {
    	
    	File ret = null;
    	String account = request.getParameter("account");
    	File uploadDir = new File(uploadedFolderPath);
    	if(!uploadDir.exists()) {
    		uploadDir.mkdirs();
    	}
    	
    	//System.out.println("request.getCharacterEncoding = " + request.getCharacterEncoding());
    	//System.out.println("file.encoding = " + System.getProperty("file.encoding"));
    	
    	//process only if its multipart content
	    if(ServletFileUpload.isMultipartContent(request)){
	    	ServletFileUpload servletFileUpload = new ServletFileUpload(new DiskFileItemFactory());
	    	servletFileUpload.setHeaderEncoding("UTF-8");
	    	
	        List<FileItem> multiparts = servletFileUpload.parseRequest(request);
	        for(FileItem item : multiparts) {
	            if(!item.isFormField()) {
	            	long itemSize = item.getSize();
	            	if(isPermittedSize(item.getSize())) {
		                String inputName = new File(item.getName()).getName();
		                //String extension = getExt(inputName);
		                
		                outputName = (outputName!=null && outputName.length()>0) ? outputName : changeName(inputName);
		                
		                String fullpath = uploadedFolderPath + File.separator + outputName;
		                DBG(String.format("<%s> uploads %s, outputName:%s, size=%d(bytes)", account, inputName, outputName, itemSize));
		                ret = new File(fullpath);
		                item.write(ret);
		                
		                /*
		                if(isImage(extension)) {
		                	String sAutoResize = request.getParameter("autoResize");
		                	boolean isAutoResize = "false".equals(sAutoResize) ? false : true;
		                	if(isAutoResize) {
		                		DBG(String.format("Auto resize %s to %dx%d ...", fullpath, DEFAULT_COVER_WIDTH, DEFAULT_COVER_HEIGHT));
		                		ScalrUtil.resize(fullpath, DEFAULT_COVER_WIDTH, DEFAULT_COVER_HEIGHT, null);
		                	}
		                }*/
		            } else {
		            	String msg = String.format("Upload file size(%d) is greater than permitted size(%d).(name=%s)", itemSize, permittedSize, item.getName());
	            		//throw new RuntimeException(msg);
	            		//throw new SizeException(msg, itemSize, maxSize);
	            		throw new FileSizeLimitExceededException(msg, itemSize, permittedSize);
		            }
	            }
	        }
	   
	        return ret;
	    } else {
	        return null;
	    }
    }
    
    /* =======================================================
     * Purpose:
     * Parameter:
     * Return:
     * Remark:		(1) http://www.java-forums.org/blogs/servlet/668-how-write-servlet-sends-file-user-download.html
     * 				(2) Don't close ServletOutputStream:
     * 						http://stackoverflow.com/questions/1159168/should-one-call-close-on-httpservletresponse-getoutputstream-getwriter
     * Author:
     * ======================================================= */    
    public HttpServletResponse download(String filePath, HttpServletResponse response, ServletContext context) throws IOException {
    	File file = new File(filePath);
        int length   = 0;
        ServletOutputStream outStream = response.getOutputStream();
        //ServletContext context  = getServletConfig().getServletContext();
        String mimetype = context.getMimeType(filePath);
        
        // sets response content type
        if (mimetype == null) {
            mimetype = "application/octet-stream";
        }
        response.setContentType(mimetype);
        response.setContentLength((int)file.length());
        String fileName = (new File(filePath)).getName();
        
        // sets HTTP header
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        
        byte[] byteBuffer = new byte[BUFSIZE];
        DataInputStream in = new DataInputStream(new FileInputStream(file));
        
        // reads the file's bytes and writes them to the response stream
        while ((in != null) && ((length = in.read(byteBuffer)) != -1)) {
            outStream.write(byteBuffer,0,length);
        }
        
        in.close();
        outStream.close(); // (Remark 2)
        
        return response;
    }   
    
    public void setMaxSize(long sizeInBytes) {
    	permittedSize = sizeInBytes;
    }
	
    private String changeName(String inputName) {
    	String mongoId = new ObjectId().toString();
    	if(inputName!=null && inputName.length() > 0) {
    		String ext = getExt(inputName);
    		if(ext != null  &&  ext.length() > 0) {
    			return mongoId + "." + ext;
    		} else {
    			return mongoId;
    		}
    	} else {
    		return mongoId;
    	}	
    }
    
    private String getExt(String inputName) {
    	int idx = inputName.lastIndexOf(".");
    	String ext = null;
    	if(idx >= 0) {
    		ext = inputName.substring(idx + 1);	
    	} else {
    		ext = "";
    	}
    	return ext;
    }
    
	private boolean isPermittedSize(long uploadSize) {
		if(permittedSize < 0) {
			return true;
		} else {
			return (uploadSize <= permittedSize);
		}
	}

    
    private void DBG(String msg) {
    	//System.out.println("[ServletFileLoader] " + msg);
    }
}
