package edu.mit.simile.welkin.tree;

public class NamespaceTreeNode extends TreeNode {

    public NamespaceTreeNode(String namespace) {
        super(namespace);
        setHashCode(namespace);
    }
    
    public boolean equals(Object o) {
        return (super.getUserObject().toString().equals(((TreeNode)o).getUserObject().toString()));
    }
    
    public int hashCode() {
        return super.getUserObject().toString().hashCode();
    }
}
