package edu.mit.simile.welkin.tree;


public class PropertyToResourceTreeNode extends TreeNode {

    String end;
    
    public PropertyToResourceTreeNode(String base, String end) {
        super(base+end);
        this.end = end;
        setHashCode((base+end));
    }
    
    public boolean equals(Object o) {
        return (super.getUserObject().toString().equals(((TreeNode)o).getUserObject().toString()));
    }
    
    public String toString() {
        return (end);
    }
}
