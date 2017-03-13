package com.csindoni.dev.liberty.nosql;

import com.cloudant.client.api.ClientBuilder;
import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;
import com.cloudant.client.org.lightcouch.CouchDbException;

import com.csindoni.dev.liberty.api.BluemixConfig;

public class CloudantClientManager {

	private static CloudantClient cloudant = null;
	private static Database db = null;

	private CloudantClientManager() {
	}
	
	private static void initClient() {
		if (cloudant==null) {
			synchronized (CloudantClientManager.class) {
				if (cloudant != null) {
					return;
				}
				cloudant = createClient();
			} // end synchronized
		}
	}

	private static CloudantClient createClient() {		
		String user = BluemixConfig.getInstance().getCloudantDBUsername();
		String password = BluemixConfig.getInstance().getCloudantDBPassword();	
		try {
			CloudantClient client = ClientBuilder.account(user)
					.username(user)
					.password(password)
					.build();
			return client;
		} catch (CouchDbException e) {
			throw new RuntimeException("Unable to connect to repository", e);
		} catch (Exception e){
			throw new RuntimeException("Exception ...", e);
		}
	} 

	public static Database getDB() {
		if (cloudant==null) {
			initClient();
		}
		if (db == null) {
			try {
				db = cloudant.database(BluemixConfig.getInstance().getCloudantDatabaseName(), true);
			} catch (Exception e) {
				throw new RuntimeException("DB Not found", e);
			}
		}
		return db;
	}
}