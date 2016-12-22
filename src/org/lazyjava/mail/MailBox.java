package org.lazyjava.mail;


import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.imageio.ImageIO;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.codec.binary.Base64;



//=============================================================
// Purpose:		Send mail by Java Mail Service
// Parameters:
// Return:
// Remark:
// Authors:		JY
//=============================================================
public class MailBox {

	//private String To = null;
	private String senderEmail = null;
	private String SMTP = null;
	private String subject = null;
	private String htmlContent = null;
	private String username = null;
	private String password = null;
	
	// add by welson
	private String senderName = null;
	private ArrayList<String> recipients = null; 
	private ArrayList<File> attachments = null;
	
	public MailBox() {
		recipients = new ArrayList<String>();
	}

	
	public void setupLogin (String smtp, String username, String password) {
		SMTP = smtp;
		this.username = username;
		this.password = password;
	}
	
	public void setMail (String senderEmail, String senderName, String subject, String content) {
		this.senderEmail = senderEmail;
		// https://javamail.java.net/nonav/docs/api/javax/mail/internet/InternetAddress.html
		this.senderName = senderName;
		this.subject = subject;
		this.htmlContent = content;
	}
	
	// add by welson
	public void setSenderName (String name) {
		senderName = name;
	}
	
	// add by welson
	public void addRecipient (String recipient) {
		recipients.add(recipient);
	}
	
	// add by welson
	public void attachFile (File f) throws FileNotFoundException {
		if(attachments == null) {
			attachments = new ArrayList<File>();
		}
		if(f.exists()) {
			attachments.add(f);
		} else {
			throw new FileNotFoundException();
		}
	}

	// add by welson
	public boolean sendMail() throws MessagingException {
		return sendHTMLEmail(null);
	}
	
	public boolean sendHTMLEmail(ArrayList<File> ImageList) throws MessagingException {
		try {
			//System.out.printf("[Sent message] start....\n");
			/*
			if ((To == null) || To.length() <= 0) {
				System.out.printf("Parameter To can't be null or emptystring!\n");
				return false;
			}*/
			if ((senderEmail == null) || senderEmail.length() <= 0) {
				System.out.printf("Parameter From can't be null or emptystring!\n");
				return false; 
			}
			if ((SMTP == null) || SMTP.length() <= 0) {
				System.out.printf("Parameter SMTP can't be null or emptystring!\n");
			  	return false; 			  
			}
			if ((subject == null) || subject.length() <= 0) {		
				System.out.printf("Parameter Subject can't be null or emptystring!\n");
			  	return false; 			  
			}
			if ((htmlContent == null) || htmlContent.length() <= 0) {		
				System.out.printf("Parameter HTMLContent can't be null or emptystring!\n");
			  	return false; 	
			}


			// Get system properties
			Properties properties = System.getProperties();

			// Setup mail server
			properties.put("mail.smtp.host", SMTP);
			properties.put("mail.smtp.port",25);
	      
			// login parameters
			properties.put("mail.user", username);
			properties.put("mail.password", password);
			//properties.setProperty("mail.smtps.auth", "true"); -- for SSL
			properties.put("mail.smtp.auth", "true");
			//properties.put("mail.smtp.starttls.enable", "true");
	      
	      

			// Get the default Session object.
			Session session = Session.getInstance(properties,
				new Authenticator() {
	            protected PasswordAuthentication  getPasswordAuthentication() {
	            	return new PasswordAuthentication(username, password);
	            }
	        });

	        MimeMessage message = new MimeMessage(session);
	        message.setFrom(new InternetAddress(senderEmail, senderName, "UTF-8")); 
	        message.setSubject(subject, "UTF-8");
	        for(int i=0; i<recipients.size(); ++i) {
	        	message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipients.get(i)));
	        }

