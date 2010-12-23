package org.highscreen.library.datamodel;

import java.util.List;

public class Author implements Comparable<Author> {
    private String id;
    private String name;
    private String lastName;
    private String sort;

    public Author(String id, String name, String sort) {
        this.id = id;
        this.name = name;
        this.sort = sort;
    }

    public String getName() {
        return name;
    }

    public String getLastName() {
        if (lastName == null) {
            int pos = getSort().lastIndexOf(",");
            if (pos >= 0) {
                lastName = getSort().substring(0, pos);
            }
        }
        return lastName;
    }

    public String toString() {
        return getID() + " - " + getName();
    }

    public String getID() {
        return id;
    }

    public String getSort() {
        return sort;
    }

    @Override
    public int compareTo(Author author) {
        // TODO Auto-generated method stub
        if (author == null) {
            return 1;
        } else {
            return getSort().compareTo(author.getSort());
        }
    }
}
