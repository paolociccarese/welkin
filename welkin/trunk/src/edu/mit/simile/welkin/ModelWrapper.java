package edu.mit.simile.welkin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

import edu.mit.simile.welkin.InfoCache.Node;

/**
 * @author Paolo Ciccarese <paolo@hcklab.org>
 */
public class ModelWrapper {
    private Model model;
    public InfoCache cache;
    
    private Map groups = new HashMap(5);

    private final String MODEL_PROPERTY = "welkin/model";
    private final String FIX = "welkin/fix";
    private final String NODE_X = "welkin/x";
    private final String NODE_VX = "welkin/vx";
    private final String NODE_Y = "welkin/y";
    private final String NODE_VY = "welkin/vy";
    
    private final String LABEL = "http://www.w3.org/2000/01/rdf-schema#label";

    private Property modelProperty;
    private Property nodeX;
    private Property nodeVX;
    private Property nodeY;
    private Property nodeVY;
    private Property fix;
    
    private Property label;

    class Group {
        String name;

        List nodes = new ArrayList(10);

        Group(String name) {
            this.name = name;
        }
    }

    /**
     * Constructs the rdf core of welkin.
     * 
     * @param model
     *            The rdf model.
     * @param uri
     *            The uri of the model
     */
    public ModelWrapper() {
        model = ModelFactory.createDefaultModel();
        cache = new InfoCache();

        modelProperty = model.createProperty(MODEL_PROPERTY);
        nodeX = model.createProperty(NODE_X);
        nodeVX = model.createProperty(NODE_VX);
        nodeY = model.createProperty(NODE_Y);
        nodeVY = model.createProperty(NODE_VY);
        fix = model.createProperty(FIX);
        
        label=model.createProperty(LABEL);
    }

    /**
     * Adds a new rdf model to the xisting one.
     * 
     * @param model
     *            The new rdf model.
     * @param uri
     *            The name of the model.
     */
    public void addModel(final Model model, final String uri) {
        setModelOwner(model, uri);
        this.model = this.model.union(model);
    }

    /**
     * Sets the properties of the model.
     * 
     * @param model
     *            The new rdf model.
     * @param uri
     *            The name of the model.
     */
    private void setModelOwner(Model model, final String uri) {
        for (Iterator it = getNodes(this.model).iterator(); it.hasNext();) {
            Object obj = it.next();

            if (obj instanceof Resource)
                model.add(model.createStatement((Resource) obj, modelProperty,
                        model.createResource(uri)));

            // I don't fix the literals owner
            // ------------------------------
            //            else {
            //                System.out.println(obj.getClass().getName());
            //                System.out.println(obj);
            //                model.add(model
            //                        .createStatement(model.createResource(obj
            //                                .toString()), modelProperty, model
            //                                .createResource(name)));
            //            }
        }
    }

    /**
     * Returns the rdf core of welkin.
     * 
     * @return The rdf model.
     */
    public Model getModel() {
        return model;
    }

    /**
     * Returns the list of resources (both Subjects and Objects) of the model.
     * It doesn't return literals.
     * 
     * @return The list of resources of the model.
     */
    public List getNodes() {
        return getNodes(model);
    }

    /**
     * Returns all the unique resources of the rdf model. It doesn't return
     * literals.
     * 
     * @return The list of unique resources in the rdf model.
     */
    public synchronized List getNodes(Model model) {
        List nodes = new ArrayList();

        for (Iterator its = model.listSubjects(); its.hasNext();) {
            Object obj = its.next();
            if (!nodes.contains(obj))
                nodes.add(obj);
        }

        for (Iterator ito = model.listObjects(); ito.hasNext();) {
            Object obj = ito.next();

            if (obj instanceof Resource)
                if (!nodes.contains(obj))
                    nodes.add(obj);
        }

        return nodes;
    }

    //TODO Remove the method changing the visualizer
    public float[] getCoordinateXY(Resource res) {
            return cache.getCoordinatesXY((res.isAnon()?res.getId().toString():res.getURI()));
    }

    //TODO Remove the method changing the visualizer   
    public void setCoordinateXY(Resource res, float x, float y) {
        cache.setCoordinatesXY((res.isAnon()?res.getId().toString():res.getURI()),x,y);
    }

    //TODO Not used anymore
    public Property getConnection(Resource from, Resource to) {
        for (Iterator i = model.listStatements(
                model.getResource(from.getURI()), null, model.getResource(to
                        .getURI())); i.hasNext();) {
            Object ob = i.next();

            return ((Statement) ob).getPredicate();
        }

        return null;
    }

    //TODO Not used anymore
    public List getConnectionTo(Resource res) {
        List finale = new ArrayList();
        for (Iterator i = model.listStatements(model.getResource(res.getURI()),
                null, (RDFNode) null); i.hasNext();) {
            Object ob = i.next();

            if (((Statement) ob).getObject() instanceof Resource)
                finale.add(((Statement) ob).getObject());
        }

        return finale;
    }

    public void addGroup(Object o, String name) {
        // TODO Group
        //		if (!groups.containsKey(o)) {
        //			Group group = new Group(name);
        //			groups.put(o,group);
        //		}
    }

