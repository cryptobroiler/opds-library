package org.highscreen.library.adapters;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// FlibustaLibraryAdapter fla = new FlibustaLibraryAdapter();

		FlibustaLibraryAdapter.fetchFlibustaDB();
		new FlibustaLibraryAdapter().mkdb();
		// FlibustaLibraryAdapter.testExtractSQLQuery();

	}

}
