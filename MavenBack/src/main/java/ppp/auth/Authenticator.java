package ppp.auth;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;

import com.sanctionco.jmail.JMail;

import jakarta.servlet.http.HttpServletRequest;
import ppp.ServerConfig;
import ppp.db.controllers.CUser;
import ppp.db.model.OUser;
import ppp.meta.LoginEnum;

public class Authenticator {
	
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
	public LoginEnum.Status compareAuthCode(String email, int inputAuthCode) {
		
		// TODO: Sanatize email input
		email = email.toLowerCase();
		
		if (!emailsSent.containsKey(email)) return LoginEnum.Status.EMAIL_INVALID;
		
		int timeSent = emailsSent.get(email)[0];
		if ((System.currentTimeMillis() / 1000D) - timeSent > 60 * 5) return LoginEnum.Status.AUTH_TOO_LATE;
		
		int attempts = emailsSent.get(email)[2];
		if (attempts > 2) return LoginEnum.Status.TOO_MANY_ATTEMPTS;
		
		int sentAuthCode = emailsSent.get(email)[1];
		if (sentAuthCode != inputAuthCode) {
			Integer[] update = emailsSent.get(email);
			update[2]++;
			emailsSent.replace(email, update);
			return LoginEnum.Status.AUTH_INVALID;
		} else {
			emailsSent.remove(email);
			return LoginEnum.Status.SUCCESS;
		}
	}
	
	/**
	 * Checks the request to be logged in. Call this to check before giving out sensitive info!
	 *
	 * @param request The HTTP Request to be checked
	 * @return {@code boolean} returns {@code true} if the session's email and token are a match
	 */
	public LoginEnum.Status login(HttpServletRequest request) {
		String email = (String)request.getSession().getAttribute("email");
		String token = (String)request.getSession().getAttribute("token");
		if (email == null || !email.endsWith("@stevens.edu")) return LoginEnum.Status.EMAIL_INVALID;
		if (token == null || token.length() < 2) return LoginEnum.Status.TOKEN_INVALID;
		return login(email, token);
	}
	
	/**
	 * Checks the input email and token to be logged in
	 *
	 * @param email The email address of the user
	 * @param token The token for the user
	 * @return {@code boolean} returns {@code true} if the email and token are a match
	 */
	public LoginEnum.Status login(String email, String token) {
		email = email.toLowerCase();
		OUser user = CUser.findByEmail(email, true);
		if (user.id == 0) return LoginEnum.Status.USER_INVALID;
		if (user.email == "") return LoginEnum.Status.EMAIL_INVALID;
		if (user.token == "" || user.tokenExpiryDate.before(new Date()) || user.token.length() < 2) return LoginEnum.Status.TOKEN_EXPIRED;
		if (!user.token.equals(token)) return LoginEnum.Status.TOKEN_INVALID;
		if (user.banned) return LoginEnum.Status.BANNED; // Banned users cannot login
		
		// Successful login. Timestamp & log it.
		user.lastSignIn = new Timestamp(new Date().getTime());
		CUser.update(user);
		
		return LoginEnum.Status.SUCCESS;
	}
	
	private Mailer getMailer() {
		Mailer mailer = MailerBuilder
				.withSMTPServer(ServerConfig.EMAIL_HOST, 587, ServerConfig.EMAIL_USER, ServerConfig.EMAIL_PASSWORD)
				.withTransportStrategy(TransportStrategy.SMTP_TLS)
		        .withSessionTimeout(10 * 1000)
		        .withEmailValidator( // or not
				  	JMail.strictValidator()
				  		.withRule(email -> email.domain().equals("stevens.edu") || email.domain().equals("sppp.pro")))
		        .withDebugLogging(true)
		        .async()
		        .buildMailer();
		return mailer;
	}
	
	/**
	 * Send an email to the user with an authentication code for login.
	 *
	 * @param toEmailAddress The email address of the user
	 * @return {@code boolean} true if the email was valid and and auth code was sent
	 */
	public LoginEnum.Status sendAuthEmail(String toEmailAddress) {
		toEmailAddress = toEmailAddress.toLowerCase();
		// TODO: Sanatize email input
		double now = (System.currentTimeMillis() / 1000D);
        if (emailsSent.containsKey(toEmailAddress)) {
	        double time = now - emailsSent.get(toEmailAddress)[0];
	        if (time < 60 * 10) { // 10 minutes, but will expire after 5 minutes. This extra 5 minute delay is anti-brute force
	            return LoginEnum.Status.AUTH_ALREADY_SENT;
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
		return LoginEnum.Status.EMAIL_SENT;
	}
}
