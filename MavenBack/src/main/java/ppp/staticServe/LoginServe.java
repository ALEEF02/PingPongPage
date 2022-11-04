package ppp.staticServe;

import java.io.IOException;

import jakarta.servlet.RequestDispatcher;
/*import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;*/
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ppp.auth.Authenticator;

@WebServlet("/login")
public class LoginServe extends HttpServlet {
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

		Authenticator auth = new Authenticator();
		boolean loggedIn = auth.login(request);

		try {
			if (loggedIn) {
				response.sendRedirect("/");
				return;
			}
			RequestDispatcher view = request.getRequestDispatcher("login.html");
			//response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // This is a way to HEAVILY recommend a browser not to cache a page
			response.setHeader("Cache-Control", "max-age=86400, public"); // 1 day cache while changing colors. Make this 7-14 days later.
			System.out.println(view.toString());
			view.forward(request, response);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}