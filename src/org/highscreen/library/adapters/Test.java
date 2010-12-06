package org.highscreen.library.adapters;

import java.util.List;

import org.apache.log4j.Logger;
import org.highscreen.library.datamodel.Book;
import org.highscreen.library.datamodel.DataModel;

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
		DataModel dm = new DataModel(new FlibustaLibraryAdapter());
//		List<Book> books = dm.getListOfBooks();
//		for (int i = 0; i < 100; i++) {
//			Book b = books.get(i);
//			logger.debug(b);
//		}
		//FlibustaLibraryAdapter.testNewMySQLExtract();
		// FlibustaLibraryAdapter.testExtractSQLQuery();

	}
}
