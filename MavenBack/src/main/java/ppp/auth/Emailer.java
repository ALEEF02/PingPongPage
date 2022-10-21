package ppp.auth;

import java.util.HashMap;
import java.util.Map;

import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;

import com.sanctionco.jmail.JMail;

import ppp.ServerConfig;

public class Emailer {
	
	// Simple Java Mail docs: https://www.simplejavamail.org/features.html
	
	// index 0: Time of email sent
	// index 1: AuthCode
	// index 2: Attempts
	private static Map<String, Integer[]> emailsSent = new HashMap<String, Integer[]>();
	
	/**
	 * Checks the input authentication code to the one sent
	 *
	 * @param email The email address of the user
	 * @param inputAuthCode The submitted auth code
	 * @return {@code boolean} returns {@code true} if the auth code submitted is timely and accurate
	 */
	public boolean compareAuthCode(String email, int inputAuthCode) {
		
		if (!emailsSent.containsKey(email)) return false;
		
		int timeSent = emailsSent.get(email)[0];
		if ((System.currentTimeMillis() / 1000D) - timeSent > 60 * 5) return false;
		
		int attempts = emailsSent.get(email)[2];
		if (attempts > 2) return false;
		
		int sentAuthCode = emailsSent.get(email)[1];
		if (sentAuthCode != inputAuthCode) {
			Integer[] update = emailsSent.get(email);
			update[2]++;
			emailsSent.replace(email, update);
		}
		return sentAuthCode == inputAuthCode;
	}
	
	private Mailer getMailer() {
		Mailer mailer = MailerBuilder
				.withSMTPServer(ServerConfig.EMAIL_HOST, 587, ServerConfig.EMAIL_USER, ServerConfig.EMAIL_PASSWORD)
				.withTransportStrategy(TransportStrategy.SMTP_TLS)
		        .withSessionTimeout(10 * 1000)
		        .withEmailValidator( // or not
				  	JMail.strictValidator()
				  		.withRule(email -> email.domain().equals("stevens.edu") || email.domain().equals("aleef.dev")))
		        .withDebugLogging(true)
		        .async()
		        .buildMailer();
		return mailer;
	}
	
	/**
	 * Send an email to the user with an authentication code for login.
	 *
	 * @param toEmailAddress The email address of the user
	 * @return {@code int} with the authentication code that was sent.
	 */
	public void sendAuthEmail(String toEmailAddress) {
		double now = (System.currentTimeMillis() / 1000D);
        if (emailsSent.containsKey(toEmailAddress)) {
	        double time = now - emailsSent.get(toEmailAddress)[0];
	        if (time < 60 * 10) { // 10 minutes, but will expire after 5 minutes. This extra 5 minute delay is anti-brute force
	            return;
	        }
        }
		final int authCode = (int)(Math.random() * Math.pow(10, ServerConfig.AUTH_NUM_DIGITS));
		Email email = EmailBuilder.startingBlank()
				.from("PingPongPage", ServerConfig.EMAIL_USER)
				.to(toEmailAddress)
			    .withSubject("One-Time Password")
				.withPlainText("Hi! Your one-time password is: " + authCode + ". It'll expire in 5 minutes.\nThanks for using our service\n\t- Backend Software Engineer, Anthony Ford")
				.buildEmail();
		
		getMailer().sendMail(email);
		Integer mapVal[] = {Integer.valueOf((int)now), Integer.valueOf(authCode), 0};
        emailsSent.put(toEmailAddress, mapVal);
		return;
	}
}
