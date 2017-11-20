/*
 * SYSTEMi Copyright 2000-2015, MetricStream, Inc. All rights reserved.
 * @Author:  narayana
 * @Created: 12/12/01
 * $Id$
 */

package com.util;

import static com.util.Check.anyNull;
import static com.util.Check.hasContent;
import static com.util.Check.noContent;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.SimpleTimeZone;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.TimeZone;

/**
 * set of util methods to manipulate and extend data structure operations
 */
public class AroundString {

	public static final String EMPTY_STRING = "";

	private static final String DOUBLE_QUOTE_STR = "\"";

	private static final String TWO_DOUBLE_QUOTE_STR = "\"\"";

	private static final char DOUBLE_QUOTE_CHAR = '"';

	private static final char COMMA_CHAR = ',';

	public static boolean isWhitespaceInside(String _value) {
		if (_value == null) {
			return false;
		}

		char[] ca = _value.toCharArray();
		int len = ca.length;
		for (int i = 0; i < len; ++i) {
			if (Character.isWhitespace(ca[i])) {
				return true;
			}
		}

		return false;
	}

	public static String quoteAndTrimAsNeeded(String _value) {
		return quoteAndTrimAsNeeded(_value, false);
	}

	/*
	 * removes whitespace at beginning and end, and adds quotes if it contains
	 * inner whitespace.
	 */
	public static String quoteAndTrimAsNeeded(String _value, boolean _single) {
		if (_value == null) {
			return "";
		}
		_value = _value.replaceAll("^\\s+|\\s+$", "");
		if (_value.matches(".+\\s.+")) {
			String q = _single ? "'" : "\"";
			_value = q + _value + q;
		}
		return _value;
	}

	/**
	 * Translates string locale presentation in form lang_COUNTRY to Locale
	 * object
	 * 
	 * @param String
	 *            locale in format lang_COUNTRY, for example en_US if value is
	 *            null, or can't be present as Locale, then default returned
	 * @return Locale locale from string
	 * @exception no
	 *                exceptions rose
	 */
	public static Locale localeFromString(String _locale) {
		if (isEmptyOrNull(_locale)) {
			return Locale.getDefault();
		}
		String[] l = _locale.split("_");
		switch (l.length) {
		case 1:
			return new Locale(l[0], "", "");
		case 2:
			return new Locale(l[0], l[1]);
		default:
			return new Locale(l[0], l[1], l[2]);
		}
	}

	/**
	 * builds TimeZone from String presentation
	 * 
	 * @param String
	 *            present timezone in form of _-speparated values that are used
	              as SimpleTimeZone constructor parameter.  The last token be Y
	              or N and controls whether to use daylight saving
	 * @return TimeZone for string or default if null or invalid
	 * @exception no
	 */
	public static TimeZone timeZoneFromString(String _tzs) {
		if (_tzs == null) {
			return TimeZone.getDefault();
		}

		String[] split = _tzs.split("_");
		if (split.length >= 9) {
			try {
				int offs = Integer.parseInt(split[0]);
				String code = split[1];
				int startm = Integer.parseInt(split[2]);
				int startd = Integer.parseInt(split[3]);
				int startt = Integer.parseInt(split[4]);
				int endm = Integer.parseInt(split[5]);
				int endd = Integer.parseInt(split[6]);
				int endt = Integer.parseInt(split[7]);
				if ("Y".equals(split[8])) {
					return new SimpleTimeZone(offs * 60 * 1000, code,
							startm, startd, -Calendar.SUNDAY, startt,
							endm, -endd, Calendar.SUNDAY, endt);
				} else {
					return new SimpleTimeZone(offs * 60 * 1000, code);
				}
			} catch (Exception e) {
			}
		}
		return TimeZone.getDefault();
	}

	/**
	 * Implements String.split method from JDK 1.4 and up, convert string to
	 * string array by separators boundaries.  This differs from String.split()
	 * in that the separator is not used as a regular expression, and that
	 * this split supresses empty strings
	 * 
	 * @param some
	 *            string
	 * @param separators
	 * @return array of string components devided by separators, no actual
	 *         separators in result string
	 */
	public static String[] split(String _s, String _sep) {
		if (_s == null) {
			return null;
		}
		StringTokenizer st = new StringTokenizer(_s, _sep);
		int nt = st.countTokens();
		String[] result = new String[nt];
		for (int i = 0; i < nt; i++) {
			result[i] = st.nextToken();
		}
		return result;
	}

