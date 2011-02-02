package org.highscreen.library.opds;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.highscreen.library.datamodel.Book;
import org.highscreen.library.datamodel.Tag;
import org.jdom.Element;


public class TagsCatalog extends Catalog {
    private List<Element> listOfEntries;
    
    public TagsCatalog(String catalogID, String catalogTitle,
            String catalogSummary, String catalogPath) {
        super(catalogID, catalogTitle, catalogSummary, catalogPath);
        createListOfEntries();
    }

    @Override
    public List<Element> getListOfEntries() {
        return listOfEntries;
    }
    
    private void createListOfEntries() {
        listOfEntries = new ArrayList<Element>();
        Map<Tag, List<Book>> mapOfBooksByTag = getDataModel().getMapOfBooksByTag();
        for (Tag tag: mapOfBooksByTag.keySet()) {
            addSubCatalog(new BooksCatalog("catalog/books/by_tag/"+tag.getID(), "Books tagged as "+ tag.getName(), getSummary(), FilenameUtils.getPath(getPath()) + tag.getID()+ "/books/all.xml", mapOfBooksByTag.get(tag)));
        }   
    }
}
