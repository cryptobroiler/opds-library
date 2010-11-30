package org.highscreen.library.database;

public enum SQLQuery {
	DROP_BOOKS_AUTHORS("drop table if exists books_authors"),
	DROP_TAGS("drop table if exists tags;"),
	DROP_BOOKS("drop table if exists books;"),
	DROP_AUTHORS("drop table if exists authors;"),
	CREATE_AUTHORS("CREATE TABLE `authors` ("
					+ "`AuthorId` integer PRIMARY KEY ASC,"
					+ "`FirstName` varchar(99) NOT NULL DEFAULT '',"
					+ "`MiddleName` varchar(99) NOT NULL DEFAULT '',"
					+ "`LastName` varchar(99) NOT NULL DEFAULT '');"),
	CREATE_BOOKS_AUTHORS("CREATE TABLE `books_authors` ("
					+ "`BookId` integer NOT NULL DEFAULT '0',"
					+ "`AuthorId` integer NOT NULL DEFAULT '0');"),
	CREATE_BOOKS("CREATE TABLE `books` ("
					+ " `BookId` integer PRIMARY KEY ASC,"
					+ " `FileSize` integer NOT NULL DEFAULT '0',"
					+ " `Time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,"
					+ " `Title` varchar(254) NOT NULL DEFAULT '',"
					+ " `Title1` varchar(254) NOT NULL,"
					+ " `FileType` char(4) NOT NULL,"
					+ " `Year` integer NOT NULL DEFAULT '0',"
					+ " `md5` char(32) NOT NULL,"
					+ " `SourceId` integer NOT NULL DEFAULT '0');"),
	INSERT_AUTHORS("insert into authors values (?,?,?,?)"),
	INSERT_BOOKS("insert into books values (?,?,?,?,?,?,?,?,?)"),
	INSERT_BOOKS_AUTHORS("insert into books_authors values (?,?)");
	
					

	private String query;

	private SQLQuery(String sqlQuery) {
		query = sqlQuery;
	}

	protected String getSQLString() {
		return query;
	}

}
