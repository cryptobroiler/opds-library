package org.highscreen.library.datamodel;

import java.util.List;

import org.highscreen.library.adapters.LibraryAdapter;

public class DataModel {
	private List<Author> listOfAuthors;
	private List<Book> listOfBooks;
	private List<Tag> listOfTags;

	private LibraryAdapter adapter;

	public DataModel(LibraryAdapter adapter) {
		this.adapter = adapter;
		clear();
	}

	public void clear() {
		listOfAuthors = null;
		listOfBooks = null;
		listOfTags = null;
	}

	public List<Book> getListOfBooks() {
		if (listOfBooks == null) {
			listOfBooks = adapter.getListOfBooks();
		}
		return listOfBooks;
	}

	public List<Author> getListOfAuthors() {
		if (listOfAuthors == null) {
			listOfAuthors = adapter.getListOfAuthors();
		}
		return listOfAuthors;
	}

	public List<Tag> getListOfTags() {
		if (listOfTags == null) {
			listOfTags = adapter.getListOfTags();
		}
		return listOfTags;
	}
}
