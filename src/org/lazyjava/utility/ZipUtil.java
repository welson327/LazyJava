package org.lazyjava.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtil {
	private static final String sep = File.separator;
	
	// =================================================================
	// Purpose: 
	// Parameters:
	// Return:
	// Remark:		<Sample code>
	//					http://www.mkyong.com/java/how-to-decompress-files-from-a-zip-file/
	//				<pollution of DIR-FILE for cross-platform>
	//					http://stackoverflow.com/questions/10849321/how-to-decompress-a-zip-archive-which-has-sub-directories
	//					http://examples.javacodegeeks.com/core-java/util/zip/zipinputstream/java-unzip-file-example/
	//					http://stackoverflow.com/questions/13261347/correctly-decoding-zip-entry-file-names-cp437-utf-8-or
	// Author:		welson
	// =================================================================
	public static void unzip(String srcZip, String dstFolderPath) throws IOException {
		unzip(srcZip, dstFolderPath, "CP437");
	}
	public static void unzip(String srcZip, String dstFolderPath, String encoding) throws IOException {
		
		int len = 0;
		byte[] buffer = new byte[1024];
		
		if(encoding == null) {
			encoding = "UTF-8";
		}
		//create output directory is not exists
    	File folder = new File(dstFolderPath);
    	if(!folder.exists()){
    		folder.mkdir();
    	}
 
    	ZipInputStream zis = new ZipInputStream(new FileInputStream(srcZip), Charset.forName(encoding));
    	ZipEntry ze = null;
    	FileOutputStream fos = null;
 
    	try {
    	while ((ze = zis.getNextEntry()) != null) {
    		String fileName = ze.getName();
    		//System.out.printf("ZipEntry name: %s\n", fileName);
    		File newFile = new File(dstFolderPath, fileName);
    		File newFileParent = newFile.getParentFile();
 
    		if(ze.isDirectory()) {
    			File newDir = new File(newFile.getAbsolutePath());
    			if(newDir.exists()) {
    				//System.out.println("  + delete file(maybe cross-platform DIR-FILE pollution) : "+ newFile.getAbsoluteFile());
    				newDir.delete();
    			}
    			//System.out.println("  + create folder : "+ newFile.getAbsoluteFile());
				newDir.mkdirs();
    		} else {
 
	    		//System.out.println("  + write file : "+ newFile.getAbsoluteFile());
	 
	            //create all non exists folders
	            //else you will hit FileNotFoundException for compressed folder
	    		if(!newFileParent.exists()) {
		    		//System.out.println("mkdirs: "+ newFileParent);
		    		newFileParent.mkdirs();
	    		}
	    		
	    		try {
		            fos = new FileOutputStream(newFile);             
		            while ((len = zis.read(buffer)) > 0) {
		            	fos.write(buffer, 0, len);
		            }
		            //IOUtils.copy(zf.getInputStream(ze), fos);
	    		} finally {
	    			if(fos!=null) {fos.close();}
	    		}
    		}  
    	}
    	} finally {
    		if(zis != null) {
		        zis.closeEntry();
		    	zis.close();
		    	zis = null;
    		}
    	}
	}
	
	
	public static void zip(String srcPath, String dstPath) throws IOException {

		File srcFile = new File(srcPath);
		/*
		int len = 0;
		byte[] b = new byte[2048];
		FileInputStream fis = new FileInputStream(srcFile);
		ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(dstPath));

		zos.putNextEntry(new ZipEntry(srcFile.getName()));

        while((len = fis.read(b)) > 0) {
        	zos.write(b, 0, len);
        }
        
        fis.close();
        zos.closeEntry();
        zos.close();*/
		
		if (srcFile.isDirectory()) {
			zipFolder(srcPath, dstPath);
		} else {
			zipFile(srcPath, dstPath);
		}
	}
	
	public static void zipAgain(String zipPath) throws IOException {
		String tmpPath = zipPath + ".tmp";
		zip(zipPath, tmpPath);
	    new File(zipPath).delete();
	    new File(tmpPath).renameTo(new File(zipPath));
	}
	
	// =================================================================
	// Purpose: 
	// Parameters:
	// Return:
	// Remark:		ref: http://www.mkyong.com/java/how-to-compress-files-in-zip-format/
	// Author:		welson
	// =================================================================
	private static void zipFile(String filePath, String dstPath) throws IOException {
		byte[] buffer = new byte[1024];
		int len;
 
		ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(dstPath));
		ZipEntry ze = new ZipEntry(new File(filePath).getName());
    	zos.putNextEntry(ze);
    	FileInputStream in = new FileInputStream(filePath);
 
    	while ((len = in.read(buffer)) > 0) {
    		zos.write(buffer, 0, len);
    	}
 
    	in.close();
    	zos.closeEntry();
 
    	//remember close it
    	zos.close();
	}
	
	//------------------------------------------------------------------------------------------//
	// Following code:
	// ref: http://www.java2s.com/Code/Java/File-Input-Output/UseJavacodetozipafolder.htm
	
	public static void zipFolder(String srcFolder, String dstZipPath) throws IOException {
		ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(dstZipPath));
		
		addFolderToZip("", srcFolder, zos);
		zos.flush();
		zos.close();
	}
	
	//================================================================
	// Purpose:		zip folder but without contains self dir
	// Parameters:
	// Return:
	// Remark:		ex: zip /a/b to 1.zip, it will zip b/* to zip without folder b
	// Author:		welson
	//================================================================
	public static void zipFolderElements(String srcFolder, String dstZipPath, boolean isPrintDetail) throws IOException {
		ZipOutputStream zos = null;
		
		File dir = new File(srcFolder);
		String[] list = dir.list();
		String filepath = null;
		
		// after list to fix if dstZipPath in srcFolder
		zos = new ZipOutputStream(new FileOutputStream(dstZipPath));
		
		for(String filename : list) {
			filepath = srcFolder + File.separator + filename;
			if(isPrintDetail) {
				System.out.printf(" > Append %s to %s\n", filepath, dstZipPath);
			}
			addFileToZip("", filepath, zos);
		}
		zos.flush();
		zos.close();
	}

	private static void addFileToZip(String path, String srcPath, ZipOutputStream zip) throws IOException {

	    File srcFile = new File(srcPath);
	    if (srcFile.isDirectory()) {
	    	addFolderToZip(path, srcPath, zip);
	    } else {
	    	byte[] buf = new byte[1024];
	    	int len;
	    	String zipEntryPath = path.equals("") ? "" : (path + sep);
	    	//String zipEntryPath = path + sep;
	    	
	    	FileInputStream in = new FileInputStream(srcPath);
	    	zip.putNextEntry(new ZipEntry(zipEntryPath + srcFile.getName()));
	    	while ((len = in.read(buf)) > 0) {
	    		zip.write(buf, 0, len);
	    	}
	    	in.close();
	    }
	}

	private static void addFolderToZip(String path, String srcFolder, ZipOutputStream zip) throws IOException {
		File folder = new File(srcFolder);
		String[] list = folder.list(); // add by welson
	    for (String fileName : list) {
	    	if (path.equals("")) {
	    		addFileToZip(folder.getName(), srcFolder + sep + fileName, zip);
	    	} else {
	    		addFileToZip(path + sep + folder.getName(), srcFolder + sep + fileName, zip);
	    	}
	    }
	}
}
