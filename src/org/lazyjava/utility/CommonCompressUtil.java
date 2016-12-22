package org.lazyjava.utility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang.RandomStringUtils;

public class CommonCompressUtil {
	//private static final String sep = File.separator;
	
	// =================================================================
	// Purpose: 
	// Parameters:
	// Return:
	// Remark:		http://www.programcreek.com/java-api-examples/index.php?api=org.apache.commons.compress.utils.IOUtils
	//				http://commons.apache.org/proper/commons-compress/examples.html
	//				http://stackoverflow.com/questions/9158565/ziparchiveentry-charset-encoding-error
	//				http://stackoverflow.com/questions/11734084/how-to-unzip-file-that-that-is-not-in-utf8-format-in-java
	// Author:		welson
	// =================================================================
	public static void uncompress(String srcZip, String dstFolderPath) throws IOException {
		
		//create output directory is not exists
    	File folder = new File(dstFolderPath);
    	if(!folder.exists()){
    		folder.mkdir();
    	}
 
    	ZipArchiveEntry ze = null;
    	ZipFile zipFile = new ZipFile(srcZip);
    	Enumeration entries = zipFile.getEntries();

    	try {
		while(entries.hasMoreElements()) { 
			ze = (ZipArchiveEntry) entries.nextElement(); 
		
    		String filename = ze.getName(); //String rawName = new String(ze.getRawName());
    		//ByteBuffer bf = ZipEncodingHelper.getZipEncoding("CP437").encode(filename);
    		//ByteBuffer bf = ZipEncodingHelper.getZipEncoding("Cp1252").encode(filename);
    		//String encFilename = new String(bf.array(), Charset.forName("UTF-8"));
    		//String encFilename = new String(bf.array());
    		DBG(String.format("ZipEntry: platform=%d,filename=%s", ze.getPlatform(), filename));// platform: windows=0, mac=3
    		File newFile = new File(dstFolderPath, filename);
    		File newFileParent = newFile.getParentFile();

    		
    		if(ze.isDirectory()) {
    			File newDir = new File(newFile.getAbsolutePath());
    			if(newDir.exists()) {
    				//DBG("  + delete file(maybe cross-platform DIR-FILE pollution) : "+ newFile.getAbsolutePath());
    				newDir.delete();
    			}
    			//DBG("  + create folder : "+ newFile.getAbsolutePath());
				newDir.mkdirs();
    		} else {
 
    			//DBG("  + write file : "+ newFile.getAbsolutePath());
	 
	            //create all non exists folders
	            //else you will hit FileNotFoundException for compressed folder
	    		if(!newFileParent.exists()) {
		    		//DBG("mkdirs: "+ newFileParent);
		    		newFileParent.mkdirs();
	    		}
	    		
	    		// fix decoding zipEntry of cross-platform variant zip format
	    		if(newFile.exists() && newFile.length()>0) {
	    			String conflict = newFile.getAbsolutePath();
	    			String fixName = "("+RandomStringUtils.randomNumeric(5)+")" + newFile.getName();
	    			newFile = new File(newFileParent, fixName);
	            	DBG("  !! Maybe "+conflict+" is be written before! Rename this zip-entry to: " + newFile.getAbsolutePath());
	            }
	    		
	    		InputStream is = null;
	    		FileOutputStream fos = null;
	    		try {
	    			is = zipFile.getInputStream(ze);
		            fos = new FileOutputStream(newFile); // will create a empty file
		            IOUtils.copy(is, fos);

		            if(newFile.exists() && newFile.length()>0) {
		            	//DBG("  ++ success: " + newFile.getAbsolutePath());
		            }
	    		} finally {
	    			if(is!=null) {is.close();}
	    			if(fos!=null) {fos.close();}
	    		}
    		}
    	}
    	} finally {
    		if(zipFile!=null) {zipFile.close();}
    	}
	}
	
	// =================================================================
	// Purpose: 
	// Parameters:
	// Return:
	// Remark:		http://stackoverflow.com/questions/21897286/how-to-extract-files-from-a-7-zip-stream-in-java-without-store-it-on-hard-disk
	// Author:		welson
	// =================================================================
	public static void uncompress7Z(String srcZip, String dstFolderPath) throws IOException {
		SevenZFile sevenZFile = new SevenZFile(new File(srcZip));
		/*
		SevenZArchiveEntry entry = sevenZFile.getNextEntry();
		byte[] content = new byte[entry.getSize()];
		LOOP UNTIL entry.getSize() HAS BEEN READ {
		    sevenZFile.read(content, offset, content.length - offset);
		}*/
		
		SevenZArchiveEntry entry = null;
		while((entry = sevenZFile.getNextEntry()) != null){
			System.out.println(entry.getName());
			File newFile = new File(dstFolderPath, entry.getName());
			FileOutputStream out = new FileOutputStream(newFile);
			byte[] content = new byte[(int) entry.getSize()];
			sevenZFile.read(content, 0, content.length);
			out.write(content);
			out.close();
		}
		sevenZFile.close();
	}
	
//	public static void uncompressDeflate(String srcZip, String dstFolderPath) throws IOException {
//		FileInputStream fin = new FileInputStream(srcZip);
//		BufferedInputStream in = new BufferedInputStream(fin);
//		FileOutputStream out = new FileOutputStream("archive.tar");
//		DeflateCompressorInputStream defIn = new DeflateCompressorInputStream(in);
//		final byte[] buffer = new byte[buffersize];
//		int n = 0;
//		while (-1 != (n = defIn.read(buffer))) {
//		    out.write(buffer, 0, n);
//		}
//		out.close();
//		defIn.close();
//		
//		
//		SevenZFile sevenZFile = new SevenZFile(new File(srcZip));
//		/*
//		SevenZArchiveEntry entry = sevenZFile.getNextEntry();
//		byte[] content = new byte[entry.getSize()];
//		LOOP UNTIL entry.getSize() HAS BEEN READ {
//		    sevenZFile.read(content, offset, content.length - offset);
//		}*/
//		
//		SevenZArchiveEntry entry = null;
//		while((entry = sevenZFile.getNextEntry()) != null){
//	        System.out.println(entry.getName());
//	        File newFile = new File(dstFolderPath, entry.getName());
//	        FileOutputStream out = new FileOutputStream(newFile);
//	        byte[] content = new byte[(int) entry.getSize()];
//	        sevenZFile.read(content, 0, content.length);
//	        out.write(content);
//	        out.close();
//	    }
//	    sevenZFile.close();
//	}
	
	
//	public static void compress(String srcPath, String dstPath) throws IOException {
//
//		File srcFile = new File(srcPath);
//		/*
//		int len = 0;
//		byte[] b = new byte[2048];
//		FileInputStream fis = new FileInputStream(srcFile);
//		ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(dstPath));
//
//		zos.putNextEntry(new ZipEntry(srcFile.getName()));
//
//        while((len = fis.read(b)) > 0) {
//        	zos.write(b, 0, len);
//        }
//        
//        fis.close();
//        zos.closeEntry();
//        zos.close();*/
//		
//		if (srcFile.isDirectory()) {
//			zipFolder(srcPath, dstPath);
//		} else {
//			zipFile(srcPath, dstPath);
//		}
//	}
	
	private static void DBG(String msg) {
		System.out.println("[CommonCompressUtil] " + msg);
	}
}
