package org.highscreen.library.adapters;

import java.util.List;

import org.apache.log4j.Logger;
import org.highscreen.library.datamodel.Book;

public class Test {
	private static Logger logger = Logger.getLogger(Test.class);
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// FlibustaLibraryAdapter fla = new FlibustaLibraryAdapter();

		//FlibustaLibraryAdapter.fetchFlibustaDB();
		//new FlibustaLibraryAdapter().mkdb();
		FlibustaLibraryAdapter fla = new FlibustaLibraryAdapter();
		List<Book> books = fla.listBooks();
		for (int i = 0; i < 100; i++) {
			Book b = books.get(i);
			logger.debug(b);
		}
		// FlibustaLibraryAdapter.testExtractSQLQuery();

	}

}
