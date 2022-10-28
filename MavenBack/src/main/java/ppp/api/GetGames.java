package ppp.api;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
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
import ppp.meta.GlickoTwo;
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
	 * 			id: id of the game to decline
	 * 		cancelGame:
	 * 			id: id of the game to cancel
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
		    return;
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
				
				if (CGames.getGamesBetweenUsers(me.id, toUser.id, status).size() >= 2) {
					response.setStatus(429);
					response.getWriter().println(createError("You already have 2 pending games with this user. Resolve those first!"));
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
				
				if ((myScore > theirScore && myScore != 11 && myScore != 21 && myScore - theirScore != 2) || 
						(myScore < theirScore && theirScore != 11 && theirScore != 21 && theirScore - myScore != 2)) {
					response.setStatus(400);
					response.getWriter().println(createError("A non-11/21 score should win by exactly 2"));
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
				
			} else if (parameters.containsKey("acceptGame") || parameters.containsKey("declineGame") || parameters.containsKey("cancelGame")) {
				
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
				
				if (((parameters.containsKey("acceptGame") || parameters.containsKey("declineGame")) && existingGame.receiver != me.id) || (parameters.containsKey("cancelGame") && existingGame.sender != me.id)) {
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
					CGames.update(existingGame);
					
					// TODO: Epic elo things here :D
					int acceptedGames = CGames.getLatestGamesByStatus(StatusEnum.Status.ACCEPTED).size();
					System.out.println(acceptedGames + " of " + GlickoTwo.RATING_PERIOD + " games until Glicko");
					if (acceptedGames >= GlickoTwo.RATING_PERIOD) {
						
						GlickoTwo.run();
						
						/*
						List<OGame> gamesPlayed = CGames.getGamesForUserByStatus(me.id, StatusEnum.Status.ACCEPTED);
						List<OUser> opponents = new ArrayList<>();
						for (int i = 0; i < GlickoTwo.RATING_PERIOD; i++) {
							int opponentID = gamesPlayed.get(i).sender == me.id ? gamesPlayed.get(i).receiver : gamesPlayed.get(i).sender;
							opponents.add(CUser.findById(opponentID, false));
						}
						int m = GlickoTwo.RATING_PERIOD;
						// Step 2 in Glicko2
						double mu = me.getMu();
						double phi = me.getPhi();
						
						// Step 3
						double vSum = 0;
						for (int j = 1; j <= m; j++) {
							int oppInd = j-1;
							vSum += ( Math.pow(GlickoTwo.g(opponents.get(oppInd).getPhi()), 2) * GlickoTwo.E(mu, opponents.get(oppInd).getMu(), opponents.get(oppInd).getPhi()) * (1 - GlickoTwo.E(mu, opponents.get(oppInd).getMu(), opponents.get(oppInd).getPhi())) );
						}
						double v = 1 / vSum;
						
						// Step 4
						double deltaSum = 0;
						for (int j = 1; j <= m; j++) {
							int oppInd = j-1;
							deltaSum += ( GlickoTwo.g(opponents.get(oppInd).getPhi()) * (gamesPlayed.get(oppInd).calcScore(me.id) - GlickoTwo.E(mu, opponents.get(oppInd).getMu(), opponents.get(oppInd).getPhi())) );
						}
						double delta = v * deltaSum;
						
						// Step 5
						final double a = Math.log(Math.pow(me.volatility, 2));
						double A = a;
						double B = Math.log(Math.pow(delta, 2) - Math.pow(phi, 2) - v);
						if (Math.pow(delta, 2) <= Math.pow(phi, 2) + v) {
							double k = 1;
							while (GlickoTwo.f((a - (k*GlickoTwo.TAU)), delta, phi, v, a) < 0) {
								k += 1;
							}
							B = a - (k*GlickoTwo.TAU);
						}
						double fA = GlickoTwo.f(A, delta, phi, v, a);
						double fB = GlickoTwo.f(B, delta, phi, v, a);
						while (Math.abs(B - A) > GlickoTwo.EPSILON) {
							double C = A + (((A - B) * fA) / (fB - fA));
							double fC = GlickoTwo.f(C, delta, phi, v, a);
							if (fC * fB <= 0) {
								A = B;
								fA = fB; 
							} else {
								fA /= 2;
							}
							B = C;
							fB = fC;
						}
						double volatilityPrime = Math.exp(A/2);
						
						// Step 6
						double phiAsterisk = Math.sqrt(Math.pow(phi, 2) + Math.pow(volatilityPrime, 2));
						
						// Step 7
						double phiPrime = 1 / (Math.sqrt( (1 / Math.pow(phiAsterisk, 2)) + (1 / v) ));
						double muPrime = mu + (Math.pow(phiPrime, 2) * deltaSum);
						
						// Step 8
						double ratingPrime = (GlickoTwo.GLICKO2_CONV * muPrime) + GlickoTwo.BASE_RATING;
						double rdPrime = GlickoTwo.GLICKO2_CONV * phiPrime;
						*/
					}
					
				} else if (parameters.containsKey("declineGame")) {
					existingGame.status = StatusEnum.Status.REJECTED;
					CGames.update(existingGame);
					
					// TODO: Anti-spam & over-rejection things here D:
				} else {
					// Do we actually want to keep the game on record?
					CGames.delete(existingGame);
					response.setStatus(200);
					return;
				}
				
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
	
	/**
	 * Creates a JSON string with an error
	 *
	 * @param error the error
	 * @return String JSON to be set to a client
	 */
	public static String createError(String error) {
		return "{\"error\":\"" + error + "\"}";
	}
}