package org.highscreen.library.database;

import java.io.LineNumberReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class DBController {
	private static final String DEFAULT_DELIMETER = ";";
	private static final Logger logger = Logger.getLogger(DBController.class);
	private static Map<String, DBController> controllersMap = new HashMap<String, DBController>();
	private Map<SQLQuery, PreparedStatement> preparedStatements = new HashMap<SQLQuery, PreparedStatement>();
	private String dataBasePath;
	private Connection connection;

	public Connection getConnection() {
		if (connection == null) {
			initConnection();
		}
		return connection;
	}

	private DBController(String dbPath) {
		dataBasePath = dbPath;
	}

	public static DBController getInstance(String dbPath) {
		DBController c = controllersMap.get(dbPath);
		if (c == null) {
			c = new DBController(dbPath);
			controllersMap.put(dbPath, c);
		}
		return c;
	}
	
	private void initConnection() {
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:"
					+ dataBasePath);
		} catch (Exception e) {
			logger.error(e);
		}

	}

	public PreparedStatement getPreparedStatement(SQLQuery query) {
		PreparedStatement preparedStatement = preparedStatements.get(query);
		if (preparedStatement == null) {
			try {
				preparedStatement = getConnection().prepareStatement(
						query.getSQLString());
				preparedStatements.put(query, preparedStatement);
			} catch (Exception e) {
				logger.error(e);
			}
		}
		return preparedStatement;
	}

	public void enableAutoCommit(boolean value) {
		try {
			getConnection().setAutoCommit(value);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.error(e);
		}
	}

	public void commit() {
		try {
			getConnection().commit();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.error(e);
		}
	}

	public void runScript(Reader scriptReader) {
		StringBuffer command = null;
		try {
			enableAutoCommit(false);
			LineNumberReader reader = new LineNumberReader(scriptReader);
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (command == null) {
					command = new StringBuffer();
				}
				String trimmedLine = line.trim();
				if (trimmedLine.startsWith("--")) {
					// skip
				} else if (trimmedLine.length() < 1
						|| trimmedLine.startsWith("//")) {
					// skip
				} else if (trimmedLine.length() < 1
						|| trimmedLine.startsWith("--")) {
				} else if (trimmedLine.endsWith(DEFAULT_DELIMETER)) {
					command.append(line.substring(0,
							line.lastIndexOf(DEFAULT_DELIMETER)));
					command.append(" ");
					Statement statement = connection.createStatement();
					logger.debug(command);
					statement.execute(command.toString());
					commit();
					command = null;
					statement.close();
					// Thread.yield();
				} else {
					command.append(line);
					command.append(" ");
				}

			}
			commit();
			enableAutoCommit(true);
		} catch (Exception e) {
			logger.error(e);
		}

	}
}
