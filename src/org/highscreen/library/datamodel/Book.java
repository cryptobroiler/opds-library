package org.highscreen.library.datamodel;

import java.util.ArrayList;
import java.util.List;

public class Book extends AbstractEntity{
    private List<Author> authors = new ArrayList<Author>();
    private String authorSort;
    private String fileType;
    private String isbn;
    private String path;
    private Series series;
    private String seriesIndex;
    private String sort;
    private List<Tag> tags = new ArrayList<Tag>();
    private String timeStamp;
    private String title;

    public Book(String id, String title, String sort, String timeStamp,
            String uri, String seriesIndex, String authorSort, String isbn,
            String path) {
        super(id);
        this.title = title;
        this.sort = sort;
        this.timeStamp = timeStamp;
        this.seriesIndex = seriesIndex;
        this.authorSort = authorSort;
        this.isbn = isbn;
        this.path = path;
    }

    public void addAuthor(Author a) {
        authors.add(a);
    }

    public void addTag(Tag e) {
        tags.add(e);
    }

    public List<Author> getAuthors() {
        return authors;
    }

    public String getAuthorSort() {
        return authorSort;
    }

    public String getFileType() {
        return fileType;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getPath() {
        return path;
    }

    public Series getSeries() {
        return series;
    }

    public String getSeriesIndex() {
        return seriesIndex;
    }

    public String getSort() {
        return sort;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public String getTitle() {
        return title;
    }

    public String getUri() {
        return path;
    }

    public void setAuthorSort(String authorSort) {
        this.authorSort = authorSort;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setSeries(Series series) {
        this.series = series;
    }

    public void setSeriesIndex(String seriesIndex) {
        this.seriesIndex = seriesIndex;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUri(String uri) {
        this.path = uri;
    }

    @Override
    public String toString() {
        return getID() + " - " + getTitle() + " - " + getTimeStamp() + " - "
                + getSeriesIndex() + " in " + getSeries() + " - main author: "
                + getAuthorSort() + " more authors: " + getAuthors().toString()
                + " isbn: " + getIsbn() + " path: " + getPath() + " tags: "
                + getTags();
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
