package edu.mit.simile.welkin;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ColorUIResource;

import edu.mit.simile.welkin.resource.PartialUri;

public class ResourcesTree extends JPanel {

    final Font font = new Font("Verdana", Font.PLAIN, 11);
    final Font bold = new Font("Verdana", Font.BOLD, 10);
    
    public static final Color DEFAULT_URI_COLOR = Color.red;
    
    public static final String EMPTY_LABEL = "No Resources Loaded!";
    public static final String ROOT_LABEL = "Resources";
    
    public static final Color BACKGROUND = Color.WHITE;
    
    public static final int MIN_VALUE = 0;
    public static final int MAX_VALUE = 360;
    public static final int INIT_VALUE = 0;
    
    static final String ICON_PATH = "resources/icons/";
    static final String OPEN_ICON = ICON_PATH + "openIcon.gif"; 
    static final String CLOSED_ICON = ICON_PATH + "closedIcon.gif"; 
    static final String LEAF_ICON = ICON_PATH + "leafIcon.gif"; 
    
    private int maxWidth=200;
    
    Welkin welkin;
    FullNode root;
    List elements;
    
    int vPos;
    int xPos;
    
    public ResourcesTree(Welkin welkin) {
        this.welkin = welkin;
        clear();
    }
    
    public void clear() {
        root = null;
        elements = new ArrayList();
        setEmptyTree();
    }
    
    public void setEmptyTree() {
        JLabel emptyLabel = new JLabel(EMPTY_LABEL);
        emptyLabel.setBounds(2,2,200,16);
        emptyLabel.setFont(font);
        
        this.removeAll();
        this.setLayout(null);
        this.setBackground(BACKGROUND);
        this.add(emptyLabel);
        this.setPreferredSize(new Dimension(300,20));
        this.repaint();
    }
    
    public void buildTree() {
        root = new FullNode(ROOT_LABEL,null);
        elements.add(root);
        
        for(Iterator it = welkin.wrapper.cache.resourcesBases.iterator(); it.hasNext();) {
        	PartialUri predicate = ((PartialUri)it.next());
        	String[] parts = Util.splitUriBases(predicate.getBase());
        	createNode(root, parts, predicate, 0);
        }
        
        calculateValues(root, root.slider.getValue());
        this.displayTree();
    	this.repaint();
    	
    	welkin.scrollingResTree.validate();
    }
    
    private void createNode(FullNode root, String[] parts, PartialUri all, int level) {
    	
    	if(parts[0]==null) return; // TODO Blank Nodes
    	
    	if(level == parts.length || (parts.length == 1 && level == 0)) {
        	for(int i = 0; i < root.children.size(); i++) {
        		if(((FullNode)root.children.get(i)).label.getText().equals(parts[level])) {
        			return;
        		} 
        	}
        	FullNode tmp = new FullNode(parts[parts.length-1], all);
        	if(level == 0) tmp.isVisible = true;
        	else tmp.isVisible = false;
        	root.children.add(tmp);
    		return;
    	}
    	boolean flag = false;
    	for(int i = 0; i < root.children.size(); i++) {
    		if(((FullNode)root.children.get(i)).label.getText().equals(parts[level])) {
    			level++;
    			createNode(((FullNode)root.children.get(i)), parts, all, level);
    			flag = true;
    		}
    	}
    	
    	if(!flag) {
    		FullNode child = new FullNode(parts[level++], null);
    		if(level == 2) child.isVisible = false;
    		root.children.add(child);
    		createNode(child, parts, all, level);
    	}
    }
    
    private void displayTree() {
        this.removeAll();
        this.setLayout(null);
        this.setBackground(BACKGROUND);
        
        root.slider.getValue();
        
        xPos=5;
        vPos=5;
        printNodes(root);
        
        this.validate();
        this.repaint();
        
        welkin.scrollingResTree.validate();
    }
    
    private void printNodes(FullNode node) {
        if(node.isAllowed) {
        	boolean open = true;
	        if(node.isVisible) {
		        node.setLocation(xPos,vPos);
		        maxWidth = maxWidth > (node.getDimension().width+50) ? maxWidth : (node.getDimension().width+50);
		        this.add(node);
		        vPos+=22;
		        
		        if(node.children.size()>0) xPos+=15;
		        for(int i=0; i<node.children.size();i++) {
		        	if(!((FullNode) node.children.get(i)).isVisible) open = false;
		            printNodes((FullNode) node.children.get(i));
		        }
		        if(node.children.size()>0) xPos-=15;
	        }
	        
	        if(node.children.size()==0) {
	            node.iconLabel.setIcon(new ImageIcon(Welkin.class.getResource(LEAF_ICON)));
	        } else if(open) {
	        	node.iconLabel.setIcon(new ImageIcon(Welkin.class.getResource(OPEN_ICON)));
	        } else {
	        	node.iconLabel.setIcon(new ImageIcon(Welkin.class.getResource(CLOSED_ICON)));
	        }
	        	
	        this.setPreferredSize(new Dimension(xPos+maxWidth, vPos+5));
        }
    }
    
