package org.lazyjava.common;


public class ServiceException extends Exception // extends Throwable
{
	private static final long serialVersionUID = -1;
	
    private int code = 0;
    private String message = "";
    
    
    public ServiceException(int code) {
    	this.code = code;
    }
    public ServiceException(String msg) {
    	this.message = msg;
    }
    public ServiceException(int code, String msg) {
    	this.code = code;
    	this.message = msg;
    }
    
    //---------------------------------------------//
    
    public int getCode() {
        return this.code;
    }
    public String getMessage() {
        return this.message;
    }
    public void setCode(int code) {
        this.code = code;
    }
    public void setMessage(String msg) {
        this.message = msg;
    }
}
