package com.wwf.shrimp.application.utils;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.wwf.shrimp.application.models.User;
import com.wwf.shrimp.application.services.main.ConfigurationService;
import com.wwf.shrimp.application.services.main.impl.PropertyConfigurationService;

/**
 * Simple date utility for conversion and formatting
 * 
 * @author AleaActaEst
 *
 */
public class EmailUtils {
	
	
	
	/**
	 * Check the validity of the email address in terms of syntax
	 * 
	 * @param emailAddress - the date to be formatted
	 * @return - true if the email has a valid format; false otherwise
	 */
	public static boolean isValid(String emailAddress){
		String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+ 
                "[a-zA-Z0-9_+&*-]+)*@" + 
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" + 
                "A-Z]{2,7}$"; 
                  
		Pattern pat = Pattern.compile(emailRegex); 
		if (emailAddress == null) 
		return false; 
		return pat.matcher(emailAddress).matches(); 
	}
	
	/**
	 * 
	 * @param user
	 * @param subject
	 * @param body
	 * @return
	 */
	public static boolean sendActivationEmail(User user, String subject, String body){
			boolean result = false;
			ConfigurationService configService = new PropertyConfigurationService();
			
			//
			// open the config service
			configService.open();
			
	       	//final String username = "inf3rum.official@gmail.com";
	        //final String password = "irae2358G";
	        
	        final String username = configService.readConfigurationProperty("system.email.server.auth.username");
	        final String password = configService.readConfigurationProperty("system.email.server.auth.password");
	        final String nameEmailFrom = configService.readConfigurationProperty("system.email.show.name.from");
	        final String addressEmailFrom = configService.readConfigurationProperty("system.email.address.from");
	        final String addressEmailReplyTo = configService.readConfigurationProperty("system.email.address.reply.to");

	        Properties prop = new Properties();
	        prop.put("mail.smtp.host", configService.readConfigurationProperty("system.mail.smtp.host"));
	        prop.put("mail.smtp.port", configService.readConfigurationProperty("system.mail.smtp.port"));
	        prop.put("mail.smtp.auth", configService.readConfigurationProperty("system.mail.smtp.auth"));
	        prop.put("mail.smtp.socketFactory.port", configService.readConfigurationProperty("system.mail.smtp.socketFactory.port"));
	        prop.put("mail.smtp.socketFactory.class", configService.readConfigurationProperty("system.mail.smtp.socketFactory.class"));
        
        Authenticator auth = new Authenticator() {
			//override the getPasswordAuthentication method
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		};
		
		Session session = Session.getDefaultInstance(prop, auth);

        try {

        	MimeMessage message = new MimeMessage(session);
            message.addHeader("Content-type", "text/HTML; charset=UTF-8");
            message.addHeader("format", "flowed");
            message.addHeader("Content-Transfer-Encoding", "8bit");
   	     	
            message.setFrom(new InternetAddress("inf3rum.official@gmail.com", "Republic Systems Help"));
            message.setReplyTo(InternetAddress.parse("inf3rum.official@gmail.com", false));
            message.setRecipients(
                    Message.RecipientType.TO,
                    //InternetAddress.parse("ppaweska@hotmail.com, inf3rum.official.us@gmail.com")
                    InternetAddress.parse(user.getContactInfo().getEmailAddress()));
            
            message.setSentDate(new Date());
            message.setSubject(subject, "UTF-8");
            message.setContent(
                    body,
                   "text/html");

            Transport.send(message);

            System.out.println("Done");
            
            result = true;

        } catch (MessagingException | UnsupportedEncodingException e) {
            e.printStackTrace();
            result = false;
        }
        
        return result;
    }

	
	/**
	 * 
	 * @param user
	 * @param subject
	 * @param body
	 * @return
	 */
	public static boolean sendNewUserRegistrationAlertEmail(User orgAdmin, String subject, String body){
			boolean result = false;
			ConfigurationService configService = new PropertyConfigurationService();
			
			//
			// open the config service
			configService.open();
			
	       	//final String username = "inf3rum.official@gmail.com";
	        //final String password = "irae2358G";
	        
	        final String username = configService.readConfigurationProperty("system.email.server.auth.username");
	        final String password = configService.readConfigurationProperty("system.email.server.auth.password");
	        final String nameEmailFrom = configService.readConfigurationProperty("system.email.show.name.from");
	        final String addressEmailFrom = configService.readConfigurationProperty("system.email.address.from");
	        final String addressEmailReplyTo = configService.readConfigurationProperty("system.email.address.reply.to");

	        Properties prop = new Properties();
	        prop.put("mail.smtp.host", configService.readConfigurationProperty("system.mail.smtp.host"));
	        prop.put("mail.smtp.port", configService.readConfigurationProperty("system.mail.smtp.port"));
	        prop.put("mail.smtp.auth", configService.readConfigurationProperty("system.mail.smtp.auth"));
	        prop.put("mail.smtp.socketFactory.port", configService.readConfigurationProperty("system.mail.smtp.socketFactory.port"));
	        prop.put("mail.smtp.socketFactory.class", configService.readConfigurationProperty("system.mail.smtp.socketFactory.class"));
        
        Authenticator auth = new Authenticator() {
			//override the getPasswordAuthentication method
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		};
		
		Session session = Session.getDefaultInstance(prop, auth);

        try {

        	MimeMessage message = new MimeMessage(session);
            message.addHeader("Content-type", "text/HTML; charset=UTF-8");
            message.addHeader("format", "flowed");
            message.addHeader("Content-Transfer-Encoding", "8bit");
   	     	
            message.setFrom(new InternetAddress("inf3rum.official@gmail.com", "Republic Systems Help"));
            message.setReplyTo(InternetAddress.parse("inf3rum.official@gmail.com", false));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(orgAdmin.getContactInfo().getEmailAddress()));
            
            message.setSentDate(new Date());
            message.setSubject(subject, "UTF-8");
            message.setContent(
                    body,
                   "text/html");

            Transport.send(message);

            System.out.println("Done");
            
            result = true;

        } catch (MessagingException | UnsupportedEncodingException e) {
            e.printStackTrace();
            result = false;
        }
        
