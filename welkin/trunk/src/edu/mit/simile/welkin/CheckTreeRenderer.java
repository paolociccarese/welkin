/*
 * Created on 14-ott-2004
 */
package edu.mit.simile.welkin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Hashtable;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.tree.TreeCellRenderer;

/**
 * @author Paolo Ciccarese
 */
public class CheckTreeRenderer extends JPanel implements TreeCellRenderer {
    private JCheckBox box;
    private TreeLabel label;
    Hashtable table;
    Hashtable checked;
    Icon icon, cicon;
    String intermediateImage = "resources/icons/file.gif";

    public CheckTreeRenderer(Hashtable table, Hashtable checked) {
        this.table = table;
        this.checked = checked;
        BorderLayout b;
        setLayout(new BorderLayout(0, 0));
        box = new JCheckBox();
        icon = box.getIcon();
        cicon = new javax.swing.ImageIcon(getClass().getResource(
                intermediateImage));
        label = new TreeLabel();
        super.setBorder(null);

        add(box, BorderLayout.WEST);
        add(label, BorderLayout.CENTER);
        label.setBackground(Color.white);
        box.setBackground(UIManager.getColor("Tree.textBackground"));
        label.setForeground(UIManager.getColor("Tree.textForeground"));
    }

    public boolean isCheckBox(int x, int y) {
        Component[] comp = this.getComponents();
        for (int i = 0; i < comp.length; i++) {
            if (comp[i].contains(x, y) == true) {
                if (comp[i] instanceof JCheckBox)
                    return true;
            }
        }
        return false;
    }

    public java.awt.Component getTreeCellRendererComponent(JTree tree,
            Object value, boolean isSelected, boolean expanded, boolean leaf,
            int row, boolean hasFocus) {
        String stringValue = tree.convertValueToText(value, isSelected,
                expanded, leaf, row, hasFocus);
        setEnabled(tree.isEnabled());
        Integer key = new Integer(value.hashCode());

        label.setFont(tree.getFont());
        label.setText(stringValue);
        boolean bool = table.containsKey(key) ? true : false;
        boolean bool2 = checked.containsKey(key) ? true : false;
        box.setSelected(bool);
        label.setSelected(isSelected);
        label.setFocus(hasFocus);

        if (bool2 && !bool)
            box.setIcon(cicon);
        else
            box.setIcon(icon);

        if (leaf)
            label.setIcon(UIManager.getIcon("Tree.leafIcon"));
        else if (expanded)
            label.setIcon(UIManager.getIcon("Tree.openIcon"));
        else
            label.setIcon(UIManager.getIcon("Tree.closedIcon"));
        return this;
    }

    /* swing1.1 */
    /* Credit goes to http://www2.gol.com/users/tame/ */
    //package jp.gr.java_conf.tame.swing.tree;
    public class TreeLabel extends JLabel {
        boolean isSelected;
        boolean hasFocus;

        public TreeLabel() {
        }

        public void setBackground(Color color) {
            if (color instanceof ColorUIResource)
                color = null;
            super.setBackground(color);
        }

        public void paintComponent(Graphics g) {
            String str;
            if ((str = getText()) != null) {
                if (0 < str.length()) {
                    if (isSelected) {
                        g.setColor(UIManager
                                .getColor("Tree.selectionBackground"));
                    } else {
                        g.setColor(UIManager.getColor("Tree.textBackground"));
                    }
                    Dimension d = getPreferredSize();
                    int imageOffset = 0;
                    Icon currentI = getIcon();
                    if (currentI != null) {
                        imageOffset = currentI.getIconWidth()
                                + Math.max(0, getIconTextGap() - 1);
                    }
                    //g.fillRect(imageOffset, 0, d.width- imageOffset+1,
                    // d.height+10);
                    g.fillRect(0, 0, d.width + 1, d.height + 10);

                    if (hasFocus) {
                        g.setColor(UIManager
                                .getColor("Tree.selectionBorderColor"));
                        //g.drawRect(imageOffset, 0, d.width -1 - imageOffset,
                        // d.height+2); //-1
                        g.drawRect(0, 0, d.width - 1, d.height + 2); //-1

                    }
                }
            }
            super.paintComponent(g);
        }

        public Dimension getPreferredSize() {
            Dimension retDimension = super.getPreferredSize();
            if (retDimension != null) {
                retDimension = new Dimension(retDimension.width + 3,
                        retDimension.height);
            }
            return retDimension;
        }

        public void setSelected(boolean isSelected) {
            this.isSelected = isSelected;
        }

        public void setFocus(boolean hasFocus) {
            this.hasFocus = hasFocus;
        }
    }

}

