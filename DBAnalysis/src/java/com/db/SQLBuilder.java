package com.db;

import static com.util.Check.hasContent;
import static com.util.Check.noContent;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.util.AroundString;


/**
 * Wrapper class around PreparedStatement
 *
 * This class helps creating parameterized SQL statements.  It allows to
 * aggregate the SQL statements in multiple steps.  Once the parameterized
 * statement is ready, it can be either converted into a ResultSet or it can
 * return a single value from the first row of the ResultSet.
 *
 * Parameters can be primitives (int, long, double), their Object equivalents
 * (Integer, Long, Double), String, or Collections.  When using a Collection as
 * parameter, the corresponding placeholder is automatically expanded to have as
 * many placeholders as the size of the Collection.
 *
 * <pre>
 * {@code
 * SQLBuilder sb = new SQLBuilder("select a from b");
 * List<String> dList = new ArrayList<>(); dList.add("a"); dList.add("b");
 * sb.append("where c=? or d in (?)", 42, dList);
 * int a = sb.getInt(connection, 1, -1);
 * }
 * </pre>
 */
public class SQLBuilder {
	private StringBuilder sql = new StringBuilder();
	private List<Object> args = new ArrayList<>();
	private Set<String> names = new HashSet<>();
	private Map<String, String> singles = new HashMap<>();
	private Map<String, List<String>> multis = new HashMap<>();
	private String delim = "";
	private int resultSetType = ResultSet.TYPE_FORWARD_ONLY;
	private int fetchSize = -1;


	/**
	 * Creates a new SQBuilder object. The number of ? in the _sql parameter
	 * must be identical to the number of _args
	 * @param _sql The intial SQL statement fragment
	 * @param _args The parameters for this fragment
	 */
	public SQLBuilder(String _sql, Object... _args) {
		append(_sql, _args);
		delim = " ";
	}

	/**
	 * Generates a shallow clone of a SQLBuilder object
	 * @param _sql A SQLBuilder object
	 */
	public SQLBuilder(SQLBuilder _sql) {
		append(_sql);
		delim = " ";
	}

	public SQLBuilder bind(String name, String value) {
		addName(name);
		singles.put(name, value);
		return this;
	}

	public SQLBuilder bind(String name, List<String> values) {
		addName(name);
		multis.put(name, values);
		return this;
	}

	public SQLBuilder bind(Map<String, String> placeholders) {
		addNames(placeholders.keySet());
		singles.putAll(placeholders);
		return this;
	}

	public SQLBuilder applyBindings() {
		sql = new StringBuilder(interpolate());
		names.clear();
		singles.clear();
		multis.clear();
		return this;
	}

	/**
	 * Append a parameterized SQL fragment.  The original fragment and the new
	 * fragment will be separated by a single space.
	 * @param _sql The SQL statement fragment
	 * @param _args The parameters for this fragment
	 * @return The modified object
	 */
	public SQLBuilder append(String _sql, Object... _args) {
		sql.append(delim).append(_sql);
		if (hasContent(_args)) {
			args.addAll(Arrays.asList(_args));
		}
		return this;
	}

	/**
	 * Append another SQLBuilder.  This appends both the fragment and shallow
	 * copies of all parameters.  The original fragment and the new fragment
	 * will be separated by a single space.
	 * @param sb The SQLBuilder object
	 * @return The modified object
	 */
	public SQLBuilder append(SQLBuilder sb) {
		addNames(sb.names);
		singles.putAll(sb.singles);
		multis.putAll(sb.multis);
		args.addAll(sb.args);
		sql.append(delim).append(sb.sql);
		return this;
	}

	/**
	 * Wraps the SQL statement fragment into () and prepends _before.  A typical
	 * use case is <pre></pre>sb.wrap("select count(*) from");</pre>
	 * @param _before
	 * @return
	 */
	public SQLBuilder wrap(String _before) {
		return wrap(_before + " (", ")");
	}

	/**
	 * Wraps the SQL statement fragment in _before and _after.  Unlike append,
	 * thre is no additional whitespace inserted between the original SQL
	 * statement fragment and the arguments.
	 * @param _before The string which will be prepended
	 * @param _after
	 * @return
	 */
	public SQLBuilder wrap(String _before, String _after) {
		sql.insert(0, _before).append(_after);
		return this;
	}