        return result;
    }

	
	
	
	/**
	 * 
	 * @param user
	 * @param subject
	 * @param body
	 */
	public static void sendUserWelcomeEmail(User user, String subject, String body){
		ConfigurationService configService = new PropertyConfigurationService();
		configService.open();
		
        final String username = configService.readConfigurationProperty("system.email.server.auth.username");
        final String password = configService.readConfigurationProperty("system.email.server.auth.password");

        Properties prop = new Properties();
        prop.put("mail.smtp.host", configService.readConfigurationProperty("system.mail.smtp.host"));
        prop.put("mail.smtp.port", configService.readConfigurationProperty("system.mail.smtp.port"));
        prop.put("mail.smtp.auth", configService.readConfigurationProperty("system.mail.smtp.auth"));
        prop.put("mail.smtp.socketFactory.port", configService.readConfigurationProperty("system.mail.smtp.socketFactory.port"));
        prop.put("mail.smtp.socketFactory.class", configService.readConfigurationProperty("system.mail.smtp.socketFactory.class"));
        
        Session session = Session.getInstance(prop,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {

        	MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress("inf3rum.official@gmail.com", "Republic Systems Help"));
            message.setReplyTo(InternetAddress.parse("inf3rum.official@gmail.com", false));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(user.getContactInfo().getEmailAddress()));
                    
            message.setSubject(subject);
            message.setContent(
                    body,
                   "text/html");

            Transport.send(message);

            System.out.println("Done");

        } catch (MessagingException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
	
	/**
	 * 
	 * @param user
	 * @param subject
	 * @param body
	 */
	public static void sendUserPasswordResetEmail(User user, String subject, String body){
		ConfigurationService configService = new PropertyConfigurationService();
		configService.open();
		
        final String username = configService.readConfigurationProperty("system.email.server.auth.username");
        final String password = configService.readConfigurationProperty("system.email.server.auth.password");

        Properties prop = new Properties();
        prop.put("mail.smtp.host", configService.readConfigurationProperty("system.mail.smtp.host"));
        prop.put("mail.smtp.port", configService.readConfigurationProperty("system.mail.smtp.port"));
        prop.put("mail.smtp.auth", configService.readConfigurationProperty("system.mail.smtp.auth"));
        prop.put("mail.smtp.socketFactory.port", configService.readConfigurationProperty("system.mail.smtp.socketFactory.port"));
        prop.put("mail.smtp.socketFactory.class", configService.readConfigurationProperty("system.mail.smtp.socketFactory.class"));
       
        Session session = Session.getInstance(prop,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(configService.readConfigurationProperty("system.email.address.from")));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(user.getContactInfo().getEmailAddress()));
                    
            message.setSubject(subject);
            message.setContent(
                    body,
                   "text/html");

            Transport.send(message);

            System.out.println("Done - sendUserPasswordResetEmail");

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
	
	
	
}
