/*
 * Created on 17-ott-2004
 */
package edu.mit.simile.welkin.tree;


/**
 * @author Paolo Ciccarese
 */
public class NamespaceTreeNode extends TreeNode {

    public NamespaceTreeNode(String namespace) {
        super(namespace);
        setHashCode(namespace);
    }
    
    public boolean equals(Object o) {
        return (super.getUserObject().toString().equals(((TreeNode)o).getUserObject().toString()));
    }
}
