package org.lazyjava.utility;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.imgscalr.Scalr;

public class ScalrUtil
{
/*
	// ====================================================
	// Purpose:		
	// Parameter:
	// Return:
	// Remark:
	// Author: 		JY
	// ====================================================
	public static Point resize(File ImageFile, int newWith, int newHeight, String OutputPath) throws IOException {
        BufferedImage image = null;
        BufferedImage thumbnail = null;
        Point ImageDimension = null;
        float tmpx, tmpy;
        long time1;
        //String tmpStr;
        File tmpFile=null;
        long StartTime=0, EndTime=0;
		
		      image = ImageIO.read(ImageFile) ;  
		      
		      if (image == null) {
		    	  System.out.printf("File name:%s, read image file fail!\n",ImageFile.getPath());
		    	  return new Point(-1,-1);
		      }
		      
		      // check width or height major
		      tmpx = image.getWidth() / newWith;
		      tmpy = image.getHeight() / newHeight;
		      
		      StartTime = System.currentTimeMillis();
		      boolean isSkip = true;
		      if ((image.getWidth() <= newWith ) && (image.getHeight() <= newHeight) ) {
		    	  //System.out.printf("%s Already resized, skip!\n",ImageFile.getName());
		    	  isSkip = true;
		      } else if ( tmpx >= tmpy) { //width major
		    	  isSkip = false;
			      thumbnail =   Scalr.resize(image, Scalr.Method.AUTOMATIC, Scalr.Mode.FIT_TO_WIDTH, newWith, newHeight, Scalr.OP_ANTIALIAS);
		      } else {
		    	  isSkip = false;
			      thumbnail =   Scalr.resize(image, Scalr.Method.AUTOMATIC, Scalr.Mode.FIT_TO_HEIGHT, newWith, newHeight, Scalr.OP_ANTIALIAS);
		      }
		      
		      // keep original extension
		      String ext = ImageFile.getName();
		      ext = ext.substring(ext.lastIndexOf('.') + 1);

		      
		      if (OutputPath == null) { //over write original
		         tmpFile = ImageFile; 
		      } else {
		         tmpFile = new File(OutputPath + ImageFile.getName());  // write back and replace
		      }
		      //System.out.printf("File name:%s, extension is : %s\n",tmpFile.getPath(),ext);
		         
		      if (thumbnail != null) {
		    	  //if (tmpFile.canWrite()) {
		              ImageIO.write(thumbnail, ext , tmpFile);  //ext
		    	  //} else {
		    		//  p.printf("%s can't write<br>\n", tmpFile.getPath());
		    	  //}
		      } else {
		    	  //if (tmpFile.canWrite()) {
		    	  if (! isSkip) {
		    	     ImageIO.write(image, ext, tmpFile);  // keep original file
		    	  }
		    	  //} else {
		    		//  p.printf("%s can't write<br>\n", tmpFile.getPath());
		    	  //}

		      }
		      EndTime = System.currentTimeMillis();
		      
		      if (!isSkip) {
		         time1 = EndTime - StartTime;
		      } else {
		    	 time1 = -1; 
		      }
		      		      
		      if (time1 == -1) {
		    	 ImageDimension = new Point(-1,image.getHeight());  
		      } else {
		         ImageDimension = new Point(image.getWidth(),image.getHeight()); //new Point((int) time1,image.getWidth());  //
		      }
		      //System.out.printf("%d x %d , time=%d\n",image.getWidth(),image.getHeight(),time1);
		      
		return ImageDimension;
	}
*/
	
	public static File convert(File imgFile, String ext, String outputPath) throws IOException {
		File ret = new File(outputPath);
		BufferedImage image = ImageIO.read(imgFile);  
		
		if(ext == null) {
			ext = "png";
		}
		if (image != null) {
			ImageIO.write(image, ext , ret);
			DBG(String.format("Convert %s as %s with ext=%s done.\n", imgFile.getPath(), ret.getPath(), ext));
		} 
		
		return ret;
	}
	
