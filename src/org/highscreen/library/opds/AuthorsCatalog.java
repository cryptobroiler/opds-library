package org.highscreen.library.opds;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.highscreen.library.datamodel.Author;
import org.highscreen.library.datamodel.Book;
import org.highscreen.library.datamodel.DataModel;
import org.jdom.Element;

public class AuthorsCatalog extends Catalog {
    private AuthorsCatalog(String catalogID, String catalogTitle,
            String catalogSummary, String catalogPath, List<Author> authors,
            int catalogLevel) {
        super(catalogID, catalogTitle, catalogSummary, catalogPath);
        createListOfEntries(authors, catalogLevel);
    }
    public AuthorsCatalog(String catalogID, String catalogTitle,
            String catalogSummary, String catalogPath, List<Author> authors)  {
        this(catalogID,catalogTitle,catalogSummary,catalogPath,authors,0);
    }

    private List<Element> listOfEntries;

    @Override
    public List<Element> getListOfEntries() {
        if (listOfEntries == null) {
            listOfEntries = new ArrayList<Element>();
        }
        return listOfEntries;
    }

    private void createListOfEntries(List<Author> authors, int catalogLevel) {
        boolean isSplitNeeded;
        listOfEntries = new ArrayList<Element>();
        isSplitNeeded = authors.size() > 100 && catalogLevel < 1;
        if (isSplitNeeded) {
            Map<String,List<Author>> mapOfAuthorsByLetter = DataModel.getMapOfEntitiesByLetter(authors);
            for(String letter: mapOfAuthorsByLetter.keySet()) {
                List<Author> listOfAuthors = mapOfAuthorsByLetter.get(letter);
                addSubCatalog(new AuthorsCatalog(getID()+"/"+letter, getTitle() + "/" + letter, listOfAuthors.size() + " книг", FilenameUtils.getPath(getPath())+"by_letter/"+Integer.toHexString(Character.codePointAt(letter, 0))+"/all.xml",listOfAuthors,catalogLevel+1));
            }
        } else {
            for (Author a: authors) {
                List<Book> listOfBooks = getDataModel().getMapOfBooksByAuthor().get(a);
                addSubCatalog(new BooksCatalog("catalog/books/by_author/"+a.getID(), "Books by "+ a.getName(), listOfBooks.size() + " книг", FilenameUtils.getPath(getPath()) + a.getID()+ "/books/all.xml", listOfBooks));
                //listOfEntries.add(OPDSFactory.getInstance().createEntry(a));
            }
        }
    }
}
