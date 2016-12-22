package org.lazyjava.utility;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

public class Zip4jUtil {
	private static final String DEFAULT_PASSWORD = "password";
	
	public static void unzipWithPassword(String srcPath, String dstDirPath, String password) throws ZipException {
		ZipFile zipFile = new ZipFile(srcPath);
		
		if (zipFile.isEncrypted()) {
			zipFile.setPassword(password);
		}
		
		List<FileHeader> fileHeaderList = zipFile.getFileHeaders();
		int cnt = fileHeaderList.size();
		for (int i=0; i<cnt; ++i) {
			FileHeader fileHeader = fileHeaderList.get(i);
			DBG(String.format(" + zipEntry=%s", fileHeader.getFileName()));
			zipFile.extractFile(fileHeader, dstDirPath);
		}
		DBG(String.format(" => cnt=%d", cnt));
	}
	
	public static void unzip(String srcPath, String dstDirPath) throws ZipException {
		unzipWithPassword(srcPath, dstDirPath, DEFAULT_PASSWORD);
	}
	
	public static void unzip(File zipFile, String dstDirPath) throws ZipException {
		new ZipFile(zipFile).extractAll(dstDirPath);
	}
	
	public static File zipFiles(ArrayList<File> files, String dstPath) throws ZipException {
		ZipParameters parameters = getDefaultZipParameters();
		
		File dstFile = new File(dstPath);
		if(dstFile.exists()) {
			dstFile.delete();
		}
		
		ZipFile zipFile = new ZipFile(dstPath);
		zipFile.addFiles(files, parameters);
		return zipFile.getFile();
	}
	
	public static File zip(String srcPath, String dstPath) throws ZipException {
		return zip(srcPath, dstPath, true);
	}
	
	//================================================================
	// Purpose:		zip folder or file
	// Parameters:	includeRootFolder: for zip a folder
	// Return:
	// Remark:		
	// Author:		welson
	//================================================================
	public static File zip(String srcPath, String dstPath, boolean includeRootFolder) throws ZipException {
		ZipParameters parameters = getDefaultZipParameters();
		parameters.setIncludeRootFolder(includeRootFolder);
		
		File dstFile = new File(dstPath);
		if(dstFile.exists()) {
			dstFile.delete();
		}
		
		ZipFile zipFile = new ZipFile(dstPath);
		File srcFile = new File(srcPath);
		if(srcFile.isDirectory()) {
			zipFile.addFolder(srcPath, parameters);
		} else {
			zipFile.addFile(srcFile, parameters);
		}
		return zipFile.getFile();
	}
	
	//================================================================
	// Purpose:		add file/fold to zip file
	// Parameters:	rootPathInZip: "" for root, "a/b/c" for any os
	// Return:
	// Remark:		it will override old file
	// Author:		welson
	//================================================================
	public static File addToZip(String zipPath, String srcPath, String rootPathInZip) throws ZipException, FileNotFoundException {
		ZipFile zf = null;
		ZipParameters parameters = getDefaultZipParameters();
		parameters.setRootFolderInZip(rootPathInZip);
		
		File dstZipFile = new File(zipPath);
		if(dstZipFile.exists()) {
			zf = new ZipFile(zipPath);
			File srcFile = new File(srcPath);
			if(srcFile.isDirectory()) {
				zf.addFolder(srcPath, parameters);
			} else {
				zf.addFile(srcFile, parameters);
			}
		} else {
			throw new FileNotFoundException();
		}
		
		return zf.getFile();
	}
	public static File addFilesToZip(String zipPath, ArrayList<File> files, String rootPathInZip) throws ZipException, FileNotFoundException {
		ZipFile zf = null;
		ZipParameters parameters = getDefaultZipParameters();
		parameters.setRootFolderInZip(rootPathInZip);

		File dstZipFile = new File(zipPath);
		if(dstZipFile.exists()) {
			zf = new ZipFile(zipPath);
			zf.addFiles(files, parameters);
		} else {
			throw new FileNotFoundException();
		}
		
		return zf.getFile();
	}
			
	/*
	public static File zip(String srcDirPath, String dstPath, String password) throws IOException, ZipException {
		ZipOutputStream outputStream = null;
		InputStream inputStream = null;
		File dstFile = new File(dstPath);
		
		try {
			dstFile = new File(dstPath);
			outputStream = new ZipOutputStream(new FileOutputStream(dstFile)); // overwrite exist zip
			
			ZipParameters parameters = new ZipParameters();
			parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);

			// Set the compression level. This value has to be in between 0 to 9
			// Several predefined compression levels are available
			// DEFLATE_LEVEL_FASTEST - Lowest compression level but higher speed of compression
			// DEFLATE_LEVEL_FAST - Low compression level but higher speed of compression
			// DEFLATE_LEVEL_NORMAL - Optimal balance between compression level/speed
			// DEFLATE_LEVEL_MAXIMUM - High compression level with a compromise of speed
			// DEFLATE_LEVEL_ULTRA - Highest compression level but low speed
			parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
			
			//This flag defines if the files have to be encrypted.
			//If this flag is set to false, setEncryptionMethod, as described below,
			//will be ignored and the files won't be encrypted
			parameters.setEncryptFiles(true);
			
			//Set encryption method to Standard Encryption
			parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_STANDARD);
			
			//self descriptive
			if(password != null) {
				parameters.setPassword(password);
			}
			
			//Now we loop through each file, determine the file CRC and set it
			//in the zip parameters and then we read the input stream and write it
			//to the outputstream
			File[] list = new File(srcDirPath).listFiles();
			for (File file : list) {
				//This will initiate ZipOutputStream to include the file
				//with the input parameters
				outputStream.putNextEntry(file,parameters);
				
				//If this file is a directory, then no further processing is required
				//and we close the entry (Please note that we do not close the outputstream yet)
				if (file.isDirectory()) {
					DBG("isDirectory:"+file.getAbsolutePath());
					outputStream.closeEntry();
					continue;
				}
				
				//Initialize inputstream
				inputStream = new FileInputStream(file);
				byte[] readBuff = new byte[4096];
				int readLen = -1;
				
				//Read the file content and write it to the OutputStream
				while ((readLen = inputStream.read(readBuff)) != -1) {
					outputStream.write(readBuff, 0, readLen);
				}
				
				//Once the content of the file is copied, this entry to the zip file
				//needs to be closed. ZipOutputStream updates necessary header information
				//for this file in this step
				outputStream.closeEntry();
				
				inputStream.close();
			}
			
			//ZipOutputStream now writes zip header information to the zip file
			outputStream.finish();
		} finally {
			if (outputStream != null) {
				outputStream.close();
			}
			
			if (inputStream != null) {
				inputStream.close();
			}
		}
		return dstFile;
	}*/
	
	private static ZipParameters getDefaultZipParameters() {
		ZipParameters param = new ZipParameters();
		param.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
		param.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
		return param;
	}
	private static void DBG(String msg) {
		System.out.println("[MyZip4jUtil] " + msg);
	}
}
