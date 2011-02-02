package org.highscreen.library.datamodel;

public abstract class AbstractEntity implements Comparable<AbstractEntity> {
    private String id;

    public String getID() {
        return id;
    }

    public void setID(String id) {
        this.id = id;
    }

    public AbstractEntity(String id) {
        this.id = id;
    }

    public abstract String getFieldToSplit();

    public abstract String getFieldToCompare();

    @Override
    public int compareTo(AbstractEntity entity) {
        // TODO Auto-generated method stub
        if (entity == null) {
            return 1;
        } else {
            return getFieldToCompare().compareTo(entity.getFieldToCompare());
        }
    }
}
