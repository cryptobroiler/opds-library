package org.highscreen.library.datamodel;

import java.util.List;

import org.highscreen.library.adapters.LibraryAdapter;

public class DataModel {
	private List<Author> authors;
	private List<Book> books;
	private List<Tag> tags;

	private LibraryAdapter adapter;

	public DataModel(LibraryAdapter adapter) {
		clear();
		this.adapter = adapter;
	}

	public void clear() {
		authors = null;
		books = null;
		tags = null;
	}

	public List<Book> getListOfBooks() {
		if (books == null) {
			books = adapter.listBooks();
		}
		return books;
	}

	public List<Author> getListOfAuthors() {
		if (authors == null) {
			authors = adapter.listAuthors();
		}
		return authors;
	}

	public List<Tag> getListOfTags() {
		if (tags == null) {
			tags = adapter.listTags();
		}
		return tags;
	}
}
