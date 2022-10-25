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

@WebServlet("/games")
public class GamesServe extends HttpServlet {

	/*public void init(ServletConfig config) throws ServletException {
		getServletContext();
		super.init(config);
	}*/
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
	throws IOException {

		Authenticator auth = new Authenticator();
		boolean loggedIn = auth.login(request);

		try {
			if (!loggedIn) {
				response.sendRedirect("/login");
				return;
			}
			RequestDispatcher view = request.getRequestDispatcher("games.html");
			System.out.println(view.toString());
			view.forward(request, response);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}