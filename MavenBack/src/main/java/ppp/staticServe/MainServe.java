package ppp.staticServe;

import java.io.IOException;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("") // Disable until future need
public class MainServe extends HttpServlet {
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		try {
			if (request.getSession().getAttribute("new") == null && request.getSession().getAttribute("email") == null) {
			    // Freshly created.
				RequestDispatcher view = request.getRequestDispatcher("/home.html"); // The static HTML file to serve.
				response.setHeader("Cache-Control", "no-store, must-revalidate");
				response.setHeader("Pragma", "no-cache");
				response.setHeader("Expires", "0");
				System.out.println(view.toString());
				view.forward(request, response);
			} else {
			    // Already created.
				RequestDispatcher view = request.getRequestDispatcher("/index.html"); // The static HTML file to serve.
				response.setHeader("Cache-Control", "max-age=86400, public"); // 1 day
				System.out.println(view.toString());
				view.forward(request, response);
			}
			request.getSession().setAttribute("new", false);
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
