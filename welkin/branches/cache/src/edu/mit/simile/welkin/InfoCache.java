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

        String unique;
        List linkedNodes=new ArrayList();;

        boolean fixed = false;
        boolean highlighted = false;

        Node(String unique) {
            this.unique = unique;
            this.x = (float) ((Math.random() - 0.5d) * 200.0d);
            this.y = (float) ((Math.random() - 0.5d) * 200.0d);
        }
        
        Node(String unique, float x, float y) {
            this.unique = unique;
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
            if ( !(o instanceof Node) ) return false;

            if(this.unique.equals(((Node)o).unique))
                return true;
            else
                return false;
        }
    }
    
    public Node getNode(String unique) {
        for(Iterator it=nodes.iterator();it.hasNext();) {
            Node tmp = (Node)it.next();
            if(tmp.unique.equals(unique)) return tmp;
        }       
        return null;
    }
    
    public Node addNode(String unique) {
        Node node = new Node(unique);
        nodes.add(node);
        return node;
    }
    
    public Node addNode(String unique, float x, float y) {
        Node node = new Node(unique, x, y);
        nodes.add(node);
        return node;
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
