package org.highscreen.library.opds;

import java.text.MessageFormat;
import java.util.List;

import org.highscreen.library.datamodel.Author;
import org.highscreen.library.datamodel.Book;
import org.highscreen.library.datamodel.Tag;
import org.jdom.DefaultJDOMFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMFactory;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class OPDSFactory {
    public enum Namespace {
        Atom("", "http://www.w3.org/2005/Atom"),
        Opds("opds", "http://opds-spec.org/2010/catalog"),
        Opf("opf", "http://www.idpf.org/2007/opf"),
        Dc("dc", "http://purl.org/dc/elements/1.1/"),
        DcTerms("dcterms", "http://purl.org/dc/terms"),
        Calibre("calibre", "http://calibre.kovidgoyal.net/2009/metadata"),
        Xhtml("xhtml", "http://www.w3.org/1999/xhtml");
        private org.jdom.Namespace jdomNamespace;

        private Namespace(String prefix, String uri) {
            this.jdomNamespace = org.jdom.Namespace.getNamespace(prefix, uri);
        }

        public org.jdom.Namespace getJDOMNamespace() {
            return jdomNamespace;
        }
    }

    private JDOMFactory factory = null;
    private XMLOutputter outputter = null;
    private static OPDSFactory opdsFactory = null;

    private OPDSFactory() {
    }

    public JDOMFactory getFactory() {
        if (factory == null) {
            factory = new DefaultJDOMFactory();
        }
        return factory;
    }

    public XMLOutputter getOutputter() {
        if (outputter == null)
            outputter = new XMLOutputter(Format.getPrettyFormat());
        return outputter;
    }

    public static OPDSFactory getInstance() {
        if (opdsFactory == null) {
            opdsFactory = new OPDSFactory();
        }
        return opdsFactory;
    }

    public Element createEntry(Object o) {
        Element result = null;
        if (o instanceof Book) {
            // TODO: book to opds serialization
            result = createBookEntry((Book) o);
        } else {
            if (o instanceof Author) {
                // TODO: author to opds serialization
            } else {
                if (o instanceof Tag) {
                    // TODO: tag to opds serialization
                }
            }
        }
        return result;
    }

    private Element createBookEntry(Book b) {
        Element entry = createElement("entry");
        String sTitle = b.getTitle();
        Element title = createElement("title").addContent(sTitle);
        entry.addContent(title);
        Element id = createElement("id").addContent(
                "flibusta:book:" + b.getID());
        entry.addContent(id);
        if (b.getAuthors().size() > 0) {
            Element author = createElement("author").addContent(
                    createElement("name").addContent(
                            b.getAuthors().get(0).getName()).addContent(
                            createElement("uri").addContent(
                                    "author_" + b.getAuthors().get(0).getID()
                                            + ".xml")));
            entry.addContent(author);
        }
        entry.addContent(createLinkElement(b.getPath(), "application/epub+zip",
                "http://opds-spec.org/acquisition", null));
        return entry;
    }

    public Document createDocument() {
        return new Document();
    }

    public Element createElement(String name, Namespace namespace) {
        Element result = getFactory().element(name);
        if (namespace != null) {
            result.setNamespace(namespace.getJDOMNamespace());
        }
        return result;
    }

    public Element createElement(String name) {
        return createElement(name, Namespace.Atom);
    }

    public Element createRootAtomElement(String name, Namespace... namespaces) {
        Element result = createElement(name);
        for (Namespace n : namespaces) {
            result.addNamespaceDeclaration(n.getJDOMNamespace());
        }
        return result;
    }

    Element createLinkElement(String url, String urlType, String urlRelation,
            String title) {
        Element link = createElement("link");
        if (isValid(urlType)) {
            link.setAttribute("type", urlType);
        }
        if (isValid(urlRelation)) {
            link.setAttribute("rel", urlRelation);
        }
        link.setAttribute("href", url);
        if (isValid(title)) {
            link.setAttribute("title", title);
        }
        return link;
    }

    public String getLinkTypeForEntry() {
        return "application/atom+xml;type=entry;profile=opds-catalog";
    }

    public String getLinkTypeForFeed() {
        return "application/atom+xml;type=feed;profile=opds-catalog";
    }

    public Element createXmlLinkElement(String url, String relation,
            String title) {
        return createLinkElement(url, getLinkTypeForFeed(), relation, title);
    }

    public Element createOPDSEntry(String title, String id, String url,
            String summary, String icon) {
        return createAtomElement(false, "entry", title, id, url,
                getLinkTypeForEntry(), null, summary, icon);
    }

    public Element createOPDSFeed(String title, String id, String summary) {
        Element root = createAtomElement(true, "feed", title, id, null,
                getLinkTypeForFeed(), null, summary, null);
        root.addContent(createXmlLinkElement("../catalog.xml", "start",
                "Back to main catalog"));
        return root;
    }

    private static boolean isValid(String s) {
        if (s != null) {
            if (!s.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public Element createAtomElement(boolean isRoot, String elementName,
            String title, String id, String url, String urlType,
            String urlRelation, String content, String icon) {
        Element element = null;
        if (isRoot)
            element = createRootAtomElement(elementName, Namespace.Atom,
                    Namespace.Xhtml, Namespace.Opds);
        else
            element = createElement(elementName);
        // title
        if (isValid(title)) {
            Element titleElement = createElement("title").addContent(title);
            element.addContent(titleElement);
        }
        // id
        if (isValid(id)) {
            Element idElement = createElement("id").addContent(id);
            element.addContent(idElement);
        }
        // content
        if (isValid(content)) {
            Element contentElement = createElement("content").addContent(
                    content);
            contentElement.setAttribute("type", "text");
            element.addContent(contentElement);
        }
        // link
        if (isValid(url)) {
            element.addContent(createLinkElement(url, urlType, urlRelation,
                    null));
        }
        // icon link
        if (isValid(icon))
            element.addContent(getOPDSIcon(icon));
        return element;
    }

    private Element getOPDSIcon(String icon) {
        return createLinkElement(icon, "image/png",
                "http://opds-spec.org/thumbnail", null);
    }
}
