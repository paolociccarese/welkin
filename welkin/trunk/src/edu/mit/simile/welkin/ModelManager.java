package edu.mit.simile.welkin;

import java.io.FileInputStream;
import java.util.Iterator;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.rio.Parser;
import org.openrdf.rio.StatementHandler;
import org.openrdf.rio.StatementHandlerException;
import org.openrdf.rio.ntriples.NTriplesParser;
import org.openrdf.rio.rdfxml.RdfXmlParser;

import edu.mit.simile.welkin.ModelCache.WResource;

public class ModelManager implements StatementHandler {

    public static final int RDFXML = 1;
    public static final int NTRIPLES = 2;
    
    private final String RDFS_LABEL = "http://www.w3.org/2000/01/rdf-schema#label";
    
    public Parser parser;
    public ModelCache cache = new ModelCache();
    
    public void handleStatement(Resource resource, URI uri, Value value) 
		throws StatementHandlerException 
	{    
        WResource sub = null;
        if (resource instanceof URI) {
            sub = cache.addResource(resource.toString(), false);
            cache.addResourcesUri((URI)resource);
        } else if (resource instanceof BNode) {
            sub = cache.addBlankResource(resource.toString());
        }
         
        if (value instanceof URI) {
            WResource obj = cache.addResource(value.toString(), true);
            sub.addObjectStatement(cache.getStatement(sub, uri, obj));
            cache.addResourcesUri((URI)value);
            cache.addStatement(sub.hash, obj.hash, uri);
            cache.addPredicatesUri(uri);
        } else if (value instanceof Literal) {
            sub.addLiteral(cache.getLiteral(uri , value.toString()));
            
            // Load labels
            // TODO Do we need to put the uri in the literals?
            if(uri.toString().equals(RDFS_LABEL)) {
                sub.label = value.toString();
            }
        } else if (value instanceof BNode) {
            WResource obj = cache.addBlankResource(value.toString());
            sub.addObjectStatement(cache.getStatement(sub, uri, obj));
            cache.addStatement(sub.hash, obj.hash, uri);
            cache.addPredicatesUri(uri);
        }
	}
    
    // -----------------------
    //      Model issues
    // -----------------------
    public boolean addModel(FileInputStream in, int type, String baseUri) {
        try {
            initParser(type);
            parser.parse(in, baseUri);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Adds a model without a base uri.
     * @param in	The inputstream
     * @param type	Type of the parser
     */
    public boolean addModel(FileInputStream in, int type) {
        return addModel(in, type, "");
    }
    
    public void clear() {
        cache.clear();
    }
    
    // -----------------------
    //       Highlights
    // -----------------------
    public void highlightNode(String text, boolean highlight, boolean highlightOnLabel) {
        for(Iterator it=cache.resources.iterator(); it.hasNext();) {
            WResource node = ((WResource) it.next());
            if(highlightOnLabel) {
	            if(node.label.startsWith(text))
	                node.highlighted = true;
            } else {
	            if(node.unique.startsWith(text))
	                node.highlighted = true;               
            }
        }
    }

    public void clearHighlights() {
        for(Iterator it=cache.resources.iterator(); it.hasNext();) {
            ((WResource) it.next()).highlighted = false;
        }
    }
    
    // -----------------------
    //  Parser initialization
    // -----------------------
    /**
     * Returns the right parser type.
     * @param type	The parser type
     * @return The requested parser.
     */
    private void initParser(int type) {
        if(type == RDFXML) setXmlRdfParserInstance();
        else if(type == NTRIPLES) setXmlN3ParserInstance();
        else throw new IllegalArgumentException("Wrong rdf parser type!");
        
        parser.setStatementHandler(this);
        parser.setVerifyData(true);
        parser.setStopAtFirstError(false);
    }
    
    /**
     * Inits the instance of the xml/rdf Rio parser if present
     * otherwise it will create one.
     * @return The parse instance.
     */
    private void setXmlRdfParserInstance() {
       if (parser==null || !(parser instanceof RdfXmlParser)) {
           // Use the SAX2-compliant Xerces parser
           System.setProperty(
                   "org.xml.sax.driver",
                   "org.apache.xerces.parsers.SAXParser"
              );
           
           parser = new RdfXmlParser();
       }
    }
    
    /**
     * Inits the instance of the xml/rdf Rio parser if present
     * otherwise it will create one.
     * @return The parse instance.
     */
    private void setXmlN3ParserInstance() {
       if (parser==null || !(parser instanceof NTriplesParser)) {
           parser = new NTriplesParser();
       }
    }
}