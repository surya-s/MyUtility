package com.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import com.core.bean.*;
import com.db.MyConnection;
import com.db.Queries;
import com.db.SQLBuilder;
import com.logger.MonitorLogger;
import com.util.AroundString;
import com.util.Check;
import com.util.PropertyReader;
import com.util.RegexExtractor;
 
public class AnalyzePLSQL {
	public static void main(String ar[]){
		LinkedHashMap<String,List<PLSQLObject>> packageGroup = listAllProcedures();
//		System.out.println(packageGroup);
		analyzeProcedure(packageGroup);
	}

	private static void analyzeProcedure(LinkedHashMap<String, List<PLSQLObject>> packageGroup) {
		for (Entry<String, List<PLSQLObject>> entry : packageGroup.entrySet()) {
			String packageName = entry.getKey();
			try(Connection con = MyConnection.getConnection()){
				String content = extractProcedureCode(con, packageName);
				content = AroundString.removeComments(content);
				if("YES".equalsIgnoreCase(PropertyReader.readProperty("WRITE_PACKAGE_TO_FILE"))){
					writeToFile(content, packageName, "pls");
				}
				analyzePLSQL(content, entry.getValue());
			}catch(Exception e){
				MonitorLogger.error(AnalyzePLSQL.class.getName()," Error while analysis of Package "+packageName , e);
			}
		}
	}

	private static void analyzePLSQL(String content, List<PLSQLObject> value) {
		for (PLSQLObject plsqlObject : value) {
			if("YES".equalsIgnoreCase(PropertyReader.readProperty("WRITE_PACKAGE_TO_FILE"))){
				try {
					String source = getPackageSource(plsqlObject.getProcedureName(), content);
					if(Check.hasContent(source)){
						writeToFile(source, plsqlObject.getProcedureName(),"pks");
					}else{
						MonitorLogger.error(AnalyzePLSQL.class.getName(),"Regex failed to extract Source for "+plsqlObject.getHookName(),null);
					}
				} catch (IOException e) {
					MonitorLogger.error(AnalyzePLSQL.class.getName(), "Error while writing Procedure or function to file : "+plsqlObject.getProcedureName(), e);
				}
			}
		}
	}

	private static String getPackageSource(String procedureName, String source) {
		return RegexExtractor.extractPLSQLCode(procedureName, source);
	}

	private static void writeToFile(String content, String name, String extension) throws IOException {
		File file = new File("Reports/"+name+"."+extension);
		FileWriter fileWriter = new FileWriter(file);
		fileWriter.write(content);
		fileWriter.flush();
		fileWriter.close();
	}

	private static String extractProcedureCode(Connection con, String procedureName) {
		StringBuilder myPackageSource = new StringBuilder();
		try(ResultSet rs = new SQLBuilder(Queries.packageSourceQuery.toString(), procedureName).getResultSet(con)){
			while(rs.next()){
				myPackageSource.append(rs.getString("TEXT"));
			}
		} catch (SQLException e) {
			MonitorLogger.error(AnalyzePLSQL.class.getName(), "Error while Extracting Package", e);
		}
		return myPackageSource.toString();
	}

	private static LinkedHashMap<String, List<PLSQLObject>> listAllProcedures() {
		LinkedHashMap<String,List<PLSQLObject>> packageGroup = new LinkedHashMap<>();
		
		StringBuilder finalSql =  ("YES".equalsIgnoreCase(PropertyReader.readProperty("ANALYZE_HOOK")) ? Queries.hookProcedures : Queries.allProcedures);
		try(Connection con = MyConnection.getConnection();
				ResultSet rs = new SQLBuilder(finalSql.toString()).getResultSet(con)){
				processResultSet(rs, packageGroup);
		}catch (Exception e) {
			MonitorLogger.error(AnalyzePLSQL.class.getName(), "Error while processing PL/SQL", e);
		}
		return packageGroup;
	}

	private static void processResultSet(ResultSet rs, LinkedHashMap<String, List<PLSQLObject>> packageGroup) throws SQLException {
		List<PLSQLObject> packageList=null;
		while(rs.next()){
			String packageName = rs.getString("PACKAGE_NAME");
			if(packageGroup.containsKey(packageName)){
				packageList = packageGroup.get(packageName);
			}else{
				packageList = new ArrayList<>();
			}
			PLSQLObject myObj = new PLSQLObject();
			myObj.setModuleName(rs.getString("MODULE_NAME"));
			myObj.setProcedureName(rs.getString("PROC"));
			myObj.setHookType(rs.getString("TYPE"));
			myObj.setHookName(rs.getString("HOOK_NAME"));
			myObj.setPackageName(rs.getString("PACKAGE_NAME"));
			packageList.add(myObj);
			packageGroup.put(packageName, packageList);
		}
	}
}
