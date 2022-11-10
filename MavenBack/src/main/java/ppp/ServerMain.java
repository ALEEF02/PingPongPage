package ppp;

import java.io.File;
import java.net.URL;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;

import com.kaaz.configuration.ConfigurationBuilder;

//import ppp.ServerConfig;
import ppp.db.WebDb;
import ppp.db.controllers.CGames;
import ppp.db.controllers.CGlicko;
import ppp.db.controllers.CUser;

/**
 * Starts up a server that serves static files and annotated servlets.
 */

public class ServerMain {

	public static void main(String[] args) throws Exception {

		// Create a server that listens on port 8080.
		Server server = new Server(8080);
		WebAppContext webAppContext = new WebAppContext();
		server.setHandler(webAppContext);

		// Load static content from the resources directory.
		URL webAppDir =
			ServerMain.class.getClassLoader().getResource("META-INF/resources");
		webAppContext.setResourceBase(webAppDir.toURI().toString());
		/*ServletHolder holderDef = new ServletHolder("default", DefaultServlet.class);
		holderDef.setInitParameter("welcomeServlets","true");
		webAppContext.addServlet(holderDef, "/");*/

		// Look for annotations in the classes directory (dev server) and in the
		// jar file (live server).
		webAppContext.setAttribute(
			"org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern",
			".*/target/classes/|.*\\.jar");

		// Load the env variables
        new ConfigurationBuilder(ServerConfig.class, new File("application.cfg")).build(true);
		
		// Connect to the DB
		System.out.print("DB connecting...\n\t");
		WebDb.init();
		System.out.println("DB connected.");
		
		// Initialize out DB caches
		System.out.print("Initializing caches... ");
		CUser.init();
		CGames.init();
		CGlicko.init();
		System.out.println("done.");
		
		// Start the server! 🚀
		server.start();
		System.out.println("Server started!");

		// Keep the main thread alive while the server is running.
		server.join();
	}
}