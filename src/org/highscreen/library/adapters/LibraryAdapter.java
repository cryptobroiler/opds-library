package org.highscreen.library.adapters;

import java.util.List;

import org.highscreen.library.datamodel.Author;
import org.highscreen.library.datamodel.Book;
import org.highscreen.library.datamodel.Tag;

public interface LibraryAdapter {
	public List<Book> listBooks();
	public List<Author> listAuthors();
	public List<Tag> listTags();
}