	/**
	 * Splits the string at the separator, puts each token in a list and returns
	 * a list returns an empty list if any of the input parameters is null
	 * 
	 * @param String
	 *            delimited string
	 * @param String
	 *            separator
	 * @return List list of string components divided by separators
	 */
	public static List<String> stringToList(String _s, String _sep) {
		List<String> list = new ArrayList<>();

		if (_s == null || _sep == null) {
			return list;
		}

		StringTokenizer st = new StringTokenizer(_s, _sep);
		while (st.hasMoreTokens()) {
			list.add(st.nextToken());
		}
		return list;
	}

	/**
	 * Forms a string of all the items in the list separated by the separator
	 * returns an empty string if any of the input parameters is null
	 * 
	 * @param List
	 *            list of items to be concatenated (list of only strings)
	 * @param String
	 *            separator
	 * @return String string of list items separated by the separator
	 */
	@Deprecated // use joinToString instead
	public static <T> String listToString(List<T> _list, String _sep) {
		return joinToString(_list, null, _sep);
	}

	/**
	 * Build a string of all the items in the list separated by the separator.
	 * Each items in the list enclosed by the <code>ecnclose</code>
	 * 
	 * @param data
	 *            List of input data
	 * @param enclose
	 *            Wrapper used to enclose each data in the list
	 * @param separator
	 *            Used to separate each data in the list
	 * @return The string of data
	 */
	@Deprecated // use joinToString instead
	public static <T> String listToString(Collection<T> data, String enclose, String separator) {
		return joinToString(data, enclose, separator);
	}

	/**
	 * @param dataList
	 * @return String array from the list
	 */
	public static String[] listToStringArray(ArrayList<?> dataList) {
		String[] values = null;
		if (dataList != null) {
			values = new String[dataList.size()];
			for (int i = 0; i < dataList.size(); i++) {
				if (dataList.get(i) != null) {
					values[i] = dataList.get(i).toString();
				}
			}
		}
		return values;
	}

	/**
	 * @param dataList
	 * @return double array from the list
	 */
	public static double[] listToDoubleArray(List<?> dataList) {
		double[] values = null;
		if (dataList != null) {
			values = new double[dataList.size()];
			for (int i = 0; i < dataList.size(); i++) {
				if (dataList.get(i) != null) {
					String curValue = dataList.get(i).toString();
					if ("HOLE".equals(curValue) || "NoValue".equals(curValue)) {
						values[i] = 0.0;
					} else {
						values[i] = Double.parseDouble(curValue);
					}
				}
			}
		}
		return values;
	}

	/**
	 * Used for getting int value from property with default. It actually
	 * duplicates standard property method and should be not used.
	 * 
	 * @param properties
	 *            which have int values
	 * @param property
	 *            name to retrieve
	 * @param default int value if absent, or can't be converted to in
	 * @return int value of property _name, or default value if property doesn't
	 *         exist or not int
	 */
	public static int getIntProperty(Properties _properties, String _name, int _defaultVal) {
		try {
			return Integer.parseInt(_properties.getProperty(_name, "" + _defaultVal));
		} catch (Exception e) {
		}
		return _defaultVal;
	}

	/**
	 * replaces all occurence of regExp by repl in in string
	 */
	@Deprecated
	public static String replaceAll(String _inString, String _regExp, String _repl) {
		return _inString.replaceAll(_regExp, _repl);
	}

	/**
	 * This method takes a input string (inStr) and replaces all occurences of a
	 * given substring (_old), within the input string with the given
	 * replacement string (_new). The replaced string is returned. This method
	 * returns the input string as is, if any one of the following conditions is
	 * true: 1) input string is null or empty 2) substring to search for is null
	 * or empty 3) replacement substring is null
	 * 
	 * @param String
	 *            inStr
	 * @param String
	 *            _old
	 * @param String
	 *            _new
	 * @return String replaced string
	 */
	public static String replaceAllPlain(String _inStr, String _old, String _new) {
		if (noContent(_inStr) || noContent(_old) || isNull(_new)) {
			return _inStr;
		}

		return _inStr.replace(_old, _new);
	}

