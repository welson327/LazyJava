package org.lazyjava.utility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;




public class FileUtil {
    //private static final String folder_pattern = "[!@$^a-zA-Z0-9_\\-\\.]+";
    //private static final String folder_pattern = "[!@$a-zA-Z0-9_\\-.]+";    // 2012-9-18 fix
    private static final String folder_pattern = "[!@a-zA-Z0-9_\\-.]+";    // 2012-11-12 fix
    
    public static boolean isLegalFolderName(String name) {
        if(name.equals(""))
            return false;
        if(name.length() > 255)
            return false;
        if(name.matches(folder_pattern))
            return true;
        return false;
    }
    
    public static boolean isExist(String fullpath) {
        return new File(fullpath).exists();
    }
    
    public static boolean deleteSymbolicLink(String path) throws IOException {
    	//Files.delete(Paths.get(path));
    	return Files.deleteIfExists(Paths.get(path)); //  no exception if the file does not exist 
    }
    public static Path createSymbolicLink(String newLink, String target) throws IOException {
    	File newLinkFile = new File(newLink);
    	if(!newLinkFile.getParentFile().exists()) {
    		newLinkFile.getParentFile().mkdirs();
    	}
    	return Files.createSymbolicLink(Paths.get(newLink), Paths.get(target));
    }
    public static Path createSymbolicLinkForcely(String newLink, String target) throws IOException {
    	File newLinkFile = new File(newLink);
    	if(!newLinkFile.getParentFile().exists()) {
    		newLinkFile.getParentFile().mkdirs();
    	}
    	if(newLinkFile.exists()) {
    		deleteSymbolicLink(newLink);
    	}
    	return Files.createSymbolicLink(Paths.get(newLink), Paths.get(target));
    }
    
    public static boolean isSymbolicLink(String fullpath) throws IOException {
    	if (fullpath == null) {
    		return false;
    	} else {
    		/*
    		ref: https://code.google.com/p/opendedup/issues/detail?id=34
    		If following exception happen, please check os system locale. "LANG=zh_TW.UTF-8" will be OK.
    		java.nio.file.InvalidPathException: Malformed input or input contains unmappable chacraters: /yiabiRoot/webuser/softlink/???
    		        at sun.nio.fs.UnixPath.encode(UnixPath.java:147)
    		        at sun.nio.fs.UnixPath.<init>(UnixPath.java:71)
    		        at sun.nio.fs.UnixFileSystem.getPath(UnixFileSystem.java:281)
    		        at java.nio.file.Paths.get(Paths.java:84)
    		*/
    		return Files.isSymbolicLink(Paths.get(fullpath));
    	}
    } 
    
    public static boolean createFolder(String fullpath) throws IOException {
        if(fullpath == null || fullpath.equals("")) {
            return false;
        } else {
        	File f = new File(fullpath);
        	if(f.exists()) {
        		return false;
        	} else {
        		return f.mkdirs();
        	}
        }
    }
    
    public static boolean createFolderUntilExist(String fullpath, long checkIntervalInMills, int timeout) throws IOException, InterruptedException {
    	long total = 0;
    	createFolder(fullpath);
    	while(!isExist(fullpath)) {
			Thread.sleep(checkIntervalInMills);
    		total += checkIntervalInMills;
    		if(total >= timeout) {
    			return false;
    		}
    	}
    	return true;
    }
    
    public static boolean remove(String fullpath) throws IOException, InterruptedException {
//        String cmd = "rm -rf " + fullpath;
//        Runtime.getRuntime().exec(cmd);
//        do {
//        	Thread.sleep(700);
//        } while(isExist(fullpath)); // fix: maybe remove a big data
//        return true;
        
        File f = new File(fullpath);
        boolean rslt = f.delete();
        while(f.exists()) { // fix: maybe remove a big data
        	Thread.sleep(700);
        }
        return rslt;
    }
    
