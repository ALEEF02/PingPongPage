package ppp;

import java.io.File;
import java.net.URL;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;

import com.kaaz.configuration.ConfigurationBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ppp.service.ServiceHandlerThread;
//import ppp.ServerConfig;
import ppp.db.WebDb;
import ppp.db.controllers.CGames;
import ppp.db.controllers.CGlicko;
import ppp.db.controllers.CUser;
import ppp.meta.GlickoTwo;

/**
 * Starts up a server that serves static files and annotated servlets.
 */

public class ServerMain {

	private static final Logger logger = LoggerFactory.getLogger(ServerMain.class);

	public static void main(String[] args) throws Exception {

		// Create a server that listens on port 8080.
		Server server = new Server(8080);
		WebAppContext webAppContext = new WebAppContext();
		server.setHandler(webAppContext);
		logger.info("Web server created.");

		// Load static content from the resources directory.
		URL webAppDir =
			ServerMain.class.getClassLoader().getResource("META-INF/resources");
		webAppContext.setResourceBase(webAppDir.toURI().toString());
		
		// An attempted fix for the welcome servlet. Keeping it here, not sure why lmao
		/*ServletHolder holderDef = new ServletHolder("default", DefaultServlet.class);
		holderDef.setInitParameter("welcomeServlets","true");
		webAppContext.addServlet(holderDef, "/");*/

		// Look for annotations in the classes directory (dev server) and in the jar file (live server).
		webAppContext.setAttribute(
			"org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern",
			".*/target/classes/|.*\\.jar");

		String environment = System.getProperty("environment", "dev");
		logger.info("Application environment: {}", environment);
		String configFileName = environment.equalsIgnoreCase("prod")
				? "application.cfg"
				: "application-development.cfg";

		// Load the env variables from the config. This will also build a new cfg file if none exists.
		// If running in dev mode, the db credentials should be obtained from the docker-compose
        new ConfigurationBuilder(ServerConfig.class, new File(configFileName)).build(true);
		
		// Connect to the DB
		logger.info("DB connecting...");
		WebDb.init();
		logger.info("DB connected.");
		
		// Initialize out DB caches
		logger.info("Initializing caches...");
		CUser.init();
		CGames.init();
		CGlicko.init();
		GlickoTwo.init();
		logger.info("Caches initialized.");
		
		// Initialize the services
		logger.info("Initializing services...");
		Thread serviceHandler = new ServiceHandlerThread();
        serviceHandler.start();
		logger.info("Services initialized.");
        
		// Start the server! 🚀
		server.start();
		logger.info("Server started!");

		// Keep the main thread alive while the server is running.
		server.join();
	}
}