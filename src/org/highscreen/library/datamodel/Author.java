package org.highscreen.library.datamodel;


public class Author extends AbstractEntity{
    
    private String name;
    private String lastName;
    private String sort;

    public Author(String id, String name, String sort) {
        super(id);
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



    public String getSort() {
        return sort;
    }



    @Override
    public String getFieldToSplit() {
        return getSort();
    }

    @Override
    public String getFieldToCompare() {
        return getSort();
    }
    
   
}
