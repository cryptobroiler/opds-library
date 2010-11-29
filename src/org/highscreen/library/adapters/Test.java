package org.highscreen.library.adapters;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//FlibustaLibraryAdapter fla = new FlibustaLibraryAdapter();
		try {
			FlibustaLibraryAdapter.fetchFlibustaDB();
			FlibustaLibraryAdapter.testDb();
			//FlibustaLibraryAdapter.testExtractSQLQuery();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
