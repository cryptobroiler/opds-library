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
    INSERT_AUTHORS("insert into authors values (?,?,?)"),
    INSERT_BOOKS("insert into books values (?,?,?,?,?,?,?,?,?)"),
    INSERT_TAGS("insert into tags values (?,?)"),
    INSERT_SERIES("insert into series values (?,?,?)"),
    INSERT_RATINGS("insert into ratings values (?,?)"),
    INSERT_PUBLISHERS("insert into publishers values (?,?,?)"),
    INSERT_BOOKS_AUTHORS("insert into books_authors values (?,?,?)"),
    INSERT_BOOKS_TAGS("insert into books_tags values (?,?,?)"),
    INSERT_BOOKS_SERIES("insert into books_series values (?,?,?)"),
    SELECT_ALL_BOOKS("select * from books"),
    SELECT_ALL_AUTHORS("select * from authors"),
    SELECT_ALL_TAGS("select * from tags"),
    SELECT_ALL_SERIES("select * from series"),
    SELECT_BOOKS_AUTHORS("select * from books_authors"),
    SELECT_BOOKS_TAGS("select * from books_tags"),
    SELECT_BOOKS_SERIES("select * from books_series"),
    SELECT_AUTHORS_BY_BOOKID(
            "select authors.authorid,authors.firstname,authors.middlename,authors.lastname from authors,books,books_authors "
                    + "where books_authors.bookid=books.bookid and books_authors.authorid = authors.authorid and books.bookid=?"),
    SELECT_BOOK_COUNT("select count(id) from books"),
    SELECT_AUTHOR_COUNT("select count(id) from authors"),
    SELECT_TAG_COUNT("select count(id) from tags"),
    SELECT_SERIES_COUNT("select count(id) from series");
    private String query;

    private SQLQuery(String sqlQuery) {
        query = sqlQuery;
    }

    protected String getSQLString() {
        return query;
    }
}
