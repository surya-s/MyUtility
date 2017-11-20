package com.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyReader {
	public static String readProperty(String name) {
		String value = null;
		try (InputStream input = PropertyReader.class.getClassLoader().getResourceAsStream("config.properties")) {
			Properties prop = new Properties();
			prop.load(input);
			value = prop.getProperty(name);
		} catch (IOException io) {
			io.printStackTrace();
		}
		return value;
	}

	public static void main(String ar[]) {
		System.out.println(PropertyReader.readProperty("PACKAGE_FILTER"));
	}
}
