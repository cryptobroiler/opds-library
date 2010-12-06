package org.highscreen.library.adapters;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.highscreen.library.database.DBController;
import org.highscreen.library.database.SQLQuery;
import org.highscreen.library.datamodel.Book;

public class FlibustaLibraryAdapter extends SQLiteLibraryAdapter {
    private static final Logger logger = Logger
            .getLogger(FlibustaLibraryAdapter.class);
    Map<Long, Long> mapOfLocalBookIDByRemoteBookID = new HashMap<Long, Long>();
    Map<Long, Long> mapOfLocalAuthorIDByRemoteAuthorID = new HashMap<Long, Long>();
    Map<String, Long> mapOfLocalAuthorIDByAuthorName = new HashMap<String, Long>();
    Map<Long, Long> mapOfRemoteGoodAuthorIDByRemoteBadAuthorID = new HashMap<Long, Long>();
    Map<Long, Long> mapOfLocalTagIDByRemoteTagID = new HashMap<Long, Long>();
    Map<String, Long> mapOfLocalTagIDByTagName = new HashMap<String, Long>();
    Set<String> setOfTags = new HashSet<String>();
    Set<String> setOfAuthorNames = new HashSet<String>();
    private static final String dbUrl = "http://93.174.93.47/sql/";
    private static final String[] filenames = { "lib.libavtorname.sql",
            "lib.libbook.sql", "lib.libavtor.sql", "lib.libgenrelist.sql",
            "lib.libgenre.sql", "lib.libavtoraliase.sql",
            "lib.libtranslator.sql", "lib.libfilename.sql",
            "lib.libjoinedbooks.sql", "lib.librate.sql", "lib.libseqname.sql",
            "lib.libseq.sql", "lib.libsrclang.sql", "lib.b.annotations.sql",
            "lib.b.annotations_pics.sql" };
    private static final int FLIBUSTA_SOURCE_ID = 0;

    void readMapOfRemoteGoodAuthorIDByRemoteBadAuthorID() {
        List<String[]> values = readValuesFromFile(filenames[5]);
        for (String[] value : values) {
            mapOfRemoteGoodAuthorIDByRemoteBadAuthorID.put(
                    Long.valueOf(value[1]), Long.valueOf(value[2]));
        }
    }

    protected static String getValuesPath(File file) {
        return file.getPath() + ".csv";
    }

    protected static String getValuesPath(String filename) {
        return filename + ".csv";
    }

    protected static void extractMySQLValues(File file) throws Exception {
        logger.debug("Extracting values from " + file.getName());
        logger.debug("Starting INSERT extraction, fileSize = "
                + FileUtils.sizeOf(file));
        long start = System.currentTimeMillis();
        Pattern p = Pattern.compile(".*INSERT.*");
        Matcher m = p.matcher(FileUtils.readFileToString(file));
        String result = "";
        while (m.find()) {
            result = result + m.group();
        }
        result = result.replaceAll("\\),\\(", "\n");
        result = result.replaceAll("\\\\'", "");
        result = result.replaceAll("\\);INSERT", "\nINSERT");
        result = result.replaceAll("INSERT INTO `"
                + file.getName().split("\\.")[1] + "` VALUES \\(", "");
        result = result.replaceAll("\\);$", "");
        File sqliteFile = new File(getValuesPath(file));
        FileUtils.writeStringToFile(sqliteFile, result);
        logger.debug("Extraction complete: "
                + (System.currentTimeMillis() - start) + "ms");
    }

