package ppp.staticServe;

import java.io.IOException;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

//@WebServlet("/images/*") // The forward-facing link // Disable until future need
public class ImagesServe extends HttpServlet {
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		try {
			// this shit broken lmao. We should be able to delete this servlet altogether bc of serviceworker
			RequestDispatcher view = request.getRequestDispatcher(request.getRequestURI()); // The static HTML file to serve.
			response.setHeader("Cache-Control", "max-age=31536000, public"); // 365 days for cache
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
