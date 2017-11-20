package com.db;

import java.sql.Connection;
import java.sql.DriverManager;

import com.logger.MonitorLogger;
import com.util.PropertyReader;

public class MyConnection {
	public static Connection getConnection() {
		Connection con = null;
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			String host = PropertyReader.readProperty("DB_HOST");
			String port =PropertyReader.readProperty("DB_PORT");
			String sid = PropertyReader.readProperty("DB_SID");
			String uid = PropertyReader.readProperty("DB_USER");
			String pass = PropertyReader.readProperty("DB_PASS");
			con=DriverManager.getConnection("jdbc:oracle:thin:@"+host+":"+port+":"+sid,uid,pass);  
		} catch (Exception e) {
			MonitorLogger.error(MyConnection.class.toString(), "Error while getting connection: ", e);
		}
		return con;
	}
	
	public static String buildQuery(String query, String... filters){
		StringBuilder build = new  StringBuilder(query);
		for (String  filter: filters) {
			build.append(" AND "+ filter);
		}
		return build.toString();
	}
	
	public static void main(String ar[]){
		System.out.println(MyConnection.getConnection());
	}
}
