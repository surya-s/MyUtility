package com.util;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.logger.MonitorLogger;

public class RegexExtractor {
	public static void main(String ar[]) throws IOException {
		/*String contents = new String(Files.readAllBytes(Paths.get("Reports/MS_CHK_CHK001_HELPER.pls")));
		String regexString = "PROCEDURE (\\s)*T010(.*)END (\\s)*"+"T010";
		Pattern p = Pattern.compile(regexString, Pattern.DOTALL);
		Matcher m = p.matcher(contents);
		while (m.find()) { 
			System.out.println("From Static : "+m.group(2));
		}
		String proc="T010";
		System.out.println("From Dynamic :"+extractPLSQLCode(proc, contents));*/
		
		System.out.println(exceptionHandled("WHEN OTHERS THEN "
				+" xn_error_code := SQLCODE; "));
		
	}
	
	public static String extractPLSQLCode(String procName, String source){
		String regexString = "PROCEDURE(\\s)+"+procName+"(.*)END(\\s)+"+procName;
		Pattern p = Pattern.compile(regexString, Pattern.DOTALL);
		Matcher m = p.matcher(source);
		
		if (m.find()) {
			return ("PROCEDURE "+procName +" \n"+m.group(2) +" \n END "+procName);
		}
		regexString = "FUNCTION(\\s)+"+procName+"(.*)END(\\s)+"+procName;
		p = Pattern.compile(regexString, Pattern.DOTALL);
		m = p.matcher(source);
		if (m.find()) {
			return ("FUNCTION "+procName +" \n"+m.group(2) +" \n END (\\s)+"+procName);
		}
		else{
			MonitorLogger.error(RegexExtractor.class.getName(), "Unable to extract the file code for "+procName, null);
			return null;
		}
	}
	
	public static boolean exceptionHandled(String source){
		return Pattern.compile(".*(?i)XN_ERROR_CODE.*:=.*",Pattern.DOTALL).matcher(source).find();
	}
}
