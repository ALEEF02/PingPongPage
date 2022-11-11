package ppp.auth;

import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.mailer.MailerBuilder;

import com.sanctionco.jmail.JMail;

import ppp.ServerConfig;

public class Emailer {
	
	/**
	 * Get a standardized mailer that will only allow emails to @stevens.edu
	 * 
	 * @param debug Whether to print debug to the console
	 * @return The {@link org.simplejavamail.api.mailer.Mailer Mailer}
	 */
	public static Mailer getMailer(boolean debug) {
		Mailer mailer = MailerBuilder
				.withSMTPServer(ServerConfig.EMAIL_HOST, 587, ServerConfig.EMAIL_USER, ServerConfig.EMAIL_PASSWORD)
				.withTransportStrategy(TransportStrategy.SMTP_TLS)
		        .withSessionTimeout(10 * 1000)
		        .withEmailValidator( // or not
				  	JMail.strictValidator()
				  		.withRule(email -> email.domain().equals("stevens.edu") || email.domain().equals("sppp.pro")))
		        .withDebugLogging(debug)
		        .async()
		        .buildMailer();
		return mailer;
	}
}
