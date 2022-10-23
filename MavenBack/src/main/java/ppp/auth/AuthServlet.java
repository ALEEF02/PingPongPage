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
import ppp.db.controllers.CUser;
import ppp.db.model.OUser;

@WebServlet("/auth")
public class AuthServlet extends HttpServlet {
	
	Authenticator emailer = new Authenticator();
	
	private String genNewToken() {
		SecureRandom random = new SecureRandom();
		Base64.Encoder b64Encoder = Base64.getUrlEncoder();
		byte[] randBytes = new byte[24];
		random.nextBytes(randBytes);
		return b64Encoder.encodeToString(randBytes);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		// TODO: Sanitize email input
	
		if (request.getParameterMap().containsKey("email") && request.getParameterMap().containsKey("authCode")) {
			int inputAuthCode;
			String email = request.getParameter("email");
			try {
				inputAuthCode = Integer.parseInt(request.getParameter("authCode"));
			} catch (Exception e) {
				response.setStatus(400);
				return;
			}
			boolean successfulAuth = emailer.compareAuthCode(email, inputAuthCode);
			if (!successfulAuth) {
				response.setStatus(401);
				response.getWriter().println("https://www.youtube.com/watch?v=GPXkjtpGCFI&t=7s");
				return;
			}

			// At this point we know that this person is that of the email provided. Setup account if it doesn't exist now.
			response.setStatus(200);
			response.getWriter().println("huh. That worked. now what?");
			request.getSession().setAttribute("email", email);
			OUser user = CUser.findBy(email);
			Date now = new Date();
			
			if (user.id == 0) { // This is the user's first time logging in. Set them up
				user.email = email;
				user.signUpDate = new Timestamp(now.getTime());
				user.lastSignIn = new Timestamp(now.getTime());
				
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(now);            
				calendar.add(Calendar.DAY_OF_YEAR, 7);
				user.tokenExpiryDate = new Timestamp(calendar.getTime().getTime());
				
				user.token = genNewToken();
				request.getSession().setAttribute("token", user.token);
				CUser.insert(user);
				
			} else { // This is not the user's first login. However, their previous auth is expired. Let's gen them a new one and store it. This will invalidate the old one, too.
				
				user.lastSignIn = new Timestamp(now.getTime());
				
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(now);            
				calendar.add(Calendar.DAY_OF_YEAR, 7);
				user.tokenExpiryDate = new Timestamp(calendar.getTime().getTime());
				
				user.token = genNewToken();
				request.getSession().setAttribute("token", user.token);
				CUser.update(user);
			}
			
		} else if (request.getParameterMap().containsKey("loggingIn")) {
			
			System.out.println("Someone is trying to log in");
			System.out.println((String)request.getSession().getAttribute("email"));
			
			String email = (String)request.getSession().getAttribute("email");
			String token = (String)request.getSession().getAttribute("token");
			if (token == null || token.length() < 2) {
				response.setStatus(400);
				response.getWriter().println("https://www.youtube.com/watch?v=GPXkjtpGCFI&t=7s");
				return;
			}
			OUser user = CUser.findBy(email);
			if (!emailer.login(email, token)) {
				response.setStatus(401);
				response.getWriter().println("https://www.youtube.com/watch?v=GPXkjtpGCFI&t=7s");
				return;
			}
			
			// At this point we know that this person is that of the email provided. Setup account if it doesn't exist now.
			response.setStatus(200);
			response.getWriter().println("hey, that's the right token :D");
			
			user.lastSignIn = new Timestamp(new Date().getTime());
				
			CUser.update(user);
			
		} else if (request.getParameterMap().containsKey("email")) {
			String email = request.getParameter("email");
			if (!email.endsWith("@stevens.edu")) {
				response.setStatus(400);
				return;
			}
			emailer.sendAuthEmail(email);
		}
	}
}