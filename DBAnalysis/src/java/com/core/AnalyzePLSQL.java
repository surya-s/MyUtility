package com.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
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
import com.export.ExcelWriter;
import com.logger.MonitorLogger;
import com.util.AroundString;
import com.util.Check;
import com.util.PropertyReader;
import com.util.RegexExtractor;
 
public class AnalyzePLSQL {
	public static void main(String ar[]){
		LinkedHashMap<String,List<PLSQLObject>> packageGroup = listAllProcedures();
		MonitorLogger.info(AnalyzePLSQL.class.getName(),"Package that are Analyzed are :"+packageGroup, null);
		analyzeProcedure(packageGroup);
	}

	private static void analyzeProcedure(LinkedHashMap<String, List<PLSQLObject>> packageGroup) {
		MonitorLogger.info(AnalyzePLSQL.class.getName(),"Start Analysizing ", null);
		for (Entry<String, List<PLSQLObject>> entry : packageGroup.entrySet()) {
			String packageName = entry.getKey();
			try(Connection con = MyConnection.getConnection()){
				String content = extractProcedureCode(con, packageName);
				content = AroundString.removeComments(content);
				if("YES".equalsIgnoreCase(PropertyReader.readProperty("WRITE_PACKAGE_TO_FILE"))){
					MonitorLogger.info(AnalyzePLSQL.class.getName(),"Write to File enabled. Writing all package source to file (pls) ", null);
					writeToFile(content, packageName, "pls");
				}
				analyzePLSQLContent(content, entry.getValue());
			}catch(Exception e){
				MonitorLogger.error(AnalyzePLSQL.class.getName()," Error while analysis of Package "+packageName , e);
			}
		}
		writeToExcel(packageGroup);
	}

	private static void writeToExcel(LinkedHashMap<String, List<PLSQLObject>> packageGroup) {
		ArrayList<String[]> data = new ArrayList<>();
		for (Entry<String, List<PLSQLObject>> entry : packageGroup.entrySet()) {
			writePojoToExcel(entry.getValue(), data);
		}
		new ExcelWriter().writeToExcel(data); 
	}

	private static void writePojoToExcel(List<PLSQLObject> objList, ArrayList<String[]> data) {
		int i=0;
		for (PLSQLObject plsqlObject : objList) {
			String nameArr[] = new String[plsqlObject.getClass().getDeclaredFields().length];
			String valueArr[] = new String[plsqlObject.getClass().getDeclaredFields().length];
			for (Field field : plsqlObject.getClass().getDeclaredFields()) {
				field.setAccessible(true); 
				Object value;
				try {
					value = field.get(plsqlObject);
					if (value != null) {
						if(data.size()==0){
							nameArr[i] = field.getName(); 
							valueArr[i] =  String.valueOf(value); 
							i++;
						}else{
							valueArr[i++] =  String.valueOf(value);
						}
					}
				} catch (IllegalArgumentException e) {
					MonitorLogger.error(AnalyzePLSQL.class.getName(), e.getMessage(), e);
				} catch (IllegalAccessException e) {
					MonitorLogger.error(AnalyzePLSQL.class.getName(), e.getMessage(), e);
				}
			}
			if(data.size()==0){
				data.add(nameArr);
			}
			data.add(valueArr);
			i=0;
		}
	}

	private static void analyzePLSQLContent(String content, List<PLSQLObject> value) {
		for (PLSQLObject plsqlObject : value) {
			try {
				String source = getPackageSource(plsqlObject.getProcedureName(), content);
				if (Check.hasContent(source)) {
					MonitorLogger.info(AnalyzePLSQL.class.getName(),"Start analyzing hook procedure:"+plsqlObject.getProcedureName(),null);
					if ("YES".equalsIgnoreCase(PropertyReader.readProperty("WRITE_PACKAGE_TO_FILE"))) {
						writeToFile(source, plsqlObject.getProcedureName(), "pks");
					}
					checkPLSQLGuidelines(source, plsqlObject);
				} else {
					MonitorLogger.error(AnalyzePLSQL.class.getName(),
							"Regex failed to extract Source for " + plsqlObject.getHookName(), null);
				}
			} catch (IOException e) {
				MonitorLogger.error(AnalyzePLSQL.class.getName(),
						"Error while writing Procedure or function to file : " + plsqlObject.getProcedureName(), e);
			}
		}
	}
	
	public static int getLineCount(String text){    
		return text.split("[\n|\r]").length;
	}

	private static void checkPLSQLGuidelines(String source, PLSQLObject plsqlObject) {
		if(source.contains("COMMIT;")){
			plsqlObject.setCommitExists(true);
		}
		if(source.contains("SAVEPOINT")){
			plsqlObject.setSavepointExists(true);
		}
		if(source.contains("ROLLBACK")){
			plsqlObject.setRollbackExists(true);  
		}
		plsqlObject.setNoOfLines(getLineCount(source));
		plsqlObject.setXnErrorCodeHandled(RegexExtractor.exceptionHandled(source));
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
		packageGroup.remove(null);
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
