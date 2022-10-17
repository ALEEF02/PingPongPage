package ppp;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import jakarta.servlet.RequestDispatcher;
/*import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;*/
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ppp.db.controllers.CGames;
import ppp.db.model.OGame;
import ppp.meta.StatusEnum;
import ppp.meta.StatusEnum.Status;

/*
 * API REF:
 * Parameters:
 * 	None: Latest games
 * 	sender: games of the sender 
 * 	receiver: games being received
 * 	user: Any games with the user
 * 	status: The status of the game.
 */

@WebServlet("/games")
public class GetGames extends HttpServlet {
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
	throws IOException {
		Map<String, String[]> parameters = request.getParameterMap();
		List<OGame> games = null;
		StatusEnum.Status status = Status.ANY;
		try {
			if (parameters.containsKey("status")) {
				status = StatusEnum.Status.fromString(parameters.get("status")[0]);
			}
			
			if (parameters.containsKey("sender")) {
				int user = 0;
				try {
					user = Integer.parseInt(parameters.get("sender")[0]);
				} catch (Exception e) {
				    response.getWriter().print("[]");
				    return;
			    }
				games = CGames.getUsersSentGames(user, status);
			} else if (parameters.containsKey("receiver")) {
				int user = 0;
				try {
					user = Integer.parseInt(parameters.get("receiver")[0]);
				} catch (Exception e) {
				    response.getWriter().print("[]");
				    return;
			    }
				games = CGames.getUsersReceivedGames(user, status);				
			} else if (parameters.containsKey("user")) {
				int user = 0;
				try {
					user = Integer.parseInt(parameters.get("user")[0]);
				} catch (Exception e) {
				    response.getWriter().print("[]");
				    return;
			    }
				games = CGames.getGamesForUserByStatus(user, status);				
			} else {
				games = CGames.getLatestGames();
			}
			response.setContentType("application/json;");
		    /*response.getWriter().println("<h1>All Games:</h1>");
		    response.getWriter().println("<table>");
		    response.getWriter().println("<tr><td>" + "id" + 
	    		"</td><td>" + "date" + 
	    		"</td><td>" + "status"+
	    		"</td><td>" + "sender"+
	    		"</td><td>" + "receiver"+
	    		"</td><td>" + "winner"+
	    		"</td><td>" + "winnerScore" +
	    		"</td><td>" + "loserScore"+
	    		"</td></tr>");*/
		    response.getWriter().print("[");
		    for (OGame game:games) {
				response.getWriter().print(game.toJSON());
		    	/*
			    response.getWriter().println("<tr><td>" + game.id + 
			    		"</td><td>" + game.date.toString() + 
			    		"</td><td>" + game.status + 
			    		"</td><td>" + game.sender + 
			    		"</td><td>" + game.receiver + 
			    		"</td><td>" + game.winner + 
			    		"</td><td>" + game.winnerScore + 
			    		"</td><td>" + game.loserScore + 
			    		"</td></tr>");
	    		*/
		    	if (games.indexOf(game) != games.size() - 1) {
		    		response.getWriter().print(",");
		    	}
		    }
		    response.getWriter().print("]");
		    //response.getWriter().println("</table><p>(<a href=\"\">Refresh</a>)</p>");*/
		    
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}