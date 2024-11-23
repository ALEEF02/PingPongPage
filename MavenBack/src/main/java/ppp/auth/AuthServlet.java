package ppp.auth;

import java.io.IOException;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ppp.ServerConfig;
import ppp.api.GetGames;
import ppp.db.UserRepository;
import ppp.db.controllers.CGlicko;
import ppp.db.controllers.CUser;
import ppp.db.controllers.CUserRepository;
import ppp.db.model.OGlicko;
import ppp.db.model.OUser;
import ppp.meta.LoginEnum;
import ppp.meta.StatusEnum;

@WebServlet("/api/auth")
public class AuthServlet extends HttpServlet {

	UserRepository repository = new CUserRepository();
	Authenticator emailer = new Authenticator(repository);
	
	/**
	 * Creates a new 24-bit, Base64 Token
	 * @return The unique token
	 */
	private String genNewToken() {
		SecureRandom random = new SecureRandom();
		Base64.Encoder b64Encoder = Base64.getUrlEncoder();
		byte[] randBytes = new byte[24];
		random.nextBytes(randBytes);
		return b64Encoder.encodeToString(randBytes);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		// The user is trying to submit their auth code.
		// TODO: Convert auth code to long?
		if (request.getParameterMap().containsKey("email") && request.getParameterMap().containsKey("authCode")) {
			int inputAuthCode;
			String email = request.getParameter("email");
			try {
				inputAuthCode = Integer.parseInt(request.getParameter("authCode"));
			} catch (Exception e) {
				response.setStatus(400);
				response.getWriter().println(GetGames.createError(LoginEnum.Status.AUTH_INVALID.getMsg()));
				return;
			}
			LoginEnum.Status successfulAuth = emailer.compareAuthCode(email, inputAuthCode);
			if (successfulAuth != LoginEnum.Status.SUCCESS) { // The inputed code does not match what we sent to the specified email
				response.setStatus(successfulAuth.getCode());
				response.getWriter().println(GetGames.createError(successfulAuth.getMsg()));
				return;
			}

			// At this point we know that this person is that of the email provided. Setup account if it doesn't exist now.
			response.getWriter().println("huh. That worked. now what?");
			request.getSession().setAttribute("email", email); // Store the email in the internal session
			request.getSession().setMaxInactiveInterval(-1); // Set the session to not expire
			response.setStatus(200);
			OUser user = CUser.findByEmail(email);
			Date now = new Date();
			
			if (user.id == 0) { // This is the user's first time logging in. Set them up
				user.email = email;
				user.username = email.substring(0, email.indexOf("@"));
				user.signUpDate = new Timestamp(now.getTime());
				user.lastSignIn = new Timestamp(now.getTime());
				
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(now);            
				calendar.add(Calendar.DAY_OF_YEAR, ServerConfig.TOKEN_VALIDITY_PERIOD);
				user.tokenExpiryDate = new Timestamp(calendar.getTime().getTime());
				
				user.token = genNewToken();
				request.getSession().setAttribute("token", user.token);
				CUser.insert(user);
				// Record their starting Glicko values
				OGlicko glickoRecord = new OGlicko();
				glickoRecord.userId = user.id;
				glickoRecord.checkRatingCycle();
				CGlicko.insert(glickoRecord);
				
			} else { // This is not the user's first login. However, their previous auth is expired. Let's gen them a new one and store it. This will invalidate the old one, too.
				
				user.lastSignIn = new Timestamp(now.getTime());
				
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(now);            
				calendar.add(Calendar.DAY_OF_YEAR, ServerConfig.TOKEN_VALIDITY_PERIOD);
				user.tokenExpiryDate = new Timestamp(calendar.getTime().getTime());
				
				user.token = genNewToken();
				request.getSession().setAttribute("token", user.token);
				CUser.update(user, true);
			}
			
		} else if (request.getParameterMap().containsKey("loggingIn")) {
			
			// TODO: This is a good example of the login process. However, it should be removed from here and implemented on each API level!
			
			String email = (String)request.getSession().getAttribute("email");
			
			System.out.print("Someone is trying to log in - ");
			System.out.println(email);
			LoginEnum.Status loginTry = emailer.login(request);
			if (loginTry != LoginEnum.Status.SUCCESS) {
				response.setStatus(401);
				response.getWriter().println(GetGames.createError(loginTry.getMsg()));
				return;
			}
			
			OUser user = CUser.findByEmail(email);
			
			// This user has a valid email & token pair. Serve them the things they want & have access to.
			response.setStatus(200);
			response.getWriter().println("hey," + user.username + ", that's the right token :D Here are the things you want...");
			return;
			
		} else if (request.getParameterMap().containsKey("email")) { // The user is requesting an auth code be sent to them
			String email = request.getParameter("email").toLowerCase();
			if (!email.endsWith("@stevens.edu")) {
				response.setStatus(400);
				response.getWriter().println(GetGames.createError(LoginEnum.Status.EMAIL_INVALID.getMsg()));
				return;
			}
			LoginEnum.Status emailStatus = emailer.sendAuthEmail(email);
			if (emailStatus != LoginEnum.Status.EMAIL_SENT) {
				response.setStatus(emailStatus.getCode()); // 429, but using .getCode is good future-proofing
				response.getWriter().println(GetGames.createError(emailStatus.getMsg()));
			} else {
				response.setStatus(200);
			}
			return;
		}
	}
}