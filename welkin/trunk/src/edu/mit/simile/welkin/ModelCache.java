package edu.mit.simile.welkin;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.openrdf.model.URI;

public class ModelCache {

    public static final String ANONYMOUS_RES = "Anonymous resource";
    
    boolean blankNodes = false;    
    Set resources = new HashSet();
    List predicatesBases = new ArrayList();
    List resourcesBases = new ArrayList();
    HashMap hash = new HashMap();
    
    public void clear() {
        blankNodes = false;
        resources.clear();
        predicatesBases.clear();
        resourcesBases.clear();
        hash.clear();
    }
    
    public void addStatement(int hashSubject, int hashObject, URI predicate) {
        if (hashSubject == hashObject) return;
        WDoubleHashKey key = new WDoubleHashKey(hashSubject, hashObject);
        Object value = hash.get(key);
        if (value != null) { 
            Object[] values = (Object[]) value;
            URI newValues[] = new URI[values.length+1];
            System.arraycopy(values, 0, newValues, 0, values.length);
            newValues[values.length] = predicate;
        } else {
            URI[] values = new URI[1];
            values[0] = predicate;
            hash.put(key, values);
        }
    }
    
    public int addPredicatesUri(URI uri) {
        WUriBase ns = new WUriBase(uri.getNamespace(), ResourceUriBasePanel.DEFAULT_URI_COLOR);
        if(!predicatesBases.contains(ns)) {
            ns.addProperty(uri.getLocalName());
            predicatesBases.add(ns);
            return predicatesBases.indexOf(ns);
        }
        
        int index = predicatesBases.indexOf(ns);
        ((WUriBase)predicatesBases.get(index)).addProperty(uri.getLocalName());
        return predicatesBases.indexOf(ns);
    }
    
    public int addResourcesUri(URI uri) {
        WUriBase ns = new WUriBase(uri.getNamespace(), ResourceUriBasePanel.DEFAULT_URI_COLOR);
        if(!resourcesBases.contains(ns)) {
            resourcesBases.add(ns);
            return resourcesBases.indexOf(ns);
        }
        
        return resourcesBases.indexOf(ns);
    }

    WDoubleHashKey tmp = new WDoubleHashKey();  
    public URI[] getEntries(int hashSubject, int hashObject) {
        tmp.x = hashSubject;
        tmp.y = hashObject;
        return (URI[]) hash.get(tmp);
    }
    
    public WResource addResource(String unique, float[] coordinates, boolean isNodeFixed, boolean isNotSubject) {
        WResource resource;
        if(coordinates!=null) {
            resource = new WResource(unique, coordinates[0], coordinates[1], isNodeFixed, isNotSubject);
        } else {
            resource = new WResource(unique, isNotSubject);
        }
        if (resources.add(resource))
            return resource;
        else
            return null;
    }
    
    public WResource addResource(String unique, boolean isNotSubject) {
        WResource res = new WResource(unique, isNotSubject);
        if (resources.add(res)) return res;
        else {
            for(Iterator it = resources.iterator(); it.hasNext();) {
                WResource tmp = (WResource) it.next();
                if(!isNotSubject) tmp.isNotSubject = false;
                if(res.equals(tmp)) return tmp;
            }
        }
        return null;
    }
    
    public WResource addBlankResource(String unique) {
        if(!blankNodes) {
            blankNodes = true;
            WUriBase ns = new WUriBase(ModelCache.ANONYMOUS_RES, ResourceUriBasePanel.DEFAULT_URI_COLOR);
            if(!resourcesBases.contains(ns)) {
                resourcesBases.add(ns);
            }
        }
        
        WResource res = new WResource(unique, true, true);
        if (resources.add(res)) return res;
        else {
            for(Iterator it = resources.iterator(); it.hasNext();) {
                WResource tmp = (WResource) it.next();
                if(res.equals(tmp)) return tmp;
            }
        }
        return null;
    }
    
    public void clearUriColors() {
        for(Iterator it = resourcesBases.iterator(); it.hasNext();) {
            WUriBase uri = (WUriBase) it.next();
            uri.color = ResourceUriBasePanel.DEFAULT_URI_COLOR;
        }
    }
    
    public void setUriColor(String prefix, Color color) {
        for(Iterator it = resourcesBases.iterator(); it.hasNext();) {
            WUriBase uri = (WUriBase) it.next();
            if(uri.base.startsWith(prefix))
                uri.color = color;
        }
    }
    
    public WResource getResource(String unique) {
        for (Iterator it = resources.iterator(); it.hasNext();) {
            WResource tmp = (WResource) it.next();
            if (tmp.unique.equals(unique))
                return tmp;
        }
        return null;
    }
      
    public WStatement getStatement(WResource subject, URI predicate, WResource object) {
        return new WStatement(subject, predicate, object);
    }

    public WLiteral getLiteral(URI predicate, String literal) {
        return new WLiteral(predicate, literal);
    }
    
    public void setLabel(String unique, String label) {
        getResource(unique).label = label;
    }

    Set validatedNodes = new HashSet();
    public void uriBasedVisualization(PredicatesTree tree) {
        validatedNodes.clear();
        for (Iterator it = resources.iterator(); it.hasNext();) {
            WResource node = (WResource) it.next();
            
            boolean flag = false;
            for (Iterator i = node.linkedObjectNodes.iterator(); i.hasNext();) {
                WStatement edge = (WStatement) i.next();
                String tmp = edge.predicate.toString();
                if (tree.isChecked(edge.predicate.toString())) {
                    flag = true;
                    validatedNodes.add(edge.object);
                }
            }

            if(flag) validatedNodes.add(node);
            else node.isVisible = false;
        }
        
        for(Iterator it = validatedNodes.iterator(); it .hasNext();) {
            ((WResource)it.next()).isVisible = true;
        }   
    }
    
