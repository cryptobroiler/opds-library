package org.highscreen.library.opds;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.highscreen.library.datamodel.DataModel;
import org.jdom.Document;
import org.jdom.Element;

public abstract class Catalog {
    private static DataModel dataModel;
    private static final Logger logger = Logger.getLogger(Catalog.class);
    private static final int PAGINATION_THRESHOLD = 50;
    public static int getPaginationThreshold() {
        return PAGINATION_THRESHOLD;
    }

    private String title, id, summary, path;
    private int openSearchTotalResults;
    private int openSearchItemsPerPage;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getID() {
        return id;
    }

    public void setID(String id) {
        this.id = id;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<Catalog> getSubCatalogs() {
        return subCatalogs;
    }

    public void setSubCatalogs(List<Catalog> subCatalogs) {
        this.subCatalogs = subCatalogs;
    }

    private List<Catalog> subCatalogs;

    public Element generate() {
        OPDSFactory factory = OPDSFactory.getInstance();
        Document doc = factory.createDocument();
        Element root = factory.createOPDSFeed(title, id, summary);
        List<Element> entries = getListOfEntries();
        logger.debug("Generating " + getClass().getSimpleName() + " at "
                + getPath() + " with " + entries.size() + " entries and "
                + subCatalogs.size() + " subcatalogs");
        File dir = new File(FilenameUtils.getPath(getPath()));
        dir.mkdirs();
        doc.setRootElement(root);
        // generate opensearch elements
        if (openSearchItemsPerPage != 0 || openSearchTotalResults != 0) {
            root.addContent(factory
                    .createOpenSearchDescriptionLink("/opensearch.xml"));
            root.addContent(factory
                    .createOpenSearchTotalResultsElement(openSearchTotalResults));
            root.addContent(factory
                    .createOpenSearchItemsPerPageElement(openSearchItemsPerPage));
        }
        if (!entries.isEmpty()) {
            root.addContent(entries);
        }
        for (Catalog catalog : subCatalogs) {
            Element entry = factory.createOPDSEntry(
                    catalog.getTitle(),
                    catalog.getID(),
                    "./"
                            + catalog.getPath().replaceFirst(
                                    FilenameUtils.getPath(getPath()), ""),
                    catalog.getSummary(), null);
            if (!catalog.hideEntry) {
                root.addContent(entry);
            }
            catalog.generate();
        }
        try {
            factory.getOutputter().output(doc, new FileOutputStream(path));
        } catch (Exception e) {
            logger.error(e);
        }
        return root;
    }

    public abstract List<Element> getListOfEntries();

    private boolean hideEntry = false;

    public void setHidden(boolean value) {
        hideEntry = value;
    }

    public Catalog(String catalogID, String catalogTitle,
            String catalogSummary, String catalogPath) {
        logger.debug("Creating " + getClass().getSimpleName() + " at "
                + catalogPath);
        id = catalogID;
        title = catalogTitle;
        summary = catalogSummary;
        path = catalogPath;
        subCatalogs = new ArrayList<Catalog>();
        openSearchItemsPerPage = 0;
        openSearchTotalResults = 0;
    }

    public void addSubCatalog(Catalog catalog) {
        subCatalogs.add(catalog);
    }

    public static void setDataModel(DataModel dataModel) {
        Catalog.dataModel = dataModel;
    }

    public static DataModel getDataModel() {
        return dataModel;
    }

    public void setOpenSearchTotalResults(int openSearchTotalResults) {
        this.openSearchTotalResults = openSearchTotalResults;
    }

    public int getOpenSearchTotalResults() {
        return openSearchTotalResults;
    }

    public void setOpenSearchItemsPerPage(int openSearchItemsPerPage) {
        this.openSearchItemsPerPage = openSearchItemsPerPage;
    }

    public int getOpenSearchItemsPerPage() {
        return openSearchItemsPerPage;
    }
}
