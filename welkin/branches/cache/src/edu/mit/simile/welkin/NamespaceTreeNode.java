/*
 * Created on 17-ott-2004
 */
package edu.mit.simile.welkin;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author Paolo Ciccarese
 */
public class NamespaceTreeNode extends DefaultMutableTreeNode {
    int hash=0;
    
    public NamespaceTreeNode(String namespace) {
        super(namespace);
        setHashCode(namespace);
    }
    
    public int hashCode() {
        return hash;
    }
    
    public void setHashCode(String namespace) {
    	int h = hash;
    	if (h == 0) {
    	    int off = 0;
    	    char val[] = namespace.toCharArray();
    	    int len = namespace.length();

            for (int i = 0; i < len; i++) {
                h = 31*h + val[off++];
            }
            hash = h;
        }          
    }
}