    public void adjustResourcesUriBaseColor() {
        for(Iterator i=resourcesBases.iterator();i.hasNext();) {
            WUriBase ns = (WUriBase) i.next();
	        for(Iterator it=resources.iterator();it.hasNext();) {
	            WResource tmp = (WResource) it.next();
	            if(blankNodes && ns.base.equals(ModelCache.ANONYMOUS_RES) && tmp.isBlankNode ) {
	                tmp.color = ns.color;
	            } else if(Util.getNameSpace(tmp.unique).equals(ns.base)) {
	                tmp.color = ns.color;
	            }
	        }
        }
    }
    
    public class WResource {
        float x, y, vx, vy;
        
        final String unique;
        
        boolean fixed = false;
        boolean isVisible = true;
        boolean isBlankNode = false;
        boolean highlighted = false;
        boolean isNotSubject = true;
        
        Set linkedSubjectNodes = new HashSet();
        Set linkedObjectNodes = new HashSet();
        Set linkedLiterals = new HashSet();
        
        int hash = 0;
        
        Color color;
        String label;
        
        WResource(final String unique, final boolean isNotSubject) {
            this.unique = this.label = unique;
            this.isNotSubject = isNotSubject;
            calculateHashCode();

            this.x = (float) ((Math.random() - 0.5d) * 200.0d);
            this.y = (float) ((Math.random() - 0.5d) * 200.0d);
        }
        
        WResource(final String unique, final boolean isNotSubject, boolean isBlank) {
            this.unique = this.label = unique;
            this.isBlankNode = isBlank;
            this.isNotSubject = isNotSubject;
            calculateHashCode();

            this.x = (float) ((Math.random() - 0.5d) * 200.0d);
            this.y = (float) ((Math.random() - 0.5d) * 200.0d);
        }
        
        WResource(final String unique, final float x, final float y, final boolean isFixed, final boolean isNotSubject) {
            this.unique = this.label = unique;
            calculateHashCode();

            this.fixed = isFixed;
            this.isNotSubject = isNotSubject;
            this.x = x;
            this.y = y;
        }

        void addObjectStatement(WStatement statement) {
            linkedObjectNodes.add(statement);
            statement.object.linkedSubjectNodes.add(statement);
        }
        
        void addLiteral(WLiteral literal) {
            linkedLiterals.add(literal);
        }
        
        Iterator getLiterals() {
            return linkedLiterals.iterator();
        }

        boolean isObjectOf(final WResource res) {
            for (Iterator it = linkedObjectNodes.iterator(); it.hasNext();) {
                if (((WStatement) it.next()).object.equals(res))
                    return true;
            }
            return false;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || !(o instanceof WResource)) return false;

            if (this.unique.equals(((WResource) o).unique)) return true;
            else return false;
        }

        public int hashCode() { return hash; }

        private void calculateHashCode() {
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

    public class WLiteral {
        final URI predicate;
        final String literal;

        WLiteral(final URI predicate, final String literal) {
            this.predicate = predicate;
            this.literal = literal;
        }
        
        public boolean equals(Object obj) {
            if(obj instanceof WLiteral) {
                WLiteral lit = (WLiteral)obj;
                if(lit.literal.equals(literal) && lit.predicate.equals(predicate))
                        return true;
            }
            return false;
        }
        
        public int hashCode() {
            int h = 0;
            int off = 0;
            char val[] = literal.toCharArray();
            int len = literal.length();

            for (int i = 0; i < len; i++) {
                h = 31 * h + val[off++];
            }
            
            return predicate.hashCode() + 16 * h;
        }     
    }
    
    public class WStatement {
        final URI predicate;
        final WResource subject, object;

        WStatement(final WResource subject, final URI predicate,  final WResource object) {
            this.predicate = predicate;
            this.subject = subject;
            this.object = object;
        }
        
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || !(o instanceof WStatement)) return false;

            if (subject.equals(((WStatement)o).subject) 
                    && object.equals(((WStatement)o).object)
                    && predicate.equals(((WStatement)o).predicate)) return true;
            else return false;
        }
        
        public int hashCode() {
            return subject.hashCode()+predicate.hashCode()*5+object.hashCode()*10;
        }
    }
    
    class WUriBase {
        int hash=0;
        String base;
        Color color;
        Set locals = new HashSet();
        WUriBase(final String base, final Color color) {
            this.base = base;
            this.color=color;
            calculateHashCode();
        }
        
        public void addProperty(String property) {
            locals.add(property);
        }
        
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || !(o instanceof WUriBase)) return false;

            if (this.base.equals(((WUriBase) o).base)) return true;
            else return false;
        }
        
        public int hashCode() { return hash; }

        private void calculateHashCode() {
            int h = hash;
            if (h == 0) {
                int off = 0;
                char val[] = base.toString().toCharArray();
                int len = base.toString().length();

                for (int i = 0; i < len; i++) {
                    h = 31 * h + val[off++];
                }
                hash = h;
            }
        }
    }
    
    class WDoubleHashKey {
        int x,y;
        public WDoubleHashKey () {}
        public WDoubleHashKey(final int x, final int y) {
            this.x = x;
            this.y = y;
        }
 
        public boolean equals(Object obj) {
        	if (obj instanceof WDoubleHashKey) {
        	    WDoubleHashKey pt = (WDoubleHashKey)obj;
        	    return (x == pt.x) && (y == pt.y);
        	}
        	return false;
        }
        
        public int hashCode() {
        	long bits = java.lang.Double.doubleToLongBits(x);
        	bits ^= java.lang.Double.doubleToLongBits(y) * 31;
        	return (((int) bits) ^ ((int) (bits >> 32)));
            }
    }

}
