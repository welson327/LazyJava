package org.lazyjava.utility;

import java.awt.image.BufferedImage;

import com.mortennobel.imagescaling.ResampleOp;
import com.mortennobel.imagescaling.AdvancedResizeOp.UnsharpenMask;
/*
 * Image Processing Util
 * 
 * Dependency: Filter.jar, java-image-scaling-0.8.6.jar
 */
public class ImageProcessingUtil {
	// ====================================================
	// Purpose:		resample image.(Result in better image quality)
	// Parameter:	
	// Return:
	// Remark:		
	// Author: 		Amingo
	// ====================================================	
	public static BufferedImage resampleImage(BufferedImage src, int newWidth, int newHeight) {
		ResampleOp resampleOp = new ResampleOp(newWidth, newHeight);
		resampleOp.setUnsharpenMask(UnsharpenMask.Normal);
		return resampleOp.filter(src, null);
	}
}
