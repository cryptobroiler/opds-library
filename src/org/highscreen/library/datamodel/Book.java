package org.highscreen.library.datamodel;

import java.util.Date;
import java.util.List;
import java.util.Vector;

public class Book {
	private List<Author> authors;
	private String id;
	private String title;
	private String link;
	private Date pubDate;
	private Date timeStamp;
	private String summary;
	private String source;
	
	public Book(String id, String title, String link, Date timeStamp, Date pubDate, String summary, List<Author> authors, String source) {
		this.id = id;
		this.title = title;
		this.link = link;
		this.timeStamp = timeStamp;
		this.pubDate = pubDate;
		this.summary = summary;
		this.authors = authors;
		this.source = source;
	}
	
	public void addAuthor(Author a) {
		authors.add(a);
	}
}
