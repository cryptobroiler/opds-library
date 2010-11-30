package org.highscreen.library.datamodel;

import java.util.List;

public class Author {
	private String id;
	private String firstName;
	private String lastName;
	private String middleName;
	private List<Book> books;

	public Author(String id, String firstName, String middleName,
			String lastName, List<Book> books) {
		this.id = id;
		this.firstName = firstName;
		this.middleName = middleName;
		this.lastName = lastName;
		this.books = books;
	}

	public void addBook(Book book) {
		books.add(book);
	}

	public List<Book> getBooks() {
		return books;
	}

	public String getName() {
		return firstName;
	}

	public String toString() {
		return id + ": " + firstName + " " + middleName + " " + lastName;
	}

}
