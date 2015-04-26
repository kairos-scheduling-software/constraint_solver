package schedulingServer;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;

public class Main extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static boolean LOCAL_RUN = false;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		if (req.getRequestURI().endsWith("/db")) {
			showDatabase(req, resp);
		} else {
			showHome(req, resp);
		}
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		String msg = "Visit kairos-api-docs.eu1.frbit.net for post method's target url";
		
		response.getWriter().print(msg);
	}

	private void showHome(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.getWriter().printf("%s\tHello from Java!", req.getRequestURI());
	}

	private void showDatabase(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			Connection connection = getConnection();

			Statement stmt = connection.createStatement();
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ticks (tick timestamp)");
			stmt.executeUpdate("INSERT INTO ticks VALUES (now())");
			ResultSet rs = stmt.executeQuery("SELECT tick FROM ticks");

			String out = "Hello!\n";
			while (rs.next()) {
				out += "Read from DB: " + rs.getTimestamp("tick") + "\n";
			}

			resp.getWriter().print(out);
		} catch (Exception e) {
			resp.getWriter().print("There was an error: " + e.getMessage());
		}
	}

	private Connection getConnection() throws URISyntaxException, SQLException {
		URI dbUri = new URI(System.getenv("DATABASE_URL"));

		String username = dbUri.getUserInfo().split(":")[0];
		String password = dbUri.getUserInfo().split(":")[1];
		String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + dbUri.getPath();

		return DriverManager.getConnection(dbUrl, username, password);
	}

	public static void main(String[] args) throws Exception {
		// Check for local-run, aka, no user database
		String para = "";
		if (args.length > 0) para = args[0].toLowerCase();
		
		if (para.equals("--local")) LOCAL_RUN = true;
		
		// Default port if there's no environment variable
		int port = 8080;
		
		// Getting env's port information
		// TODO: Should we handle parseInt exception here?
		String portStr = System.getenv("PORT");
		if (portStr != null) port = Integer.valueOf(portStr);
		
		// Create server object
		Server server = new Server(port);
		
		// Configure server settings
		ServletContextHandler context = new ServletContextHandler(
				ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		server.setHandler(context);
		context.addServlet(new ServletHolder(new Main()), "/*");
		context.addServlet(new ServletHolder(new ApiServlet()), "/api/*");
		
		// Start server and wait
		server.start();
		server.join();
	}
}
