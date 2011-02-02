package org.highscreen.library.opds;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.highscreen.library.datamodel.Author;
import org.highscreen.library.datamodel.Book;
import org.highscreen.library.datamodel.DataModel;
import org.jdom.Element;

public class MainCatalog extends Catalog {
    private static final Logger logger = Logger.getLogger(MainCatalog.class);

    public MainCatalog(DataModel dm) {
        super("catalog:main", "OPDS Catalog", "no summary",
                "catalog.xml");
        setDataModel(dm);
        Map<Author, List<Book>> mapOfBooksByAuthor = getDataModel()
                .getMapOfBooksByAuthor();
        List<Book> listOfBooks = getDataModel().getListOfBooks();
        addSubCatalog(new BooksCatalog("catalog/all_books", "All books",
                "All books", "catalog/books/all.xml", listOfBooks));
        addSubCatalog(new AuthorsCatalog("catalog/all_authors", "All authors",
                "Books by author", "catalog/authors/all.xml",
                new ArrayList<Author>(mapOfBooksByAuthor.keySet())));
        addSubCatalog(new TagsCatalog("catalog/all_tags", "All tags", "Books by tag", "catalog/tags/all.xml"));
        
    }

    @Override
    public List<Element> getListOfEntries() {
        return new ArrayList<Element>();
    }
}
