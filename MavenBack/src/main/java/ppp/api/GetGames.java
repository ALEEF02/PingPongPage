package ppp.api;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

/*import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;*/
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ppp.auth.Authenticator;
import ppp.db.controllers.CGames;
import ppp.db.controllers.CUser;
import ppp.db.model.OGame;
import ppp.db.model.OUser;
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
@WebServlet("/api/games")
public class GetGames extends HttpServlet {
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Map<String, String[]> parameters = request.getParameterMap();
		List<OGame> games = null;
		StatusEnum.Status status = Status.ANY;
		int limit = 20;

		Authenticator auth = new Authenticator();
		boolean loggedIn = auth.login(request);
		
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
				
				if (!loggedIn) {
					response.setStatus(401);
					response.getWriter().println("https://www.youtube.com/watch?v=GPXkjtpGCFI&t=7s");
					return;
				}
				
				int user = 0;
				try {
					user = Integer.parseInt(parameters.get("sender")[0]);
				} catch (Exception e) {
					response.setStatus(400);
				    response.getWriter().print("[]");
				    return;
			    }
				games = CGames.getUsersSentGames(user, status);
				
			} else if (parameters.containsKey("receiver")) {
				
				if (!loggedIn) {
					response.setStatus(401);
					response.getWriter().println("https://www.youtube.com/watch?v=GPXkjtpGCFI&t=7s");
					return;
				}
				
				int user = 0;
				try {
					user = Integer.parseInt(parameters.get("receiver")[0]);
				} catch (Exception e) {
					response.setStatus(400);
				    response.getWriter().print("[]");
				    return;
			    }
				games = CGames.getUsersReceivedGames(user, status);
				
			} else if (parameters.containsKey("user")) {
				
				if (!loggedIn) {
					response.setStatus(401);
					response.getWriter().println("https://www.youtube.com/watch?v=GPXkjtpGCFI&t=7s");
					return;
				}
				
				int user = 0;
				if (parameters.get("user").length == 2) {
					
					int user2 = 0;
					
					try {
						user = Integer.parseInt(parameters.get("user")[0]);
						user2 = Integer.parseInt(parameters.get("user")[1]);
					} catch (Exception e) {
						response.setStatus(400);
					    response.getWriter().print("[]");
					    return;
				    }
					games = CGames.getGamesBetweenUsers(user, user2, status);
					
				} else {
					
					try {
						user = Integer.parseInt(parameters.get("user")[0]);
					} catch (Exception e) {
						response.setStatus(400);
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
			response.setStatus(500);
		    response.getWriter().print(createError(e.toString()));
		    return;
		}
	}
	
	
	/*
	 * API REF:
	 * Ind: independent, Exc: exclusive of other exclusives
	 * Parameters, highest priority first:
	 * 		newGame:
	 * 			to: username of person to send game to
	 * 			myScore: Requester's score
	 * 			theirScore: Reciever's score
	 * 		acceptGame:
	 * 			id: id of the game to accept
	 * 		declineGame:
	 * 			id: id of the game to accept
	 */
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		Map<String, String[]> parameters = request.getParameterMap();
		StatusEnum.Status status = Status.ANY;
		
		Authenticator auth = new Authenticator();
		boolean loggedIn = auth.login(request);
		if (!loggedIn) {
			response.setStatus(401);
			response.getWriter().println("https://www.youtube.com/watch?v=GPXkjtpGCFI&t=7s");
			return;
		}
		
		OUser me = CUser.findByEmail((String)request.getSession().getAttribute("email"));
		if (me.id == 0) {
			response.setStatus(500);
		    response.getWriter().print(createError("User is both logged in and not logged in??"));
		}
		
		try {
			
			if (parameters.containsKey("newGame")) {
				status = StatusEnum.Status.PENDING;
				if (!(parameters.containsKey("to") && parameters.containsKey("myScore") & parameters.containsKey("theirScore"))) {
					response.setStatus(400);
					response.getWriter().println(createError("Missing Parameters"));
					return;
				}
				
				int myScore = 0;
				int theirScore = 0;
				
				try {
					myScore = Integer.parseInt(parameters.get("myScore")[0]);
					theirScore = Integer.parseInt(parameters.get("theirScore")[0]);
				} catch (Exception e) {
					response.setStatus(400);
					response.getWriter().println(createError("Invalid Score[s]"));
				    return;
			    }
				
				// TODO: Sanitize ALL these inputs
				
				OUser toUser = CUser.findByUsername(parameters.get("to")[0]);
				if (toUser.id == 0 || toUser.id == me.id) {
					response.setStatus(400);
					response.getWriter().println(createError("Invalid Other User"));
					return;
				}
				
				if (CGames.getGamesBetweenUsers(me.id, toUser.id, status).size() > 0) {
					response.setStatus(429);
					response.getWriter().println(createError("You already have a pending game with this user. Resolve that one first!"));
					return;
				}

				OGame newGame = new OGame();
				newGame.sender = me.id;
				newGame.receiver = toUser.id;
				newGame.date = new Timestamp(new Date().getTime());
				
				if (myScore < 11 && theirScore < 11) {
					response.setStatus(400);
					response.getWriter().println(createError("Winner should be at least 11"));
					return;
				}
				
				if (myScore > 30 || theirScore > 30 || myScore < 0 || theirScore < 0 || myScore == theirScore) {
					response.setStatus(400);
					response.getWriter().println(createError("Scores should be 0 <=< 30"));
					return;
				}
				
				// Everything looks good with this submission. Let's chuck it into the database.
				
				if (myScore > theirScore) {
					newGame.winner = me.id;
					newGame.winnerScore = myScore;
					newGame.loserScore = theirScore;
				} else {
					newGame.winner = toUser.id;
					newGame.winnerScore = theirScore;
					newGame.loserScore = myScore;					
				}
				
				CGames.insert(newGame);
				response.setStatus(200);
				return;
				
			} else if (parameters.containsKey("acceptGame") || parameters.containsKey("declineGame")) {
				
				if (!parameters.containsKey("id")) {
					response.setStatus(400);
					response.getWriter().println(createError("Missing Parameters"));
					return;
				}
				
				int gameId = 0; 
				try {
					gameId = Integer.parseInt(parameters.get("id")[0]);
				} catch (Exception e) {
					response.setStatus(400);
					response.getWriter().println(createError("Invalid gameId"));
				    return;
			    }
				
				OGame existingGame = CGames.getGameById(gameId);
				if (existingGame.id == 0) {
					response.setStatus(400);
					response.getWriter().println(createError("Invalid gameId"));
				    return;
				}
				
				if (existingGame.receiver != me.id) {
					response.setStatus(401);
					response.getWriter().println(createError("Not your game!"));
				    return;
				}

				if (existingGame.status != StatusEnum.Status.PENDING) {
					response.setStatus(400);
					response.getWriter().println(createError("This game has already been finalized"));
				    return;
				}
				
				if (parameters.containsKey("acceptGame")) {
					existingGame.status = StatusEnum.Status.ACCEPTED;
					
					// TODO: Epic elo things here :D
					
				} else {
					existingGame.status = StatusEnum.Status.REJECTED;					
				}
				
				CGames.update(existingGame);
				response.setStatus(200);
				return;
			}

			response.setStatus(400);
			response.getWriter().println("https://www.youtube.com/watch?v=GPXkjtpGCFI&t=7s");
			return;
		    
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			response.setStatus(500);
		    response.getWriter().print("{\"error\":\"" + e + "\"}");
		    return;
		}
	}
	
	private String createError(String error) {
		return "{\"error\":\"" + error + "\"}";
	}
}