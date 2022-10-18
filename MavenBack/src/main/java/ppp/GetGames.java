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
 * Ind: independent, Exc: exclusive of other exclusives
 * Parameters, highest priority first:
 * 	None: 20 latest games, regardless of status
 * 	status [ind]: The status of the game.
 * 	limit [ind]: The maximum number of games to return when NOT looking for a User's games. Valid 1-200. Default 20.
 * 	sender [exc]: games of the sender 
 * 	receiver [exc]: games being received
 * 	user [exc]: Any games with the user(s)
 * 		if two users are provided, all games between those two are returned
 */

@WebServlet("/games")
public class GetGames extends HttpServlet {
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
	throws IOException {
		Map<String, String[]> parameters = request.getParameterMap();
		List<OGame> games = null;
		StatusEnum.Status status = Status.ANY;
		int limit = 20;
		try {
			if (parameters.containsKey("status")) {
				status = StatusEnum.Status.fromString(parameters.get("status")[0]);
			}
			if (parameters.containsKey("limit")) {
				try {
					limit = Integer.parseInt(parameters.get("limit")[0]);
				} catch (Exception e) {}
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
				if (parameters.get("user").length == 2) {
					
					int user2 = 0;
					
					try {
						user = Integer.parseInt(parameters.get("user")[0]);
						user2 = Integer.parseInt(parameters.get("user")[1]);
					} catch (Exception e) {
					    response.getWriter().print("[]");
					    return;
				    }
					games = CGames.getGamesBetweenUsers(user, user2, status);
					
				} else {
					
					try {
						user = Integer.parseInt(parameters.get("user")[0]);
					} catch (Exception e) {
					    response.getWriter().print("[]");
					    return;
				    }
					games = CGames.getGamesForUserByStatus(user, status);
					
				}
			} else if (status != StatusEnum.Status.ANY) {
				games = CGames.getLatestGamesByStatus(status, limit);
			} else {
				games = CGames.getLatestGames(limit);
			}
			
			response.setContentType("application/json;");
		    response.getWriter().print("[");
		    for (OGame game:games) {
				response.getWriter().print(game.toJSON());
		    	if (games.indexOf(game) != games.size() - 1) {
		    		response.getWriter().print(",");
		    	}
		    }
		    response.getWriter().print("]");
		    
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}