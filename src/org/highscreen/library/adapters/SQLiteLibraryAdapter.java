package org.highscreen.library.adapters;

import java.io.File;
import java.io.FileReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.highscreen.library.database.DBController;
import org.highscreen.library.database.SQLQuery;
import org.highscreen.library.datamodel.Author;
import org.highscreen.library.datamodel.Book;
import org.highscreen.library.datamodel.Tag;

public abstract class SQLiteLibraryAdapter implements LibraryAdapter {

	private List<Author> listOfAuthors;
	private List<Book> listOfBooks;
	private List<Tag> listOfTags;
	private Map<String, Author> mapOfAuthors;
	private Map<String, Book> mapOfBooks;
	private Map<Author, List<Book>> mapOfBooksByAuthor;
	private Map<String, List<Author>> mapOfAuthorsByBookId;

	private static final Logger logger = Logger
			.getLogger(SQLiteLibraryAdapter.class);
	private String databaseName;

	public String getDatabaseName() {
		return databaseName;
	}

	private DBController db = null;

	protected void makeCleanDatabase() {
		FileUtils.deleteQuietly(new File(databaseName));
		try {
			// db.getPreparedStatement(SQLQuery.CREATE_AUTHORS).execute();
			// db.getPreparedStatement(SQLQuery.CREATE_BOOKS).execute();
			// db.getPreparedStatement(SQLQuery.CREATE_BOOKS_AUTHORS).execute();
			db.runScript(new FileReader("res/metadata_sqlite.sql"));
		} catch (Exception e) {
			logger.error(e);
		}
	}

	protected DBController getDB() {
		return db;
	}

	public SQLiteLibraryAdapter(String dbName) {
		databaseName = dbName;
		db = DBController.getInstance(dbName);
		clear();
		makeCleanDatabase();
	}

	private void clear() {
		listOfAuthors = null;
		listOfBooks = null;
		listOfTags = null;
		mapOfAuthorsByBookId = null;
		mapOfAuthors = null;
		mapOfBooks = null;
		mapOfBooksByAuthor = null;
	}

	public abstract String getURL(Book book);

	@Override
	public Map<Author, List<Book>> getMapOfBooksByAuthor() {
		if (mapOfBooksByAuthor == null) {
			mapOfBooksByAuthor = new HashMap<Author, List<Book>>();
			for (Book book : getListOfBooks()) {
				for (Author author : book.getAuthors()) {
					List<Book> books = mapOfBooksByAuthor.get(author);
					if (books == null) {
						books = new Vector<Book>();
						mapOfBooksByAuthor.put(author, books);
					}
					books.add(book);
				}
			}
		}
		return mapOfBooksByAuthor;
	}

	@Override
	public List<Book> getListOfBooks() {
		if (listOfBooks == null) {
			listOfBooks = new Vector<Book>();
			PreparedStatement ps;
			try {
				ps = db.getPreparedStatement(SQLQuery.SELECT_ALL_BOOKS);
				ResultSet rs = ps.executeQuery();
				while (rs.next()) {
					String id = rs.getString("bookid");
					String title = rs.getString("title") + " "
							+ rs.getString("title1");
					String source = rs.getString("sourceid");
					String timestamp = rs.getString("time");
					String fileType = rs.getString("filetype");
					Book b = new Book(id, title, timestamp, "no summary",
							fileType, source);
					b.setUri(getURL(b));
					List<Author> authors = getMapOfAuthorsByBookId().get(id);
					if (authors != null) {
						for (Author author : authors) {
							b.addAuthor(author);
						}
					}
					listOfBooks.add(b);
				}
			} catch (Exception e) {
				logger.error(e);
			}
		}
		return listOfBooks;
	}

	public Map<String, List<Author>> getMapOfAuthorsByBookId() {
		if (mapOfAuthorsByBookId == null) {
			mapOfAuthorsByBookId = new HashMap<String, List<Author>>();
			PreparedStatement ps;
			try {
				ps = db.getPreparedStatement(SQLQuery.SELECT_BOOKS_AUTHORS);
				ResultSet rs = ps.executeQuery();
				while (rs.next()) {
					String bookId = rs.getString("bookid");
					String authorId = rs.getString("authorid");
					List<Author> authors = mapOfAuthorsByBookId.get(bookId);
					if (authors == null) {
						authors = new Vector<Author>();
						mapOfAuthorsByBookId.put(bookId, authors);
					}
					Author author = getMapOfAuthors().get(authorId);
					if (author != null) {
						authors.add(author);
					} else {
						logger.error("No author with id=" + authorId);
					}
				}
			} catch (Exception e) {
				logger.error(e);
			}
		}
		return mapOfAuthorsByBookId;
	}

	@Override
	public List<Author> getListOfAuthors() {
		if (listOfAuthors == null) {
			listOfAuthors = new Vector<Author>();
			List<String> ids = new Vector<String>();
			PreparedStatement ps;
			try {
				ps = db.getPreparedStatement(SQLQuery.SELECT_ALL_AUTHORS);
				ResultSet rs = ps.executeQuery();
				while (rs.next()) {
					String id = rs.getString("authorid");
					String firstname = rs.getString("firstname");
					String middleanme = rs.getString("middlename");
					String lastname = rs.getString("lastname");
					if (!ids.contains(id)) {
						ids.add(id);
						listOfAuthors.add(new Author(id, firstname, middleanme,
								lastname));
					}
				}
			} catch (Exception e) {
				logger.error(e);
			}
		}
		return listOfAuthors;
	}

	public Map<String, Author> getMapOfAuthors() {
		if (mapOfAuthors == null) {
			mapOfAuthors = new HashMap<String, Author>();
			for (Author author : getListOfAuthors()) {
				mapOfAuthors.put(author.getId(), author);
			}
		}
		return mapOfAuthors;
	}

	// public getListOfAuthorsByBookId() {
	//
	// }
	@Override
	public List<Tag> getListOfTags() {
		// TODO Auto-generated method stub
		return null;
	}

}
