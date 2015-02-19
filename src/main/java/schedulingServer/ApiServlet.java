package schedulingServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;

import scheduleSolver.*;
import static util.Json.*;

public class ApiServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

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
		if (uri.equals("/new")) 
		{
			response.getWriter().print("Hello");
		} 
		else if (uri.equals("/check")) 
		{
			BufferedReader reader = request.getReader();
			StringBuffer jb = new StringBuffer();
			String line;
			
			while ((line = reader.readLine()) != null)
				jb.append(line);
			
			String json = jb.toString();
			
			try 
			{
				 ArrayList<scheduleSolver.Schedule.EventPOJO> solution = checkSchedule(json);
				 String toReturn = gson.toJson(solution);
				 response.getWriter().print(toReturn);
				 
			} 
			catch (JSONException e) 
			{
				ErrorPojo toReturn = new ErrorPojo();
				toReturn.Error = "Error parsing JSON request string";
				response.getWriter().print(gson.toJson(toReturn));
			}
			catch(Exception e)
			{
				e.printStackTrace();
				ErrorPojo toReturn = new ErrorPojo();
				toReturn.Error = "An unexpected error occured";
				response.getWriter().print(gson.toJson(toReturn));
			}	
		} 
		else if(uri.equals("/requestKey"))
		{
			//parse the response
			
			//generate key
			
			//register username to key
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

	private ArrayList<scheduleSolver.Schedule.EventPOJO> checkSchedule(String json) throws JSONException {
		JSONObject toCheck = new JSONObject(json);
		
		JSONArray jsonClasses = toCheck.getJSONArray("EVENT");
		JSONArray jsonResources = toCheck.getJSONArray("SPACE");
		
		Event[] events = parseEvents(jsonClasses);
		Space[] rooms = parseSpaces(jsonResources);
		
		Schedule scheduler = new Schedule("My Schedule", events, rooms);
		
		return scheduler.getSolution();
		
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

}
