package edu.mit.simile.welkin;

import java.awt.Rectangle;
import java.awt.event.MouseListener;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import edu.mit.simile.welkin.ModelCache.WUriBase;
import edu.mit.simile.welkin.tree.NamespaceTreeNode;
import edu.mit.simile.welkin.tree.PropertyToResourceTreeNode;
import edu.mit.simile.welkin.tree.TreeNode;

public class PredicatesTree extends JTree {
    Mouse mouseListener;
    Welkin welkin;
    PredicatesTreeRenderer renderer;
    Hashtable map;
    Hashtable checked;
    boolean pathcheck = false;
    
    public PredicatesTree(Welkin welkin) {
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
        super.setCellRenderer(renderer = new PredicatesTreeRenderer(map, checked));
        this.addMouseListener(mouseListener);
    }
 
    public void buildTree() {
        init();
        fillTree();
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
        DefaultMutableTreeNode root=new DefaultMutableTreeNode("Predicates uri");
        DefaultTreeModel model=new DefaultTreeModel(root);
        for(Iterator it=welkin.wrapper.cache.predicatesBases.iterator();it.hasNext();) {
            WUriBase ns=((WUriBase)it.next());
            TreeNode nsTreeNode=new NamespaceTreeNode(ns.base);
            model.insertNodeInto(nsTreeNode,root,root.getChildCount());
            for(Iterator i=ns.locals.iterator();i.hasNext();) {
                model.insertNodeInto(new PropertyToResourceTreeNode(ns.base,(String)i.next()),nsTreeNode,nsTreeNode.getChildCount());
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
        if (PredicatesTree.this.pathcheck)
            addCheck(path.getParentPath(), false);
    }

    public void removeTreeCheck(TreePath path) {
        Integer key = new Integer(path.getLastPathComponent().hashCode());
        if (map.containsKey(key) == false)
            return;
        map.remove(key);
        if (PredicatesTree.this.pathcheck)
            removeCheck(path.getParentPath(), false);
    }
    
    private void forwardPropagation(DefaultMutableTreeNode node, boolean bool) {
        checkManagement(new TreePath(node.getPath()),bool);
        for (int i = 0; i < PredicatesTree.this.getModel().getChildCount(node); i++) {
            forwardPropagation((DefaultMutableTreeNode) PredicatesTree.this.getModel().getChild(node,i),bool);
        }
    }
    
    private void backwardPropagation(DefaultMutableTreeNode node, boolean bool) {
        boolean allSelectedFlag = true;
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
        if(parent==null) return;
        for(Enumeration e = parent.children(); e.hasMoreElements(); ) {
            if(!PredicatesTree.this.isChecked(e.nextElement()))
                allSelectedFlag = false;
        }
        
        boolean allNotSelectedFlag = true;
        for(Enumeration e = parent.children(); e.hasMoreElements(); ) {
            if(PredicatesTree.this.isChecked(e.nextElement()))
                allNotSelectedFlag = false;
        }
        
        if(allSelectedFlag||allNotSelectedFlag) {
            if (bool)
                PredicatesTree.this.setTreeCheck(new TreePath(parent.getUserObjectPath()));
            else
                PredicatesTree.this.removeTreeCheck(new TreePath(parent.getUserObjectPath()));
        }
        backwardPropagation(parent,bool);
    }
    
    private void checkManagement(TreePath path, boolean bool) {
        if (bool)
            PredicatesTree.this.setTreeCheck(path);
        else
            PredicatesTree.this.removeTreeCheck(path);
    }
    
    public class Mouse implements MouseListener {

        public void mousePressed(java.awt.event.MouseEvent mouseEvent) {

            TreePath path = PredicatesTree.this.getPathForLocation(
                    mouseEvent.getX(), mouseEvent.getY());

            if (path == null) return;            
            
            Object value=path.getLastPathComponent();

            Integer key = new Integer(value.hashCode());
            Rectangle rect = PredicatesTree.this.getPathBounds(path);
            boolean isCheckBox = PredicatesTree.this.renderer.isCheckBox(mouseEvent
                    .getX()
                    - (int) rect.getX(), mouseEvent.getY() - (int) rect.getY());

            if (!isCheckBox) return;
            
            if (map.containsKey(key) == true) {
                map.remove(key);
                if (PredicatesTree.this.pathcheck)
                    removeCheck(path.getParentPath(), false);
            } else {
                map.put(key, value);
                if (PredicatesTree.this.pathcheck)
                    addCheck(path.getParentPath(), false);
            }
            
            boolean bool=PredicatesTree.this.isChecked(value);
            forwardPropagation((DefaultMutableTreeNode) value, bool);
            backwardPropagation((DefaultMutableTreeNode) value, bool);
            PredicatesTree.this.welkin.notifyTreeChange();
            PredicatesTree.this.repaint();
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