package schedulingServer;

import java.io.BufferedReader;
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
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import scheduleSolver.*;
import util.ScheduleData;

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
		Gson gson = new Gson();
		
		if (uri == null) 
		{
			ErrorPojo toReturn = new ErrorPojo();
			toReturn.Error = "Invalid path for post method";
			response.getWriter().print(gson.toJson(toReturn));
		}
		if (uri.endsWith("/")) 
		{
			uri = uri.substring(0, uri.length() - 1);
		} 
		else if (uri.equals("/check") || uri.equals("/new")) 
		{
			BufferedReader reader = request.getReader();
			StringBuffer jb = new StringBuffer();
			String line;
			
			while ((line = reader.readLine()) != null)
				jb.append(line);
			
			String json = jb.toString();

			try 
			{
				if (!verifyKey(json))
				{
					ErrorPojo toReturn = new ErrorPojo();
					toReturn.Error = "Error invalid API key";
					response.getWriter().print(gson.toJson(toReturn));
				} 
				else 
				{
					Map<String, Object> solution = checkSchedule(json);
					String toReturn = gson.toJson(solution);
					response.getWriter().print(toReturn);
				}
			} 
			catch (IllegalArgumentException e) 
			{
				ErrorPojo toReturn = new ErrorPojo();
				toReturn.Error = "Error parsing JSON request string";
				response.getWriter().print(gson.toJson(toReturn));
			}
			catch (JSONException e) {
				ErrorPojo toReturn = new ErrorPojo();
				toReturn.Error = "Solver error";
				response.getWriter().print(gson.toJson(toReturn));
			}
			catch (Exception e) 
			{
				e.printStackTrace();
				ErrorPojo toReturn = new ErrorPojo();
				toReturn.Error = "An unexpected error occured";
				response.getWriter().print(gson.toJson(toReturn));
			}
		}
		else if(uri.equals("/requestKey"))
		{
			try
			{
				BufferedReader reader = request.getReader();
				StringBuffer jb = new StringBuffer();
				String line;
			
				while ((line = reader.readLine()) != null)
					jb.append(line);
			
				String json = jb.toString();
				//parse the response
				
				//this will be the api key assigned to our api site
				ApiKeyRequest apikeyrequest = gson.fromJson(json, ApiKeyRequest.class);
				
				if(apikeyrequest.key.equals("e5506213-3196-4e6a-9613-237fd987446d"))
				{
					//generate and register key
					String generatedKey = requestKey(apikeyrequest.email);
					ApiKeyRequest keyResponse = new ApiKeyRequest();
					keyResponse.key = generatedKey;
					keyResponse.email = apikeyrequest.email;
					
					response.getWriter().print(gson.toJson(keyResponse));
				}
				else
				{
					ErrorPojo toReturn = new ErrorPojo();
					toReturn.Error = "Error APIKey mismatch";
					response.getWriter().print(gson.toJson(toReturn));
				}
			
			}
			catch(Exception e)
			{
				e.printStackTrace();
				ErrorPojo toReturn = new ErrorPojo();
				toReturn.Error = "An unexpected error occured";
				response.getWriter().print(gson.toJson(toReturn));
			}
		}
		else 
		{
			ErrorPojo toReturn = new ErrorPojo();
			toReturn.Error = "The requested API path does not exist";
			response.getWriter().print(gson.toJson(toReturn));
		}
	}
	
	/*private JSONObject generateSchedule(JSONObject data) throws JSONException {
		// Work with the data using methods like...
		// int someInt = jsonObject.getInt("intParamName");
		// String someString = jsonObject.getString("stringParamName");
		// JSONObject nestedObj = jsonObject.getJSONObject("nestedObjName");
		// JSONArray arr = jsonObject.getJSONArray("arrayParamName");
		// etc...
		
		System.out.println("Get request for creating new schedule: " + data.toString());
		
		Schedule scheduler = new Schedule();
		
		if (scheduler.findSolution())
			return scheduler.getSolution();
		
		return null;
	}*/

	private Map<String, Object> checkSchedule(String json) throws JSONException {
		ScheduleData data = ScheduleData.parseJson(json);
		
		Schedule scheduler = new Schedule("My Schedule", data.events, data.spaces);
		
		return scheduler.getSolution2();
		
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
	
	private Boolean verifyKey(String json) throws URISyntaxException, SQLException
	{
//		JSONObject toCheck = new JSONObject(json);
//		String key = toCheck.getString("APIKey");
		
		String key = null;
		try {
			JsonParser parser = new JsonParser();
			JsonObject toCheck = parser.parse(json).getAsJsonObject();
			key = toCheck.get("APIKey").getAsString();
		} catch (Exception e) {
			throw new IllegalArgumentException("Json parser exception");
		}
		
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
		// TODO Auto-generated method stub
		
		// Add code for testing the module

	}
	
	public class ErrorPojo
	{
		public String Error;
	}
	
	public class ApiKeyRequest
	{
		public String key;
		public String email;
	}

}
