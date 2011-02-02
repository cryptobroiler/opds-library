package org.highscreen.library.database;

import java.io.LineNumberReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public abstract class DBController {
    private static final String DEFAULT_DELIMETER = ";";
    private static final Logger logger = Logger.getLogger(DBController.class);
    private static Map<String, DBController> controllersMap = new HashMap<String, DBController>();
    private Map<SQLQuery, PreparedStatement> preparedStatements = new HashMap<SQLQuery, PreparedStatement>();
    protected String dataBasePath;
    protected Connection connection;

    public Connection getConnection() {
        if (connection == null) {
            initConnection();
        }
        return connection;
    }

    protected DBController(String dbPath) {
        dataBasePath = dbPath;
    }
    public String getDBPath() {
        return dataBasePath;
    }

    public static DBController getInstance(String dbPath) {
        DBController c = controllersMap.get(dbPath);
        if (c == null) {
            if (dbPath.startsWith("$SQLITE$")) {
                c = new SQLiteDBController(dbPath.replaceFirst("$SQLITE$", ""));
            } else {
                if (dbPath.startsWith("$MYSQL$")) {
                    c = new MySQLDBController(
                            dbPath.replaceFirst("$MYSQL$", ""));
                }
            }
            controllersMap.put(dbPath, c);
        }
        return c;
    }

    protected abstract void initConnection();

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

    public List<String> getTableHeader(String tableName) {
        List<String> result = null;
        try {
            result = new ArrayList<String>();
            ResultSetMetaData md = getConnection().prepareStatement(
                    "select * from " + tableName).getMetaData();
            int columns = md.getColumnCount();
            for (int i = 1; i <= columns; i++) {
                result.add(md.getColumnName(i));
            }
        } catch (Exception e) {
            logger.error(e);
        }
        return result;
    }

    private PreparedStatement prepareInsertStatement(String tableName,
            int columns) {
        PreparedStatement result = null;
        try {
            StringBuffer sql = new StringBuffer("insert into " + tableName
                    + " values (");
            for (int i = 0; i < columns; i++) {
                sql.append("?");
                if (i != columns - 1) {
                    sql.append(",");
                }
            }
            sql.append(")");
            logger.debug("Prepared insert statment: " + sql);
            result = getConnection().prepareStatement(sql.toString());
        } catch (Exception e) {
            logger.error(e);
        }
        return result;
    }

    public void importValuesMapIntoTable(
            Map<Integer, Map<String, String>> mapOfValues, String tableName) {
        try {
            enableAutoCommit(false);
            List<String> columns = getTableHeader(tableName);
            PreparedStatement ps = prepareInsertStatement(tableName,
                    columns.size());
            for (Integer key : mapOfValues.keySet()) {
                Map<String, String> value = mapOfValues.get(key);
                for (int i = 1; i <= columns.size(); i++) {
                    String column = columns.get(i - 1);
                    // logger.debug("setting column " + column);
                    ps.setString(i, value.get(column));
                }
                ps.addBatch();
            }
            ps.executeBatch();
            commit();
            enableAutoCommit(true);
        } catch (SQLException e) {
            logger.error(e);
        }
    }
}
