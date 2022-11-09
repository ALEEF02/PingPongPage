package ppp.staticServe;

import java.io.IOException;

import jakarta.servlet.RequestDispatcher;
/*import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;*/
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/qa")
public class QAServe extends HttpServlet {
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
	throws IOException {
		
		try {
			RequestDispatcher view = request.getRequestDispatcher("/qaa.html");
			
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