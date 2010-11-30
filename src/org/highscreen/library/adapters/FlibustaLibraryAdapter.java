package org.highscreen.library.adapters;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

import jregex.Matcher;
import jregex.Pattern;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ArrayHandler;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.highscreen.library.datamodel.Author;
import org.highscreen.library.datamodel.Book;
import org.highscreen.library.datamodel.Tag;

public class FlibustaLibraryAdapter implements LibraryAdapter {
	private static final Logger logger = Logger
			.getLogger(FlibustaLibraryAdapter.class);

	//private static final String dbUrl = "http://93.174.93.47/sql/";
	// private static final String dbUrl = "http://overlord.local:8000/";

	private static final String dbUrl = "http://abomination.vobis.local/sql/";
	private static final String[] filenames = { "lib.libavtorname.sql",
			"lib.libavtor.sql", "lib.libbook.sql", };
	private static final int FLIBUSTA_SOURCE_ID = 0;
	private static final String CREATE_AUTHORS = "CREATE TABLE `authors` ("
			+ "`AuthorId` integer PRIMARY KEY  AUTOINCREMENT,"
			+ "`FirstName` varchar(99)  NOT NULL DEFAULT '',"
			+ "`MiddleName` varchar(99)  NOT NULL DEFAULT '',"
			+ "`LastName` varchar(99)  NOT NULL DEFAULT '')";
	private static final String CREATE_BOOKS_AUTHORS = "CREATE TABLE `books_authors` (                                         "
			+ "  `BookId` integer NOT NULL DEFAULT '0',                          "
			+ "  `AuthorId` integer NOT NULL DEFAULT '0'                          "
			+ ");                                                                ";
	private static final String CREATE_BOOKS = "CREATE TABLE `books` (  "
			+ "  `BookId` integer PRIMARY KEY AUTOINCREMENT,                     "
			+ "  `FileSize` integer NOT NULL DEFAULT '0',                        "
			+ "  `Time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,            "
			+ "  `Title` varchar(254)  NOT NULL DEFAULT '',                      "
			+ "  `Title1` varchar(254) NOT NULL,                                 "
			+ "  `FileType` char(4) NOT NULL,                                    "
			+ "  `Year` integer NOT NULL DEFAULT '0',                            "
			+ "  `md5` char(32) NOT NULL,                                        "
			+ "  `SourceId` integer NOT NULL DEFAULT '0'   ,                      "
			+ "  `RemoteId` integer  UNIQUE NOT NULL  " + ");";

	protected static void convertToSQLite(File file) throws Exception {
		logger.debug("Extracting values");
		Pattern p = new Pattern(".*INSERT.*");
		Matcher m = p.matcher(FileUtils.readFileToString(file));
		String result = "";
		while (m.find()) {
			logger.trace("Found group");
			result = result + m.toString();
		}
		result = result.replaceAll("\\),\\(", "\n");
		result = result.replaceAll("\\\\'", "");
		result = result.replaceAll("\\);INSERT", "\nINSERT");
		result = result.replaceAll("INSERT INTO `"
				+ file.getName().split("\\.")[1] + "` VALUES \\(", "");
		result = result.replaceAll("\\);$", "");
		File sqliteFile = new File(file.getPath() + "values");
		FileUtils.writeStringToFile(sqliteFile, result);
	}


	protected static void fetchFlibustaDB() throws Exception {
		for (String f : filenames) {
			File file = new File(getRelativePath(f));
			long localTimestamp = file.lastModified();
			logger.debug("Local timestamp of " + f + ":" + localTimestamp);
			URL url = new URL(dbUrl + f + ".gz");
			URLConnection conn = url.openConnection();
			long remoteTimestamp = conn.getHeaderFieldDate("Last-Modified", 0);
			logger.debug("Remote timestamp of " + f + ":" + remoteTimestamp);
			if (remoteTimestamp > 0) {
				if (remoteTimestamp != localTimestamp) {
					logger.debug("Timestamps differ: downloading&ungzipping file "
							+ url.toString());
					InputStream is = url.openStream();
					GZIPInputStream gzipInputStream = new GZIPInputStream(
							new BufferedInputStream(is));

					FileOutputStream fos = new FileOutputStream(file);
					byte[] buffer = new byte[16 * 1024];
					int count;
					while ((count = gzipInputStream.read(buffer)) > 0) {
						fos.write(buffer, 0, count);
					}

					file.setLastModified(remoteTimestamp);
					is.close();
					logger.debug("Extracting values");
					convertToSQLite(file);
				}
			}
		}
	}

