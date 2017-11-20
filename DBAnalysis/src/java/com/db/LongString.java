package com.db;

import java.io.Reader;
import java.io.StringReader;

/**
 * Holds LONGVARCHAR (CLOB) data
 * @author narayana 
 *
 */
public class LongString {
	
	private String data;
	
	public LongString(String _data) {
		data = _data;
	}
		
	public Reader getReader() {
		return new StringReader(data);
	}
	
	public String getData() {
		return data;
	}
	
	@Override
	public boolean equals(Object _obj) {
		if (_obj instanceof LongString) {
			return data.equals(((LongString)_obj).getData());
		}
		return false;
	}
}
