package edu.mit.simile.welkin.tree;

import com.hp.hpl.jena.rdf.model.Property;

public class PropertyToResourceTreeNode extends TreeNode {

    public PropertyToResourceTreeNode(Property property) {
        super(property);
        setHashCode(property.getURI());
    }
    
    public boolean equals(Object o) {
        return (super.getUserObject().toString().equals(((TreeNode)o).getUserObject().toString()));
    }
    
    public String toString() {
        return ((Property)super.getUserObject()).getLocalName();
    }
}
