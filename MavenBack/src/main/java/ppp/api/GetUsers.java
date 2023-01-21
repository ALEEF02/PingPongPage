package ppp.api;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/*import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;*/
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ppp.auth.Authenticator;
import ppp.db.controllers.CUser;
import ppp.db.model.OUser;
import ppp.meta.LoginEnum;

/*
 * API REF:
 * 
 * Ind: independent, Exc: exclusive of other exclusives
 * 
 * GET:
 * 	Parameters, highest priority first:
 * 		None: The logged in user. Useful for determining if the user is logged in
 * 		withRank: Specify to also get the user's rank. Will add some processing time
 * 		ranks [exc]: Return the top ranking players, limited to parameter. Valid 1-50. Default 10.
 * 			mRD: The maximum RD to get
 * 		user [exc]:
 * 			name: Return the user by the name
 * 		cached: Specify whether to use the cached users
 * 		withHistory: Specify to also get the user's Glicko histories.
 */

@WebServlet("/api/users")
public class GetUsers extends HttpServlet {
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)	throws IOException {
		
		Map<String, String[]> parameters = request.getParameterMap();
		List<OUser> users = null;
		Authenticator auth = new Authenticator();
		boolean loggedIn = auth.login(request) == LoginEnum.Status.SUCCESS;
		int limit = 10; // Return maximum 10 users by default
		boolean withHistory = parameters.containsKey("withHistory");
		
		try {
			// Get the top-ranked players
			if (parameters.containsKey("ranks")) {
				
				boolean cache = false;
				
				try {
					limit = Integer.parseInt(parameters.get("ranks")[0]);
				} catch (Exception e) {
					response.setStatus(400);
				    response.getWriter().print("[]");
				    return;
			    }
				
				if (parameters.containsKey("cached")) {
					cache = true;
				}
				
				// mRD limits the maximum Rating Deviation a user can have. This is useful for filtering out new players, as they have the highest RD (350)
				if (parameters.containsKey("mRD")) {
					int minRD = 350;
					try {
						minRD = Integer.parseInt(parameters.get("mRD")[0]);
					} catch (Exception e) {
						response.setStatus(400);
					    response.getWriter().print("[]");
					    return;
				    }
					users = CUser.getTopRanks(limit, minRD, cache);
				} else {
					users = CUser.getTopRanks(limit, cache);
				}
			
			// Get a specific user
			} else if (parameters.containsKey("user")) {
				
				// TODO: Sanatize user input
				
				String userQuery = parameters.get("user")[0];
				OUser user;
				
				response.setContentType("application/json;");
				
				try {
					user = CUser.findByUsername(userQuery);
				} catch (Exception e) {
					response.setStatus(400);
				    response.getWriter().print("{}");
				    return;
			    }
				if (user.id == 0) {
					response.setStatus(404);
				    response.getWriter().print("{}");
				    return;
				}
				
				// Fetch the user's rank
				if (parameters.containsKey("withRank")) {
					user.getRank();
				}
				response.getWriter().print(user.toPublicJSON(false, withHistory));
				return;
			
			// Return the logged in user's own information. This can be used as a front-end login check for UI adjustment
			} else {
				if (!loggedIn) {
					response.setStatus(401);
					response.getWriter().print("{}");
					return;
				}
				OUser user = CUser.findByEmail((String)request.getSession().getAttribute("email"));
				
				// This user has a valid email & token pair. Serve them the things they want & have access to.
				response.setStatus(200);
				response.getWriter().print(user.toPublicJSON());
				return;
			}
			
			// Return the users in a JSON array
			response.setContentType("application/json;");
		    response.getWriter().print("[");
		    for (OUser user:users) {
				response.getWriter().print(user.toPublicJSON(true, withHistory));
		    	if (users.indexOf(user) != users.size() - 1) {
		    		response.getWriter().print(",");
		    	}
		    }
		    response.getWriter().print("]");
		    
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			response.setStatus(500);
		    response.getWriter().print("{\"error\":\"" + e + "\"}");
		    return;
		}
	}
}