package org.highscreen.library.adapters;

import java.io.File;
import java.io.FileReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.highscreen.library.database.DBController;
import org.highscreen.library.database.SQLQuery;
import org.highscreen.library.datamodel.Author;
import org.highscreen.library.datamodel.Book;
import org.highscreen.library.datamodel.Series;
import org.highscreen.library.datamodel.Tag;

public abstract class SQLiteLibraryAdapter implements LibraryAdapter {
    private List<Author> listOfAuthors;
    private List<Book> listOfBooks;
    private List<Tag> listOfTags;
    private List<Series> listOfSeries;
    private Map<String, Author> mapOfAuthors;
    private Map<String, Series> mapOfSeries;
    private Map<String, Book> mapOfBooks;
    private Map<String, Tag> mapOfTags;
    private Map<Author, List<Book>> mapOfBooksByAuthor;
    private Map<String, List<Author>> mapOfAuthorsByBookID;
    private Map<String, List<Series>> mapOfSeriesByBookID;
    private Map<String, List<Tag>> mapOfTagsByBookID;
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
            db.runScript(new FileReader("res/metadata_sqlite.sql"));
        } catch (Exception e) {
            logger.error(e);
        }
    }


    protected DBController getDB() {
        return db;
    }

    protected String getPathToAdapter(String filename) {
        String dir = "./adapters/" + this.getClass().getSimpleName() + "/";
        File path = new File(dir);
        if (!path.exists()) {
            boolean result = path.mkdirs();
            logger.info("Creating directories: " + result);
        }
        return dir + filename;
    }

    public SQLiteLibraryAdapter(String dbName) {
        databaseName = dbName;
        db = DBController.getInstance(dbName);
        clear();
        if (isUpdateNeeded()) {
            makeCleanDatabase();
        }
        logger.debug(this.getClass());
    }

    private void clear() {
        listOfAuthors = null;
        listOfBooks = null;
        listOfTags = null;
        listOfSeries = null;
        mapOfAuthors = null;
        mapOfBooks = null;
        mapOfTags = null;
        mapOfSeries = null;
        mapOfBooksByAuthor = null;
        mapOfSeriesByBookID = null;
        mapOfAuthorsByBookID = null;
        mapOfTagsByBookID = null;
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
                        books = new ArrayList<Book>();
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
            listOfBooks = new ArrayList<Book>();
            PreparedStatement ps;
            try {
                ps = db.getPreparedStatement(SQLQuery.SELECT_ALL_BOOKS);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    String id = rs.getString("id");
                    String title = rs.getString("title");
                    String sort = rs.getString("sort");
                    String timestamp = rs.getString("timestamp");
                    String uri = rs.getString("uri");
                    String seriesIndex = rs.getString("series_index");
                    String authorSort = rs.getString("author_sort");
                    String isbn = rs.getString("isbn");
                    String path = rs.getString("path");
                    Book book = new Book(id, title, sort, timestamp, uri,
                            seriesIndex, authorSort, isbn, path);
                    List<Author> authors = getMapOfAuthorsByBookId().get(id);
                    if (authors != null) {
                        for (Author author : authors) {
                            book.addAuthor(author);
                        }
                    }
                    List<Tag> tags = getMapOfTagsByBookId().get(id);
                    if (tags != null) {
                        for (Tag tag : tags) {
                            book.addTag(tag);
                        }
                    }
                    List<Series> seriesList = getMapOfSeriesByBookId().get(id);
                    if (seriesList != null) {
                        if (seriesList.size() > 0) {
                            book.setSeries(seriesList.get(0));
                        }
                    }
                    listOfBooks.add(book);
                }
            } catch (Exception e) {
                logger.error(e);
            }
        }
        return listOfBooks;
    }

    public Map<String, List<Author>> getMapOfAuthorsByBookId() {
        if (mapOfAuthorsByBookID == null) {
            mapOfAuthorsByBookID = new HashMap<String, List<Author>>();
            PreparedStatement ps;
            try {
                ps = db.getPreparedStatement(SQLQuery.SELECT_BOOKS_AUTHORS);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    String bookId = rs.getString("book");
                    String authorId = rs.getString("author");
                    List<Author> authors = mapOfAuthorsByBookID.get(bookId);
                    if (authors == null) {
                        authors = new ArrayList<Author>();
                        mapOfAuthorsByBookID.put(bookId, authors);
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
        return mapOfAuthorsByBookID;
    }

    public Map<String, List<Series>> getMapOfSeriesByBookId() {
        if (mapOfSeriesByBookID == null) {
            mapOfSeriesByBookID = new HashMap<String, List<Series>>();
            PreparedStatement ps;
            try {
                ps = db.getPreparedStatement(SQLQuery.SELECT_BOOKS_SERIES);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    String bookID = rs.getString("book");
                    String seriesID = rs.getString("series");
                    List<Series> series = mapOfSeriesByBookID.get(bookID);
                    if (series == null) {
                        series = new ArrayList<Series>();
                        mapOfSeriesByBookID.put(bookID, series);
                    }
                    Series serie = getMapOfSeries().get(seriesID);
                    if (serie != null) {
                        series.add(serie);
                    } else {
                        logger.error("No series with id=" + seriesID);
                    }
                }
            } catch (Exception e) {
                logger.error(e);
            }
        }
        return mapOfSeriesByBookID;
    }

    public Map<String, List<Tag>> getMapOfTagsByBookId() {
        if (mapOfTagsByBookID == null) {
            mapOfTagsByBookID = new HashMap<String, List<Tag>>();
            PreparedStatement ps;
            try {
                ps = db.getPreparedStatement(SQLQuery.SELECT_BOOKS_TAGS);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    String bookID = rs.getString("book");
                    String tagID = rs.getString("tag");
                    List<Tag> tags = mapOfTagsByBookID.get(bookID);
                    if (tags == null) {
                        tags = new ArrayList<Tag>();
                        mapOfTagsByBookID.put(bookID, tags);
                    }
                    Tag tag = getMapOfTags().get(tagID);
                    if (tag != null) {
                        tags.add(tag);
                    } else {
                        logger.error("No tag with id=" + tagID);
                    }
                }
            } catch (Exception e) {
                logger.error(e);
            }
        }
        return mapOfTagsByBookID;
    }

    @Override
    public List<Author> getListOfAuthors() {
        if (listOfAuthors == null) {
            listOfAuthors = new ArrayList<Author>();
            List<String> ids = new ArrayList<String>();
            PreparedStatement ps;
            try {
                ps = db.getPreparedStatement(SQLQuery.SELECT_ALL_AUTHORS);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    String id = rs.getString("id");
                    String name = rs.getString("name");
                    String sort = rs.getString("sort");
                    if (!ids.contains(id)) {
                        ids.add(id);
                        listOfAuthors.add(new Author(id, name, sort));
                    }
                }
            } catch (Exception e) {
                logger.error(e);
            }
        }
        return listOfAuthors;
    }

    public List<Series> getListOfSeries() {
        if (listOfSeries == null) {
            listOfSeries = new ArrayList<Series>();
            List<String> ids = new ArrayList<String>();
            PreparedStatement ps;
            try {
                ps = db.getPreparedStatement(SQLQuery.SELECT_ALL_SERIES);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    String id = rs.getString("id");
                    String name = rs.getString("name");
                    String sort = rs.getString("sort");
                    if (!ids.contains(id)) {
                        ids.add(id);
                        listOfSeries.add(new Series(id, name, sort));
                    }
                }
            } catch (Exception e) {
                logger.error(e);
            }
        }
        return listOfSeries;
    }

    @Override
    public List<Tag> getListOfTags() {
        if (listOfTags == null) {
            listOfTags = new ArrayList<Tag>();
            List<String> ids = new ArrayList<String>();
            PreparedStatement ps;
            try {
                ps = db.getPreparedStatement(SQLQuery.SELECT_ALL_TAGS);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    String id = rs.getString("id");
                    String name = rs.getString("name");
                    if (!ids.contains(id)) {
                        ids.add(id);
                        listOfTags.add(new Tag(id, name));
                    }
                }
            } catch (Exception e) {
                logger.error(e);
            }
        }
        return listOfTags;
    }

    public Map<String, Series> getMapOfSeries() {
        if (mapOfSeries == null) {
            mapOfSeries = new HashMap<String, Series>();
            for (Series series : getListOfSeries()) {
                mapOfSeries.put(series.getID(), series);
            }
        }
        return mapOfSeries;
    }

    public Map<String, Tag> getMapOfTags() {
        if (mapOfTags == null) {
            mapOfTags = new HashMap<String, Tag>();
            for (Tag tag : getListOfTags()) {
                mapOfTags.put(tag.getID(), tag);
            }
        }
        return mapOfTags;
    }

    public Map<String, Author> getMapOfAuthors() {
        if (mapOfAuthors == null) {
            mapOfAuthors = new HashMap<String, Author>();
            for (Author author : getListOfAuthors()) {
                mapOfAuthors.put(author.getID(), author);
            }
        }
        return mapOfAuthors;
    }
    // public getListOfAuthorsByBookId() {
    //
    // }
}
