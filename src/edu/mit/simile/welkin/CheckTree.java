package edu.mit.simile.welkin;

import java.awt.Rectangle;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

import edu.mit.simile.welkin.InfoCache.Node;
import edu.mit.simile.welkin.tree.NamespaceTreeNode;
import edu.mit.simile.welkin.tree.PropertyToLiteralTreeNode;
import edu.mit.simile.welkin.tree.PropertyToResourceTreeNode;
import edu.mit.simile.welkin.tree.TreeNode;

public class CheckTree extends JTree {
    Mouse mouseListener;
    Welkin welkin;
    CheckTreeRenderer renderer;
    Hashtable map;
    Hashtable checked;
    boolean pathcheck = false;
    
    List namespaces;
    
    public class Namespace {
        String namespace;
        Set properties=new HashSet();
        public Namespace(String namespace) {
            this.namespace = namespace;
        }
        public void addProperty(PropertyToLiteralTreeNode property) {
            properties.add(property);
        }
        public void addProperty(PropertyToResourceTreeNode property) {
            properties.add(property);
        }
        public boolean equals(String namespace) {
            if (namespace == null) return false;

            if (this.namespace.equals(namespace))
                return true;
            else
                return false;
        }
    }
    
    public CheckTree(Welkin welkin) {
        super();
        this.welkin = welkin;
        init();
        defineEmptyRoot();
    }
    
    public void clear() {
        init();
        defineEmptyRoot();
    }
    
    public void init() {
        if(mouseListener!=null) this.removeMouseListener(mouseListener);
        mouseListener = new Mouse();
        map = new Hashtable();
        checked = new Hashtable();
        namespaces = new ArrayList();
        super.setCellRenderer(renderer = new CheckTreeRenderer(map, checked));
        this.addMouseListener(mouseListener);
    }
 
    public void buildTree() {
        init();
        listNamespaces();
        fillNamespaces();
        fillTree();
    }
    
    private void listNamespaces() {
        for(Iterator it=welkin.wrapper.getModel().listNameSpaces();it.hasNext();) {
            namespaces.add(new Namespace((String)it.next()));
        }       
    }
    
    private void fillNamespaces() {
        for(Iterator it=welkin.wrapper.cache.nodes.iterator();it.hasNext();) {
            Node node=((Node)it.next());
            Resource res=welkin.wrapper.getModel().getResource(node.unique);
            for(Iterator i=welkin.wrapper.getModel().listStatements(res,null,(RDFNode)null);i.hasNext();) {
                Statement st = (Statement)i.next();
                Property property = st.getPredicate();
                if(st.getObject() instanceof Resource)
                    findNamespace(property.getNameSpace()).addProperty(new PropertyToResourceTreeNode(property));
                else if(st.getObject() instanceof Literal)
                    findNamespace(property.getNameSpace()).addProperty(new PropertyToLiteralTreeNode(property));
            }
        }
    }
    
    private Namespace findNamespace(String namespace) {
        for(Iterator it=namespaces.iterator();it.hasNext();) {
            Namespace ns=(Namespace) it.next();
            if(ns.equals(namespace)) return ns;
        }
        return null;
    }
    
    private void defineEmptyRoot() {
        DefaultMutableTreeNode root=new DefaultMutableTreeNode("Any ontology");
        DefaultTreeModel model=new DefaultTreeModel(root);
        this.setModel(model);
        this.revalidate();
        this.validate();
        this.repaint();
    }
    
    public void fillTree() {
        DefaultMutableTreeNode root=new DefaultMutableTreeNode("Ontologies");
        DefaultTreeModel model=new DefaultTreeModel(root);
        for(Iterator it=namespaces.iterator();it.hasNext();) {
            Namespace ns=((Namespace)it.next());
            TreeNode nsTreeNode=new NamespaceTreeNode(ns.namespace);
            model.insertNodeInto(nsTreeNode,root,root.getChildCount());
            for(Iterator i=ns.properties.iterator();i.hasNext();) {
                model.insertNodeInto(((DefaultMutableTreeNode)i.next()),nsTreeNode,nsTreeNode.getChildCount());
            }
        }
        
        this.setTreeCheck(new TreePath(root.getPath()));
        forwardPropagation(root, true);
        this.setModel(model);
        this.revalidate();
        this.validate();
        this.repaint();
    }

    public class TreeSelect implements TreeSelectionListener {
        public void valueChanged(TreeSelectionEvent treeSelectionEvent) {
            Object value = treeSelectionEvent.getPath().getLastPathComponent();
        }
    }

    public void setPathCheck(boolean pathcheck) {
        this.pathcheck = pathcheck;
        if (this.pathcheck == false)
            checked.clear();
        else {
            checked.clear();
            includePathCheck();
        }
        repaint();
    }

    private void includePathCheck() {
        int[] rows = new int[getRowCount()];
        int[] selected = getSelectionRows();
        int rowcount = super.getRowCount();
        for (int i = 0; i < rowcount; i++) {
            if (isExpanded(i) == true)
                rows[i] = i;
            else
                rows[i] = -1;
        }

        for (int i = rowcount - 1; i >= 0; i--) {
            if (rows[i] != -1)
                collapseRow(rows[i]);
        }

        int rc = getRowCount();
        int be, ae;
        be = rc; // before expansion = current row count
        TreePath path;
        for (int i = 0; i < rc; i++) {
            path = getPathForRow(i);
            if (isChecked(path))
                addCheck(path.getParentPath(), false);

            expandRow(i);
            ae = getRowCount(); //after expansion = current row count..
            checkNodes(ae - be, i);
            collapseRow(i);
        }
        // End Traversal...

        //return them to normal status...
        for (int i = 0; i < rowcount; i++) {
            if (rows[i] != -1)
                expandRow(rows[i]);
        }
        if (selected != null)
            setSelectionRows(selected);
    }

