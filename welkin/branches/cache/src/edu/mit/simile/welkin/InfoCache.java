package edu.mit.simile.welkin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author Paolo Ciccarese <paolo@hcklab.org>
 */
public class InfoCache {
//  Nodes (Rdf resources)
    public Set nodes = new HashSet();

    public class Node {
        float x;
        float y;
        float vx;
        float vy;

        int hash = 0;
        int offset;
        
        String unique;
        String label;
        List linkedNodes=new ArrayList();;

        boolean fixed = false;
        boolean highlighted = false;

        Node(String unique) {
            this.unique = unique;
            this.label = unique;
            this.x = (float) ((Math.random() - 0.5d) * 200.0d);
            this.y = (float) ((Math.random() - 0.5d) * 200.0d);
        }
        
        Node(String unique, float x, float y) {
            this.unique = unique;
            this.label = unique;
            this.x = x;
            this.y = y;
        }
        
        public void addObject(Node node) {
            linkedNodes.add(node);
        }
        
        public boolean isObject(Node node) {
            for(Iterator it=linkedNodes.iterator();it.hasNext();) {
                Node tmp = (Node) it.next();
                if(tmp.equals(node)) return true;
            }
            return false;
        }
        
        public boolean equals(Object o) {
            if ( this == o ) return true;
            if (o == null || !(o instanceof Node)) return false;

            if(this.unique.equals(((Node)o).unique))
                return true;
            else
                return false;
        }
        
        public int hashCode() {
        	int h = hash;
        	if (h == 0) {
        	    int off = offset;
        	    char val[] = unique.toCharArray();
        	    int len = unique.length();

                for (int i = 0; i < len; i++) {
                    h = 31*h + val[off++];
                }
                hash = h;
            }
            return h;
        }
    }
    
    public Node getNode(String unique) {
        for(Iterator it=nodes.iterator();it.hasNext();) {
            Node tmp = (Node)it.next();
            if(tmp.unique.equals(unique)) return tmp;
        }       
        return null;
    }
    
    public Node addNode(String unique, String label) {
        Node node = new Node(unique);
        if(nodes.add(node)) return node;
        else return null;
    }
    
    public Node addNode(String unique, String label, float x, float y) {
        Node node = new Node(unique, x, y);
        if(nodes.add(node)) return node;
        else return null;
    }
    
    public void setLabel(String unique, String label) {
        getNode(unique).label = "Label: "+label;
    }

    public float[] getCoordinatesXY(String unique) {
        float coordinates[] = new float[2];
        for (Iterator it = nodes.iterator(); it.hasNext();) {
            Node node = (Node) it.next();
            if (node.unique.equals(unique)) {
                coordinates[0] = node.x;
                coordinates[1] = node.y;
                return coordinates;
            }
        }
        return null;       
    }
    
    public void setCoordinatesXY(String unique, float x, float y) {
        for (Iterator it = nodes.iterator(); it.hasNext();) {
            Node node = (Node) it.next();
            if (node.unique.equals(unique)) {     
                node.x = x;
                node.y = y;
            }
        }
    }
    
    public float[] getCoordinatesVXY(String unique) {
        float coordinates[] = new float[2];
        for (Iterator it = nodes.iterator(); it.hasNext();) {
            Node node = (Node) it.next();
            if (node.unique.equals(unique)) {
                coordinates[0] = node.vx;
                coordinates[1] = node.vy;
                return coordinates;
            }
        }
        return null;
    }
    
    public void setCoordinatesVXY(String unique, float vx, float vy) {
        for (Iterator it = nodes.iterator(); it.hasNext();) {
            Node node = (Node) it.next();
            if (node.unique.equals(unique)) {     
                node.vx = vx;
                node.vy = vy;
            }
        }
    }
}
