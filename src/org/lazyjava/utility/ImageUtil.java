package org.lazyjava.utility;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Rotation;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;

public class ImageUtil {
	public static File fillBackground(File inputFile, Color c, String outputPath) throws IOException {
		File output = null;
    	
    	BufferedImage inputImage = ImageIO.read(new FileInputStream(inputFile)); // fix if no registered ImageReader is not found
    	//BufferedImage inputImage = ImageIO.read(inputFile);
 
    	int w = inputImage.getWidth();
    	int h = inputImage.getHeight();
    	
    	//BufferedImage outputImage = new BufferedImage(w, h, inputImage.getType());
        BufferedImage outputImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
       
        // scales the input image to the output image
        Graphics2D g2d = outputImage.createGraphics();
    	g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    	
    	//g2d.setBackground(Color.WHITE);
    	//g2d.clearRect(0, 0, w, h); // for .setBackground()
    	if(c == null) {
    		c = Color.WHITE;
    	}
    	g2d.setColor(c);
        g2d.fillRect(0, 0, w, h);
    	
    	g2d.drawImage(inputImage, 0, 0, w, h, null);
        g2d.dispose();
 
        // extracts extension of output file
        //String formatName = "png";
        String formatName = outputPath.substring(outputPath.lastIndexOf(".") + 1);
        
 
        // writes to output file
        output = new File(outputPath);
        if(!output.getParentFile().exists()) {
        	output.getParentFile().mkdirs();
        }
        ImageIO.write(outputImage, formatName, output);
        
        return output;
	}
	
	// ====================================================
	// Purpose:		Resizes an image to a absolute width and height (the image may not be proportional)
	// Parameter:
	// Return:
	// Remark:		reads input image: http://docs.oracle.com/javase/tutorial/2d/images/loadimage.html
	//				RenderingHints: http://docs.oracle.com/javase/tutorial/2d/advanced/quality.html
	// Author: 		welson
	// ====================================================
    public static File resize(File inputFile, String outputImagePath, int scaledWidth, int scaledHeight, Object hintValue) throws IOException {
    	File output = null;
    	
    	BufferedImage inputImage = ImageIO.read(new FileInputStream(inputFile)); // fix if no registered ImageReader is not found
    	//BufferedImage inputImage = ImageIO.read(inputFile);
 
        BufferedImage outputImage = new BufferedImage(scaledWidth, scaledHeight, inputImage.getType());
       
        // scales the input image to the output image
        Graphics2D g2d = outputImage.createGraphics();
        if(hintValue != null) {
        	//hintValue = RenderingHints.VALUE_INTERPOLATION_BILINEAR;
        	g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hintValue);
        }
        g2d.drawImage(inputImage, 0, 0, scaledWidth, scaledHeight, null);
        g2d.dispose();
 
        // extracts extension of output file
        String formatName = outputImagePath.substring(outputImagePath.lastIndexOf(".") + 1);
 
        // writes to output file
        output = new File(outputImagePath);
        if(!output.getParentFile().exists()) {
        	output.getParentFile().mkdirs();
        }
        ImageIO.write(outputImage, formatName, output);
        
