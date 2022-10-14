package ppp;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.RequestDispatcher;
/*import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;*/
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ppp.db.controllers.CGames;
import ppp.db.model.OGame;

@WebServlet("/games")
public class GetGames extends HttpServlet {

	/*public void init(ServletConfig config) throws ServletException {
		getServletContext();
		super.init(config);
	}*/
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
	throws IOException {

		try {
			List<OGame> games = CGames.getLatestGames();
			response.setContentType("text/html;");
		    response.getWriter().println("<h1>All Games:</h1>");
		    response.getWriter().println("<table>");
		    response.getWriter().println("<tr><td>" + "id" + 
	    		"</td><td>" + "date" + 
	    		"</td><td>" + "status"+
	    		"</td><td>" + "sender"+
	    		"</td><td>" + "receiver"+
	    		"</td><td>" + "winner"+
	    		"</td><td>" + "winnerScore" +
	    		"</td><td>" + "loserScore"+
	    		"</td></tr>");
		    for (OGame game:games) {
			    response.getWriter().println("<tr><td>" + game.id + 
			    		"</td><td>" + game.date.toString() + 
			    		"</td><td>" + game.status + 
			    		"</td><td>" + game.sender + 
			    		"</td><td>" + game.receiver + 
			    		"</td><td>" + game.winner + 
			    		"</td><td>" + game.winnerScore + 
			    		"</td><td>" + game.loserScore + 
			    		"</td></tr>");
		    	
		    }
		    response.getWriter().println("</table><p>(<a href=\"\">Refresh</a>)</p>");
		    
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}