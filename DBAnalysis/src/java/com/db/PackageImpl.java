package com.db;

import java.sql.Connection;
import java.util.Set;

import com.logger.MonitorLogger;

public class PackageImpl {
	public static Set<String> getPackages(Connection con){
		String query = "SELECT * FROM USER_OBJECTS WHERE OBJECT_TYPE IN ('PACKAGE')";
		String packageFilter ="AND OBJECT_NAME LIKE 'MS_%'";
		String finalQuery = MyConnection.buildQuery(query, packageFilter);
		try{
			System.out.println(Queries.hookProcedures);
		}catch (Exception e) {
			MonitorLogger.error(PackageImpl.class.getName(), "Error while geting Packages", e);
		}
		return null;
	}
	
	public static void main(String ar[]){
		System.out.println(getPackages(MyConnection.getConnection()));
	}
}
