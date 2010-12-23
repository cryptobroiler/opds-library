package org.highscreen.library.opds;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.highscreen.library.datamodel.Book;
import org.highscreen.library.datamodel.DataModel;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

public class Catalog {
    private static Logger logger = Logger.getLogger(Catalog.class);
    private DataModel dataModel;

    public void generate() {
        // List<Book> books = dataModel.getListOfBooks();
        // for (Book book : books) {
        // logger.debug(book);
        // }
        OPDSFactory factory = OPDSFactory.getInstance();
        Document doc = factory.createDocument();
        Element root = factory.createOPDSFeed("Ебучий каталог",
                "flibusta:opds", "Ну а что тут сказать...");
        doc.setRootElement(root);
        Element fuckingEntry = factory.createOPDSEntry("Ну а хули тут!",
                "flibusta:entry", "http://www.vobis.ru",
                "Да бля, полный пиздец", null);
        root.addContent(fuckingEntry);
        root.addContent(factory.createOPDSEntry("Библиотека",
                "flibusta:library", "./lib.xml", "Что, блядь, книжек захотел?",
                null));
        Document booksDoc = factory.createDocument();
        Element booksRoot = factory.createOPDSFeed("Библиотека",
                "flibusta:library", "Что, блядь, книжек захотел?");
        booksDoc.setRootElement(booksRoot);
        List<Book> books = dataModel.getListOfBooks();
        int count = 0;
        for (Book b : books) {
            if (count == 100)
                break;
            if (b.getSeries() != null) {
                booksRoot.addContent(factory.createEntry(b));
                count++;
            }
        }
        XMLOutputter out = factory.getOutputter();
        try {
            out.output(doc, new FileOutputStream("catalog.xml"));
            out.output(booksDoc, new FileOutputStream("lib.xml"));
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public Catalog(DataModel dm) {
        dataModel = dm;
        generate();
    }

    public void testCatalog() {
        List<Book> books = dataModel.getListOfBooks();
        int count = 0;
        try {
            FileWriter w = new FileWriter("index.html");
            w.write("<html><head><title>Catalog</title></head><body>");
            for (Book b : books) {
                if (count == 100)
                    break;
                if (b.getSeries() != null) {
                    logger.debug(b);
                    w.write("<a href=\"" + b.getPath() + "\">" + b.getTitle()
                            + "</a>\n");
                    count++;
                }
            }
            w.write("</body></html>");
            w.close();
        } catch (Exception e) {
            logger.error(e);
        }
    }
}
