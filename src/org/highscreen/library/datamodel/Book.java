package org.highscreen.library.datamodel;

import java.util.Date;
import java.util.List;
import java.util.Vector;

public class Book {
	private List<Author> authors;
	private String id;
	private String title;
	private String link;
	private String timeStamp;
	private String summary;
	private String source;
	
	public Book(String id, String title, String link, String timeStamp, String summary, List<Author> authors, String source) {
		this.id = id;
		this.title = title;
		this.link = link;
		this.timeStamp = timeStamp;
		this.summary = summary;
		this.authors = authors;
		this.source = source;
	}
	
	public void addAuthor(Author a) {
		authors.add(a);
	}
	
	public String toString(){
		return id + " " + title + " " + link + " " + timeStamp + " " + summary + " Written by: " + authors + " " + source;
	}
}
