package org.highscreen.library.adapters;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Vector;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ArrayHandler;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.highscreen.library.database.DBController;
import org.highscreen.library.database.SQLQuery;
import org.highscreen.library.datamodel.Author;
import org.highscreen.library.datamodel.Book;
import org.highscreen.library.datamodel.Tag;

public class SQLiteLibraryAdapter implements LibraryAdapter {
	protected static final String DROP_BOOKS_AUTHORS = "drop table if exists books_authors";
	protected static final String DROP_TAGS = "drop table if exists tags;";
	protected static final String DROP_BOOKS = "drop table if exists books;";
	protected static final String DROP_AUTHORS = "drop table if exists authors;";

	protected static final String CREATE_AUTHORS = "CREATE TABLE `authors` ("
			+ "`AuthorId` integer PRIMARY KEY ASC,"
			+ "`FirstName` varchar(99) NOT NULL DEFAULT '',"
			+ "`MiddleName` varchar(99) NOT NULL DEFAULT '',"
			+ "`LastName` varchar(99) NOT NULL DEFAULT '');";
	// + " `RemoteId` integer UNIQUE NOT NULL);";
	protected static final String CREATE_BOOKS_AUTHORS = "CREATE TABLE `books_authors` ("
			+ "`BookId` integer NOT NULL DEFAULT '0',"
			+ "`AuthorId` integer NOT NULL DEFAULT '0');";
	protected static final String CREATE_BOOKS = "CREATE TABLE `books` ("
			+ " `BookId` integer PRIMARY KEY ASC,"
			+ " `FileSize` integer NOT NULL DEFAULT '0',"
			+ " `Time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,"
			+ " `Title` varchar(254) NOT NULL DEFAULT '',"
			+ " `Title1` varchar(254) NOT NULL,"
			+ " `FileType` char(4) NOT NULL,"
			+ " `Year` integer NOT NULL DEFAULT '0',"
			+ " `md5` char(32) NOT NULL,"
			+ " `SourceId` integer NOT NULL DEFAULT '0');";
	private static final Logger logger = Logger
			.getLogger(SQLiteLibraryAdapter.class);
	// + " `RemoteId` integer UNIQUE NOT NULL);";
	private String databaseName;

	public String getDatabaseName() {
		return databaseName;
	}

	protected void makeCleanDatabase() {
		FileUtils.deleteQuietly(new File(databaseName));
		DBController controller = DBController.getInstance(databaseName);
		try {
			controller.getPreparedStatement(SQLQuery.CREATE_AUTHORS).execute();
			controller.getPreparedStatement(SQLQuery.CREATE_BOOKS).execute();
			controller.getPreparedStatement(SQLQuery.CREATE_BOOKS_AUTHORS)
					.execute();
		} catch (Exception e) {
			logger.error(e);
		}
	}

	public SQLiteLibraryAdapter(String dbName) {
		databaseName = dbName;
		// makeCleanDatabase();
	}

	// public abstract String formHyperlink(Book book);
	@Override
	public List<Book> listBooks() {
		// TODO Auto-generated method stub
		List<Book> result = new Vector<Book>();
		DBController db = DBController.getInstance(databaseName);
		PreparedStatement ps;
		try {
			ps = db.getPreparedStatement(SQLQuery.SELECT_ALL_BOOKS);
			ps.execute();
			ResultSet rs = ps.getResultSet();
			while (rs.next()) {
				Book b = new Book(rs.getString("bookid"), rs
						.getString("title") + rs.getString("title1"),
						"http://flibusta.net", "0", "no summary",
						listAuthorsByBookId(rs.getString("bookid")), "0");
				logger.debug(b);
				result.add(b);
			}
		} catch (Exception e) {
			// TODO: handle exception
			logger.error(e);
		}
		return result;
	}

	public List<Author> listAuthorsByBookId(String id) {
		List<Author> result = new Vector<Author>();
		DBController db = DBController.getInstance(databaseName);
		PreparedStatement ps;
		try {
			ps = db.getPreparedStatement(SQLQuery.SELECT_AUTHORS_BY_BOOKID);
			ps.setInt(1, Integer.valueOf(id));
			ps.execute();
			ResultSet rs = ps.getResultSet();
			while (rs.next()) {
				result.add(new Author(rs.getString("authorid"), rs
						.getString("firstname"), rs.getString("middlename"), rs
						.getString("lastname"), null));
			}
		} catch (Exception e) {
			// TODO: handle exception
			logger.error(e);
		}
		return result;
	}

	@Override
	public List<Author> listAuthors() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Tag> listTags() {
		// TODO Auto-generated method stub
		return null;
	}

}
