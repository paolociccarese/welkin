package edu.mit.simile.welkin;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author Paolo Ciccarese <paolo@hcklab.org>
 */
public class InfoCache {
    //  Nodes (Rdf resources)
    public Set nodes = new HashSet();
    public Hashtable hash = new Hashtable();
    
    public class Node {
        float x, y;
        float vx, vy;

        int hash = 0;

        boolean fixed = false;
        boolean isVisible = true;
        boolean highlighted = false;

        String label;
        final String unique;

        List linkedObjectNodes = new ArrayList();
        List linkedLiterals = new ArrayList();

        Node(final String unique) {
            this.unique = this.label = unique;
            setHashCode();

            this.x = (float) ((Math.random() - 0.5d) * 200.0d);
            this.y = (float) ((Math.random() - 0.5d) * 200.0d);
        }

        Node(final String unique, final float x, final float y) {
            this.unique = this.label = unique;
            setHashCode();

            this.x = x;
            this.y = y;
        }

        public void addObjectEdge(Edge edge) {
            linkedObjectNodes.add(edge);
        }
        
        public void addLiteral(CachedLiteral literal) {
            linkedLiterals.add(literal);
        }
        
        public Iterator getLiterals() {
            return linkedLiterals.iterator();
        }

        public boolean isObjectOf(final Node node) {
            for (Iterator it = linkedObjectNodes.iterator(); it.hasNext();) {
                if (((Edge) it.next()).object.equals(node))
                    return true;
            }
            return false;
        }

        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || !(o instanceof Node))
                return false;

            if (this.unique.equals(((Node) o).unique))
                return true;
            else
                return false;
        }

        public int hashCode() {
            return hash;
        }

        private void setHashCode() {
            int h = hash;
            if (h == 0) {
                int off = 0;
                char val[] = unique.toCharArray();
                int len = unique.length();

                for (int i = 0; i < len; i++) {
                    h = 31 * h + val[off++];
                }
                hash = h;
            }
        }
    }

    public class Predicate {
        final String namespace;
        final String property;

        Predicate(final String namespace, final String property) {
            this.namespace = namespace;
            this.property = property;
        }
    }

    public class Edge {
        final Predicate predicate;
        final Node object;

        Edge(final Predicate predicate, final Node node) {
            this.predicate = predicate;
            this.object = node;
        }
    }
    
    public class CachedLiteral {
        final Predicate predicate;
        final String literal;

        CachedLiteral(final Predicate predicate, final String literal) {
            this.predicate = predicate;
            this.literal = literal;
        }
    }

    public void addEntry(int hashSubject, int hashObject, String prNamespace, String prURI) {
//        String key = hashSubject+","+hashSubject;
        if(hashSubject==hashObject) return;
        Point key = new Point(hashSubject, hashObject);
        Object value = hash.get(key);
        if(value!=null) { 
            Object[] values = (Object[]) value;
            Predicate newValues[] = new Predicate[values.length+1];
            System.arraycopy(values, 0, newValues, 0, values.length);
            newValues[values.length] = new Predicate(prNamespace, prURI);
        } else {
            Predicate[] values = new Predicate[1];
            values[0] = new Predicate(prNamespace, prURI);
            hash.put(key, values);
        }
    }

    Point tmp = new Point();
    
    public Predicate[] getEntries(int hashSubject, int hashObject) {
        tmp.x = hashSubject;
        tmp.y = hashObject;
        return (Predicate[]) hash.get(tmp);
    }
    
    public Node getNode(String unique) {
        for (Iterator it = nodes.iterator(); it.hasNext();) {
            Node tmp = (Node) it.next();
            if (tmp.unique.equals(unique))
                return tmp;
        }
        return null;
    }

    public Node addNode(String unique, String label) {
        Node node = new Node(unique);
        if (nodes.add(node))
            return node;
        else
            return null;
    }

    public Node addNode(String unique, String label, float x, float y) {
        Node node = new Node(unique, x, y);
        if (nodes.add(node))
            return node;
        else
            return null;
    }

    public Edge getEdge(String namespace, String predicate, Node node) {
        return new Edge(new Predicate(namespace, predicate), node);
    }

    public CachedLiteral getLiteral(String namespace, String predicate, String literal) {
        return new CachedLiteral(new Predicate(namespace, predicate), literal);
    }
    
    public void setLabel(String unique, String label) {
        getNode(unique).label = "rdfs#label: " + label;
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

    public void verifyVisualized(CheckTree tree) {
        for (Iterator it = nodes.iterator(); it.hasNext();) {
            Node node = (Node) it.next();
            boolean flag = false;
//          boolean in = false;
            for (Iterator i = node.linkedObjectNodes.iterator(); i.hasNext();) {
//                in = true;
                if (tree.isChecked((((Edge) i.next()).predicate.property)))
                    flag = true;
            }
//            if (in) {
                if (flag)
                    node.isVisible = true;
                else
                    node.isVisible = false;
//            }
        }
    }
}