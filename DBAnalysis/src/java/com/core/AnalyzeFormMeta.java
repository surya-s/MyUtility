package com.core;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.db.MyConnection;
import com.db.Queries;
import com.db.SQLBuilder;
import com.export.ExcelWriter;
import com.logger.MonitorLogger;
import com.util.PropertyReader;

public class AnalyzeFormMeta {
	public static void main(String ar[]){
		String packageFilter = getPackageFilter();
		SQLBuilder build = buildSQL(packageFilter);
		try(ResultSet rs = build.getResultSet(MyConnection.getConnection())){
			createExcel(rs);
		}catch(Exception e){
			MonitorLogger.error(AnalyzeFormMeta.class.getName(), "Error While running Analyzer", e);
		}
	}

	private static void createExcel(ResultSet rs) throws SQLException, IOException {
		List<String> header = new ArrayList<>();
		ResultSetMetaData rMeta =null;
		ArrayList<String> values=null;
		Map<Integer,ArrayList<String>> valueMap = new LinkedHashMap<>();
		int j=0;
		while(rs.next()){ 
			if(j==0){
				rMeta = rs.getMetaData();
				for(int i=1;i<=rMeta.getColumnCount();i++){
					header.add(rMeta.getColumnName(i));
				}
			}
			values = new ArrayList<>();
			for (String col : header) {
				values.add(rs.getString(col));
			}
			valueMap.put(++j, values);
		}
		String fileName = "Reports/FormAnalysisReport_"+System.currentTimeMillis()+".xls";
		new ExcelWriter().writeExcel(header, valueMap,fileName);	
	}

	private static SQLBuilder buildSQL(String packageFilter) {
		String sql =null; 
		SQLBuilder build=null;
		if(packageFilter.length()>0){
			sql = Queries.formInfoletDetails.toString() + " " + Queries.packageFilter + packageFilter+")";
		}else{
			sql = Queries.formInfoletDetails.toString();
		}
		build = new SQLBuilder(sql);
		return build;
	}

	private static String getPackageFilter() {
		String pkg = PropertyReader.readProperty("PACKAGE_FILTER");
		StringBuilder str = new StringBuilder();
		for (String data : pkg.split(",")) {
			if(str.length()==0){
				if(data.length()>0)
					str.append("'"+data+"'");
			}else{
				str.append(",'"+data+"'");
			}
		}
		return str.toString();
	}	
}