    private void calculateValues(FullNode node, float ancestorValue) {
        for(int i=0; i<node.children.size();i++) {
            ((FullNode) node.children.get(i)).adjustValue(ancestorValue);
            calculateValues((FullNode) node.children.get(i),ancestorValue);
        }
        
        welkin.notifyBaseUriColorChange();
    }
    
    private void visualizeAll() {
        for(Iterator it=elements.iterator();it.hasNext();) {
            ((FullNode)it.next()).isAllowed = true;
        }
    }
    
    private void devisualizeAll() {
        for(Iterator it=elements.iterator();it.hasNext();) {
            ((FullNode)it.next()).isAllowed = false;
        }
    }
    
    public void crawlingTree(String prefix) {
        devisualizeAll();
        for(Iterator it=elements.iterator();it.hasNext();) {
            FullNode node = ((FullNode)it.next());
            if(node.resource != null && node.resource.getBase().startsWith(prefix))
                setAllowedBranch(node);
        }
        displayTree();
    }
    
    private void setAllowedBranch(FullNode node) {
        if(node == null) return;
        else {
            node.isAllowed = true;
            setAllowedBranch(node.father);
        }
    }
    
    class FullNode extends JPanel implements ChangeListener {
        private JLabel iconLabel;
        private JSlider slider;
        private TreeLabel label;
        
        boolean isVisible;
        boolean isAllowed;
        
        FullNode me;
        FullNode father;
        Vector children = new Vector();
        PartialUri resource;
        
        FullNode(String labelT, PartialUri resource) {
        	this.resource = resource;
            me=this;
            
            this.setLayout(new FlowLayout(FlowLayout.LEFT,0,0));
            this.setBackground(BACKGROUND);
            
            iconLabel = new JLabel();
            iconLabel.setSize(20,18);
            
            slider = new JSlider(JSlider.HORIZONTAL,MIN_VALUE,MAX_VALUE,INIT_VALUE);
            slider.addChangeListener(this);
            slider.setBackground(BACKGROUND);
            slider.setSize(new Dimension(80,14));
            slider.setPreferredSize(new Dimension(80,14));
            slider.setMajorTickSpacing(1);
            slider.setSnapToTicks(false);
            slider.setPaintTicks(false);
            
            this.label = new TreeLabel();
            this.label.setFont(font);
            this.label.setText(labelT);
            this.label.setBackground(BACKGROUND);
            
            this.add(iconLabel);
            this.add(slider);
            this.add(this.label);
            
            this.isVisible = true;
            this.isAllowed = true;
            
            adjustValue(INIT_VALUE);
            
            this.setSize(getDimension().width,18);
            
            this.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    if(e.getPoint().x<=20) {
                        openCloseNodeChildren(me);
                        displayTree();
                    }
                }
            });
        }
        
		public void adjustValue(float f) {
			slider.setValue((int)(f));
			adjustValue();
		}
		
		public void adjustValue() {
			if(resource!=null) resource.color = Color.getHSBColor(slider.getValue()/(float)MAX_VALUE,1,1);
			label.setForeground(Color.getHSBColor(slider.getValue()/(float)MAX_VALUE,1,1)) ;
		}

		private void openCloseNodeChildren (FullNode node) {
            if(node.children.size()>0) {
                if(((FullNode)node.children.get(0)).isVisible) {
                    closeChildren(node);
                } else {
                    openChildren(node);
                }
            }
        }
        
        private void openChildren(FullNode node) {
            for(int i=0;i<node.children.size();i++) 
                ((FullNode)node.children.get(i)).isVisible = true;
        }
        
        private void closeChildren(FullNode node) {
            for(int i=0;i<node.children.size();i++) 
                ((FullNode)node.children.get(i)).isVisible = false;
        }
        
        public void stateChanged(ChangeEvent e) { 
        	adjustValue();
            calculateValues(this,slider.getValue());
            this.repaint();
        }
        
        public Dimension getDimension() {
            return new Dimension (140+label.getPreferredSize().width,16);
        }
    }
    
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

                    g.setColor(UIManager.getColor("Tree.textBackground"));
                    
                    Dimension d = getPreferredSize();
                    int imageOffset = 0;
                    Icon currentI = getIcon();
                    if (currentI != null) {
                        imageOffset = currentI.getIconWidth()
                                + Math.max(0, getIconTextGap() - 1);
                    }
                    g.fillRect(0, 0, d.width + 1, d.height + 10);
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

        public void setFocus(boolean hasFocus) {
            this.hasFocus = hasFocus;
        }
    }
}
