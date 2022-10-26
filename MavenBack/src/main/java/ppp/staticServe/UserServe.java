package ppp.staticServe;

import java.io.IOException;

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

@WebServlet("/user/*")
public class UserServe extends HttpServlet {
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
	throws IOException {

		//Authenticator auth = new Authenticator();
		//boolean loggedIn = auth.login(request);
		String userRequested = request.getRequestURI().substring("/user/".length());
		
		try {
			RequestDispatcher view = request.getRequestDispatcher("/user.html");
			/*if (loggedIn) {
				OUser me = CUser.findByEmail((String)request.getSession().getAttribute("email"));
				if (me.id == 0) {
					response.setStatus(500);
				    response.getWriter().print(GetGames.createError("User is both logged in and not logged in??"));
				    return;
				}
				if (me.username == userRequested) {
					view = request.getRequestDispatcher("/userEditable.html");
				}
			}*/
			
			OUser reqUser = CUser.findByUsername(userRequested);
			
			if (reqUser.id == 0) {
				response.setStatus(404);
			    response.getWriter().print(GetGames.createError("User does not exist!"));
			    return;
			}
			
			/*
			request.setAttribute("reqUsername", reqUser.username);
			request.setAttribute("reqElo", reqUser.elo);
			request.setAttribute("reqSignUp", reqUser.signUpDate);
			request.setAttribute("reqLastLog", reqUser.lastLogin);
			*/
			System.out.println(view.toString());
			view.forward(request, response);
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