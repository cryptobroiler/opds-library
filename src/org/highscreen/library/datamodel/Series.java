package org.highscreen.library.datamodel;


public class Series{
    private String id;
    private String name;
    private String sort;

    public Series(String id, String name, String sort) {
        super();
        this.id = id;
        this.name = name;
        this.sort = sort;
    }

    public String getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSort() {
        return sort;
    }

    public String toString() {
        return getID() + " - " + getName();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Series) {
            return ((Series)obj).getID().equals(getID());
        } else
            return super.equals(obj);
    }

    public String getTitleToSplitByLetter(Object options) {
        return getName();
    }
}