    private void checkNodes(int amount, int row) {
        if (amount == 0)
            return;

        int be, ae;
        be = getRowCount();
        TreePath path;
        for (int i = 1; i <= amount; i++) {
            path = getPathForRow(i + row);
            if (isChecked(path))
                addCheck(path.getParentPath(), false);
            expandRow(i + row);
            ae = getRowCount();
            checkNodes(ae - be, i + row);
            collapseRow(i + row);
        }
    }

    public boolean isChecked(Object value) {
        Integer key = new Integer(value.hashCode());
        if (map.containsKey(key))
            return true;
        else
            return false;
    }

    public boolean isChecked(TreePath path) {
        return isChecked(path.getLastPathComponent());
    }

    public Object[] getCheckedObjects() {
        return map.values().toArray();
    }

    private void addCheck(TreePath path, boolean now) {
        if (path == null)
            return;
        Object value = path.getLastPathComponent();
        Integer v = new Integer(value.hashCode());
        if (checked.containsKey(v) == false || now)
            checked.put(v, new Integer(1));
        else
            checked.put(v, new Integer(1 + ((Integer) checked.get(v))
                    .intValue()));
        addCheck(path.getParentPath(), false);
    }

    private void removeCheck(TreePath path, boolean now) {
        if (path == null)
            return;
        Object value = path.getLastPathComponent();
        Integer v = new Integer(value.hashCode());
        if (checked.containsKey(v)) {
            int t = ((Integer) checked.get(v)).intValue();
            if (t <= 1 || now)
                checked.remove(v);
            else
                checked.put(v, new Integer(t - 1));
        }
        removeCheck(path.getParentPath(), false);
    }

    public void setTreeCheck(TreePath path) {
        Object value = path.getLastPathComponent();
        Integer key = new Integer(value.hashCode());
        if (map.containsKey(key) == true)
            return;
        map.put(key, value);
        if (CheckTree.this.pathcheck)
            addCheck(path.getParentPath(), false);
    }

    public void removeTreeCheck(TreePath path) {
        Integer key = new Integer(path.getLastPathComponent().hashCode());
        if (map.containsKey(key) == false)
            return;
        map.remove(key);
        if (CheckTree.this.pathcheck)
            removeCheck(path.getParentPath(), false);
    }
    
    private void forwardPropagation(DefaultMutableTreeNode node, boolean bool) {
        checkManagement(new TreePath(node.getPath()),bool);
        for (int i = 0; i < CheckTree.this.getModel().getChildCount(node); i++) {
            forwardPropagation((DefaultMutableTreeNode) CheckTree.this.getModel().getChild(node,i),bool);
        }
    }
    
    private void backwardPropagation(DefaultMutableTreeNode node, boolean bool) {
        boolean allSelectedFlag = true;
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
        if(parent==null) return;
        for(Enumeration e = parent.children(); e.hasMoreElements(); ) {
            if(!CheckTree.this.isChecked(e.nextElement()))
                allSelectedFlag = false;
        }
        
        boolean allNotSelectedFlag = true;
        for(Enumeration e = parent.children(); e.hasMoreElements(); ) {
            if(CheckTree.this.isChecked(e.nextElement()))
                allNotSelectedFlag = false;
        }
        
        if(allSelectedFlag||allNotSelectedFlag) {
            if (bool)
                CheckTree.this.setTreeCheck(new TreePath(parent.getUserObjectPath()));
            else
                CheckTree.this.removeTreeCheck(new TreePath(parent.getUserObjectPath()));
        }
        backwardPropagation(parent,bool);
    }
    
    private void checkManagement(TreePath path, boolean bool) {
        if (bool)
            CheckTree.this.setTreeCheck(path);
        else
            CheckTree.this.removeTreeCheck(path);
    }
    
    public class Mouse implements MouseListener {

        public void mousePressed(java.awt.event.MouseEvent mouseEvent) {

            TreePath path = CheckTree.this.getPathForLocation(
                    mouseEvent.getX(), mouseEvent.getY());

            if (path == null)
                return;            
            
            Object value=path.getLastPathComponent();

            Integer key = new Integer(value.hashCode());
            Rectangle rect = CheckTree.this.getPathBounds(path);
            boolean isCheckBox = CheckTree.this.renderer.isCheckBox(mouseEvent
                    .getX()
                    - (int) rect.getX(), mouseEvent.getY() - (int) rect.getY());

            if (!isCheckBox)
                return;
            if (map.containsKey(key) == true) {
                map.remove(key);
                if (CheckTree.this.pathcheck)
                    removeCheck(path.getParentPath(), false);
            } else {
                map.put(key, value);
                if (CheckTree.this.pathcheck)
                    addCheck(path.getParentPath(), false);
            }
            
            boolean bool=CheckTree.this.isChecked(value);
            forwardPropagation((DefaultMutableTreeNode) value, bool);
            backwardPropagation((DefaultMutableTreeNode) value, bool);
            CheckTree.this.welkin.notifyTreeChange();
            CheckTree.this.repaint();
        }

        public void mouseEntered(java.awt.event.MouseEvent mouseEvent) {
        }

        public void mouseExited(java.awt.event.MouseEvent mouseEvent) {
        }

        public void mouseClicked(java.awt.event.MouseEvent mouseEvent) {
        }

        public void mouseReleased(java.awt.event.MouseEvent mouseEvent) {
        }

    };
}