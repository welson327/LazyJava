package org.lazyjava.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Copy a file or a directory from the source to the destination.
 * @author Wayne
 */
public class FileCopy {
	public void copyFile (File sourceFile, File destinationFile) throws IOException {
		if(sourceFile.isDirectory())
    		this.copyDirectory(sourceFile, destinationFile);
    	else if(destinationFile.isDirectory())
    		throw new IOException("Cannot copy a file to a directory.");
		
		// Check if the source file and destination file are exist.
		if(!sourceFile.exists())
			throw new IOException("The source file is not exist.");
		
		if(!destinationFile.exists())
			destinationFile.createNewFile();
		
        InputStream in = new FileInputStream(sourceFile);
        OutputStream out = new FileOutputStream(destinationFile);
        // Copy content of files
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0)
            out.write(buf, 0, len);
        in.close();
        out.close();
    }
	
	public void copyDirectory (File sourceDir, File destinationDir) throws IOException {
		this.copyDirectory(sourceDir, destinationDir, true);
	}
	
    public void copyDirectory (File sourceDir, File destinationDir, boolean isFirstOfRecursive) throws IOException {
    	if(sourceDir.isFile())
    		this.copyFile(sourceDir, destinationDir);
    	else if(destinationDir.isFile())
    		throw new IOException("Cannot copy a directory to a file.");
    	/*
    	if(sourceDir.getAbsolutePath().contains("\\.")) {
    		System.out.println("Ignore " + sourceDir.getAbsolutePath());
    		return ;
    	} */
    	
    	// Delete the destination folder for making sure that the result of copy will keep consistency.
    	if(isFirstOfRecursive && destinationDir.exists()) {
    		boolean isDirectory = destinationDir.isDirectory();
    		//System.out.println("Remove " + destinationDir.getAbsolutePath());
    		Runtime.getRuntime().exec("rm -rf " + destinationDir.getAbsolutePath());
    		if(isDirectory) {
    			//System.out.println("Create folder (first) " + destinationDir.getAbsolutePath());
    			Runtime.getRuntime().exec("mkdir " + destinationDir.getAbsolutePath());
    		}
    	}
    	
        File[] fileList = sourceDir.listFiles();
        for (File currentFile : fileList) {
            if(currentFile.isFile()) {
            	/* -------------------------- */
        		/*  Copy files in the folder  */
        		/* -------------------------- */
                File destDemo = new File(destinationDir.getAbsolutePath() + "/" + currentFile.getName());
                this.copyFile(currentFile, destDemo);
            }
            else {
                File destDemo = new File(destinationDir.getAbsolutePath() + "/" + currentFile.getName());
                boolean mkdir = destDemo.mkdirs();
                //System.out.println("Create " + destDemo.getAbsolutePath() + " " + mkdir);
                if(mkdir)
                	this.copyDirectory(currentFile, destDemo, false);
                else
                	throw new IOException("Create folder failed.");
            }
        }
    }
}
