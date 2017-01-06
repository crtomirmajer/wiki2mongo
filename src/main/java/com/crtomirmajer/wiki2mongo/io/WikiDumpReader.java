package com.crtomirmajer.wiki2mongo.io;

import com.crtomirmajer.wiki2mongo.store.Page;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Majer on 30.10.2016.
 */
public class WikiDumpReader implements Iterator<Page> {
    
    private static final List<String> disambiguationCategories = Arrays.asList("Category:All article disambiguation pages",
                                                                               "Category:All disambiguation pages",
                                                                               "Category:Disambiguation pages");
    
    private static final String PAGE      = "page";
    private static final String REDIRECT  = "redirect";
    private static final String TITLE     = "title";
    private static final String TEXT      = "text";
    private static final String NAMESPACE = "ns";
    
    private String          path;
    private XMLStreamReader streamReader;
    
    private AtomicLong position = new AtomicLong(0);
    private boolean    finished = false;
    
    public WikiDumpReader(String path) throws Exception {
        
        if(!Files.exists(Paths.get(path))) {
            throw new IllegalArgumentException("File does not exist.");
        }
        
        this.path = path;
        open();
    }
    
    public void open() throws Exception {
        InputStream inputStream = new FileInputStream(path);
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        streamReader = inputFactory.createXMLStreamReader(inputStream, "UTF-8");
    }
    
    public Page readNext() throws XMLStreamException {
        
        Page page = new Page();
        boolean endReached = false;
        
        while(streamReader.hasNext() && !endReached) {
            position.incrementAndGet();
            switch(streamReader.next()) {
                case XMLStreamConstants.START_ELEMENT: {
                    
                    switch(streamReader.getLocalName()) {
                        case TITLE: {
                            page.setTitle(streamReader.getElementText());
                            if(page.getTitle().contains("(disambiguation)"))
                                page.setDisambiguation(true);
                            break;
                        }
                        case REDIRECT: {
                            page.setRedirect(streamReader.getAttributeValue(null, TITLE));
                            break;
                        }
                        case NAMESPACE: {
                            page.setNamespace(Integer.parseInt(streamReader.getElementText()));
                            break;
                        }
                        case TEXT: {
                            if(page.getRedirect() == null) {
                                page.setText(streamReader.getElementText());
                                
                                if(!page.isDisambiguation()) {
                                    for(String category : disambiguationCategories)
                                        if(page.getText().contains(category)) {
                                            page.setDisambiguation(true);
                                            break;
                                        }
                                }
                            }
                            break;
                        }
                    }
                    
                    break;
                }
                case XMLStreamConstants.END_ELEMENT: {
                    switch(streamReader.getLocalName()) {
                        case PAGE: {
                            endReached = true;
                        }
                    }
                    break;
                }
                case XMLStreamConstants.END_DOCUMENT: {
                    endReached = true;
                }
            }
        }
        
        if(page.getTitle() == null)
            finished = true;
        
        return page;
    }
    
    public String getPath() {
        return path;
    }
    
    @Override
    public boolean hasNext() {
        return !finished;
    }
    
    @Override
    public Page next() {
        try {
            return readNext();
        } catch(XMLStreamException e) {
            e.printStackTrace();
        }
        return null;
    }
}
