package ppp;

import java.io.File;
import java.net.URL;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

import com.kaaz.configuration.ConfigurationBuilder;

import ppp.ServerConfig;
import ppp.db.WebDb;

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

		// Look for annotations in the classes directory (dev server) and in the
		// jar file (live server).
		webAppContext.setAttribute(
			"org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern",
			".*/target/classes/|.*\\.jar|.*/WEB-INF/classes/");
		//webAppContext.setContextPath("/");
		//webAppContext.setWar("target/ppp.war");

		// Load the env variables
		if (!System.getProperty("CFG").equals(null)) { // We're running on the actual server. This is a really nasty way to do this, but Kaaz's configbuilder doesn't support this & I'm lazy.
			if (!System.getProperty("EMAIL_HOST").equals(null)) ServerConfig.EMAIL_HOST = System.getProperty("EMAIL_HOST");
			if (!System.getProperty("EMAIL_USER").equals(null)) ServerConfig.EMAIL_USER = System.getProperty("EMAIL_USER");
			if (!System.getProperty("EMAIL_PASSWORD").equals(null)) ServerConfig.EMAIL_PASSWORD = System.getProperty("EMAIL_PASSWORD");
			if (!System.getProperty("DB_HOST").equals(null)) ServerConfig.DB_HOST = System.getProperty("DB_HOST");
			if (!System.getProperty("DB_USER").equals(null)) ServerConfig.DB_USER = System.getProperty("DB_USER");
			if (!System.getProperty("DB_PASS").equals(null)) ServerConfig.DB_PASS = System.getProperty("DB_PASS");
		} else { // We're running through Eclipse
			new ConfigurationBuilder(ServerConfig.class, new File("application.cfg")).build(true);
		}
		
		// Connect to the DB
		WebDb.init();
		
		// Start the server! 🚀
		server.start();
		System.out.println("Server started!");

		// Keep the main thread alive while the server is running.
		server.join();
	}
}