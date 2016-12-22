package org.lazyjava.utility;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class ProcessExecutor {
	
	public static String getExecuteResponse(InputStream input){
		
		StringBuffer buffer = new StringBuffer();
		int charCount = 0;
		int i = 1;
		for (;;) { 
			try {
				charCount = input.read();
				//System.out.println(charCount);
			} catch (IOException e) {
				e.printStackTrace();
			} 
			if (charCount == -1){
				break;
			}
			
			if (charCount == 10){//space
				continue;
			}
			
			else{
				buffer.append((char) charCount);
			}	
		}
		//System.out.println(buffer.toString());
		return buffer.toString();
	}
	
	public static List<String> execWaitEnd(String... command){
		
		Process processOnLinux = null;
		
		try {
			processOnLinux = Runtime.getRuntime().exec(command);
			processOnLinux.waitFor();

			return getProcessData(processOnLinux);
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	public static List<String> exec(String... command){
		
		Process processOnLinux = null;
		
		try {
			processOnLinux = Runtime.getRuntime().exec(command);
			return getProcessData(processOnLinux);
			
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	public static List<String> exec(String command){
		
		Process processOnLinux = null;
		
		try {
			processOnLinux = Runtime.getRuntime().exec(command);
			return getProcessData(processOnLinux);
			
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	public static List<String> getProcessData(Process processOnLinux){
		List<String> result = null;
		//List<String> resultError = null;
		
		BufferedReader reader=null;
		BufferedReader readerError = null;
		
		try {
			
			reader = new BufferedReader(
					new InputStreamReader(
							new BufferedInputStream(processOnLinux.getInputStream())));
			
			readerError = new BufferedReader(
					new InputStreamReader(
							new BufferedInputStream(processOnLinux.getErrorStream())));
			
			
			result = new ArrayList<String>();
			//resultError =  new ArrayList<String>();
			
			String line = "";
			while((line = reader.readLine()) != null){
				result.add(line.replace("\n", "").trim());
			}
			
			String lineError = "";
			while((lineError = readerError.readLine()) != null){
				result.add(lineError.replace("\n", "").trim());
			}
			
			//System.out.println(result);
			reader.close();
			readerError.close();
			
		} catch (IOException e) {
			e.printStackTrace();
			return null;
			
		}finally{
			if(reader != null){
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(readerError != null){
				try {
					readerError.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(processOnLinux != null){
				try {
					IOUtils.closeQuietly(processOnLinux.getInputStream());
					IOUtils.closeQuietly(processOnLinux.getOutputStream());
					IOUtils.closeQuietly(processOnLinux.getErrorStream());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}
	

	private final long MAXIMUM_WAIT_TIME = 10000;  // 10 seconds
	private final long POLL_INTERVAL     = 250;    // 1/4 second

	public JSONObject execute(final String[] commandAndParameters)throws IOException,InterruptedException,JSONException {
		return(execute(commandAndParameters, MAXIMUM_WAIT_TIME));
	}

	
	public JSONObject execute(final String[] commandAndParameters, final long waitTime) 
			throws IOException,InterruptedException,JSONException{
		JSONObject  jsonResult  = null;
	    final Process process = Runtime.getRuntime().exec(commandAndParameters);
	    try{
	    	StreamCatcher stdoutCatcher = new StreamCatcher(process.getInputStream());
		    StreamCatcher stderrCatcher = new StreamCatcher(process.getErrorStream());
		    
		    stdoutCatcher.setDaemon(true);
		    stdoutCatcher.setName("stdoutCaptureThread");
		        
		    stderrCatcher.setDaemon(true);
		    stderrCatcher.setName("stderrCaptureThread");
		        
		    stdoutCatcher.start();
		    stderrCatcher.start();
		    
		    // Give the process waitTime milliseconds to do its thing
		    int     exitCode  = -1;
		    long    waitUntil = System.currentTimeMillis() + (waitTime < 0 ? 0 : waitTime);
		    boolean done      = false;
		    
		    while (!done && System.currentTimeMillis() < waitUntil){
		        try
		        {
		            exitCode = process.exitValue();
		            done     = true;
		        }
		        catch (final IllegalThreadStateException itse)
		        {
		            Thread.sleep(POLL_INTERVAL); 
		        }
		    }
		    
		    if (!done)
		    {
		        process.destroy();
		    }
		    
		    stdoutCatcher.interrupt();
		    stderrCatcher.interrupt();
		    
		    jsonResult = new JSONObject().put("exitCode",new Integer(exitCode)).
		    		put("out", stdoutCatcher.getOutput()).put("err", stderrCatcher.getOutput());
	    }catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(process != null){
				try {
					IOUtils.closeQuietly(process.getInputStream());
					IOUtils.closeQuietly(process.getOutputStream());
					IOUtils.closeQuietly(process.getErrorStream());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	    
	    
	    return(jsonResult);
	}
	

	private class StreamCatcher extends Thread {
	    private final InputStream  inputStream;
	    private final StringBuffer output;
	    
	    
	    StreamCatcher(final InputStream inputStream)
	    {
	        this.inputStream = inputStream;
	        this.output      = new StringBuffer();
	    }

	    public void run()
	    {
	        try
	        {
	            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
	            String line   = null;
	            
	            synchronized(output)
	            {
	                while ((line = reader.readLine()) != null && !interrupted())
	                {
	                    output.append(line);
	                    output.append("\r\n");
	                }
	            }
	        }
	        catch (final IOException ioe)
	        {
	            // Swallow the exception and move on
	        }
	    }
	    
	    public String getOutput()
	    {
	        synchronized(output)
	        {
	            return(output.toString().trim());
	        }
	    }
	}


	
}
