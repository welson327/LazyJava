package org.lazyjava.mail;

import java.io.File;
import java.io.FileNotFoundException;

import javax.mail.MessagingException;

public class MailSender {
	private static final String SMTP = "smtp.lazyjava.org";
	private static final String POP3 = "pop3.lazyjava.org";
	private static final String SENDER = "service@lazyjava.org";
	private static final String SENDER_NAME = "LazyJava Customer Service";//"客服中心"
	
	private static final String MAIL_HOST_USERNAME = "lazyjava";
	private static final String MAIL_HOST_PASSWORD = "lazyjava";
	
	private MailBox mailBox = null;
	private String senderEmail = "";
	private String senderName = "";
	private String subject = "";
	private String content = "";
	
	public MailSender() {
		mailBox = new MailBox();
		mailBox.setupLogin(SMTP, MAIL_HOST_USERNAME, MAIL_HOST_PASSWORD);
	}
	
	public void setSender(String senderEmail, String senderName) {
		this.senderEmail = senderEmail;
		this.senderName = senderName;
	}
	public void addReceiver(String receiver) {
		mailBox.addRecipient(receiver);
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public void attachFile(File f) throws FileNotFoundException {
		mailBox.attachFile(f);
	}

	
	public boolean send() throws MessagingException {
		mailBox.setMail(senderEmail, senderName, subject, content);
		return mailBox.sendMail();
	}
}