    public void setFixedNode(Resource res, boolean bol) {
        if (bol)
            model.add(res, fix, bol);
        else
            freeFixedNode(res);
    }

    public boolean getFixedNode(Resource res) {
        for (Iterator i = model.listStatements(res, fix, true); i.hasNext();) {
            return true;
        }

        return false;
    }

    public void freeFixedNode(Resource res) {
        Collection coll = new ArrayList();

        for (Iterator i = model.listStatements(model.getResource(res.getURI()),
                fix, (RDFNode) null); i.hasNext();) {
            coll.add((Statement) i.next());
        }

        for (Iterator i = coll.iterator(); i.hasNext();) {
            model.remove((Statement) i.next());
        }
    }

    public void highlightNode(String text, boolean highlight) {
        // TODO Highlighting
    }

    public void clearHighlights() {
        // TODO Clear Highlights
    }
    
    public boolean importModel() throws FileNotFoundException {
        File fileName;

        // Save File Manager
        JFileChooser openWin = new JFileChooser();
        //saveWin.setFileFilter(new GeneDataFilter());

        int returnVal = openWin.showOpenDialog(null);
        
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            fileName = openWin.getSelectedFile();

            // create an empty model
            Model model = ModelFactory.createDefaultModel();

            FileInputStream in = new FileInputStream(fileName);
            if (in == null) {
                throw new IllegalArgumentException("File: " + fileName
                        + " not found");
            }

            addModel(inChaching(model.read(in, "")), fileName.getName());

            Welkin.log("Statements processed: " + model.size());
            return true;
        }
        return false;
    }
    
    private Model inChaching(Model model) {
        Iterator its = model.listSubjects();
        Iterator ito = model.listObjects();
        
        List total = new ArrayList();
        while(its.hasNext()) {
            total.add(its.next());
        }
        while(ito.hasNext()) {
            total.add(ito.next());
        }
        
        for(Iterator it=total.iterator();it.hasNext();) {
            float x=0;
            float y=0;
            boolean flagX=false;
            boolean flagY=false;
            String unique;
            Node node;
            
            // Querying the model for coordinates
            Object o=it.next(); 
            if(!(o instanceof Resource)) continue;          
            Resource res = (Resource) o;
            for(Iterator ix=model.listStatements(res,nodeX,(RDFNode)null);ix.hasNext();) {
                Statement st = (Statement) ix.next();
                x = st.getFloat();
                flagX=true;
                break;
            }
            for(Iterator ix=model.listStatements(res,nodeY,(RDFNode)null);ix.hasNext();) {
                Statement st = (Statement) ix.next();
                y = st.getFloat();
                flagY=true;
                break;
            }
            
            // Querying the model for URI or Id
            unique=res.isAnon()?res.getId().toString():res.getURI();
            if(flagX && flagY)
                node = cache.addNode(unique, unique,x,y);
            else
                node = cache.addNode(unique, unique);
            
            if(node!=null) {
	            for(Iterator ix=model.listStatements(res,fix,(RDFNode)null);ix.hasNext();) {
	                Statement st = (Statement) ix.next();
	                node.fixed=st.getBoolean();
	                break;
	            }    
            }
        }
        
        for(Iterator it1 = cache.nodes.iterator();it1.hasNext();) {
            Node node = (Node) it1.next();
            for (Iterator io = model.listStatements(model.getResource(node.unique), null, (RDFNode) null); io
            .hasNext();) {
                Statement st = (Statement) io.next();
                Property pr = st.getPredicate();
                RDFNode obj2 = st.getObject();
                if (obj2 instanceof Resource) {
                    String un = ((Resource) obj2).isAnon() ? ((Resource) obj2)
                            .getId().toString() : ((Resource) obj2).getURI();
                            
                    Node objectNode = cache.getNode(un);
                    node.addObjectEdge(cache.getEdge(pr.getNameSpace(), pr.getURI(),objectNode));
                    cache.addEntry(node.hash,objectNode.hash, pr.getNameSpace(), pr.getURI());
                } else {
                    String literal = ((Literal)obj2).toString();
                    node.addLiteral(cache.getLiteral(pr.getNameSpace(), pr.getURI(),literal));
                }
            }
        }
        
        for(Iterator it = cache.nodes.iterator(); it.hasNext();) {
            Node node = (Node) it.next();
            for(Iterator i=model.listStatements(model.getResource(node.unique),label,(RDFNode)null);i.hasNext();) {
                String lab=((Statement)i.next()).getObject().toString();
                cache.setLabel(node.unique,lab);
                break;
            }          
        }
        
        return model;
    }
    
    private void outCaching(Model model) {
        
    }

    public Iterator getLiterals(Resource res) {
        List lits = new ArrayList();
        for (Iterator it = model.listStatements(res, null, (RDFNode) null); it
                .hasNext();) {
            Statement st = (Statement) it.next();
            if (!(st.getObject() instanceof Resource))
                lits.add(st);
        }

        return lits.iterator();
    }

    /**
     *  Clears 
     */
    public void clear() {
        model = ModelFactory.createDefaultModel();
        cache.nodes.clear();
    }
}