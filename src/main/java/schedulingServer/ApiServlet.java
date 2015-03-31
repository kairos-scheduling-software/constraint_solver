package schedulingServer;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import scheduleSolver.*;
import scheduleSolver.Schedule.SolutionLevel;
import util.ScheduleData;
import static util.IO.*;

public class ApiServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public ApiServlet()
	{
		super();
		SetUpAPIDatabase();
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		
		response.getWriter().write("ERROR: All api calls must be made with post");
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		String uri = request.getPathInfo();
		response.setContentType("application/json");
		
		Map<String, Object> outputMap;
		if (uri == null) {
			outputMap = getError("Invalid path for post method");
		} else {
			Scanner sc = new Scanner(request.getReader());
			String input = readInput(sc, true);
			sc.close();
			
			if (uri.endsWith("/"))
				uri = uri.substring(0, uri.length() - 1);
			uri = uri.toLowerCase();
			switch (uri) {
				case "/check":
					outputMap = getSchedule(input, SolutionLevel.CONFLICTS);
					break;
				case "/new":
					outputMap = getSchedule(input, SolutionLevel.ALL_EVENTS);
					break;
				case "/requestKey":
					outputMap = getRequestKey(input);
					break;
				default:
					outputMap = getError("The requested API path does not exist");
			}
		}
		Gson gson = new Gson();
		String toReturn = gson.toJson(outputMap);
		response.getWriter().print(toReturn);
	}
	
	private Map<String, Object> getSchedule(String input, SolutionLevel level) {
		Map<String, Object> outputMap;
		try {
			if (!verifyKey(input))
				outputMap = getError("Error invalid API key");
			else {
				ScheduleData data = ScheduleData.parseJson(input);
				if (data != null) {
					Schedule scheduler = new Schedule(data);
					outputMap = scheduler.getSolution(level);
				} else {
					outputMap = getError("Error parsing JSON request string");
				}
			}
		} catch (JsonSyntaxException e) {
			outputMap = getError("Error parsing JSON request string");
		} catch (Exception e) {
			e.printStackTrace();
			outputMap = getError("An unexpected error occured");
		}
		
		return outputMap;
	}
	
	private Map<String, Object> getRequestKey(String input) {
		Map<String, Object> outputMap = new HashMap<String, Object>();
		
		//this will be the api key assigned to our api site
		String secretKey = "e5506213-3196-4e6a-9613-237fd987446d";
		try {
			//parse the response
			
			JsonParser parser = new JsonParser();
			JsonObject toCheck = parser.parse(input).getAsJsonObject();
			String key = getValue(toCheck, "key");
			String email = getValue(toCheck, "email");
			
			if (secretKey.equals(key)) {
				//generate and register key
				String generatedKey = requestKey(email);
				outputMap.put("key", generatedKey);
				outputMap.put("email", email);
			}
			else outputMap = getError("Error APIKey mismatch");
		
		} catch(JsonSyntaxException e) {
			outputMap = getError("JSON parsing error");
		}
		catch(Exception e) {
			e.printStackTrace();
			outputMap = getError("An unexpected error occured");
		}
		
		return outputMap;
	}
	
	private Map<String, Object> getError(String str) {
		Map<String, Object> error = new HashMap<String, Object>();
		error.put("Error", str);
		return error;
	}
	
	private String getValue(JsonObject jsonObj, String key) {
		String value;
		
		JsonElement jsonEl = jsonObj.get(key);
		if (jsonEl == null) value = null;
		else value = jsonEl.getAsString();
		
		return value;
	}
	
	private String requestKey(String email) throws URISyntaxException, SQLException
	{
		UUID apiKey = UUID.randomUUID();
		Connection connect = getConnection();
		PreparedStatement stmt = connect.prepareStatement("insert into apikey (key, email, blacklist) values (?, ?, ?)");
		
		stmt.setString(1, apiKey.toString());
		stmt.setString(2, email);
		stmt.setBoolean(3, false);
		stmt.executeUpdate();
		stmt.close();
		
		return apiKey.toString();
	}
	
	private boolean verifyKey(String json)
			throws URISyntaxException, SQLException, JsonSyntaxException
	{
		String key = null;
		JsonParser parser = new JsonParser();
		JsonObject toCheck = parser.parse(json).getAsJsonObject();
		key = getValue(toCheck, "APIKey");
		
		if (key == null) return false;
		Connection connect = getConnection();
		
		PreparedStatement stmt = connect.prepareStatement("select * from apikey where key = ?");
		stmt.setString(1, key);
		
		ResultSet rs = stmt.executeQuery();
		
		if(rs.next())
		{
			if(rs.getBoolean("blacklist"))
			{
				rs.close();
				return false;
			}
			
			stmt.close();
			stmt = connect.prepareStatement("insert into log (id, key, timestamp) values (default, ?, ?)");
			stmt.setString(1, key);
			stmt.setTimestamp(2, new Timestamp(new java.util.Date().getTime()));
			stmt.executeUpdate();
			
			stmt.close();
			connect.close();
			
			rs.close();
			return true;
		}
		
		rs.close();
		return false;
	}

	private Connection getConnection() throws URISyntaxException, SQLException
	{
		URI dbUri = new URI(System.getenv("DATABASE_URL"));

		String username = dbUri.getUserInfo().split(":")[0];
		String password = dbUri.getUserInfo().split(":")[1];
		String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + dbUri.getPath();

		return DriverManager.getConnection(dbUrl, username, password);
	}
	
	private void SetUpAPIDatabase()
	{
		try 
		{
			Connection connection = getConnection();

			Statement stmt = connection.createStatement();
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ApiKey (key TEXT unique, email TEXT, blacklist BOOLEAN)");
			stmt.close();
			
			stmt = connection.createStatement();
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS log (id SERIAL, key TEXT, timestamp TIMESTAMP)");
			stmt.close();
			
			try 
			{
				PreparedStatement prepstmt = (PreparedStatement) connection.prepareStatement("insert into ApiKey (key, email, blacklist) values (?,?,?)");
				prepstmt.setString(1, "1bb0ea87-d786-4300-903d-e3aa4e3ac670");
				prepstmt.setString(2, "foo@bar.com");
				prepstmt.setBoolean(3, false);

				prepstmt.executeUpdate();
				prepstmt.close();

				prepstmt = (PreparedStatement) connection.prepareStatement("insert into ApiKey (key, email, blacklist) values (?,?,?)");
				prepstmt.setString(1, "e5506213-3196-4e6a-9613-237fd987446d");
				prepstmt.setString(2, "foo@bar.com");
				prepstmt.setBoolean(3, false);

				prepstmt.executeUpdate();
				prepstmt.close();
			} 
			catch (Exception e) 
			{
				System.out.println("The keys were already in the database");
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Database could not be initialized");
		}
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Add code for testing the module
		String input = "{\"email\": \"dtt.vinh@gmail.com\"; \"APIKey\": 13}";
		JsonParser parser = new JsonParser();
		JsonObject toCheck = parser.parse(input).getAsJsonObject();
		String key = toCheck.get("APIKey").getAsString();
		
		System.out.println(key);
	}
}
