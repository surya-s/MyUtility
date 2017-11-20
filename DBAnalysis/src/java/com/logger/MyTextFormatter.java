package com.logger;

import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class MyTextFormatter extends Formatter {
	public String format(LogRecord record) {
		return ((new Date(record.getMillis())).toString() 
				+ " " +  record.getLevel() 
				+ " " + formatMessage(record) + "\n");
	}
	public String getHead(Handler h) {
		return "";
	}

	public String getTail(Handler h) {
		return "";
	}
}