	/**
	 * A helper method to parse int without raising an exception and using a
	 * default value instead of
	 * 
	 * @param String
	 *            value to parse
	 * @param int default value
	 * @return int value of parsed string or default value if an exception
	 *         happened
	 * @exception none
	 */
	@Deprecated
	public static int parseIntWithDefault(String _sint, int _def) {
		return Integer.parseInt(_sint, _def);
	}

	
	// TODO: This should better be called limitToLength
	public static String assureLength(String _s, int _len) {
		if (_s == null || _len <= 0 || _s.length() < _len) {
			return _s;
		}
		return _s.substring(0, _len);
	}

	/**
	 * Replaces all the characters that are not used for filename with
	 * underscore. Characters include /, \, :, *, ?, |, ", <, >
	 */
	public static String replaceSpecialCharsInString(String _s) {
		return replaceSpecialCharsInString(_s, "_");
	}

	/**
	 * Replaces all the characters that are not used for filename with _with.
	 * Characters include /, \, :, *, ?, |, ", <, >
	 */
	public static String replaceSpecialCharsInString(String _s, String _with) {
		char[] spChars = new char[] { '/', '\\', ':', '*', '?', '"', '|', '>', '<' };
		return replaceSpecialCharsInString(_s, spChars, _with);
	}

	/**
	 * Replaces all the characters that are in spChars with _with.
	 */
	public static String replaceSpecialCharsInString(String _s, char[] spChars, String _with) {
		if (_s == null) {
			return "";
		}

		char[] ca = _s.toCharArray();
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < ca.length; i++) {
			boolean _found = false;

			for (int j = 0; j < spChars.length; j++) {
				if (ca[i] == spChars[j]) {
					_found = true;
					break;
				}
			}

			sb.append(_found ? _with : ca[i]);
		}

