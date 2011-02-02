package org.highscreen.library.datamodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.highscreen.library.adapters.LibraryAdapter;

public class DataModel {
    private static Logger logger = Logger.getLogger(DataModel.class);
    private List<Author> listOfAuthors;
    private List<Book> listOfBooks;
    private List<Tag> listOfTags;
    private Map<Author, List<Book>> mapOfBooksByAuthor;
    private Map<Tag, List<Book>> mapOfBooksByTag;
    private LibraryAdapter adapter;

    public DataModel(LibraryAdapter adapter) {
        this.adapter = adapter;
        clear();
    }

    public void clear() {
        listOfAuthors = null;
        listOfBooks = null;
        listOfTags = null;
    }

    public List<Book> getListOfBooks() {
        if (listOfBooks == null) {
            logger.debug("Computing list of books");
            listOfBooks = adapter.getListOfBooks();
        }
        return listOfBooks;
    }

    public List<Author> getListOfAuthors() {
        logger.debug("Computing list of authors");
        if (listOfAuthors == null) {
            listOfAuthors = adapter.getListOfAuthors();
        }
        return listOfAuthors;
    }

    public List<Tag> getListOfTags() {
        logger.debug("Computing list of tags");
        if (listOfTags == null) {
            listOfTags = adapter.getListOfTags();
        }
        return listOfTags;
    }

    public Map<Author, List<Book>> getMapOfBooksByAuthor() {
        if (mapOfBooksByAuthor == null) {
            mapOfBooksByAuthor = adapter.getMapOfBooksByAuthor();
        }
        return mapOfBooksByAuthor;
    }

    public Map<String, List<Book>> getMapOfBooksByLetter() {
        return getMapOfEntitiesByLetter(getListOfBooks());
    }

    public Map<Tag, List<Book>> getMapOfBooksByTag() {
        if (mapOfBooksByTag == null) {
            mapOfBooksByTag = adapter.getMapOfBooksByTag();
        }
        return mapOfBooksByTag;
    }

    public static <T extends AbstractEntity> Map<String, List<T>> getMapOfEntitiesByLetter(
            List<T> objects) {
        final String LETTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZÄÅÇÉÑÖ›Üáàâäãåçéèêëíìîäñóòôöõúùûü";
        Map<String, List<T>> mapOfObjectsByLetter = new TreeMap<String, List<T>>();
        for (T object : objects) {
            if (object == null) {
                continue;
            }
            String firstLetter = "";
            String string = object.getFieldToSplit();
            if (string == null) {
                string = "";
            }
            if (!string.isEmpty()) {
                firstLetter = string.substring(0, 1).toUpperCase();
            } else {
                firstLetter = "_";
            }
            if (!LETTERS.contains(firstLetter)) {
                firstLetter = "_";
            }
            List<T> objectsByLetter = mapOfObjectsByLetter.get(firstLetter);
            if (objectsByLetter == null) {
                objectsByLetter = new ArrayList<T>();
                mapOfObjectsByLetter.put(firstLetter, objectsByLetter);
            }
            objectsByLetter.add(object);
        }
        for (String letter : mapOfObjectsByLetter.keySet()) {
            List<T> objectsByLetter = mapOfObjectsByLetter.get(letter);
            Collections.sort(objectsByLetter);
        }
        return mapOfObjectsByLetter;
    }
}
