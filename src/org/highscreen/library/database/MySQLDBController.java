package org.highscreen.library.database;

import java.sql.DriverManager;

import org.apache.log4j.Logger;

public class MySQLDBController extends DBController {
    private static final Logger logger = Logger.getLogger(MySQLDBController.class);
    protected MySQLDBController(String dbPath) {
        super(dbPath);
    }

    @Override
    protected void initConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            
            connection = DriverManager.getConnection("jdbc:mysql:"
                    + dataBasePath);
        } catch (Exception e) {
            logger.error(e);
        }
    }
}