	// ====================================================
	// Purpose:		resize
	// Parameter:	
	// Return:		
	// Remark:
	// Author: 		welson
	// ====================================================	
	public static File resize(File imgFile, String outputPath, int newWith, int newHeight) throws IOException {
		return _resize(imgFile, outputPath, newWith, newHeight, Scalr.Method.AUTOMATIC);
	}
	public static File resizeForQualityConcern(File imgFile, String outputPath, int newWith, int newHeight) throws IOException {
		return _resize(imgFile, outputPath, newWith, newHeight, Scalr.Method.QUALITY);
	}
	public static File resizeForSpeedConcern(File imgFile, String outputPath, int newWith, int newHeight) throws IOException {
		return _resize(imgFile, outputPath, newWith, newHeight, Scalr.Method.SPEED);
	}
	private static File _resize(File imgFile, String outputPath, int newWith, int newHeight, Scalr.Method quality) throws IOException {
		File outputFile = new File(outputPath);
		BufferedImage thumbnail = resizeImage(imgFile, newWith, newHeight, quality);
		
		if(thumbnail != null) {
			// keep original extension
			String ext = getFormat(imgFile.getName());
			
			/*
			if (outputDir == null) { //over write original
				outputFile = imgFile; 
			} else {
				outputFile = new File(outputDir + File.separator + imgFile.getName());  // write back and replace
			}*/
			if(!outputFile.getParentFile().exists()) {
				outputFile.getParentFile().mkdirs();
			}
			
			if (thumbnail != null) {
				ImageIO.write(thumbnail, ext, outputFile);
				DBG(String.format("Resize %s into %s\n", imgFile.getPath(), outputFile.getPath()));
			} 
		} 
		return outputFile;
	}	
	
	// ====================================================
	// Purpose:		crop
	// Parameter:	outputPath: null to override original.
	// Return:		
	// Remark:
	// Author: 		welson
	// ====================================================	
	public static File crop(File imageFile, int x, int y, int w, int h, String outputPath) throws IOException {
		File ret = null;
		BufferedImage output = null;
        BufferedImage input = ImageIO.read(imageFile);  
		      
        if (input != null) {
        	// keep original extension
			String ext = getFormat(imageFile.getName());
        				
        	// check width or height major
        	float oriW = input.getWidth();
        	float oriH = input.getHeight();
  
        	if(x>=0 && y>=0 && w>0 && h>0 && x+w<=oriW && y+h<=oriH) {
        		output = Scalr.crop(input, x, y, w, h);
        	} else {
        		output = input;
        	}
        	
        	if (outputPath == null) { //over write original
        		outputPath = imageFile.getAbsolutePath(); 
			}
			
        	ret = new File(outputPath);
			if (output != null) {
				ImageIO.write(output, ext, ret);
				DBG(String.format("Crop %s as %s\n", imageFile.getPath(), ret.getPath()));
			} 
        	input.flush(); // http://www.thebuzzmedia.com/downloads/software/imgscalr/javadoc/org/imgscalr/Scalr.html
        }
        
        return ret;
	}	
	
	// ====================================================
	// Purpose:		
	// Parameter:	ScalrMethodQuality: Scalr.Method.AUTOMATIC, Scalr.Method.SPEED, ...
	// Return:
	// Remark:		http://www.thebuzzmedia.com/downloads/software/imgscalr/javadoc/org/imgscalr/Scalr.Method.html
	// Author: 		welson
	// ====================================================		
	public static BufferedImage resizeImage(File imageFile, int newWidth, int newHeight, Scalr.Method quality) throws IOException, ArithmeticException {
		return resizeImage(imageFile, newWidth, newHeight, quality, false);
	}
	public static BufferedImage resizeImage(File imageFile, int newWidth, int newHeight, Scalr.Method quality, boolean fitLongerEdge) throws IOException, ArithmeticException {

		BufferedImage thumbnail = null;
        BufferedImage image = ImageIO.read(imageFile);  
        
        if(quality == null) {
        	quality = Scalr.Method.AUTOMATIC;
        }

        if (image != null) {
        	if(fitLongerEdge) {
	        	// check width or height major
	        	float tmpx = image.getWidth() / newWidth;
	        	float tmpy = image.getHeight() / newHeight;
	        	
	        	if((image.getWidth() <= newWidth)  &&  (image.getHeight() <= newHeight)) {
	        		thumbnail = image;
	        	} else if (tmpx >= tmpy) { //width major
				    thumbnail = Scalr.resize(image, quality, Scalr.Mode.FIT_TO_WIDTH, newWidth, newHeight, Scalr.OP_ANTIALIAS);
			    } else {
			    	thumbnail = Scalr.resize(image, quality, Scalr.Mode.FIT_TO_HEIGHT, newWidth, newHeight, Scalr.OP_ANTIALIAS);
			    }
        	} else {
        		thumbnail = Scalr.resize(image, quality, Scalr.Mode.FIT_EXACT, newWidth, newHeight, Scalr.OP_ANTIALIAS);
        	}
        }
        
        //thumbnail = ImageProcessingUtil.resampleImage(thumbnail, newWidth, newHeight);
        
        return thumbnail;
	}
	
	protected static void DBG(String msg) {
		//System.out.printf("[ScalrUtil] %s\n", msg);
	}
	
    private static String getFormat(String inputName) {
    	int idx = inputName.lastIndexOf(".");
    	String formatExt = null;
    	if(idx >= 0) {
    		formatExt = inputName.substring(idx + 1);	
    	} else {
    		formatExt = "png";
    	}
    	return formatExt;
    }
}
