package com.csindoni.dev.liberty.servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import com.cloudant.client.api.Database;

import com.csindoni.dev.liberty.nosql.CloudantClientManager;
import com.csindoni.dev.liberty.api.*;

/**
 * Servlet implementation class NewsSearchServlet
 */
@WebServlet(name = "com.csindoni.dev.liberty.NewsSearchServlet",
			loadOnStartup=1,
        	urlPatterns = "/news")
public class NewsSearchServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	/**
	* @see HttpServlet#HttpServlet()
	*/
	public NewsSearchServlet() {
		super();
	}

	/**
	* @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	*/
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
		throws ServletException, IOException {

	String hosturl = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+""+request.getContextPath();

	SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
	Calendar now = Calendar.getInstance();
	String searchdate = format1.format(now.getTimeInMillis());
	String startdate = (String) request.getParameter("startdate");
	String enddate = (String) request.getParameter("enddate");
	String searchterm = (String) request.getParameter("searchterm");
	String count = (String) request.getParameter("count");

	// call AlchemyData News API
	String alchemyResults = getAlchemyNewsApi(hosturl, startdate, enddate, searchterm, count);

	// build responseJson
	JsonArray responseJsonArray = 
			buildResponseJson(alchemyResults, searchdate, startdate, enddate, searchterm, count);
	request.getSession().setAttribute("result", responseJsonArray);

	request.getRequestDispatcher("jsp/result.jsp").forward(request, response);

	// Save to CloudantDB
	String cloudantResults = saveToCloudantDb(hosturl, responseJsonArray);	
	
	}

	/**
	* @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	*/
	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
		throws ServletException, IOException {
	doGet(request, response);
	}

	private JsonArray buildResponseJson(String alchemyResults, String searchdate, String startdate,
		String enddate, String searchterm, String count) {
	// results json
	Gson gson = new Gson();
	JsonArray resultArray = gson.fromJson(alchemyResults, JsonArray.class);
	JsonObject resultJsonObject = resultArray.get(0).getAsJsonObject();
	JsonArray docs = resultJsonObject.getAsJsonArray("docs");

	JsonObject result = new JsonObject();
	result.add("result", docs);
	result.addProperty("searchdate", searchdate);
	result.addProperty("startdate", startdate);
	result.addProperty("enddate", enddate);
	result.addProperty("searchterm", searchterm);
	result.addProperty("count", count);

	JsonArray responseJsonArray = new JsonArray();
	responseJsonArray.add(result);

	return responseJsonArray;		
	}

	private String getAlchemyNewsApi(String hosturl, String startdate, String enddate, String searchterm, String count){		
	String params = "startdate="+startdate+"&enddate="+enddate+"&searchterm="+searchterm+"&count="+count;
	String urlString = hosturl+"/api/watson/news";

	String response = callApi("get", urlString, params);
	return response;
	}

	private String callApi(String method, String urlString, String params){

	String response = "";
	HttpURLConnection conn = null;
	try {
		if(method.toLowerCase()=="get"){

			URL url = new URL(urlString+"?"+params);
			conn = (HttpURLConnection) url.openConnection();
			// I am getting a failure on this url: http://localhost:9080/JavaCloudantApp/api/watson/news?startdate=12/12/2010&enddate=12/12/2017&searchterm=Donald Trump&count=1
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");

		}else{
			return "Illegal HTTP Method";
		}

		if (conn.getResponseCode() != 200) {
			throw new RuntimeException("Failed : HTTP error code : "+ conn.getResponseCode());
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(
			(conn.getInputStream())));	
		String output;			
		while ((output = br.readLine()) != null) {
			response += output;
		}
		conn.disconnect();

	} catch (MalformedURLException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	}		
	return response;
	}

	private String saveToCloudantDb(String hosturl, JsonArray responseJsonArray) 
			 throws IOException {	
				
				String id = String.valueOf(System.currentTimeMillis());
				JsonObject results = responseJsonArray.get(0).getAsJsonObject();
				JsonArray result = results.getAsJsonArray("result");
				String searchdate = results.get("searchdate").getAsString();
				String startdate = results.get("startdate").getAsString();
				String enddate = results.get("enddate").getAsString();
				String searchterm = results.get("searchterm").getAsString();
				String count = results.get("count").getAsString();
				
				Database db = CloudantClientManager.getDB();
				
				Map<String, Object> data = new HashMap<String, Object>();
				data.put("_id", id);
				data.put("results", results);		
				data.put("searchdate", searchdate);
				data.put("startdate", startdate);
				data.put("enddate", enddate);
				data.put("searchterm", searchterm);
				data.put("count", count);
				db.save(data);
				
				HashMap<String, Object> obj = db.find(HashMap.class, id);
				
				JsonObject jsonObject = new JsonObject();
				jsonObject.addProperty("id", obj.get("_id") + "");
				jsonObject.addProperty("results", obj.get("results") + "");
				
				return jsonObject.toString();
		}
}