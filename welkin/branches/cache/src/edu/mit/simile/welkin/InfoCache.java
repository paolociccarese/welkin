package edu.mit.simile.welkin;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Paolo Ciccarese <paolo@hcklab.org>
 */
public class InfoCache {
    // Nodes divided by type
    public Set nodes = new HashSet();

    public class Node {
        float x;
        float y;
        float vx;
        float vy;

        String unique;

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
        
        public boolean equals(Object o) {
            if(this.unique.equals(((Node)o).unique))
                return true;
            else
                return false;
        }
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