    protected static void testNewMySQLExtract() {
        try {
            for (String name : filenames) {
                extractMySQLValues(new File(getRelativeAdaptersPath(name)));
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    protected static void fetchFlibustaDB() {
        try {
            for (String f : filenames) {
                File file = new File(getRelativeAdaptersPath(f));
                long localTimestamp = file.lastModified();
                logger.debug("Local timestamp of " + f + ":" + localTimestamp);
                URL url = new URL(dbUrl + f + ".gz");
                URLConnection conn = url.openConnection();
                long remoteTimestamp = conn.getHeaderFieldDate("Last-Modified",
                        0);
                logger.debug("Remote timestamp of " + f + ":" + remoteTimestamp);
                if (remoteTimestamp > 0) {
                    if (remoteTimestamp != localTimestamp) {
                        logger.debug("Timestamps differ: downloading & gunzipping file "
                                + url.toString());
                        InputStream is = url.openStream();
                        GZIPInputStream gzipInputStream = new GZIPInputStream(
                                new BufferedInputStream(is));
                        FileUtils.copyInputStreamToFile(gzipInputStream, file);
                        file.setLastModified(remoteTimestamp);
                        is.close();
                        extractMySQLValues(file);
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }

    private static String getRelativeAdaptersPath(String filename) {
        String dir = "./adapters/"
                + FlibustaLibraryAdapter.class.getSimpleName() + "/";
        File path = new File(dir);
        if (!path.exists()) {
            boolean result = path.mkdirs();
            logger.trace("Creating directories: " + result);
        }
        return dir + filename;
    }

    private static List<String> tokenizeMySQLValue(String value) {
        Vector<String> result = new Vector<String>();
        String current = "";
        boolean inString = false;
        boolean stringExit = false;
        char c;
        for (int i = 0; i < value.length(); i++) {
            c = value.charAt(i);
            if (inString) {
                if (c != '\'') {
                    current += c;
                } else {
                    inString = false;
                    stringExit = true;
                    result.add(current);
                    current = "";
                }
            } else {
                if (c == '\'') {
                    inString = true;
                } else {
                    if (i == value.length() - 1 && c != ',') {
                        current += c;
                        result.add(current);
                        current = "";
                    } else {
                        if (c != ',') {
                            current += c;
                        } else {
                            if (stringExit) {
                                stringExit = false;
                                continue;
                            }
                            result.add(current);
                            current = "";
                        }
                    }
                }
            }
        }
        logger.trace(result);
        return result;
    }

    private static List<String[]> readValuesFromFile(String filename) {
        List<String[]> values = null;
        try {
            values = new Vector<String[]>();
            BufferedReader br = getUnicodeBufferedReader(filename);
            String value;
            while ((value = br.readLine()) != null) {
                values.add(tokenizeMySQLValue(value).toArray(new String[0]));
            }
        } catch (Exception e) {
            logger.error(e);
        }
        return values;
    }

    public long getLastBookID() {
        long result = 0;
        try {
            ResultSet rs = getDB().getPreparedStatement(
                    SQLQuery.SELECT_BOOK_COUNT).executeQuery();
            while (rs.next()) {
                result = rs.getLong(1);
            }
        } catch (SQLException e) {
            logger.error(e);
        }
        return result;
    }

    public long getLastAuthorID() {
        long result = 0;
        try {
            ResultSet rs = getDB().getPreparedStatement(
                    SQLQuery.SELECT_AUTHOR_COUNT).executeQuery();
            while (rs.next()) {
                result = rs.getLong(1);
            }
        } catch (SQLException e) {
            logger.error(e);
        }
        return result;
    }

    public long getLastTagID() {
        long result = 0;
        try {
            ResultSet rs = getDB().getPreparedStatement(
                    SQLQuery.SELECT_TAG_COUNT).executeQuery();
            while (rs.next()) {
                result = rs.getLong(1);
            }
        } catch (SQLException e) {
            logger.error(e);
        }
        return result;
    }

    public void importFlibustaToSQLiteDB() {
        DBController db = getDB();
        PreparedStatement ps;
        List<String[]> authorsValues = readValuesFromFile(filenames[0]);
        readMapOfRemoteGoodAuthorIDByRemoteBadAuthorID();
        List<String[]> booksValues = readValuesFromFile(filenames[1]);
        List<String[]> books_authorsValues = readValuesFromFile(filenames[2]);
        List<String[]> tagsValues = readValuesFromFile(filenames[3]);
        List<String[]> books_tagsValues = readValuesFromFile(filenames[4]);
        long start;
        long miss;
        try {
            db.enableAutoCommit(false);
            ps = db.getPreparedStatement(SQLQuery.INSERT_BOOKS);
            long counter = getLastBookID() + 1;
            for (String[] value : booksValues) {
                if (!value[8].equals("0")) {
                    continue;
                }
                int bookID = Integer.valueOf(value[0]);
                String fileType = value[6];
                String title = value[3];
                if (!value[4].isEmpty()) {
                    title += " [" + value[4] + "]";
                }
                String timeStamp = value[2];
                String uri = "http://flibusta.net/b/" + String.valueOf(bookID)
                        + "/" + fileType;
                mapOfLocalBookIDByRemoteBookID.put(Long.valueOf(bookID),
                        Long.valueOf(counter));
                // ps.setInt(1, bookId); // BookID
                ps.setString(2, title); // Filesize
                ps.setString(3, title); // Timestamp
                ps.setString(4, timeStamp);
                ps.setString(5, uri); // FileType
                ps.setString(6, "DEFAULT_SERIES_INDEX");
                ps.setString(7, "DEFAULT_AUTHOR_SORT"); // Year
                ps.setString(8, "0"); // md5
                ps.setString(9, "flibusta.net"); // SourceID
                ps.addBatch();
                counter++;
            }
            ps.executeBatch();
            ps = db.getPreparedStatement(SQLQuery.INSERT_AUTHORS);
            counter = getLastAuthorID() + 1;
            for (String[] value : authorsValues) {
                Long authorID = Long.valueOf(value[0]);
                String name = value[1];
                String sort = value[1];
                if (!value[2].isEmpty()) {
                    name += " " + value[2];
                    sort += " " + value[2];
                }
                if (!value[3].isEmpty()) {
                    name += " " + value[3];
                    sort = value[3] + ", " + sort;
                }
                name = name.trim();
                sort = sort.trim();
                if (mapOfRemoteGoodAuthorIDByRemoteBadAuthorID
                        .containsKey(authorID)) { // Author entry is marked as
                                                  // 'bad'
                    logger.debug("Found bad author entry [skipping]: "
                            + authorID);
                    continue;
                }
                if (setOfAuthorNames.add(name.toLowerCase())) { // new
                    mapOfLocalAuthorIDByRemoteAuthorID.put(authorID,
                            Long.valueOf(counter));
                    mapOfLocalAuthorIDByAuthorName.put(name.toLowerCase(),
                            Long.valueOf(counter));
                    ps.setString(2, name);
                    ps.setString(3, sort);
                    ps.addBatch();
                    counter++;
                } else {
                    logger.debug("Duplicate entry: " + name.toLowerCase()
                            + " counter:" + counter);
                    if (mapOfRemoteGoodAuthorIDByRemoteBadAuthorID
                            .containsKey(authorID)) {
                        logger.debug("bad author");
                    }
                    mapOfLocalAuthorIDByRemoteAuthorID.put(authorID,
                            mapOfLocalAuthorIDByAuthorName.get(name
                                    .toLowerCase()));
                }
            }
            ps.executeBatch();
            ps = db.getPreparedStatement(SQLQuery.INSERT_BOOKS_AUTHORS);
            Set<String> dupSet = new HashSet<String>();
            for (String[] value : books_authorsValues) {
                Long remoteBookID = Long.valueOf(value[0]);
                Long remoteAuthorID = Long.valueOf(value[1]);
                if (mapOfRemoteGoodAuthorIDByRemoteBadAuthorID
                        .containsKey(remoteAuthorID)) {
                    // Author entry is marked as 'bad' so replace it with 'good'
                    remoteAuthorID = mapOfRemoteGoodAuthorIDByRemoteBadAuthorID
                            .get(remoteAuthorID);
                }
                Long localBookID = mapOfLocalBookIDByRemoteBookID
                        .get(remoteBookID);
                Long localAuthorID = mapOfLocalAuthorIDByRemoteAuthorID
                        .get(remoteAuthorID);
                if (localBookID == null || localAuthorID == null) {
                    logger.warn("Pair match not found for: (bookID="
                            + remoteBookID + "; authorID=" + remoteAuthorID
                            + ")");
                    continue;
                }
                String pair = localBookID + " " + localAuthorID;
                if (!dupSet.add(pair)) {
                    logger.warn("Duplicate pair:" + pair);
                    continue;
                }
                ps.setInt(2, localBookID.intValue());
                ps.setInt(3, localAuthorID.intValue());
                ps.addBatch();
            }
            ps.executeBatch();
            // import genresnames to tags
            logger.info("Importing libgenrelist to tags start at" + (start=System.currentTimeMillis())+"ms");

            ps = db.getPreparedStatement(SQLQuery.INSERT_TAGS);
            miss=0;
            counter = getLastTagID() + 1;
            for (String[] value : tagsValues) {
                Long remoteTagID = Long.valueOf(value[0]);
                String genre = value[2];
                String meta = value[3];
                String tag = meta + ":" + genre;
                if (setOfTags.add(tag.toLowerCase())) { // new tag value
                    mapOfLocalTagIDByRemoteTagID.put(remoteTagID,
                            Long.valueOf(counter));
                    mapOfLocalTagIDByTagName.put(tag.toLowerCase(),
                            Long.valueOf(counter));
                    ps.setString(2, tag);
                    ps.addBatch();
                    counter++;
                } else { // duplicate
                    logger.debug("Duplicate TAG entry:" + tag.toLowerCase()
                            + " counter: " + counter);
                    mapOfLocalTagIDByRemoteTagID.put(remoteTagID,
                            mapOfLocalTagIDByTagName.get(tag.toLowerCase()));
                    miss++;
                }
            }
            logger.info("Import complete in: " + (System.currentTimeMillis()-start) + "ms; " + miss + " duplicate values bypassed");

            ps.executeBatch();
            // import genres to books_tags
            logger.info("Importing libgenre to books_tags start at" + (start=System.currentTimeMillis())+"ms");
            ps = db.getPreparedStatement(SQLQuery.INSERT_BOOKS_TAGS);
            miss = 0;
            for (String[] value : books_tagsValues) {
                Long remoteBookID = Long.valueOf(value[1]);
                Long remoteTagID = Long.valueOf(value[2]);
                Long localBookID = mapOfLocalBookIDByRemoteBookID
                        .get(remoteBookID);
                Long localTagID = mapOfLocalTagIDByRemoteTagID.get(remoteTagID);
                if (localBookID == null || localTagID == null) {
                    //logger.warn("Pair match not found for: (bookID="
                    //        + remoteBookID + "; tagID=" + remoteTagID + ")");
                    miss++;
                    continue;
                }
                ps.setInt(2, remoteBookID.intValue());
                ps.setInt(3, remoteTagID.intValue());
                ps.addBatch();
            }
            logger.info("Import complete in: " + (System.currentTimeMillis()-start) + "ms; " + miss + " value matches missing");
            ps.executeBatch();
            db.commit();
            db.enableAutoCommit(true);
            logger.debug(getLastAuthorID() + " authors imported");
            logger.debug(getLastBookID() + " books imported");
            logger.debug(getLastTagID() + " tags imported");
        } catch (Exception e) {
            logger.error(e);
        }
    }

    public void mkdb() {
        DBController db = getDB();
        PreparedStatement ps;
        BufferedReader br;
        List<String> parts;
        String value;
        try {
            db.enableAutoCommit(false);
            ps = db.getPreparedStatement(SQLQuery.INSERT_AUTHORS);
            br = getUnicodeBufferedReader(filenames[0]);
            while ((value = br.readLine()) != null) {
                parts = tokenizeMySQLValue(value);
                ps.setInt(1, Integer.valueOf(parts.get(0)));
                ps.setString(2, parts.get(1));
                ps.setString(3, parts.get(2));
                ps.setString(4, parts.get(3));
                ps.addBatch();
            }
            ps.executeBatch();
            ps = db.getPreparedStatement(SQLQuery.INSERT_BOOKS);
            br = getUnicodeBufferedReader(filenames[1]);
            while ((value = br.readLine()) != null) {
                parts = tokenizeMySQLValue(value);
                if (!parts.get(8).equals("0")) { // deleted
                    continue;
                }
                int bookId = Integer.valueOf(parts.get(0));
                String fileType = parts.get(6);
                ps.setInt(1, bookId); // BookID
                ps.setLong(2, Long.valueOf(parts.get(1))); // Filesize
                ps.setString(3, parts.get(2)); // Timestamp
                ps.setString(4, parts.get(3)); // Title
                ps.setString(5, parts.get(4)); // Title1
                ps.setString(6, fileType); // FileType
                ps.setInt(7, Integer.valueOf(parts.get(7))); // Year
                ps.setString(8, parts.get(13)); // md5
                ps.setInt(9, FLIBUSTA_SOURCE_ID); // SourceID
                ps.addBatch();
            }
            ps.executeBatch();
            br = getUnicodeBufferedReader(filenames[2]);
            ps = db.getPreparedStatement(SQLQuery.INSERT_BOOKS_AUTHORS);
            while ((value = br.readLine()) != null) {
                parts = tokenizeMySQLValue(value);
                ps.setInt(1, Integer.valueOf(parts.get(0)));
                ps.setInt(2, Integer.valueOf(parts.get(1)));
                ps.addBatch();
            }
            ps.executeBatch();
            db.commit();
            db.enableAutoCommit(true);
        } catch (Exception e) {
            logger.error(e);
        }
    }

    private static BufferedReader getUnicodeBufferedReader(String filename) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(
                    getRelativeAdaptersPath(getValuesPath(filename))), "UTF-8"));
        } catch (Exception e) {
            logger.error(e);
        }
        return br;
    }

    public FlibustaLibraryAdapter() {
        super("flibusta.db");
        fetchFlibustaDB();
        importFlibustaToSQLiteDB();
        logger.debug("Last BookID=" + getLastBookID());
    }

    @Override
    public String getURL(Book book) {
        return "http://flibusta.net/b/" + book.getId() + "/"
                + book.getFileType();
    }
}
