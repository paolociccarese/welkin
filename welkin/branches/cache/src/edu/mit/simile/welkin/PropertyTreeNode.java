/*
 * Created on 18-ott-2004
 */
package edu.mit.simile.welkin;

import javax.swing.tree.DefaultMutableTreeNode;

import com.hp.hpl.jena.rdf.model.Property;

/**
 * @author Paolo Ciccarese
 */
public class PropertyTreeNode extends DefaultMutableTreeNode {
    int hash=0;
    Property property;
    
    public PropertyTreeNode(Property property) {
        super(property.getLocalName());
        this.property = property;
        setHashCode(property.getURI());
    }
    
    public int hashCode() {
        return hash;
    }
    
    public void setHashCode(String property) {
    	int h = hash;
    	if (h == 0) {
    	    int off = 0;
    	    char val[] = property.toCharArray();
    	    int len = property.length();

            for (int i = 0; i < len; i++) {
                h = 31*h + val[off++];
            }
            hash = h;
        }          
    }
}
