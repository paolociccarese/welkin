package edu.mit.simile.welkin;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class InfoCache {

    Set nodes = new HashSet();
    HashMap hash = new HashMap();
    
    class Node {
        float x, y;
        float vx, vy;

        int hash = 0;

        boolean fixed = false;
        boolean isVisible = true;
        boolean highlighted = false;
        boolean isObjectOnly = true;

        String label;
        final String unique;

        List linkedSubjectNodes = new ArrayList();
        List linkedObjectNodes = new ArrayList();
        List linkedLiterals = new ArrayList();
        
        Node(final String unique, final boolean isObjectOnly) {
            this.unique = this.label = unique;
            this.isObjectOnly = isObjectOnly;
            setHashCode();

            this.x = (float) ((Math.random() - 0.5d) * 200.0d);
            this.y = (float) ((Math.random() - 0.5d) * 200.0d);
        }
        
        Node(final String unique, final float x, final float y, final boolean isNodeFixed, final boolean isSubjectOnly) {
            this.unique = this.label = unique;
            setHashCode();

            this.fixed = isNodeFixed;
            this.isObjectOnly = isSubjectOnly;
            this.x = x;
            this.y = y;
        }

        void addObjectEdge(Edge edge) {
            linkedObjectNodes.add(edge);
            edge.object.linkedSubjectNodes.add(edge);
        }
        
        void addLiteral(Literal literal) {
            linkedLiterals.add(literal);
        }
        
        Iterator getLiterals() {
            return linkedLiterals.iterator();
        }

        boolean isObjectOf(final Node node) {
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

    class Predicate {
        final String namespace;
        final String property;

        Predicate(final String namespace, final String property) {
            this.namespace = namespace;
            this.property = property;
        }
        
        public String toString() {
        	    return this.property;
        }
    }

    class Edge {
        final Predicate predicate;
        final Node subject;
        final Node object;

        Edge(final Node subject, final Predicate predicate,  final Node object) {
            this.predicate = predicate;
            this.subject = subject;
            this.object = object;
        }
    }
    
    public class Literal {
        final Predicate predicate;
        final String literal;

        Literal(final Predicate predicate, final String literal) {
            this.predicate = predicate;
            this.literal = literal;
        }
    }
    
    public void addEntry(int hashSubject, int hashObject, String prNamespace, String prURI) {
        if (hashSubject == hashObject) return;
        Point key = new Point(hashSubject, hashObject);
        Object value = hash.get(key);
        if (value != null) { 
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

    public Node addNode(String unique, float[] coordinates, boolean isNodeFixed, boolean isSubject) {
        Node node;
        if(coordinates!=null) {
        	node = new Node(unique, coordinates[0], coordinates[1], isNodeFixed, isSubject);
        } else {
            node = new Node(unique, isSubject);
        }
        if (nodes.add(node))
            return node;
        else
            return null;
    }
    
    public Node addNode(String unique, boolean isSubject) {
        Node node = new Node(unique, isSubject);
        if (nodes.add(node))
            return node;
        else
            return null;
    }

    public Edge getEdge(Node subject, String namespace, String predicate, Node object) {
        return new Edge(subject, new Predicate(namespace, predicate), object);
    }

    public Literal getLiteral(String namespace, String predicate, String literal) {
        return new Literal(new Predicate(namespace, predicate), literal);
    }
    
    public void setLabel(String unique, String label) {
        getNode(unique).label = label;
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

    Set tmpNodes = new HashSet();
    public void verifyVisualized(CheckTree tree) {
        for (Iterator it = nodes.iterator(); it.hasNext();) {
            Node node = (Node) it.next();
            
            tmpNodes.clear();
            
            boolean flag = false;
            for (Iterator i = node.linkedObjectNodes.iterator(); i.hasNext();) {
                Edge edge = (Edge) i.next();
                if (tree.isChecked(edge.predicate.property)) {
                    flag = true;
                    tmpNodes.add(edge.object);
                }
            }

            if (flag) {
                node.isVisible = true;
                for(Iterator ite = tmpNodes.iterator(); ite.hasNext();) 
                    ((Node)ite.next()).isVisible = true;
            } else
                node.isVisible = false;

        }
    }
}
