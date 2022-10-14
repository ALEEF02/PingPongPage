package ppp;

import java.io.IOException;

import jakarta.servlet.RequestDispatcher;
/*import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;*/
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/user")
public class StaticServe extends HttpServlet {

	/*public void init(ServletConfig config) throws ServletException {
		getServletContext();
		super.init(config);
	}*/
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
	throws IOException {

		try {
			RequestDispatcher view = request.getRequestDispatcher("index.html");
			System.out.println(view.toString());
			view.forward(request, response);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}