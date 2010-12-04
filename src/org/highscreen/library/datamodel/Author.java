package org.highscreen.library.datamodel;

import java.util.List;

public class Author {
	private String id;
	private String firstName;
	private String lastName;
	private String middleName;

	public Author(String id, String firstName, String middleName,
			String lastName) {
		this.id = id;
		this.firstName = firstName;
		this.middleName = middleName;
		this.lastName = lastName;
	}

	public String getName() {
		return firstName;
	}

	public String toString() {
		return id + ": " + firstName + " " + middleName + " " + lastName;
	}

	public String getId() {
		return id;
	}

}
