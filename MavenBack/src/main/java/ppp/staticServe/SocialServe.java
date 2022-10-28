package ppp.staticServe;

import java.io.IOException;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/socials") // The forward-facing link
public class SocialServe extends HttpServlet {
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		try {
			
			RequestDispatcher view = request.getRequestDispatcher("/socials.html"); // The static HTML file to serve.
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
