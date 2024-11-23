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
import ppp.db.UserRepository;
import ppp.db.controllers.CUserRepository;
import ppp.meta.LoginEnum;

@WebServlet("/games")
public class GamesServe extends HttpServlet {

	/*public void init(ServletConfig config) throws ServletException {
		getServletContext();
		super.init(config);
	}*/
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
	throws IOException {

		UserRepository repository = new CUserRepository();
		Authenticator auth = new Authenticator(repository);
		boolean loggedIn = auth.login(request) == LoginEnum.Status.SUCCESS;

		try {
			if (!loggedIn) {
				response.sendRedirect("/login");
				return;
			}
			RequestDispatcher view = request.getRequestDispatcher("games.html");
			response.setHeader("Cache-Control", "max-age=86400, public"); // 1 day
			System.out.println(view.toString());
			view.forward(request, response);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			response.setStatus(500);
		    response.getWriter().print("{\"error\":\"" + e + "\"}");
		    return;
		}
	}
}