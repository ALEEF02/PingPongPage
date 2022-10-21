package ppp.auth;

import java.io.IOException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/auth")
public class AuthServlet extends HttpServlet {
	
	Emailer emailer = new Emailer();

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
	
		if (request.getParameterMap().containsKey("email") && request.getParameterMap().containsKey("authCode")) {
			int inputAuthCode;
			try {
				inputAuthCode = Integer.parseInt(request.getParameter("authCode"));
			} catch (Exception e) {
				response.setStatus(400);
				return;
			}
			boolean successfulAuth = emailer.compareAuthCode(request.getParameter("email"), inputAuthCode);
			if (!successfulAuth) {
				response.setStatus(401);
				response.getWriter().println("https://www.youtube.com/watch?v=GPXkjtpGCFI&t=7s");
				return;
			}

			response.setStatus(200);
			response.getWriter().println("huh. That worked. now what?");
		}
		
		if (request.getParameterMap().containsKey("email")) {
			String email = request.getParameter("email");
			if (!email.endsWith("@stevens.edu")) {
				response.setStatus(400);
				return;
			}
			emailer.sendAuthEmail(email);
		}
	}
}