	private static String getRelativePath(String filename) {
		String dir = "./adapters/"
				+ FlibustaLibraryAdapter.class.getSimpleName() + "/";
		File path = new File(dir);
		if (!path.exists()) {
			boolean result = path.mkdirs();
			logger.trace("Creating directories: " + result);
		}
		return dir + filename;
	}

	private static List<String> parseValue(String value) {
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

	public static void testDb() throws Exception {
		Connection conn = null;
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection("jdbc:sqlite:flibusta.db");
			QueryRunner runner = new QueryRunner();
			ArrayHandler handler = new ArrayHandler();
			runner.update(conn, "drop table if exists authors;");
			runner.update(conn, "drop table if exists books;");
			runner.update(conn, "drop table if exists tags;");
			runner.update(conn, "drop table if exists books_authors");
			runner.update(conn, CREATE_AUTHORS);

			PreparedStatement prep = conn
					.prepareStatement("insert into authors values (?,?,?,?)");
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(
							getRelativePath(filenames[0] + "values")), "UTF-8"));
			String value = null;
			while ((value = br.readLine()) != null) {
				List<String> parts = parseValue(value);
				// prep.setInt(1,Integer.valueOf(parts[0]));
				prep.setString(2, parts.get(1));
				prep.setString(3, parts.get(2));
				prep.setString(4, parts.get(3));
				prep.addBatch();
			}
			conn.setAutoCommit(false);
			prep.executeBatch();
			conn.commit();

			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					getRelativePath(filenames[2] + "values")), "UTF-8"));
			runner.update(conn, CREATE_BOOKS);
			prep = conn
					.prepareStatement("insert into books values (?,?,?,?,?,?,?,?,?,?)");
			while ((value = br.readLine()) != null) {
				List<String> parts = parseValue(value);
				// logger.debug(parts.get(8));
				if (!parts.get(8).equals("0")) { // deleted
					continue;
				}

				// prep.setInt(1,Integer.valueOf(parts[0]));
				logger.trace(value + ":");
				int count = 0;
				for (String part : parts) {
					logger.trace(count++ + ":  " + part);
				}
				prep.setLong(2, Long.valueOf(parts.get(1))); // Filesize
				prep.setString(3, parts.get(2)); // Timestamp
				prep.setString(4, parts.get(3)); // Title
				prep.setString(5, parts.get(4)); // Title1
				prep.setString(6, parts.get(6)); // FileType
				prep.setInt(7, Integer.valueOf(parts.get(7))); // Year
				prep.setString(8, parts.get(13)); // md5
				prep.setInt(9, FLIBUSTA_SOURCE_ID); // SourceID
				prep.setInt(10, Integer.valueOf(parts.get(0))); // RemoteID
				prep.addBatch();
			}
			conn.setAutoCommit(false);
			prep.executeBatch();
			conn.commit();

			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					getRelativePath(filenames[1] + "values")), "UTF-8"));
			runner.update(conn, CREATE_BOOKS_AUTHORS);
			prep = conn
					.prepareStatement("insert into books_authors values (?,?)");
			while ((value = br.readLine()) != null) {
				List<String> parts = parseValue(value);
				String q = "select bookid from books where remoteid=" + "'"
						+ parts.get(0) + "'";
				Object[] result = (Object[]) runner.query(conn, q, handler);

				if (result != null) {
					if (result.length == 1) {
						// logger.debug(q + " result " + result[0].toString());
						prep.setInt(1, Integer.valueOf(result[0].toString()));
						prep.setInt(2, Integer.valueOf(parts.get(1)));
						prep.addBatch();
					} else {
						throw new Exception("Unexpected query result");
					}
				}
			}
			conn.setAutoCommit(false);
			prep.executeBatch();
			conn.commit();
			// runner.batch(conn, CLEAN_UPDATE,null);

		} catch (Exception e) {
			logger.error(e);
		} finally {
			DbUtils.close(conn);
		}

	}

	

	@Override
	public List<Author> listAuthors() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Book> listBooks() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Tag> listTags() {
		// TODO Auto-generated method stub
		return null;
	}

}