	private void addNames(Collection<String> names) {
		for (String n : names) {
			addName(n);
		}
	}

	private void addName(String name) {
		if (!name.matches("\\w+")) {
			throw new IllegalArgumentException(name + " must be an identifier");
		}
		if (!names.add(name)) {
			throw new IllegalArgumentException(name + " must be unique");
		}
	}

	public static String nameQuote(String _name, boolean _noQuotes) throws IllegalArgumentException {
		if (_name == null) {
			throw new IllegalArgumentException("Object name is null");
		}
		if (_name.matches("[A-Za-z][A-Za-z0-9_.]*|\"[^\"]+\"")) {
			return _name;
		}
		String[] alias = _name.split("(?i)\\s+as\\s+", 2);
		if (alias.length == 2) {
			return nameQuote(alias[0], _noQuotes) + " AS " + nameQuote(alias[1], _noQuotes);
		}
		if (_noQuotes || _name.indexOf('"') != -1) {
			throw new IllegalArgumentException("Object name contains invalid character");
		}
		return "\"" + _name + "\"";
	}
	
	public static String nameQuote(String _name) throws IllegalArgumentException {
		return nameQuote(_name, true);
	}
	
	private String interpolate() {
		Map<String, String> quoted = new HashMap<>(names.size());

		for (Map.Entry<String, String> placeholder : singles.entrySet()) { 
			quoted.put(placeholder.getKey(), nameQuote(placeholder.getValue()));
		}

		for (Map.Entry<String, List<String>> placeholder : multis.entrySet()) {
			List<String> values = placeholder.getValue();
			List<String> q = new ArrayList<>(values.size());
			for (String v : values) {
				q.add(nameQuote(v));
			}
			quoted.put(placeholder.getKey(), AroundString.joinToString(q, ", "));
		}

		String s = sql.toString();
		for (Map.Entry<String, String> q : quoted.entrySet()) {
			// ${name} is used for string interpolation in Groovy and Kotlin.
			// Thus, adding :{name} as alternative.  At some point we should
			// deprecate and then after that remove the ${name} syntax.
			s = s.replace(":{" + q.getKey() + "}", q.getValue());
			s = s.replace("${" + q.getKey() + "}", q.getValue());
		}
		return s;
	}

	public SQLBuilder randomAccess() {
		resultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE;
		return this;
	}

	public SQLBuilder withFetchSize(int _fetchSize) {
		fetchSize = _fetchSize;
		return this;
	}

	private PreparedStatement build(Connection _con) throws SQLException {

		ArrayList<Object> expanded = null;
		if (hasContent(args)) {
			expanded = new ArrayList<>(args.size());
			int sqlPos = 0;
			for (int i = 0; i < args.size(); i++) {
				sqlPos = sql.indexOf("?", sqlPos) + 1;
				if (sqlPos == 0) {
					// We ran out of placeholders (i.e. we have extra parameters).
					// We do not consider that as a bug (though one could argue this
					// should result in a warning).
					break;
				}
				Object arg = args.get(i);
				if (arg instanceof Collection) {
					Collection<?> col = (Collection<?>) arg;
					int length = col == null ? 0 : col.size();
					if (length == 0) {
						throw new SQLException("Collection parameters must contain at least one element");
					}
					// The statement already contains one "?", therefore we start
					// with 1 instead of 0
					for (int k = 1; k < length; k++) {
						sql.insert(sqlPos, ",?");
					}
					// move sqlPos beyond the inserted ",?"
					sqlPos += 2 * (length - 1);
					expanded.addAll(col);
				} else {
					expanded.add(arg);
				}
			}
		}

		PreparedStatement ps = _con.prepareStatement(interpolate(), resultSetType, ResultSet.CONCUR_READ_ONLY);
		try {
			if (fetchSize > 0) {
				ps.setFetchSize(fetchSize);
			}

			if (hasContent(expanded)) {
				int idx = 0;
				for (Object arg : expanded) {
					if (arg instanceof LongString) {
						ps.setCharacterStream(++idx, ((LongString) arg).getReader());
					} else {
						ps.setObject(++idx, arg);
					}
				}
			}
		} catch (SQLException ex) {
			throw ex;
		}
		return ps;
	}

