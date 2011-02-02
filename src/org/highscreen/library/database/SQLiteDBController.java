package org.highscreen.library.database;


import java.sql.DriverManager;

import org.apache.log4j.Logger;

public class SQLiteDBController extends DBController {
    private static final Logger logger = Logger.getLogger(SQLiteDBController.class);
    protected SQLiteDBController(String dbPath) {
        super(dbPath);
    }

    @Override
    protected void initConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:"
                    + dataBasePath);
        } catch (Exception e) {
            logger.error(e);
        }
    }
    
}
