package org.highscreen.library.datamodel;

import java.util.Date;
import java.util.List;
import java.util.Vector;

public class Book {
	private List<Author> authors = new Vector<Author>();
	private String fileType;
	private String id;

	private String source;

	private String summary;

	private List<Tag> tags = new Vector<Tag>();

	private String timeStamp;

	private String title;

	private String uri;
	public Book(String id, String title, String timeStamp, String summary,
			String fileType, String source) {
		this.id = id;
		this.title = title;
		this.timeStamp = timeStamp;
		this.summary = summary;
		this.source = source;
		this.fileType = fileType;
	}
	public void addAuthor(Author a) {
		authors.add(a);
	}
	public void addTag(Tag e) {
		tags.add(e);
	}
	public List<Author> getAuthors() {
		return authors;
	}

	public String getFileType() {
		return fileType;
	}

	public String getId() {
		return id;
	}

	public String getUri() {
		return uri;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String toString() {
		return id + " " + title + " " + uri + " " + timeStamp + " " + summary
				+ " Written by: " + authors + " " + source + " " + uri;
	}
}
