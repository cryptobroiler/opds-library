package org.highscreen.library.datamodel;

public class Tag {
    private String id;
    private String name;

    public void setID(String id) {
        this.id = id;
    }

    public String getID() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Tag(String id, String name) {
        this.id = id;
        this.name = name;
    }
    
    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return getID() + " - " + getName();
    }
    
}
