package org.highscreen.library.datamodel;

import java.util.List;
import java.util.Vector;

public class Author {
	private String id;
	private String name;
	private List<Book> books = new Vector<Book>();

	public Author(String name) {
		this.name = name;
	}

	public void addBook(Book book) {
		books.add(book);
	}

	public List<Book> getBooks() {
		return books;
	}

	public String getName() {
		return name;
	}

}