	        /* mark by welson
	        // HTML image
	        if ((ImageList==null) || (ImageList.size() == 0)) { 
	            message.setContent(HTMLContent,"text/html; charset=utf-8");
	        } else {  // multipart email
	        	// create mail multi body
	        	//System.out.printf("[Sent message] MIME with mulit-parts....\n");
	        	MimeMultipart multipart = new MimeMultipart();
	        	multipart.setSubType("related"); 
	        	// put text & HTML code first
	        	MimeBodyPart bodyPart = new MimeBodyPart();
	        	bodyPart.setContent(HTMLContent, "text/html; charset=utf-8"); 
                multipart.addBodyPart(bodyPart);	        	
	        	
                for (int i=0; i< ImageList.size(); i++) {
                    // put images
                    multipart.addBodyPart(createInlineImagePart(ImageList.get(i)));
                    // each image must has a "<img src=\"cid:image_name.jpg\"/>" tag in HTML body
                }
                
                // set data to main email body
                message.setContent(multipart);
                message.setHeader("MIME-Version", "1.0");
                message.setHeader("Content-Type", multipart.getContentType());
	        }
	        */
	        //--------------------------------------- add by welson
	        MimeMultipart multipart = new MimeMultipart();
	        //multipart.setSubType("related"); 
	        
	        // content
        	MimeBodyPart contentPart = new MimeBodyPart();
        	contentPart.setContent(htmlContent, "text/html; charset=utf-8"); 
            multipart.addBodyPart(contentPart);
	        
            // HTML image (add by welson)
	        if (ImageList != null) { 
	        	for (int i=0; i< ImageList.size(); i++) {
                    multipart.addBodyPart(createInlineImagePart(ImageList.get(i)));
                }
	        }
	        
	        // attachments (add by welson)
	        if(attachments != null) {
		        for(File f : attachments) {
		        	multipart.addBodyPart(createAttachmentBodyPart(f));
		        }
	        }
	        
	        message.setContent(multipart);
	        //--------------------------------------- end by welson
            
	        message.saveChanges();


	        // send
	        Transport transport = session.getTransport("smtp");  	//ssl is smtps
	        transport.connect(SMTP, 25, username, password); 		// ssl is 465
	        transport.sendMessage(message, message.getAllRecipients());
	        transport.close();
		} catch (MessagingException e) {
			e.printStackTrace();
	    	throw e;
		} catch (Exception e) {
			e.printStackTrace();
			//throw e;
		}
	    return true;
	}
	
	private BodyPart createInlineImagePart(File ImageFile)  {

        MimeBodyPart imagePart =null;
        try
        {

        	if (! ImageFile.exists()) {
        		return null;
        	} 
        	// get file size
        	long filesize = ImageFile.length();
        	
            ByteArrayOutputStream baos=new ByteArrayOutputStream((int) filesize);
            BufferedImage img=ImageIO.read(ImageFile);
            ImageIO.write(img, "jpg", baos); // always use jpg even source is PNG
            baos.flush();

            byte[] bytearray = Base64.encodeBase64(baos.toByteArray());
            baos.close();

            InternetHeaders headers = new InternetHeaders();
            headers.addHeader("Content-Type", "image/jpeg");
            headers.addHeader("Content-Transfer-Encoding", "base64");
            imagePart = new MimeBodyPart(headers, bytearray);
            imagePart.setDisposition(MimeBodyPart.INLINE);
            imagePart.setContentID("<"+ ImageFile.getName() + ">");  // for different mail box compatible
            imagePart.setHeader("Content-ID", "<" + ImageFile.getName() + ">");
            imagePart.setFileName(ImageFile.getName());
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return imagePart;
	}
	
	//=============================================================
	// Purpose:		
	// Parameters:
	// Return:
	// Remark:
	// Authors:		welson
	//=============================================================
	private MimeBodyPart createAttachmentBodyPart(File f) throws MessagingException {
		MimeBodyPart attachPart = new MimeBodyPart();
	    DataSource source = new FileDataSource(f);
	    attachPart.setDataHandler(new DataHandler(source));
	    attachPart.setFileName(f.getName());
	    return attachPart;
	}
}
