/*
 * Created on 14-ott-2004
 */
package edu.mit.simile.welkin;

import java.awt.Rectangle;
import java.awt.event.MouseListener;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * @author Paolo Ciccarese
 */
public class CheckTree extends JTree {
    Welkin welkin;
    CheckTreeRenderer renderer;
    Hashtable map;
    Hashtable checked;
    boolean pathcheck = false;

    public CheckTree(Welkin welkin) {
        super();
        this.welkin = welkin;
        init();
    }
    
    public CheckTree(TreeModel model) {
        super(model);
        init();
    }

    public CheckTree(TreeNode node, boolean bool) {
        super(node, bool);
        init();
    }
    
    public void init() {
        map = new Hashtable();
        checked = new Hashtable();
        super.setCellRenderer(renderer = new CheckTreeRenderer(map, checked));
        this.addMouseListener(new Mouse());
    }
 
    public void fillTree(Iterator iter) {
        DefaultMutableTreeNode root=new DefaultMutableTreeNode("base");
        DefaultTreeModel model=new DefaultTreeModel(root);
        for(Iterator it=iter;it.hasNext();) {
            model.insertNodeInto(new NamespaceTreeNode((String)it.next()),root,root.getChildCount());
        }
        
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
            removePathCheck();
        else {
            removePathCheck();
            includePathCheck();
        }
        repaint();
    }

    private void removePathCheck() {
        checked.clear();
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

    public boolean getPathCheck() {
        return this.pathcheck;
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
    
    public class Mouse implements MouseListener {

        private void propagate(DefaultMutableTreeNode node, boolean bool) {
            checkManagement(new TreePath(node.getPath()),bool);
            for (int i = 0; i < CheckTree.this.getModel().getChildCount(node); i++) {
                propagate((DefaultMutableTreeNode) CheckTree.this.getModel().getChild(node,i),bool);
            }
        }

        private void checkManagement(TreePath path, boolean bool) {
            if (bool)
                CheckTree.this.setTreeCheck(path);
            else
                CheckTree.this.removeTreeCheck(path);
        }

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
            propagate((DefaultMutableTreeNode) value,bool);
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