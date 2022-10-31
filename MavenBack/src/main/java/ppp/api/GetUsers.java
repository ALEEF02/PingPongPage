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
 * 		user [exc]:
 * 			name: Return the user by the name
 * 
 * POST:
 */

@WebServlet("/api/users")
public class GetUsers extends HttpServlet {
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)	throws IOException {
		
		Map<String, String[]> parameters = request.getParameterMap();
		List<OUser> users = null;
		Authenticator auth = new Authenticator();
		boolean loggedIn = auth.login(request);
		int limit = 10;
		try {			
			
			if (parameters.containsKey("ranks")) {
				
				try {
					limit = Integer.parseInt(parameters.get("ranks")[0]);
				} catch (Exception e) {
					response.setStatus(400);
				    response.getWriter().print("[]");
				    return;
			    }
				users = CUser.getTopRanks(limit);
				
			} else if (parameters.containsKey("user")) {
				
				// TODO: Sanatize user input
				
				String userQuery = parameters.get("user")[0];
				OUser user;
				
				try {
					user = CUser.findByUsername(userQuery);
				} catch (Exception e) {
					response.setStatus(400);
				    response.getWriter().print("{}");
				    return;
			    }
				
				response.setContentType("application/json;");
				if (user.id == 0) {
					response.setStatus(404);
				    response.getWriter().print("{}");
				    return;
				}
				
				if (parameters.containsKey("withRank")) {
					user.getRank();
				}
				response.getWriter().print(user.toPublicJSON());
				return;
				
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
			
			response.setContentType("application/json;");
		    response.getWriter().print("[");
		    for (OUser user:users) {
				response.getWriter().print(user.toPublicJSON(true));
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