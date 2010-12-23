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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private Map<Integer, Integer> mapOfLocalBookIDByRemoteBookID = new HashMap<Integer, Integer>();
    private Map<Integer, Integer> mapOfLocalAuthorIDByRemoteAuthorID = new HashMap<Integer, Integer>();
    private Map<String, Integer> mapOfLocalAuthorIDByAuthorName = new HashMap<String, Integer>();
    private Map<Integer, Integer> mapOfRemoteGoodAuthorIDByRemoteBadAuthorID = new HashMap<Integer, Integer>();
    private Map<Integer, Integer> mapOfRemoteGoodBookIDByRemoteBadBookID = new HashMap<Integer, Integer>();
    private Map<Integer, Integer> mapOfLocalTagIDByRemoteTagID = new HashMap<Integer, Integer>();
    private Map<String, Integer> mapOfLocalSeriesIDBySeriesName = new HashMap<String, Integer>();
    private Map<Integer, Integer> mapOfLocalSeriesIDByRemoteSeriesID = new HashMap<Integer, Integer>();
    private Map<String, Integer> mapOfLocalTagIDByTagName = new HashMap<String, Integer>();
    private static final String dbUrl = "http://93.174.93.47/sql/";
    private static final String[] filenames = { "lib.libavtorname.sql",
            "lib.libbook.sql", "lib.libavtor.sql", "lib.libgenrelist.sql",
            "lib.libgenre.sql", "lib.libavtoraliase.sql",
            "lib.libjoinedbooks.sql", "lib.libseqname.sql", "lib.libseq.sql",
            "lib.libtranslator.sql", "lib.libfilename.sql", "lib.librate.sql",
            "lib.libsrclang.sql", };// "lib.b.annotations.sql",
    // "lib.b.annotations_pics.sql" };
    private static final int FLIBUSTA_SOURCE_ID = 0;

    void readMapOfRemoteGoodAuthorIDByRemoteBadAuthorID() {
        List<String[]> values = readValuesFromFile(filenames[5]);
        for (String[] value : values) {
            mapOfRemoteGoodAuthorIDByRemoteBadAuthorID.put(
                    Integer.valueOf(value[1]), Integer.valueOf(value[2]));
        }
    }

    void readMapOfRemoteGoodBookIDByRemoteBadBookID() {
        List<String[]> values = readValuesFromFile(filenames[6]);
        for (String[] value : values) {
            mapOfRemoteGoodBookIDByRemoteBadBookID.put(
                    Integer.valueOf(value[2]), Integer.valueOf(value[3]));
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


    protected void fetchFlibustaDB() {
        try {
            for (String f : filenames) {
                File file = new File(getPathToAdapter(f));
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

    private static List<String> tokenizeMySQLValue(String value) {
        List<String> result = new ArrayList<String>();
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

    private List<String[]> readValuesFromFile(String filename) {
        List<String[]> values = null;
        try {
            values = new ArrayList<String[]>();
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

    public int getLastBookID() {
        int result = 0;
        try {
            ResultSet rs = getDB().getPreparedStatement(
                    SQLQuery.SELECT_BOOK_COUNT).executeQuery();
            while (rs.next()) {
                result = rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error(e);
        }
        return result;
    }

    public int getLastAuthorID() {
        int result = 0;
        try {
            ResultSet rs = getDB().getPreparedStatement(
                    SQLQuery.SELECT_AUTHOR_COUNT).executeQuery();
            while (rs.next()) {
                result = rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error(e);
        }
        return result;
    }

    public int getLastSeriesID() {
        int result = 0;
        try {
            ResultSet rs = getDB().getPreparedStatement(
                    SQLQuery.SELECT_SERIES_COUNT).executeQuery();
            while (rs.next()) {
                result = rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error(e);
        }
        return result;
    }

    public int getLastTagID() {
        int result = 0;
        try {
            ResultSet rs = getDB().getPreparedStatement(
                    SQLQuery.SELECT_TAG_COUNT).executeQuery();
            while (rs.next()) {
                result = rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error(e);
        }
        return result;
    }

    public void importFlibustaToSQLiteDB() {
        List<String[]> authorsValues = readValuesFromFile(filenames[0]);
        readMapOfRemoteGoodAuthorIDByRemoteBadAuthorID();
        readMapOfRemoteGoodBookIDByRemoteBadBookID();
        List<String[]> booksValues = readValuesFromFile(filenames[1]);
        List<String[]> books_authorsValues = readValuesFromFile(filenames[2]);
        List<String[]> tagsValues = readValuesFromFile(filenames[3]);
        List<String[]> books_tagsValues = readValuesFromFile(filenames[4]);
        List<String[]> seriesValues = readValuesFromFile(filenames[7]);
        List<String[]> books_seriesValues = readValuesFromFile(filenames[8]);
        Map<Integer, Map<String, String>> localBooksValues = new HashMap<Integer, Map<String, String>>();
        Map<Integer, Map<String, String>> localAuthorsValues = new HashMap<Integer, Map<String, String>>();
        Map<Integer, Map<String, String>> localBooksAuthorsValues = new HashMap<Integer, Map<String, String>>();
        Map<Integer, Map<String, String>> localTagsValues = new HashMap<Integer, Map<String, String>>();
        Map<Integer, Map<String, String>> localBooksTagsValues = new HashMap<Integer, Map<String, String>>();
        Map<Integer, Map<String, String>> localSeriesValues = new HashMap<Integer, Map<String, String>>();
        Map<Integer, Map<String, String>> localBooksSeriesValues = new HashMap<Integer, Map<String, String>>();
        long start = 0;
        int miss = 0, dups = 0, bads = 0, key = 0;
        try {
            start = logImportPrologue("libbook", "book");
            key = getLastBookID() + 1;
            for (String[] value : booksValues) {
                if (!value[8].equals("0")) {
                    bads++;
                    continue;
                }
                Integer remoteBookID = Integer.valueOf(value[0]);
                if (mapOfRemoteGoodBookIDByRemoteBadBookID
                        .containsKey(remoteBookID)) {
                    // Book entry is marked as 'bad' so skip it
                    bads++;
                    continue;
                }
                String fileType = value[6];
                String title = value[3];
                if (!value[4].isEmpty()) {
                    title += " [" + value[4] + "]";
                }
                String timestamp = value[2];
                String uri = "http://flibusta.net/b/" + remoteBookID + "/"
                        + fileType;
                mapOfLocalBookIDByRemoteBookID.put(remoteBookID,
                        Integer.valueOf(key));
                Map<String, String> valuesMap = localBooksValues.get(key);
                if (localBooksValues.get(key) == null) {
                    valuesMap = new HashMap<String, String>();
                    localBooksValues.put(key, valuesMap);
                }
                valuesMap.put("id", String.valueOf(key));
                valuesMap.put("title", title);
                valuesMap.put("sort", title);
                valuesMap.put("timestamp", timestamp);
                // valuesMap.put("uri", uri);
                valuesMap.put("series_index", "1");
                valuesMap.put("isbn", "");
                valuesMap.put("lccn", "");
                valuesMap.put("path", uri);
                key++;
            }
            logImportEpilogue(start, miss, dups, bads, key);
            start = logImportPrologue("libavtorname", "authors");
            key = getLastAuthorID() + 1;
            bads = dups = miss = 0;
            for (String[] value : authorsValues) {
                Integer remoteAuthorID = Integer.valueOf(value[0]);
                Integer localAuthorID = Integer.valueOf(key);
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
                        .containsKey(remoteAuthorID)) { // Author entry is
                                                        // marked as `bad`
                    bads++;
                    continue;
                }
                if (!mapOfLocalAuthorIDByAuthorName.containsKey(name
                        .toLowerCase())) { // new
                    mapOfLocalAuthorIDByRemoteAuthorID.put(remoteAuthorID,
                            localAuthorID);
                    mapOfLocalAuthorIDByAuthorName.put(name.toLowerCase(),
                            localAuthorID);
                    Map<String, String> valuesMap = localAuthorsValues.get(key);
                    if (valuesMap == null) {
                        valuesMap = new HashMap<String, String>();
                        localAuthorsValues.put(key, valuesMap);
                    }
                    valuesMap.put("id", String.valueOf(key));
                    valuesMap.put("name", name);
                    valuesMap.put("sort", sort);
                    key++;
                } else {
                    // logger.debug("Duplicate entry: " + name.toLowerCase()
                    // + " counter:" + key);
                    mapOfLocalAuthorIDByRemoteAuthorID.put(remoteAuthorID,
                            mapOfLocalAuthorIDByAuthorName.get(name
                                    .toLowerCase()));
                    dups++;
                }
            }
            logImportEpilogue(start, miss, dups, bads, key);
            start = logImportPrologue("libavtor", "books_author");
            Set<String> dupSet = new HashSet<String>();
            dups = miss = bads = 0;
            key = 1;
            for (String[] value : books_authorsValues) {
                Integer remoteBookID = Integer.valueOf(value[0]);
                Integer remoteAuthorID = Integer.valueOf(value[1]);
                if (mapOfRemoteGoodAuthorIDByRemoteBadAuthorID
                        .containsKey(remoteAuthorID)) {
                    // Author entry is marked as 'bad' so replace it with 'good'
                    remoteAuthorID = mapOfRemoteGoodAuthorIDByRemoteBadAuthorID
                            .get(remoteAuthorID);
                    bads++;
                }
                if (mapOfRemoteGoodBookIDByRemoteBadBookID
                        .containsKey(remoteBookID)) {
                    // Book entry is marked as 'bad' so replace it with 'good'
                    remoteBookID = mapOfRemoteGoodBookIDByRemoteBadBookID
                            .get(remoteBookID);
                    bads++;
                }
                Integer localBookID = mapOfLocalBookIDByRemoteBookID
                        .get(remoteBookID);
                Integer localAuthorID = mapOfLocalAuthorIDByRemoteAuthorID
                        .get(remoteAuthorID);
                if (localBookID == null || localAuthorID == null) {
                    miss++;
                    continue;
                }
                String pair = localBookID + " " + localAuthorID;
                if (!dupSet.add(pair)) {
                    // logger.warn("Duplicate pair:" + pair);
                    dups++;
                    continue;
                }
                Map<String, String> valuesMap = localBooksAuthorsValues
                        .get(key);
                if (valuesMap == null) {
                    valuesMap = new HashMap<String, String>();
                    localBooksAuthorsValues.put(key, valuesMap);
                }
                valuesMap.put("id", String.valueOf(key));
                valuesMap.put("book", String.valueOf(localBookID));
                valuesMap.put("author", String.valueOf(localAuthorID));
                Map<String, String> bookValue = localBooksValues
                        .get(localBookID);
                if (bookValue != null) {
                    if (!bookValue.containsKey("author_sort")) {
                        Map<String, String> authorValue = localAuthorsValues
                                .get(localAuthorID);
                        if (authorValue != null) {
                            if (authorValue.containsKey("sort")) {
                                bookValue.put("author_sort",
                                        authorValue.get("sort"));
                            }
                        }
                    }
                }
                key++;
            }
            logImportEpilogue(start, miss, dups, bads, key);
            // import genresnames to tags
            start = logImportPrologue("libgenrelist", "tags");
            miss = dups = bads = 0;
            key = getLastTagID() + 1;
            for (String[] value : tagsValues) {
                Integer remoteTagID = Integer.valueOf(value[0]);
                String genre = value[2];
                String meta = value[3];
                String tag = meta + ":" + genre;
                if (!mapOfLocalTagIDByTagName.containsKey(tag.toLowerCase())) { // new
                                                                                // tag
                                                                                // value
                    mapOfLocalTagIDByRemoteTagID.put(remoteTagID,
                            Integer.valueOf(key));
                    mapOfLocalTagIDByTagName.put(tag.toLowerCase(),
                            Integer.valueOf(key));
                    Map<String, String> valuesMap = localTagsValues.get(key);
                    if (valuesMap == null) {
                        valuesMap = new HashMap<String, String>();
                        localTagsValues.put(key, valuesMap);
                    }
                    valuesMap.put("id", String.valueOf(key));
                    valuesMap.put("name", tag);
                    key++;
                } else { // duplicate
                    // logger.debug("Duplicate TAG entry:" + tag.toLowerCase()
                    // + " counter: " + key);
                    mapOfLocalTagIDByRemoteTagID.put(remoteTagID,
                            mapOfLocalTagIDByTagName.get(tag.toLowerCase()));
                    dups++;
                }
            }
            logImportEpilogue(start, miss, dups, bads, key);
            // import genres to books_tags
            start = logImportPrologue("libgenre", "books_tags");
            key = 1;
            miss = bads = dups = 0;
            dupSet.clear();
            for (String[] value : books_tagsValues) {
                Integer remoteBookID = Integer.valueOf(value[1]);
                Integer remoteTagID = Integer.valueOf(value[2]);
                if (mapOfRemoteGoodBookIDByRemoteBadBookID
                        .containsKey(remoteBookID)) {
                    // Book entry is marked as 'bad' so replace it with 'good'
                    remoteBookID = mapOfRemoteGoodBookIDByRemoteBadBookID
                            .get(remoteBookID);
                    bads++;
                }
                Integer localBookID = mapOfLocalBookIDByRemoteBookID
                        .get(remoteBookID);
                Integer localTagID = mapOfLocalTagIDByRemoteTagID
                        .get(remoteTagID);
                if (localBookID == null || localTagID == null) {
                    // logger.warn("Pair match not found for: (bookID="
                    // + remoteBookID + "; tagID=" + remoteTagID + ")");
                    miss++;
                    continue;
                }
                if (!dupSet.add(localBookID + " " + localTagID)) {
                    dups++;
                } else {
                    Map<String, String> valuesMap = localBooksTagsValues
                            .get(key);
                    if (valuesMap == null) {
                        valuesMap = new HashMap<String, String>();
                        localBooksTagsValues.put(key, valuesMap);
                    }
                    valuesMap.put("id", String.valueOf(key));
                    valuesMap.put("book", String.valueOf(localBookID));
                    valuesMap.put("tag", String.valueOf(localTagID));
                    key++;
                }
            }
            logImportEpilogue(start, miss, dups, bads, key);
            start = logImportPrologue("libseqname", "series");
            miss = dups = bads = 0;
            key = getLastSeriesID() + 1;
            for (String[] value : seriesValues) {
                Integer remoteSeriesID = Integer.valueOf(value[0]);
                Integer localSeriesID = Integer.valueOf(key);
                String name = value[1];
                String sort = value[1];
                name = name.trim();
                sort = sort.trim();
                if (!mapOfLocalSeriesIDBySeriesName.containsKey(name
                        .toLowerCase())) { // new
                    mapOfLocalSeriesIDByRemoteSeriesID.put(remoteSeriesID,
                            localSeriesID);
                    mapOfLocalSeriesIDBySeriesName.put(name.toLowerCase(),
                            localSeriesID);
                    Map<String, String> valuesMap = localSeriesValues.get(key);
                    if (valuesMap == null) {
                        valuesMap = new HashMap<String, String>();
                    }
                    valuesMap.put("id", String.valueOf(key));
                    valuesMap.put("name", name);
                    valuesMap.put("sort", sort);
                    localSeriesValues.put(key, valuesMap);
                    key++;
                } else {
                    // logger.debug("Duplicate entry: " + name.toLowerCase()
                    // + " counter:" + key);
                    mapOfLocalSeriesIDByRemoteSeriesID.put(remoteSeriesID,
                            mapOfLocalSeriesIDBySeriesName.get(name
                                    .toLowerCase()));
                    dups++;
                }
            }
            logImportEpilogue(start, miss, dups, bads, key);
            start = logImportPrologue("libseq", "books_series");
            key = 0;
            miss = 0;
            bads = 0;
            dups = 0;
            dupSet.clear();
            for (String[] value : books_seriesValues) {
                Integer remoteBookID = Integer.valueOf(value[0]);
                Integer remoteSeriesID = Integer.valueOf(value[1]);
                Integer remoteSeriesIndex = Integer.valueOf(value[2]);
                if (mapOfRemoteGoodBookIDByRemoteBadBookID
                        .containsKey(remoteBookID)) {
                    // Book entry is marked as 'bad' so replace it with 'good'
                    remoteBookID = mapOfRemoteGoodBookIDByRemoteBadBookID
                            .get(remoteBookID);
                    bads++;
                }
                Integer localBookID = mapOfLocalBookIDByRemoteBookID
                        .get(remoteBookID);
                Integer localSeriesID = mapOfLocalSeriesIDByRemoteSeriesID
                        .get(remoteSeriesID);
                if (localBookID == null || localSeriesID == null) {
                    // logger.warn("Pair match not found for: (bookID="
                    // + remoteBookID + "; tagID=" + remoteTagID + ")");
                    miss++;
                    continue;
                }
                if (!dupSet.add(localBookID + " " + localSeriesID)) {
                    dups++;
                } else {
                    Map<String, String> valuesMap = localBooksSeriesValues
                            .get(key);
                    if (valuesMap == null) {
                        valuesMap = new HashMap<String, String>();
                    }
                    valuesMap.put("id", String.valueOf(key));
                    valuesMap.put("book", String.valueOf(localBookID));
                    valuesMap.put("series", String.valueOf(localSeriesID));
                    localBooksSeriesValues.put(key, valuesMap);
                    Map<String, String> bookValue = localBooksValues
                            .get(localBookID);
                    if (bookValue != null) {
                        if (bookValue.containsKey("series_index")) {
                            bookValue.put("series_index",
                                    String.valueOf(remoteSeriesIndex));
                        }
                    }
                    key++;
                }
            }
            logImportEpilogue(start, miss, dups, bads, key);
            getDB().importValuesMapIntoTable(localBooksValues, "books");
            getDB().importValuesMapIntoTable(localAuthorsValues, "authors");
            getDB().importValuesMapIntoTable(localTagsValues, "tags");
            getDB().importValuesMapIntoTable(localSeriesValues, "series");
            getDB().importValuesMapIntoTable(localBooksAuthorsValues,
                    "books_authors");
            getDB().importValuesMapIntoTable(localBooksTagsValues, "books_tags");
            getDB().importValuesMapIntoTable(localBooksSeriesValues,
                    "books_series");
            logger.debug(Integer.MAX_VALUE);
        } catch (Exception e) {
            logger.error(e);
        }
    }

    static private long logImportPrologue(String origin, String dest) {
        long start;
        logger.info("Importing `" + origin + "` to `" + dest + "`: start at "
                + (start = System.currentTimeMillis()) + "ms");
        return start;
    }

    static private void logImportEpilogue(long start, long miss, long dups,
            long bads, long key) {
        logger.info("Import complete in: "
                + (System.currentTimeMillis() - start) + "ms; " + (key - 1)
                + " entries imported" + "\n\tduplicates rate(count): "
                + (((float) dups / (key + dups - 1)) * 100) + "%(" + dups + ")"
                + "\n\tbad entries replace rate(count): "
                + (((float) bads / (key - 1)) * 100) + "%(" + bads + ")"
                + "\n\tmissed pairs rate(count): "
                + (((float) miss / (key + miss - 1)) * 100) + "%(" + miss + ")");
    }

    private BufferedReader getUnicodeBufferedReader(String filename) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(
                    getPathToAdapter(getValuesPath(filename))), "UTF-8"));
        } catch (Exception e) {
            logger.error(e);
        }
        return br;
    }

    public FlibustaLibraryAdapter() {
        super("flibusta.db");
        if (isUpdateNeeded()) {
            fetchFlibustaDB();
            importFlibustaToSQLiteDB();
        }
        logger.debug("Last BookID=" + getLastBookID());
    }

    @Override
    public String getURL(Book book) {
        return "http://flibusta.net/b/" + book.getID() + "/"
                + book.getFileType();
    }

    @Override
    public boolean isUpdateNeeded() {
        return false;
    }
}
