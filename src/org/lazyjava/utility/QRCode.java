package org.lazyjava.utility;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.EncodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.swetake.util.Qrcode;

public class QRCode {
	private static final int DEF_QRCODE_SIZE = 140;
	
	public static File generate(String str, String outputPath, int w) throws IOException {
		BufferedImage bi = generate(str, w);
        File f = new File(outputPath);
        ImageIO.write(bi, "png", f);
        return f;
	}
	
	//===============================================================
	// Purpose: 	generate QRCode by swetake
	// Parameters:	str: content of qrcode
	//				w: dimension of qrcode
	// Return:
	// Remark:		http://blog.yslifes.com/archives/684
	//				http://www.oschina.net/code/snippet_96965_17226
	//				JAR: http://www.swetake.com/qrcode/java/qr_java.html
					/* 
					QR碼資料容量:
					 	數字	最多7,089字元
						字母	最多4,296字元
						二進位數（8 bit）	最多2,953 位元組
						日文漢字／片假名	最多1,817字元（採用Shift JIS）
						中文漢字	最多984字元（採用UTF-8）
						最多1,800字元（採用BIG5）
					容錯:
						L:7% 的字碼可被修正
						M:15% 的字碼可被修正
						Q:25% 的字碼可被修正
						H:30% 的字碼可被修正
					*/
	// Author:		welson
	//===============================================================
	public static BufferedImage generate(String str, int w) throws UnsupportedEncodingException {
		BufferedImage bi = null;
		
		if(w<=0) {
			w = DEF_QRCODE_SIZE;
		}
		
        try {
        	bi = new BufferedImage(w, w, BufferedImage.TYPE_INT_RGB);
 
        	//com.swetake.util.Qrcode qrcode = new com.swetake.util.Qrcode();
            Qrcode qrcode = new Qrcode();
            
            //容錯率L M Q H 
            qrcode.setQrcodeErrorCorrect('M');
            //字元模式,N A 或其它的A是英文,N是數字,其它是8 byte
            //qrcode.setQrcodeEncodeMode('B');
            //可使用的字串長短跟所設定的QrcodeVersion有關,越大可設定的字越多
            //0-40,0是自動
            qrcode.setQrcodeVersion(0);//這個值最大40，值越大可容納的信息越多，够用就行了
 
            // QR Code 
            byte[] d = str.getBytes("UTF-8");
 
            // createGraphics
            Graphics2D g = (Graphics2D) bi.getGraphics();
 
            // set parameters
            g.setBackground(Color.WHITE);
            g.clearRect(0, 0, w, w);
            g.setColor(Color.BLACK);

            //if (d.length > 0 && d.length < 120) {
                boolean[][] bRect = qrcode.calQrcode(d); // 以此函式產生與QR Code相對應的boolean二維陣列
                for (int i=0; i<bRect.length; ++i) {
                    for (int j=0; j<bRect.length; ++j) {
                        if (bRect[j][i]) {
                        	// 依據陣列值繪出條碼方塊，每個圖為165*165
                            g.fillRect(j*3+3, i*3+3, 3, 3);
                            //g.fillRect(j*10+5, i*10+5, 10, 10); 
                        }
                    }
                }
            //}
 
            g.dispose();
            // bi.flush();
        } catch (Exception e) {
            throw e;
        }
        return bi;
	}
	
//	public static String decode(File imageFile) {
//        // QRCode 二维码图片的文件  
//        //File imageFile = new File(imgPath);  
//	  
//        BufferedImage bufImg = null;  
//        String decodedData = null;  
//        try {  
//            bufImg = ImageIO.read(imageFile);  
//  
//            QRCodeDecoder decoder = new QRCodeDecoder();  
//            decodedData = new String(decoder.decode(new J2SEImage(bufImg)));  
//  
//            // try {  
//            // System.out.println(new String(decodedData.getBytes("gb2312"),  
//            // "gb2312"));  
//            // } catch (Exception e) {  
//            // // TODO: handle exception  
//            // }  
//        } catch (IOException e) {  
//            System.out.println("Error: " + e.getMessage());  
//            e.printStackTrace();  
//        } catch (DecodingFailedException dfe) {  
//            System.out.println("Error: " + dfe.getMessage());  
//            dfe.printStackTrace();  
//        }  
//        return decodedData;  
//	}
	
//	public static String decode(File qrcodeFile) throws ReaderException {
//		BufferedImage image = ImageIO.read(qrcodeFile);
//		LuminanceSource source = new BufferedImageLuminanceSource(image);
//		BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
//		QRCodeReader reader = new QRCodeReader();
//		String plainText = null;
//		try {
//		    @SuppressWarnings("rawtypes") Hashtable hints = new Hashtable();
//		    Result result = reader.decode(bitmap, hints);
//		    plainText = result.getText();
//		} catch (ReaderException e) {
//		    throw e;
//		}
//		return plainText;
//	}
	
	
	//===============================================================
	// Purpose: 	generate QRCode by ZXing
	// Parameters:	str: content of qrcode
	// Return:
	// Remark:		http://crunchify.com/java-simple-qr-code-generator-example/
	// Author:		welson
	//===============================================================
	public static File generate2(String str, String outputPath, int w) throws IOException {
        //int size = 125;
        File output = null;
        try {
            Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<EncodeHintType, ErrorCorrectionLevel>();
            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix byteMatrix = qrCodeWriter.encode(str, BarcodeFormat.QR_CODE, w, w, hintMap);
            int CrunchifyWidth = byteMatrix.getWidth();
            BufferedImage image = new BufferedImage(CrunchifyWidth, CrunchifyWidth, BufferedImage.TYPE_INT_RGB);
            image.createGraphics();
 
            Graphics2D graphics = (Graphics2D) image.getGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, CrunchifyWidth, CrunchifyWidth);
            graphics.setColor(Color.BLACK);
 
            for (int i = 0; i < CrunchifyWidth; i++) {
                for (int j = 0; j < CrunchifyWidth; j++) {
                    if (byteMatrix.get(i, j)) {
                        graphics.fillRect(i, j, 1, 1);
                    }
                }
            }
            
            output = new File(outputPath);
            ImageIO.write(image, "png", output);
        } catch (WriterException e) {
        	e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return output;
    } 
}
