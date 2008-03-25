package edu.mit.simile.welkin.tree;

import com.hp.hpl.jena.rdf.model.Property;

public class PropertyToLiteralTreeNode extends TreeNode {

    public PropertyToLiteralTreeNode(Property property) {
        super(property);
        setHashCode(property.getURI());
    }
    
    public boolean equals(Object o) {
        return (super.getUserObject().toString().equals(((TreeNode)o).getUserObject().toString()));
    }
    
    public String toString() {
        return ((Property)super.getUserObject()).getLocalName()+" (Literal) ";
    }
}