		return sb.toString();
	}

	/**
	 * Checks if the given string is present in the given array of strings
	 * 
	 * @param _array
	 * @param _toSearch
	 * @param _ignoreCase
	 * @return boolean true if string is in the array, false otherwise. returns
	 *         false if either param is null
	 */
	public static boolean contains(String[] _array, String _toSearch, boolean _ignoreCase) {
		if (anyNull(_array, _toSearch)) {
			return false;
		}

		for (int i = 0; i < _array.length; i++) {
			if (_ignoreCase) {
				if (_toSearch.equalsIgnoreCase(_array[i])) {
					return true;
				}
			} else {
				if (_toSearch.equals(_array[i])) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Checks if the given string contains any of the strings given in the
	 * string array. Can be used to check if a string contains some special
	 * chars by passing the chars as an array of strings
	 * 
	 * @param String
	 *            _str string to scan
	 * @param String
	 *            [] _array string array
	 * @return boolean true if the string contains any of the strings given in
	 *         the array, false otherwise
	 */
	public static boolean contains(String _str, String[] _array) {
		if (anyNull(_str, _array)) {
			return false;
		}

		int len = _array.length;
		for (int i = 0; i < len; i++) {
			if (_str.indexOf(_array[i]) != -1) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if the given string contains any of the chars given in the
	 * char array. Can be used to check if a string contains some special
	 * chars by passing the chars as an array of strings
	 *
	 * @param String
	 *            _str string to scan
	 * @param String
	 *            [] _array char array
	 * @return boolean true if the string contains any of the chars given in
	 *         the array, false otherwise
	 */
	public static boolean contains(String _str, char[] _array) {
		if (anyNull(_str, _array)) {
			return false;
		}

		int len = _array.length;
		for (int i = 0; i < len; i++) {
			if (_str.indexOf(_array[i]) != -1) {
				return true;
			}
		}
		return false;
	}

	/**
	 * This method takes a Column value as a String (inStr) and strips the
	 * leading & trailing double quote character (") and strips all double quote
	 * chars used for escaping the double quote literal in the column value
	 * (normalization).
	 *
	 * This method returns an empty String if any one of the following
	 * conditions is true: 1) input String is null or empty 2) input String
	 * contains only 2 double quote characters
	 *
	 * @param String
	 *            inStr
	 * @return String the normalized column value as String
	 */
	private static String normalizeToken(String inStr) {
		String retVal = (inStr == null) ? "" : inStr.trim();
		if (retVal.length() <= 0) {
			return retVal;
		}
		if (retVal.indexOf(DOUBLE_QUOTE_STR) >= 0) {
			// if the entire token is enclosed in double quote chars then strip
			// the
			// first & last double quote chars
			if (retVal.startsWith(DOUBLE_QUOTE_STR)) {
				int len = retVal.length();
				if (len > 2) {
					retVal = retVal.substring(1, (len - 1));
					// Strip all double quote chars used for escaping the double
					// quote literal
					// in the token
					retVal = replaceAllPlain(retVal, TWO_DOUBLE_QUOTE_STR, DOUBLE_QUOTE_STR);
				}

				else {
					// If a string starts with a double quote and is only 2
					// chars long
					// then it is an empty string
					retVal = "";
				}
			}
		}
		return retVal;
	}

	/**
	 * This method takes a record in CSV format as a String (inRec) and parses
	 * the record and returns a List of the column values. All column Values are
	 * returned as Strings.
	 *
	 * This method returns an empty list if any one of the following conditions
	 * is true: 1) input string is null or empty 2) an exception is encountered
	 * while parsing the input record
	 *
	 * @param String
	 *            inRec
	 * @return List list of Column values (each element in the list is of type
	 *         String)
	 */
	public static List<String> parseCsvRecord(String inRec) {
		return parseRecord(inRec, COMMA_CHAR);
	}

	/**
	 * This method takes a record in a predefined character delimited format as
	 * a String (inRec) and parses the record and returns a List of the column
	 * values. All column Values are returned as Strings.
	 *
	 * This method returns an empty list if any one of the following conditions
	 * is true: 1) input string is null or empty 2) an exception is encountered
	 * while parsing the input record
	 *
	 * @param String
	 *            inRec
	 * @param char inSeparator
	 * @return List list of Column values (each element in the list is of type
	 *         String)
	 */
	public static List<String> parseRecord(String inRec, char inSeparator) {
		List<String> retVal = new ArrayList<>(16);
		try {
			inRec = (inRec == null) ? "" : inRec;
			inRec = (!Character.isWhitespace(inSeparator)) ? inRec.trim() : inRec;
			if (inRec.length() <= 0) {
				return retVal;
			}

			boolean stackPush = true;
			int startIndex = 0;
			int iIndex;
			Stack<String> stack = new Stack<>(); // to keep track of (")
			char[] charArray = inRec.toCharArray();

			// parse the CSV record
			for (iIndex = 0; iIndex < charArray.length; iIndex++) {
				if (charArray[iIndex] == DOUBLE_QUOTE_CHAR) {
					stackPush = !(stackPush);
				} else if ((charArray[iIndex] == inSeparator) && (stack.empty())) {
					// if it is a separator char and the stack is empty then
					// this indicates the end of a token
					String token = new String(charArray, startIndex, (iIndex - startIndex));
					startIndex = iIndex + 1;
					// Normalize each column value
					retVal.add(normalizeToken(token));
				}
			}
			// Add the last token
			String token = new String(charArray, startIndex, (iIndex - startIndex));
			retVal.add(normalizeToken(token));
		} catch (Exception e) {
			// Probably add a log statement here
			retVal.clear();
		}
		return retVal;
	}

	/**
	 * Trims all the first and last occurances of the specified character
	 * 
	 * @param String
	 *            Target string for trimming
	 * @param char Character to be trimmed from the target string
	 * @return
	 */
	public static String trim(String _str, char _token) {
		if (noContent(_str)) {
			return _str;
		}

		_str = _str.trim();

		while (_str.startsWith(String.valueOf(_token))) {
			_str = _str.substring(1);
		}

		while (_str.endsWith(String.valueOf(_token))) {
			_str = _str.substring(0, _str.length() - 1);
		}

		return _str;
	}

	/**
	 * Normalizes back slash character to forward slash Removes special
	 * characters that are not allowed in directory names Removes multiple
	 * forward slash characters
	 * 
	 * This method will always retain a trailing forward slash at the end Eg:
	 * /abc*\\\\\\\\\\def?//\\//fgd\\dd\\e/ will become /abc/def/fgd/dd/e/
	 * 
	 * @param _toNormalize
	 * @return
	 */
	public static String normalizeDirectoryName(String _toNormalize) {
		if (_toNormalize == null) {
			return _toNormalize;
		}

		_toNormalize = _toNormalize.replaceAll("[:*?\"|><,;]", "");
		_toNormalize = _toNormalize.replaceAll("[/\\\\]+", "/");
		_toNormalize = _toNormalize.replace("../", "");
		_toNormalize = trim(_toNormalize, '/');

		return _toNormalize + "/";
	}

	@Deprecated
	public static String normalizeBackSlash(String string) {
		return string;
	}

	public static boolean isEmptyOrNull(String value) {
		return value == null || "".equals(value.trim());
	}

	public static boolean isTrueOrYes(String val) {
		return "TRUE".equalsIgnoreCase(val) || "YES".equalsIgnoreCase(val);
	}

	/**
	 * This function is used to check if a given string is not "null" and not
	 * "blank".
	 * 
	 * @param value
	 *            String
	 * @return boolean
	 */
	public static boolean isNonEmpty(String value) {
		return value != null && !"".equals(value.trim());
	}

	/**
	 * This function is used to check if given string is not null and is blank.
	 * 
	 * @param value
	 *            String
	 * @return boolean
	 */
	public static boolean isEmpty(String value) {
		return value != null && "".equals(value.trim());
	}

	/**
	 * Converts the {@link Throwable} object stack into {@link String} form.
	 * 
	 * @param _t
	 * @return {@link String} form of {@link Throwable} object stack
	 */
	public static String normalizeException(Throwable _t) {
		StringWriter _writer = new StringWriter();
		_t.printStackTrace(new PrintWriter(_writer));

		return _writer.toString();
	}

	/**
	 * Retrieves index value from given String array or last index value if
	 * index is out of bounds, -1 if array is null or index is less than 0
	 * 
	 * @param array
	 *            - String source array
	 * @param index
	 *            - int index number
	 * @return String - value of index in String array
	 */
	public static String getArrayIndexValue(String[] array, int index) {
		if (array == null || index < 0) {
			return "-1";
		}
		int length = array.length;
		if (index >= length) {
			index = length -1;
		}
		return array[index];
	}

	public static String[] listToStringArrayWithReplace(List<?> dataList) {
		String[] values = null;
		if (dataList != null) {
			values = new String[dataList.size()];
			for (int i = 0; i < dataList.size(); i++) {
				if (dataList.get(i) != null) {
					String value = (String) dataList.get(i);
					value = value.substring(1, value.length() - 1);
					values[i] = value;
				}
			}
		}
		return values;
	}

	/**
	 * This method replaces with the given regular expressions if string starts
	 * with given _repStr.
	 * 
	 * @param _inString
	 *            -- String need to be replaced
	 * @param _repStr
	 *            -- String used to check if string starts with specified
	 *            characters or not.
	 * @param _regExp
	 *            -- RegularExpression for replacing String
	 * @return
	 */
	public static String replaceStringForFirstOccurance(String _inString, String _repStr, String _regExp) {
		if (isEmptyOrNull(_regExp)) {
			_regExp = _repStr;
		}
		if (isNonEmpty(_inString) && _inString.startsWith(_repStr)) {
			return _inString.replaceFirst(_regExp, "");
		}
		return _inString;

	}

	/**
	 * @param t
	 * @return
	 */
	public static <T> boolean isNull(T t) {
		return t == null;
	}

	/**
	 * @param t
	 * @return
	 */
	public static <T> boolean isNotNull(T t) {
		return t != null;
	}

	/**
	 * @param string
	 * @param str
	 * @return
	 */
	public static boolean isYes(String string, String str) {
		if (isNull(string)) {
			string = str;
		}
		if (isNull(string) || string.length() != 1 && string.length() != 3) {
			return false;
		}
		string = string.toUpperCase();
		return "Y".equals(string) || "YES".equals(string);
	}

	/**
	 * @param string
	 * @return
	 */
	public static boolean isYes(String string) {
		return isYes(string, null);
	}

	public static int[] toIntArray(String string, String separator, int defaultInt) {
		if (isEmptyOrNull(string)) {
			return new int[0];
		} else {
			String[] strings = string.split(separator);
			int[] values = new int[strings.length];
			for (int i = 0; i < strings.length; i++) {
				try {
					values[i] = Integer.parseInt(strings[i]);
				} catch (NumberFormatException e) {
					values[i] = defaultInt;
				}
			}
			return values;
		}
	}

	public static int[] toIntArray(String string, String separator) {
		return toIntArray(string, separator, 0);
	}
	
	public static String getContentDisposition(String fileName) {
		return "attachment; filename=\"" + fileName + "\"";
	}
	
	public static String getFileExtension(String strFileName) {
		if (strFileName != null) {
			final int dot = strFileName.lastIndexOf(".");
			if (dot != -1) {
				return strFileName.substring(dot + 1);
			}
		}
        return ""; 
    }
	
	
	/**
	 * Splits the string at the separator, puts each token in a set and returns
	 * a set returns an empty set if any of the input parameters is null
	 * 
	 * @param String
	 *            delimited string
	 * @param String
	 *            separator
	 * @return Set set of string components divided by separator
	 */
	public static Set<String> stringToSet(String _s, String _sep) {
		Set<String> set = new HashSet<String>();

		if (_s == null || _sep == null) {
			return set;
		}
		StringTokenizer st = new StringTokenizer(_s, _sep);
		while (st.hasMoreTokens()) {
			set.add(st.nextToken());
		}
		return set;
	}

	/**
	 * /**
	 * Forms a string of all the items in the set separated by the separator
	 * returns an empty string if any of the input parameters is null
	 * 
	 * @param Set
	 *            set of items to be concatenated (set of only strings)
	 * @param String
	 *            separator
	 * @return String string of list items separated by the separator
	 */
	@Deprecated // use joinToString instead
	public static <T> String setToString(Set<T> data, String separator) {
		return joinToString(data, "", separator);
	}

	public static <T> String joinToString(Collection<T> data, String separator) {
		return joinToString(data, "", separator);
	}

	public static <T> String joinToString(Collection<T> data) {
		return joinToString(data, "", ",");
	}

	/**
	 * /**
	 * Forms a string of all the items in the set separated by the separator.
	 * null values in data are ignored
	 * 
	 * @param data
	 *            collection of items to be joined
	 * @param enclose
	 *            added as prefix and postfix for each item
	 * @param separator
	 *            added between enclosed items
	 * @return string of joined items
	 */
	public static <T> String joinToString(Collection<T> data, String enclose, String separator) {
		if (noContent(data)) {
			return "";
		}
		if (enclose == null) {
			enclose = "";
		}
		if (separator == null) {
			separator = ",";
		}
		StringBuilder builder = new StringBuilder();
		String sep = "";
		for (T object : data) {
			if (object != null) {
				builder.append(sep).append(enclose).append(object).append(enclose);
				sep = separator;
			}
		}
		return builder.toString();
	}
	
	/**
	 * Forms a string of all the items in the set separated by the separator.
	 * null values in data are ignored
	 * 
	 * @param separator
	 * 			added between items. Default is ','
	 * @param args
	 * 			 items to be joined
	 * @return string of joined items
	 */
	public static <T> String joinToString(String separator, String... args) {
		if (noContent(args)) {
			return "";
		}
		if (separator == null) {
			separator = ",";
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < args.length; i++) {
			if (hasContent(args[i])) {
				if (i > 0) {
					sb.append(separator);
				}
				sb.append(args[i]);
			}
		}
		return sb.toString();
	}
 
	public static String removeComments(String content){
//		String result=content.replaceAll("/\\*([^*]|[\r\n]|(\\*+([^*/]|[\r\n])))*\\*+/","");
		String result = content.replaceAll( "//.*|(\"(?:\\\\[^\"]|\\\\\"|.)*?\")|(?s)/\\*.*?\\*/", "$1 " );
		return result;
	}
	
	public static void main(String ar[]){
		String data = "In java /*Mydata /* is */ dddd"
				+ " comment */ do you know */ */*//**** */*how comments */ written"
				+ " for ";
		System.out.println(removeComments(data));
	}
}