        return output;
    }
    public static File resize(File imgFile, String outputImagePath, int scaledWidth, int scaledHeight) throws IOException {
    	return resize(imgFile, outputImagePath, scaledWidth, scaledHeight, null);
    }
	public static File resize(String inputImagePath, String outputImagePath, int scaledWidth, int scaledHeight) throws IOException {
		return resize(new File(inputImagePath), outputImagePath, scaledWidth, scaledHeight, null);
	}
 
	// ====================================================
	// Purpose:		Resizes image by a percentage of original size (proportional).
	// Parameter:
	// Return:
	// Remark:		
	// Author: 		welson
	// ====================================================
    public static File resize(String inputImagePath, String outputImagePath, double percent) throws IOException {
    	return resize(new File(inputImagePath), outputImagePath, percent);
    }
    public static File resize(File inputFile, String outputImagePath, double percent) throws IOException {
        BufferedImage inputImage = ImageIO.read(inputFile);
        int scaledWidth = (int) (inputImage.getWidth() * percent);
        int scaledHeight = (int) (inputImage.getHeight() * percent);
        return resize(inputFile, outputImagePath, scaledWidth, scaledHeight, null);
    }
    
    public static int[] getSize(File imgFile) throws IOException {
    	BufferedImage buf = ImageIO.read(imgFile);
    	int[] size = new int[2];
    	size[0] = buf.getWidth();
    	size[1] = buf.getHeight();
    	return size;
    }
    
	// ====================================================
	// Purpose:		
	// Params:	override original if outputPath == null
	// Return:
	// Remark:		
	// Author: 	Amingo
	// ====================================================
    public static File correctOrientation(File imageFile, String outputPath) throws IOException {
    	File outputFile = null;
    	try {
			Metadata metadata = JpegMetadataReader.readMetadata(imageFile);
			if(metadata == null) 
				return imageFile;
			
			Directory exif = metadata.getDirectory(ExifIFD0Directory.class);
			if(exif == null) 
				return imageFile;
			
			String orientation = exif.getDescription(ExifIFD0Directory.TAG_ORIENTATION);
			if(orientation == null || orientation.equals("")) 
				return imageFile;
			
			System.out.printf("[ImageUtil#correctOrientation] orientation:%s for %s\n", orientation, imageFile);
			
			BufferedImage output = null;
	        BufferedImage input = ImageIO.read(imageFile);  
	        
	        String[] orientationAry = orientation.split(",");
	        Rotation rotation = null;
	        // 2:flip horizontal
	        if(orientationAry[0].toLowerCase().contains("top") && orientationAry[1].toLowerCase().contains("right")) {
	        	rotation = Rotation.FLIP_HORZ;
			}
			// 3:轉180度
	        else if(orientationAry[0].toLowerCase().contains("bottom") && orientationAry[1].toLowerCase().contains("right")) {
	        	rotation = Rotation.CW_180;
			}
	        // 4:flip vertical
	        else if(orientationAry[0].toLowerCase().contains("bottom") && orientationAry[1].toLowerCase().contains("left")) {
	        	rotation = Rotation.FLIP_VERT;
			}
			// 6:轉90度
			else if(orientationAry[0].toLowerCase().contains("right") && orientationAry[1].toLowerCase().contains("top")) {
				rotation = Rotation.CW_90;
			}
			// 8:轉270度
			else if(orientationAry[0].toLowerCase().contains("left") && orientationAry[1].toLowerCase().contains("bottom")) {
				rotation = Rotation.CW_270;
			}
	        // 1,5,7不處理
			else {
				return imageFile;
			}
			
	        output = Scalr.rotate(input, rotation);
	        
	        if(output != null) {
				// keep original extension
				String ext = imageFile.getName();
				ext = ext.substring(ext.lastIndexOf('.') + 1);
				
				if (outputPath == null) { //over write original
					outputFile = imageFile; 
				} else {
					outputFile = new File(outputPath);  // write back and replace
				}
				
				if (output != null) {
					ImageIO.write(output, ext, outputFile);
					//System.out.printf("[ImageUtil#correctOrientation] Rotate %s into %s\n", imageFile.getPath(), outputFile.getPath());
				} 
			} 
	        System.out.printf("[ImageUtil#correctOrientation] Rotate %s for %s\n", rotation.name(), imageFile.getPath());
		} catch (JpegProcessingException e) {
			outputFile = imageFile;
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
			//outputFile = imageFile;
		}
    	return outputFile;
	}
    
	// ====================================================
	// Purpose:		
	// Parameter:	
	// Return:
	// Remark:		(1) https://github.com/drewnoakes/metadata-extractor
    //				(2) http://stackoverflow.com/questions/21951892/how-to-determine-and-auto-rotate-images
    // Dependency:  xmpcore.jar
	// Author: 		welson
	// ====================================================
    public static Metadata getMeta(File image) throws IOException {
    	Metadata metadata = null;
		try {
			metadata = ImageMetadataReader.readMetadata(image);
		
			/*
			Directory exif = metadata.getDirectory(ExifIFD0Directory.class);
			if(exif != null) {
				String orientation = exif.getDescription(ExifIFD0Directory.TAG_ORIENTATION);
				System.out.println("----- orientation:"+orientation+" -----");
			} else {
				System.out.println("EXIF empty!");
			}
			*/
		} catch (ImageProcessingException e) {
			e.printStackTrace();
		}
		
    	return metadata;
    }
    
	// ====================================================
	// Purpose:		convert dataUrl string to image
	// Parameter:	dataUrl: ex: "data:image/png;base64,iVBORw0KGgoxxxxxxxxx", must start with format of "data:image/xxx;base64"
	// Return:
	// Remark:		
	// Author: 		welson
	// ====================================================
    public static BufferedImage base64ToPNG(String dataUrl, String outputPath) throws IOException {
    	BufferedImage image = null;
    	ByteArrayInputStream bis = null;
    	try {
	    	String imageString = dataUrl.substring(dataUrl.indexOf(",")+1);
	    	byte[] imageBytes = Base64.decodeBase64(imageString);
	    	bis = new ByteArrayInputStream(imageBytes);
	    	image = ImageIO.read(bis);
    	} finally {
    		if(bis != null) {
    			bis.close();
    			bis = null;
    		}
    	}
    	return image;
    }
    
    public static File base64ToFile(String dataUrl, String outputPath) throws IOException {
    	File outputFile = null;
    	FileOutputStream fos = null;
    	try {
    		outputFile = new File(outputPath);
	    	String imageString = dataUrl.substring(dataUrl.indexOf(",")+1);
	    	byte[] imageBytes = Base64.decodeBase64(imageString);
	        
	    	if(!outputFile.getParentFile().exists()) {
	    		outputFile.getParentFile().mkdirs();
	    	}
	    	if(!outputFile.exists()) {
	    		outputFile.createNewFile();
	    	} 
	    	
	    	// v1
	    	/*
	        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
	        ImageIO.write(image, "png", outputFile);
	    	*/
	        
	    	// v2
	        fos = new FileOutputStream(outputFile);
	        fos.write(imageBytes);
    	} finally {
    		if(fos != null) {
    			fos.close();
    			fos = null;
    		}
    	}
    	return outputFile;
    }
    
    public static String toBase64(File img) throws IOException {
    	byte[] b = Base64.encodeBase64(FileUtils.readFileToByteArray(img));
    	return "data:image/png;base64," + new String(b, "UTF-8");
    }
}
