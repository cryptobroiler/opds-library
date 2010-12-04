package org.highscreen.library.adapters;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

import jregex.Matcher;
import jregex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.highscreen.library.database.DBController;
import org.highscreen.library.database.SQLQuery;
import org.highscreen.library.datamodel.Book;

public class FlibustaLibraryAdapter extends SQLiteLibraryAdapter {
	private static final Logger logger = Logger
			.getLogger(FlibustaLibraryAdapter.class);

	private static final String dbUrl = "http://93.174.93.47/sql/";
	// private static final String dbUrl = "http://overlord.local:8000/";

	// private static final String dbUrl =
	// "http://abomination.vobis.local/sql/";
	private static final String[] filenames = { "lib.libavtorname.sql",
			"lib.libavtor.sql", "lib.libbook.sql", };
	private static final int FLIBUSTA_SOURCE_ID = 0;

	protected static String getValuesPath(File file) {
		return file.getPath() + ".values";
	}

	protected static String getValuesPath(String filename) {
		return filename + ".values";
	}

	protected static void extractMySQLValues(File file) throws Exception {
		logger.debug("Extracting values");
		Pattern p = new Pattern(".*INSERT.*");
		Matcher m = p.matcher(FileUtils.readFileToString(file));
		String result = "";
		while (m.find()) {
			result = result + m.toString();
		}
		result = result.replaceAll("\\),\\(", "\n");
		result = result.replaceAll("\\\\'", "");
		result = result.replaceAll("\\);INSERT", "\nINSERT");
		result = result.replaceAll("INSERT INTO `"
				+ file.getName().split("\\.")[1] + "` VALUES \\(", "");
		result = result.replaceAll("\\);$", "");
		File sqliteFile = new File(getValuesPath(file));
		FileUtils.writeStringToFile(sqliteFile, result);
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

						FileOutputStream fos = new FileOutputStream(file);
						byte[] buffer = new byte[16 * 1024];
						int count;
						while ((count = gzipInputStream.read(buffer)) > 0) {
							fos.write(buffer, 0, count);
						}

						file.setLastModified(remoteTimestamp);
						is.close();
						extractMySQLValues(file);
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
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

	public void mkdb() {
		DBController db = getDB();
		PreparedStatement ps;
		BufferedReader br;
		List<String> parts;
		String value;
		try {
			db.enableAutoCommit(false);
			ps = db.getPreparedStatement(SQLQuery.INSERT_AUTHORS);
			br = getBufferedReader(filenames[0]);
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
			br = getBufferedReader(filenames[2]);
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
			br = getBufferedReader(filenames[1]);
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

	private static BufferedReader getBufferedReader(String filename) {
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
		mkdb();
	}

	@Override
	public String getURL(Book book) {
		return "http://flibusta.net/b/" + book.getId() + "/"
				+ book.getFileType();
	}

}
