/*
 * Created on 22-ott-2004
 */
package edu.mit.simile.welkin.tree;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author Paolo Ciccarese
 */
public class TreeNode extends DefaultMutableTreeNode {
    private int hash=0;
    
    public TreeNode(Object o) {
        super(o);
    }
    
    public int hashCode() {
        return hash;
    }
    public void setHashCode(String value) {
    	int h = hash;
    	if (h == 0) {
    	    int off = 0;
    	    char val[] = value.toCharArray();
    	    int len = value.length();

            for (int i = 0; i < len; i++) {
                h = 31*h + val[off++];
            }
            hash = h;
        }          
    }
}
