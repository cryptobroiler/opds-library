package org.highscreen.library.adapters;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.highscreen.library.adapters.filters.ReplacingRegexFilter;
import org.highscreen.library.datamodel.Author;
import org.highscreen.library.datamodel.Book;
import org.highscreen.library.datamodel.Tag;

public class FlibustaLibraryAdapter implements LibraryAdapter {
	private static final Logger logger = Logger
			.getLogger(FlibustaLibraryAdapter.class);

	private static final String dbUrl = "http://93.174.93.47/sql/";
	private static final String[] filenames = { "lib.libavtorname.sql",
			"lib.libavtor.sql", "lib.libbook.sql", };

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

	protected static void fetchFlibustaDB() throws Exception {

		for (String f : filenames) {

			File file = new File(getRelativePath(f));
			long localTimestamp = file.lastModified();
			logger.trace("Local timestamp of " + f + ":" + localTimestamp);
			URL url = new URL(dbUrl + f + ".gz");
			URLConnection conn = url.openConnection();
			long remoteTimestamp = conn.getHeaderFieldDate("Last-Modified", 0);
			logger.trace("Remote timestamp of " + f + ":" + remoteTimestamp);
			if (remoteTimestamp > 0) {
				if (remoteTimestamp != localTimestamp) {
					logger.trace("Timestamps differ: downloading file "
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
					convertToSQLite(file);
				}
			}
		}
	}

	protected static String extractSQLQuery(FileInputStream fis)
			throws Exception {
		GZIPInputStream gzipInputStream = new GZIPInputStream(
				new BufferedInputStream(fis));
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buf = new byte[16 * 1024];
		int count;
		while ((count = gzipInputStream.read(buf)) > 0) {
			baos.write(buf, 0, count);
		}
		// logger.debug(sb.toString());
		return baos.toString("UTF-8");
	}

	protected static void convertToSQLite(File file) throws Exception {
		Pattern p = Pattern.compile(".*INSERT.*", Pattern.MULTILINE);
		Matcher m = p.matcher(FileUtils.readFileToString(file));
		String result = "";
		while (m.find()) {
			result += m.group();
		}

		// logger.debug(result);
		ReplacingRegexFilter splitInsert = new ReplacingRegexFilter(
				"\\)\\s*,\\s*\\(", ");\ninsert into "
						+ file.getName().split("\\.")[1] + " values (");
		ReplacingRegexFilter removeSlashes = new ReplacingRegexFilter("\\\\'",
				"");
		File sqliteFile = new File(file.getPath() + "ite3");
		FileUtils.writeStringToFile(sqliteFile,
				"BEGIN TRANSACTION;\n"+removeSlashes.process(splitInsert.process(result))+"COMMIT;\n");

	}

	protected static void testExtractSQLQuery() {
		try {
			for (String f : filenames) {
				// logger.trace(extractSQLQuery(new FileInputStream(f)));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<Book> listBooks() {
		// TODO Auto-generated method stub
		return null;
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
