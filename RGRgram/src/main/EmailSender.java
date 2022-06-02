package main;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailSender {
	public EmailSender(String to, String text) {

		String from = "rgrgramm@mail.ru"; // receiver email
		String host = "smtp.mail.ru"; // mail server host

		Properties properties = System.getProperties();
		properties.setProperty("mail.smtp.host", host);

		properties.setProperty("mail.smtp.auth", "true");

		properties.put("mail.smtp.starttls.enable", "true");

		properties.put("mail.smtp.port", "465");

		properties.put("mail.smtp.ssl.protocols", "TLSv1.2");

		properties.put("mail.smtps.ssl.checkserveridentity", true);
		properties.put("mail.smtps.ssl.trust", "*");
		properties.put("mail.smtp.ssl.enable", "true");

		Authenticator auth = new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(from, "cU3z3a2FM53h4hUBHXNh");
			}
		};
		Session session = Session.getInstance(properties, auth); // default session
		// EmailUtil.sendEmail(session, to,"TLSEmail Testing Subject", "TLSEmail Testing
		// Body");
		try {
			MimeMessage message = new MimeMessage(session); // email message

			message.setFrom(new InternetAddress(from)); // setting header fields

			message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

			message.setSubject("Registration to RGRgramm"); // subject line

			// actual mail body
			message.setText(text);

			// Send message
			Transport.send(message);
			System.out.println("Password sent successfully....");
		} catch (MessagingException mex) {
			mex.printStackTrace();
		}

	}

}
