package ppp.staticServe;

import java.io.IOException;
import java.util.Map;

import jakarta.servlet.RequestDispatcher;
/*import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;*/
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ppp.api.GetGames;
import ppp.auth.Authenticator;
import ppp.db.controllers.CUser;
import ppp.db.model.OUser;
import ppp.meta.GlickoTwo;

@WebServlet("/dash")
public class DashServe extends HttpServlet {
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

		Authenticator auth = new Authenticator();
		boolean loggedIn = auth.login(request);
		OUser user = new OUser();
		if (loggedIn) user = CUser.findByEmail((String)request.getSession().getAttribute("email"));

		try {
			if (loggedIn && user.username.equalsIgnoreCase("aford1")) {
				RequestDispatcher view = request.getRequestDispatcher("dash.html");
				System.out.println(view.toString());
				view.forward(request, response);
				return;
			}
			System.out.println(user.username);
			response.sendRedirect("/");
			return;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			response.setStatus(500);
		    response.getWriter().print("{\"error\":\"" + e + "\"}");
		    return;
		}
	}
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

		Map<String, String[]> parameters = request.getParameterMap();
		
		Authenticator auth = new Authenticator();
		boolean loggedIn = auth.login(request);
		if (!loggedIn) {
			response.setStatus(401);
			response.getWriter().println(GetGames.createError("https://www.youtube.com/watch?v=GPXkjtpGCFI&t=7s"));
			return;
		}
		OUser me = CUser.findByEmail((String)request.getSession().getAttribute("email"));
		if (me.id != 1) {
			response.setStatus(401);
		    response.getWriter().print(GetGames.createError("https://www.youtube.com/watch?v=GPXkjtpGCFI&t=7s"));
		    return;
		}
		
		try {
			if (parameters.containsKey("glicko")) {
				GlickoTwo.run();
				response.setStatus(200);
				return;
			}

			response.setStatus(400);
			response.getWriter().println(GetGames.createError("Shit parameters"));
			return;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			response.setStatus(500);
		    response.getWriter().print("{\"error\":\"" + e + "\"}");
		    return;
		}
		
	}
	
	
}