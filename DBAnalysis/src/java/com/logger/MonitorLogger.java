package com.logger;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;


public class MonitorLogger {
	static private FileHandler fileLog;
	static private Formatter formatterText;
	static private LogManager lm;
	static String GLOBAL_LOGGER_NAME ="Monitor Logger";
	static Logger logger;
			
	static public void setup() throws IOException {
		lm = LogManager.getLogManager();
		logger = Logger.getLogger(MonitorLogger.GLOBAL_LOGGER_NAME);
		lm.addLogger(logger);
		
		String dateTime = new SimpleDateFormat("dd-MMM-yyyy HH.mm.ss").format(System.currentTimeMillis());
		dateTime = "Monitor_logger_"+dateTime;
		fileLog = new FileHandler(dateTime+".log");
		formatterText = new MyTextFormatter();
	    fileLog.setFormatter(formatterText);
	    logger.addHandler(fileLog);
	}
	
	static {
		try {
			setup();
		} catch (IOException e) {
			error(MonitorLogger.class.toString(), "Error in Log setup ", e);
		}
	}
	
	public static void error(String _src, String _msg, Throwable _e) {
		log(Level.SEVERE, _src, _msg, null, _e);
	}
	
	public static void warning(String _src, String _msg, Throwable _e) {
		log(Level.WARNING, _src, _msg, null, _e);
	}
	
	public static void info(String _src, String _msg, Object[] _msgArgs) {
		log(Level.INFO, _src, _msg, _msgArgs, null);
	}
	
	protected static void log(Level level, String _src, String _msg, Object[] _msgArgs, Throwable _e) {
		_msg = _src +" :: "+ _msg;
		
		if(level == Level.SEVERE || level == Level.WARNING) {
			if(null != _e) {
				logger.log(level, _msg, _e);
			}else {
				logger.log(level, _msg);
			}
		}
		if(null != _msgArgs && _msgArgs.length > 0) {
			logger.log(level, _msg, _msgArgs);
		}else {
			logger.log(level, _msg);
		}
	}
	public static void closeHandlers() {
		try {
			if(null != MonitorLogger.fileLog) {
				MonitorLogger.fileLog.close();
			}
		}catch(Exception e) {
			MonitorLogger.error("MonitorLogger", "Error while closing ", e);
		}
	}
}