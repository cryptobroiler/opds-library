package org.highscreen.library.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class DBController {
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

}