	/**
	 * Returns a ResultSet object created from a PreparedStatement created using
	 * the SQL statement fragment and the parameters.  The PreparedStatement
	 * will be automatically closed when the ResultSet is closed.
	 * @param _con The Connection from which the PreparedStatement is created
	 * @return The ResultSet
	 * @throws SQLException
	 */
	public ResultSet getResultSet(Connection _con) throws SQLException {
		return getResultSet(_con, false);
	}

	public ResultSet getResultSet(Connection _con, boolean _wrapConnection) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = build(_con);
			return _wrapConnection
				? wrapConnection(ps.executeQuery())
				: wrapStatement(ps.executeQuery());
		} catch (SQLException e) {
			close(ps);
			throw e;
		}
	}
	
	public static ResultSet wrapStatement(ResultSet rs) {
		return rs != null ? (ResultSet) Proxy.newProxyInstance(ResultSet.class.getClassLoader(),
				new Class[] { ResultSet.class }, new WrappedResultSet(rs, WrappedResultSet.Scope.Statement)) : null;
	}

	public static ResultSet wrapConnection(ResultSet rs) {
		return rs != null ? (ResultSet) Proxy.newProxyInstance(ResultSet.class.getClassLoader(),
				new Class[] { ResultSet.class }, new WrappedResultSet(rs, WrappedResultSet.Scope.Connection)) : null;
	}
	
	private static class WrappedResultSet implements InvocationHandler {
		private ResultSet rs = null;
		private Scope scope = Scope.ResultSet;

		public enum Scope { ResultSet, Statement, Connection }

		public WrappedResultSet(ResultSet rs, Scope scope) {
			this.rs = rs;
			this.scope = scope;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			// Warning: we have to go though the code below even is
			// rs.isClosed() is true because we still would need to close the
			// statement and connection.  Also, Oracle's ResultSet.next()
			// implicitly closes the ResultSet when it returns false (bypassing
			// this proxy nethod).
			if (rs == null) {
				return null;
			}
			if (method.getName().equals("close")) {
				Statement stmt = null;
				Connection conn = null;
				switch (scope) {
				case Connection:
					stmt = rs.getStatement();
					if (stmt != null) {
						conn = stmt.getConnection();
					}
					break;
				case Statement:
					stmt = rs.getStatement();
					break;
				default:
					break;
				}
				close(rs, stmt, conn);
				rs = null;
				return null;
			}
			try {
				return method.invoke(rs, args);
			} catch (InvocationTargetException e) {
	            throw e.getCause();
	        }
		}
	}
	
	/**
	 * This function closes AutoCloseable objects.
	 *
	 * @return true is all non-null resources could be closed, false otherwise
	 */
	public static boolean close(AutoCloseable... resources)
	{
		boolean allClosed = true;
		if (resources != null) {
			for (AutoCloseable resource : resources) {
				if (resource != null) {
					try {
						resource.close();
					} catch (Exception e) {
						allClosed = false;
					}
				}
			}
		}
		return allClosed;
	}

	/**
	 * Returns a value from the first row returned when executing the query.
	 * @param _con The Connection from which the PreparedStatement is created
	 * @param _numColumn The index of the column (starting with 1) from which to return the value
	 * @param _defValue The default value that is returned if the query did not return any rows
	 * @return
	 * @throws SQLException
	 */
	public int getInt(Connection _con, int _numColumn, int _defValue) throws SQLException {
		try (PreparedStatement ps = build(_con); ResultSet rs = ps.executeQuery()) {
			if (rs.next()) {
				return rs.getInt(_numColumn);
			}
		}
		return _defValue;
	}

	/**
	 * Returns a value from the first row returned when executing the query.
	 * @param _con The Connection from which the PreparedStatement is created
	 * @param _numColumn The index of the column (starting with 1) from which to return the value
	 * @param _defValue The default value that is returned if the query did not return any rows
	 * @return
	 * @throws SQLException
	 */
	public long getLong(Connection _con, int _numColumn, long _defValue) throws SQLException {
		try (PreparedStatement ps = build(_con); ResultSet rs = ps.executeQuery()) {
			if (rs.next()) {
				return rs.getLong(_numColumn);
			}
		}
		return _defValue;
	}

	/**
	 * Returns a value from the first row returned when executing the query.
	 * @param _con The Connection from which the PreparedStatement is created
	 * @param _numColumn The index of the column (starting with 1) from which to return the value
	 * @param _defValue The default value that is returned if the query did not return any rows
	 * @return
	 * @throws SQLException
	 */
	public String getString(Connection _con, int _numColumn, String _defValue) throws SQLException {
		try (PreparedStatement ps = build(_con); ResultSet rs = ps.executeQuery()) {
			if (rs.next()) {
				return rs.getString(_numColumn);
			}
		}
		return _defValue;
	}

	/**
	 * Returns a value from the first row returned when executing the query.
	 * @param _con The Connection from which the PreparedStatement is created
	 * @param _numColumn The index of the column (starting with 1) from which to return the value
	 * @param _defValue The default value that is returned if the query did not return any rows
	 * @return
	 * @throws SQLException
	 */
	public Object getObject(Connection _con, int _numColumn, Object _defValue) throws SQLException {
		try (PreparedStatement ps = build(_con); ResultSet rs = ps.executeQuery()) {
			if (rs.next()) {
				return rs.getObject(_numColumn);
			}
		}
		return _defValue;
	}

	/**
	 * Executes the SQL statement.
	 * @param _con The Connection from which the PreparedStatement is created
	 * @return The result of executeUpdate of that statement
	 * @throws SQLException
	 */
	public int execute(Connection _con) throws SQLException {
		try (PreparedStatement ps = build(_con)) {
			return ps.executeUpdate();
		}
	}

	/**
	 *
	 * @param sql
	 * @param _params
	 * @return
	 */
	public static SQLBuilder fromNamedParams(String _sql, QueryParams _params) {
		List<String> paramNames = _params.getParamNames();
		if (noContent(paramNames)) {
			return new SQLBuilder(_sql);
		}
		StringBuilder names = new StringBuilder();
        String sep = "";
        for (String name : paramNames) {
            names.append(sep).append(":").append(Pattern.quote(name)).append("\\b");
            sep = "|";
        }

        Pattern p = Pattern.compile(names.toString());

		List<Object> args = new ArrayList<>();
		// To avoid replacement within quotes, split the string with quote
		// replace with alternative tokens and join after replacement logic from
		// NpiUtil.getTokens
		String[] tokens = _sql.split("'");
		List<String> queryTokens = new ArrayList<String>();
		for (int i = 0; i < tokens.length; i++) {
			if (i % 2 == 0) {
				Matcher m = p.matcher(tokens[i]);
		        StringBuffer sb = new StringBuffer();
		        while (m.find()) {
		            // To check if parameter expects multiple values.  We cannot
		            // depend on parameter metadata since existing code depends
		            // on IN clause as part of the query and split the value
		            // considering comma as delimiter accordingly e.g. `select
		            // col1 from tab1 where col2 in(:1)`.  As part of parameter
		            // metadata update, parameter might not be chosen as multi,
		            // however it still works with existing code.
		            String subStr = tokens[i].substring(0, m.start());
					boolean isMulti = subStr.matches("(?is).*\\bin\\s*\\(\\s*");
		            boolean dateAsString = _params.dateAsStringNeeded(subStr);
		            if (dateAsString) {
		            	m.appendReplacement(sb, _params.getDateParameterAsString());
		            } else {
		            	m.appendReplacement(sb, "?");
		            }
		            // parameter name is prefixed with ':', so get correct name with trimming ':';
		            String paramName = m.group().substring(1);
		            args.add(_params.getParameterValue(paramName, isMulti, dateAsString));
		        }
		        m.appendTail(sb);
				queryTokens.add(sb.toString());
			} else {
				// alternative tokens are from with-in single quotes.
				queryTokens.add("'" + tokens[i] + "'");
			}
		}
		return new SQLBuilder(AroundString.joinToString(queryTokens, ""), args.toArray(new Object[args.size()]));
	}

	/**
	 * Returns a String representation for logging purposes. This will contain
	 * both the SQL statement fragment and the parameters.
	 * @return A String representation
	 */
	@Override
	public String toString() {
		return interpolate() + "; args=" + args;
	}
}
