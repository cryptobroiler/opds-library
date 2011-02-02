package org.highscreen.library.datamodel;

public class Tag extends AbstractEntity {
    private String name;



    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Tag(String id, String name) {
        super(id);
        this.name = name;
    }

    @Override
    public String toString() {
        return getID() + " - " + getName();
    }

    @Override
    public String getFieldToSplit() {
        return getName();
    }
    
    @Override
    public String getFieldToCompare() {
        return getName();
    }
}
