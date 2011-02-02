package org.highscreen.library.opds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.highscreen.library.datamodel.Book;
import org.highscreen.library.datamodel.DataModel;
import org.jdom.Element;

public class BooksCatalog extends Catalog {
    private static final Logger logger = Logger.getLogger(BooksCatalog.class);
    private List<Element> listOfEntries;

    private BooksCatalog(String catalogID, String catalogTitle,
            String catalogSummary, String catalogPath, List<Book> books,
            int catalogLevel) {
        super(catalogID, catalogTitle, catalogSummary, catalogPath);
        createListOfEntries(books, catalogLevel);
        if (catalogLevel != 0) {
            setOpenSearchTotalResults(books.size());
            setOpenSearchItemsPerPage(getPaginationThreshold());
        }
    }

    public BooksCatalog(String catalogID, String catalogTitle,
            String catalogSummary, String catalogPath, List<Book> books) {
        this(catalogID, catalogTitle, catalogSummary, catalogPath, books, 0);
    }

    @Override
    public List<Element> getListOfEntries() {
        if (listOfEntries == null) {
            listOfEntries = new ArrayList<Element>();
        }
        return listOfEntries;
    }

    private void createListOfEntries(List<Book> books, int catalogLevel) {
        if (books == null) {
            logger.debug("Got null!");
            return;
        }
        boolean isSplitNeeded;
        listOfEntries = new ArrayList<Element>();
        isSplitNeeded = books.size() > 100 && catalogLevel < 1;
        if (isSplitNeeded) {
            Map<String, List<Book>> mapOfBooksByLetter = DataModel
                    .getMapOfEntitiesByLetter(books);
            for (String letter : mapOfBooksByLetter.keySet()) {
                List<Book> listOfBooks = mapOfBooksByLetter.get(letter);
                addSubCatalog(new BooksCatalog(getID() + "/" + letter,
                        getTitle() + "/" + letter,
                        listOfBooks.size() + " книг",
                        FilenameUtils.getPath(getPath())
                                + "by_letter/"
                                + Integer.toHexString(Character.codePointAt(
                                        letter, 0)) + ".xml", listOfBooks,
                        catalogLevel + 1));
            }
        } else {
            createListOfEntriesRecursive(books, 0);
        }
    }

    private String createPaginatedID(String id) {
        int semicolonIndex = id.lastIndexOf(":");
        if (semicolonIndex < 0) {
            return id + ":"+getPaginationThreshold();
        }
        String base = id.substring(0, semicolonIndex);
        return base
                + ":"
                + String.valueOf(Integer.valueOf(id
                        .substring(semicolonIndex + 1)) + getPaginationThreshold());
    }

    private String createPaginatedPath(String path) {
        String localPath = FilenameUtils.getPath(path);
        String paginatedName = FilenameUtils.getBaseName(path);
        int underscoreIndex = paginatedName.lastIndexOf("_");
        if (underscoreIndex < 0) {
            return localPath + paginatedName + "_" + getPaginationThreshold() + ".xml";
        }
        String pageNumber = String.valueOf(Integer.valueOf(paginatedName
                .substring(underscoreIndex + 1)) + getPaginationThreshold());
        String base = paginatedName.substring(0, underscoreIndex);
        return localPath + base + "_" + pageNumber + ".xml";
    }

    private void createListOfEntriesRecursive(List<Book> books, int from) {
        int catalogSize = books.size();
        
        for (int i = from; i < catalogSize; i++) {
            if ((i - from) >= getPaginationThreshold()) {
                String subPath = createPaginatedPath(getPath());
                Catalog sub = new BooksCatalog(createPaginatedID(getID()),
                        "следующие "+getPaginationThreshold()+" книг", "нажмите для перехода...",
                        subPath, books.subList(i,
                                catalogSize), 1);
                sub.setHidden(true);
                addSubCatalog(sub);
                listOfEntries.add(0, OPDSFactory.getInstance().createNextLink(FilenameUtils.getName(subPath)));
                break;
            }
            listOfEntries.add(OPDSFactory.getInstance().createEntry(
                    books.get(i)));
        }
    }
}