    public static boolean rename(String oldname, String newname) throws IOException {
//        if(isLegalFolderName(new File(oldname).getName())==false) {
//            return false;
//        }
//        if(isLegalFolderName(new File(newname).getName())==false) {
//            return false;
//        }
//        if(oldname.equals(newname))
//            return true;
//            
//        String cmd = String.format("mv -f %s %s", oldname, newname);
//        Runtime.getRuntime().exec(cmd);
//        return true;
        
        
        // new
        return new File(oldname).renameTo(new File(newname));
    }
    
    public static File[] search(String path, String _keyword, boolean _only_folder) {
    	
        final String keyword = _keyword;
    	final boolean only_folder = _only_folder;
    	
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if(name.contains(keyword)) {
					if(only_folder) {
						String fullpath = dir.getPath()+ File.separator + name;                       
						if(new File(fullpath).isDirectory())
							return true;
						else
							return false;
					}
					else
						return true;
				}
				return false;
			}
		};
		File[] rslt = new File(path).listFiles(filter);
		return rslt; 
    }
        
    /*
    public static ArrayList<String> search(String path, String keyword) throws IOException {
        ArrayList<String> list = new ArrayList<String>();
        String tmpfile = "/tmp/_sharefolder_search_rslt";
        String cmd = "find " + path + " -name '" + keyword + "' > " + tmpfile;

        Runtime.getRuntime().exec(cmd);
        
        // parse
        String tmpstr = null;
        InputStreamReader isr = new InputStreamReader(new FileInputStream(tmpfile), "UTF-8");
	    BufferedReader br = new BufferedReader(isr);
        while((tmpstr=br.readLine()) != null)
            list.add(new String(tmpstr));

        //Runtime.getRuntime().exec("rm -rf " + tmpfile); // sync problem
        isr.close();
        br.close();
            
        return list;
    }*/
     
    public static ArrayList<String> list(String path, boolean is_return_fullpath) throws SecurityException {
        
    	ArrayList<String> arraylist = new ArrayList<String>();          

    	if(path.equals(""))
            return arraylist;
            
        File[] f = new File(path).listFiles();     
        for(int i=0; i<f.length; ++i) {
            if(is_return_fullpath)
                arraylist.add(f[i].getParent() + File.separator + f[i].getName());
            else
                arraylist.add(f[i].getName());
        }
                
        return arraylist;
    }
    
    public static ArrayList<String> listFiles(String path, boolean isReturnFullPath) throws SecurityException {
    	ArrayList<String> list = new ArrayList<String>();
        File[] f = new File(path).listFiles();
        if(isReturnFullPath) {
        	for(int i=0; i<f.length; ++i) {
                if(f[i].isFile()) {
                	//list.add(f[i].getParent() + File.separator + f[i].getName());
                    list.add(path + File.separator + f[i].getName());
                }
            }
        } else {
        	for(int i=0; i<f.length; ++i) {
                if(f[i].isFile()) {
                    list.add(f[i].getName());
                }
            }
        }
        return list;
    }
     
    public static ArrayList<String> listDirectories(String path, boolean isReturnFullPath) throws SecurityException {
    	ArrayList<String> list = new ArrayList<String>();
        File[] f = new File(path).listFiles();  
        if(isReturnFullPath) {
        	for(int i=0; i<f.length; ++i) {
                if(f[i].isDirectory()) {
                	//list.add(f[i].getParent() + File.separator + f[i].getName());
                    list.add(path + File.separator + f[i].getName());
                }
            }
        } else {
        	for(int i=0; i<f.length; ++i) {
                if(f[i].isDirectory()) {
                    list.add(f[i].getName());
                }
            }
        }
        return list;
    }
    
    public static boolean isDir(String fullpath) throws SecurityException {
        return new File(fullpath).isDirectory();
    }
    
    //===============================================================
    // Purpose:		Read file and must return value if file not found
    // Parameters:
    // Return:
    // Remark:		http://www.javapractices.com/topic/TopicAction.do?Id=42
    //				(FileReader implicitly use the system's default character encoding)
    // Author:		welson
    //===============================================================
    public static StringBuilder readFile(String file, String linefeed) throws FileNotFoundException, IOException {
    	InputStreamReader isr = null;
    	BufferedReader br = null;
    	String tmpstr = null;
    	StringBuilder sb = new StringBuilder(""); // StringBuffer is synchronized, StringBuilder is not.
    	
    	if(!new File(file).exists()) {
    		return sb;
    	}
    	if(linefeed == null) {
    		linefeed = "";
    	}
    	
    	try {
    		//FileReader fr = new FileReader(file); 
    		//BufferedReader br = new BufferedReader(fr);
    		isr = new InputStreamReader(new FileInputStream(file), "UTF-8");
    		br = new BufferedReader(isr);
    		
    		while((tmpstr=br.readLine()) != null) {
    			sb.append(new String(tmpstr) + linefeed);
    		}
    	} catch (FileNotFoundException e) {
    		e.printStackTrace();
    		throw e;
    	} catch (IOException e) {
    		e.printStackTrace();
    		throw e;
    	} finally {
    		// Can check by: sudo du -a /proc/${pid}/fd | wc;
    		if(isr != null) {
    			isr.close();
    			isr = null;
    		}
    		if(br != null) {
    			br.close();
    			br = null;
    		}
    	}
    	
    	return sb;
    }
    
    //===============================================================
    // Purpose:		Read file and must return value if file not found
    // Parameters:
    // Return:
    // Remark:		
    // Author:		welson
    //===============================================================
    public static ArrayList<String> readFile(String file) throws FileNotFoundException, IOException {
    	InputStreamReader isr = null;
    	BufferedReader br = null;
    	String tmpstr = null;
    	ArrayList<String> list = new ArrayList<String>();
    	
    	if(!new File(file).exists()) {
    		return list;
    	}
    	
    	try {
    		isr = new InputStreamReader(new FileInputStream(file), "UTF-8");
    		br = new BufferedReader(isr);
    		
    		while((tmpstr=br.readLine()) != null) {
    			list.add(new String(tmpstr));
    		}
    	} catch (FileNotFoundException e) {
    		e.printStackTrace();
    		throw e;
    	} catch (IOException e) {
    		e.printStackTrace();
    		throw e;
    	} finally {
    		if(isr != null) {
    			isr.close();
    			isr = null;
    		}
    		if(br != null) {
    			br.close();
    			br = null;
    		}
    	}
    	
    	return list;
    }
    public static ArrayList<String> readFile(String file, int offset, int length) throws FileNotFoundException, IOException {
    	InputStreamReader isr = null;
    	BufferedReader br = null;
    	String tmpstr = null;
    	ArrayList<String> list = new ArrayList<String>();
    	int idx = -1;
    	
    	if(!new File(file).exists()) {
    		return list;
    	}
    	
    	try {
    		isr = new InputStreamReader(new FileInputStream(file), "UTF-8");
    		br = new BufferedReader(isr);
    		
    		while((tmpstr=br.readLine()) != null) {
    			++idx;
    			if(idx >= offset && list.size() < length) {
    				list.add(new String(tmpstr));
    			}
    			if(list.size() >= length) {
    				break;
    			}
    		}
    	} catch (FileNotFoundException e) {
    		e.printStackTrace();
    		throw e;
    	} catch (IOException e) {
    		e.printStackTrace();
    		throw e;
    	} finally {
    		if(isr != null) {
    			isr.close();
    			isr = null;
    		}
    		if(br != null) {
    			br.close();
    			br = null;
    		}
    	}
    	
    	return list;
    }
    
    //===============================================================
    // Purpose:		Read file. Throws exception if any error
    // Parameters:
    // Return:
    // Remark:		
    // Author:		welson
    //===============================================================
    public static String read(String file, String linefeed) throws FileNotFoundException, IOException {
    	InputStreamReader isr = null;
        BufferedReader br = null;
        String tmpstr = null;
        StringBuilder sb = new StringBuilder(""); // StringBuffer is synchronized, StringBuilder is not.

        if(linefeed == null) {
        	linefeed = "";
        }
        
        try {
	        isr = new InputStreamReader(new FileInputStream(file), "UTF-8");
	        br = new BufferedReader(isr);
	        while((tmpstr=br.readLine()) != null) {
	            sb.append(new String(tmpstr) + linefeed);
	        }
    	} catch (FileNotFoundException e) {
    		e.printStackTrace();
    		throw e;
    	} catch (IOException e) {
    		e.printStackTrace();
    		throw e;
    	} finally {
    		if(isr != null) {
    			isr.close();
    			isr = null;
    		}
    		if(br != null) {
    			br.close();
    			br = null;
    		}
    	}
        return sb.toString();
    }

    //===============================================================
    // Purpose:		Read file. Throws exception if any error
    // Parameters:
    // Return:
    // Remark:		
    // Author:		welson
    //===============================================================
    public static ArrayList<String> read(String file) throws FileNotFoundException, IOException {
    	InputStreamReader isr = null;
        BufferedReader br = null;
        String tmpstr = null;
        ArrayList<String> list = new ArrayList<String>();

        try {
	        isr = new InputStreamReader(new FileInputStream(file), "UTF-8");
	        br = new BufferedReader(isr);
	        while((tmpstr=br.readLine()) != null) {
	            list.add(new String(tmpstr));
	        }
    	} catch (FileNotFoundException e) {
    		e.printStackTrace();
    		throw e;
    	} catch (IOException e) {
    		e.printStackTrace();
    		throw e;
    	} finally {
    		if(isr != null) {
    			isr.close();
    			isr = null;
    		}
    		if(br != null) {
    			br.close();
    			br = null;
    		}
    	}
        return list;
    }
    
    public static void append(String str, String filePath) throws IOException {
    	File f = null;
    	FileWriter fw = null;
    	try {
    		f = new File(filePath);
    		if(!f.exists()) {
    			if(!f.getParentFile().exists()) {
    				f.getParentFile().mkdirs();
    			}
    			f.createNewFile();
    		}
    		
    		fw = new FileWriter(f, true);
    		fw.write(str);
    	} catch(IOException e) {
    		throw e;
    	} finally {
    		if(fw != null) {
    			fw.close();
    		}
    	}
    }
    public static void append(ArrayList<String> lines, String filePath) throws IOException {
    	File f = null;
    	FileWriter fw = null;
    	try {
    		f = new File(filePath);
    		if(!f.exists()) {
    			if(!f.getParentFile().exists()) {
    				f.getParentFile().mkdirs();
    			}
    			f.createNewFile();
    		}
    		
    		fw = new FileWriter(f, true);
    		for(int i=0; i<lines.size(); ++i) {
    			fw.write(lines.get(i));
    		}
    	} catch(IOException e) {
    		throw e;
    	} finally {
    		if(fw != null) {
    			fw.close();
    		}
    	}
    }
    public static boolean prepend(String str, String filePath) throws IOException {
    	String oldData = readFile(filePath, System.lineSeparator()).toString();
    	return writeFile(str+oldData, filePath);
    	
    	/*
    	RandomAccessFile f = null;
    	try {
    		f = new RandomAccessFile(new File(filePath), "rw");
    		f.getChannel().position(0);
    		f.write(str.getBytes("UTF-8"));
    	} catch(IOException e) {
    		throw e;
    	} finally {
    		if(f != null) {
    			f.close();
    		}
    	}*/
    }
    public static boolean prepend(ArrayList<String> lines, String filePath) throws IOException {
    	ArrayList<String> newLines = readFile(filePath);
    	int len = lines.size();
    	for(int i=len-1; i>=0; --i) {
    		newLines.add(0, lines.get(i));
		}
    	return writeFile(newLines, filePath);
    }
    
    public static StringBuilder nioReadFile(String filePath, String linefeed) throws FileNotFoundException, IOException {
    	String tmpstr = null;
    	BufferedReader br = null;
    	StringBuilder sb = null; // StringBuffer is synchronized, StringBuilder is not.

        if(linefeed == null)
        	linefeed = "";
        
        try {
        	sb = new StringBuilder();
        	br = Files.newBufferedReader(Paths.get(filePath), Charset.defaultCharset());
        	//br = Files.newBufferedReader(Paths.get(filePath), Charset.forName("UTF-8"));
            while ((tmpstr=br.readLine()) != null) {
            	sb.append(new String(tmpstr) + linefeed);
            }
    	} catch (FileNotFoundException e) {
    		e.printStackTrace();
    		throw e;
    	} catch (IOException e) {
    		e.printStackTrace();
    		throw e;
    	} finally {
    		if(br != null) {
    			br.close();
    			br = null;
    		}
    	}
        return sb;
    }
    
    public static List<String> nioReadFile(String filePath) throws FileNotFoundException, IOException {
    	String tmpstr = null;
    	BufferedReader br = null;
    	List<String> list = null;
    	
        try {
        	list = new ArrayList<String>();
        	br = Files.newBufferedReader(Paths.get(filePath), Charset.defaultCharset());
        	//br = Files.newBufferedReader(Paths.get(filePath), Charset.forName("UTF-8"));
            while ((tmpstr=br.readLine()) != null) {
            	list.add(tmpstr); //list.add(new String(tmpstr));
            }
    	} catch (FileNotFoundException e) {
    		e.printStackTrace();
    		throw e;
    	} catch (IOException e) {
    		e.printStackTrace();
    		throw e;
    	} finally {
    		if(br != null) {
    			br.close();
    			br = null;
    		}
    	}
        
        return list;
    }
    
    public synchronized static boolean writeFile(String data, String file) throws IOException {
    	BufferedWriter bw = null;
    	try {
	    	if(data == null)
	    		return false;
	    	
	    	bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),"UTF-8"));
	    	bw.write(data);
	    	//bw.newLine();
	    	//bw.flush(); // BufferedWriter required
    	} finally {
    		if(bw != null) {
    			bw.close();
    			bw = null;
    		}
    	}
    	return true;
    }
    
    public synchronized static boolean writeFile(ArrayList<String> lines, String file) throws IOException {
    	BufferedWriter bw = null;
    	try {
	        if(lines == null)
	            return false;
	
	        int len = lines.size();
	        StringBuffer buffer = new StringBuffer();
	        String lineSeparator = System.lineSeparator();
	        //String lineSeparator = System.getProperty("line.separator");
	        
	        bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
	        for(int i=0; i<len; ++i) {
	        	buffer.append((String) lines.get(i) + lineSeparator);
	        }
	        
	        bw.write(buffer.toString());
	        //bw.newLine();
	        //bw.flush(); // BufferedWriter required
    	} finally {
    		if(bw != null) {
    			bw.close();
    			bw = null;
    		}
    	}
		
		return true;
    }
    
    public synchronized static boolean nioWriteFile(String data, String filePath) throws IOException {
    	BufferedWriter bw = null;
    	try {
    		if(data == null)
    			return false;
    		
	    	bw = Files.newBufferedWriter(Paths.get(filePath),  StandardCharsets.UTF_8, StandardOpenOption.WRITE);
	    	bw.write(data);
	    	//bw.newLine();
	    	//bw.flush(); // BufferedWriter required
    	} finally {
    		if(bw != null) {
        		bw.close();
        		bw = null;
        	}
    	}
    	return true;
    }
    
    public synchronized static boolean nioWriteFile(List<String> list, String filePath) throws IOException {
    	BufferedWriter bw = null;
        try {
        	if(list == null)
        		return false;
        	
	        bw = Files.newBufferedWriter(Paths.get(filePath),  StandardCharsets.UTF_8, StandardOpenOption.WRITE);
	        StringBuffer buffer = new StringBuffer();
	        //String lineSeparator = System.getProperty("line.separator");
	        String lineSeparator = System.lineSeparator();
	        int len = list.size();
	        
	        for(int i=0; i<len; ++i) {
	        	buffer.append(list.get(i) + lineSeparator);
	        }
	        
	        bw.write(buffer.toString());
	        //bw.newLine();
	        //bw.flush(); // BufferedWriter required
        } finally {
        	if(bw != null) {
        		bw.close();
        		bw = null;
        	}
        }
		
		return true;
    }
    
    public static String getNameWithoutExtension(String filename) {
    	int idx = filename.lastIndexOf(".");
    	String name = filename;
    	if(idx >= 0) {
    		name = filename.substring(0, idx);	
    	}
    	return name;
    }    
    public static String getExtension(String filename) {
    	int idx = filename.lastIndexOf(".");
    	String ext = null;
    	if(idx >= 0) {
    		ext = filename.substring(idx + 1);	
    	} else {
    		ext = "";
    	}
    	return ext;
    }    
    public static String readableSize(long size) {
        if(size <= 0) return "0";
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }    
    
    //===============================================================
    // Purpose:
    // Parameters:
    // Return:		null if the content type cannot be determined
    //				ex: image/[jpeg|gif|png], text/plain, text/html, application/pdf
    //				MSOffice: application/msword, application/vnd.ms-excel, application/vnd.ms-powerpoint
    // Remark:		http://docs.oracle.com/javase/7/docs/api/java/nio/file/Files.html#probeContentType(java.nio.file.Path)
    //				MS Office MIME: http://technet.microsoft.com/en-us/library/ee309278%28office.12%29.aspx
    // Author:		welson
    //===============================================================
    public static String getMimeType(File f) throws IOException {
    	//Path path = FileSystems.getDefault().getPath(f.getParent(), f.getName());
    	Path path = FileSystems.getDefault().getPath(f.getAbsolutePath());
		return Files.probeContentType(path);
    }
    
    //===============================================================
    // Purpose:
    // Parameters:
    // Return:		
    // Remark:		http://puremonkey2010.blogspot.tw/2010/10/java-utf-8.html?m=0
    // Author:		welson
    //===============================================================
	public static boolean isUTF8(File file) throws IOException {  
	    try {  
	        byte[] buf = FileUtils.readFileToByteArray(file);  
	        //System.out.println("\t<<>>");  
	        //showBinary(buf);  
	        String UTF8Cntent = FileUtils.readFileToString(file, "UTF-8");  
	        //String big5Cntent = new String(buf, "Big5");  
	        //String defCntent = new String(buf); //Default is UTF8  
	        //System.out.println("\t<<>>\n"+UTF8Cntent);  
	        //showBinary(UTF8Cntent);  
	        //System.out.println("\t<<>>\n"+big5Cntent);  
	        //showBinary(big5Cntent);  
	        //System.out.println("\t<<>>\n"+defCntent);  
	        //showBinary(defCntent);  
	          
	        if(buf.length == UTF8Cntent.getBytes().length) {  
	            byte[] buf_utf8 = UTF8Cntent.getBytes();  
	            for(int i=0; i<buf_utf8.length; ++i) {
	                if(buf_utf8[i] != buf[i]) {  
	                    return false;  
	                }  
	            }  
	            return true;  
	        }  
	    } catch (IOException e) {  
	        e.printStackTrace();  
	        throw e;
	    }  
	    return false;  
	}  
}
