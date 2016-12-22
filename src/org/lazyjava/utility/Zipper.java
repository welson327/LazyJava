package org.lazyjava.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Zipper {
	private String dstZipPath = null;
	private FileOutputStream fos = null;
	private ZipOutputStream zos = null;
	
	public Zipper(String dstPath) throws IOException {
		this.dstZipPath = dstPath;
		
		this.fos = new FileOutputStream(dstPath);
		this.zos = new ZipOutputStream(fos);
	}
	
	
	// =================================================================
	// Purpose: 	Append file to Zip Output Stream.  
	// Parameters:
	// Return:
	// Remark:		Zip files, not zip a folder
	// Author:		welson
	// =================================================================
	public void appendFile(File attachedFile) throws IOException {
		byte[] buffer = new byte[1024];
		int len = 0;
		
		ZipEntry ze = new ZipEntry(attachedFile.getName());
    	zos.putNextEntry(ze);
    	FileInputStream in = new FileInputStream(attachedFile);
 
    	while ((len = in.read(buffer)) > 0) {
    		zos.write(buffer, 0, len);
    	}
 
    	in.close();
    	zos.closeEntry();
 
    	//zos.close();
	}
	
	public String getPath() {
		return dstZipPath;
	}
	
	public void close() throws IOException {
		if(zos != null) {
			zos.close();
			zos = null;
		}
	}
}
