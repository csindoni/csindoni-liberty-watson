package com.csindoni.dev.liberty.api;

import java.text.SimpleDateFormat;
import java.util.Calendar; 
import java.util.Date; 


public class TestAlchemyAPI {

	public static void main(String[] args) throws Exception {
		
		String startdate="12/12/2010"; 
		String enddate="12/12/2017";
        String searchTerm="Donald Trump";
        int count=1;

		// format arguments
		SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");	
		// startdate
		Calendar startdate1 = Calendar.getInstance();
// Exception at the following step. 
// Exception at the following step. 
// Exception at the following step. 
// Exception at the following step. 
//		Date a = new Date() ; 
//		a = format1.parse(startdate); 
		startdate1.setTime(format1.parse(startdate));
		long startdate2 = startdate1.getTimeInMillis()/1000;
		// enddate
		Calendar enddate1 = Calendar.getInstance();
		enddate1.setTime(format1.parse(enddate));
		long enddate2 = enddate1.getTimeInMillis()/1000;
        
        
		// Create a class to test the IBM Alchemy API 
		AlchemyDataNewsAPI testAPI = new AlchemyDataNewsAPI (); 
		testAPI.getAlchemyDataNews(startdate2, enddate2, searchTerm, count);
		
		
	}

}
