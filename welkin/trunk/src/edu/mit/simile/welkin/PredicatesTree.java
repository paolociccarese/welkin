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

import edu.mit.simile.welkin.resource.PredicateUri;

public class PredicatesTree extends JPanel {
    
    final Font font = new Font("Verdana", Font.PLAIN, 11);
    final Font bold = new Font("Verdana", Font.BOLD, 10);
    
    public static final String EMPTY_LABEL = "Empty model!";
    public static final String ROOT_LABEL = "Predicates";
    
    public static final Color BACKGROUND = Color.WHITE;
    public static final Color ACTIVE_FOREG = Color.BLACK;
    public static final Color PASSIVE_FOREG = Color.GRAY;
    
    public static final int MIN_VALUE = 0;
    public static final int MAX_VALUE = 10;
    public static final int INIT_VALUE = 10;
    public static final float FACTOR = 10;
    
    static final String ICON_PATH = "resources/icons/";
    static final String OPEN_ICON = ICON_PATH + "openIcon.gif"; 
    static final String CLOSED_ICON = ICON_PATH + "closedIcon.gif"; 
    static final String LEAF_ICON = ICON_PATH + "leafIcon.gif"; 
    
    Welkin welkin;
    FullNode root;
    List elements;
    
    int maxWidth = 0;
    int vPos;
    int xPos;
    
    public PredicatesTree(Welkin welkin) {
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
        FullNode rootNode = new FullNode(ROOT_LABEL,null);
        elements.add(rootNode);
        
        for(Iterator it = welkin.wrapper.cache.predicates.iterator(); it.hasNext();) {
        	PredicateUri predicate = ((PredicateUri)it.next());
        	String[] parts = Util.getParts(predicate.getUri());
        	createNode(rootNode, parts, predicate, 0);
        }

        root = rootNode;
        calculateValues(root, root.value);
        this.displayTree();
    	this.repaint();
    }
    
    private void createNode(FullNode root, String[] parts, PredicateUri all, int level) {
    	if(level == 2) {
        	for(int i = 0; i < root.children.size(); i++) {
        		if(((FullNode)root.children.get(i)).label.getText().equals(parts[level])) {
        			return;
        		} 
        	}
        	FullNode tmp = new FullNode(parts[2], all);
        	tmp.isVisible = false;
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
    		if(level==2) child.isVisible = false;
    		root.children.add(child);
    		createNode(child, parts, all, level);
    	}
    }
    
    private void displayTree() {
        this.removeAll();
        this.setLayout(null);
        this.setBackground(BACKGROUND);
        
        root.value = root.slider.getValue()/FACTOR;
        //calculateValues(root, root.value);
        
        xPos=5;
        vPos=5;
        printNodes(root);
        
        this.validate();
        this.repaint();
    }
    
    private void printNodes(FullNode node) {
        if(node.isAllowed) {
	        if(node.isVisible) {
	        	node.setLocation(xPos,vPos);
	        	maxWidth = maxWidth > (node.getDimension().width) ? maxWidth : (node.getDimension().width);
	        	this.add(node);
	        	vPos+=22;
	        	
	            if(node.children.size()>0) xPos+=15;
	            for(int i=0; i<node.children.size();i++) {
	                printNodes((FullNode) node.children.get(i));
	            }
	            if(node.children.size()>0) xPos-=15;
	        }
	        
	        if(node.children.size()==0) {
	            node.iconLabel.setIcon(new ImageIcon(Welkin.class.getResource(LEAF_ICON)));
	        }
	        
	        this.setPreferredSize(new Dimension(xPos+maxWidth, vPos+5));
        } 
    }
    
    private void calculateValues(FullNode node, float ancestorValue) {
    	if(node.predicate!=null)
    			node.adjustValue();
        for(int i=0; i<node.children.size();i++) {
            ((FullNode) node.children.get(i)).sum = ancestorValue;
            ((FullNode) node.children.get(i)).adjustValue(Math.min(ancestorValue,1));
            calculateValues((FullNode) node.children.get(i),((FullNode) node.children.get(i)).value);
            ((FullNode) node.children.get(i)).setFace();
        }
        PredicatesTree.this.welkin.notifyTreeChange();
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
            if(node.absolute != null && node.absolute.getUri().startsWith(prefix))
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
        private Icon icon;
//        private JLabel weight;
        private JSlider slider;
        private TreeLabel label;
        
        PredicateUri absolute;
        
        /**
         * Actual value of the node
         */
        private float value;
        /**
         * Sum of values of the ancestors
         */
        private float sum;
        
        boolean isVisible;
        boolean isAllowed;
        boolean isSelected;
        
        FullNode me;
        FullNode father;
        Vector children = new Vector();
        PredicateUri predicate;
        
        FullNode(String labelT, PredicateUri predicate) {
        	this.predicate = predicate;
            me=this;
            
            this.setLayout(new FlowLayout(FlowLayout.LEFT,0,0));
            this.setBackground(BACKGROUND);
            
            iconLabel = new JLabel();
            iconLabel.setSize(20,18);
            
            icon = new ImageIcon(Welkin.class.getResource(CLOSED_ICON));
            iconLabel.setIcon(icon);
            
//            weight = new JLabel();
//            weight.setHorizontalAlignment(JTextField.RIGHT);
//            weight.setBackground(BACKGROUND);
//            weight.setFont(font);
//            weight.setSize(30,16);
//            weight.setBorder(null);
            
            slider = new JSlider(JSlider.HORIZONTAL,MIN_VALUE,MAX_VALUE,INIT_VALUE);
            slider.addChangeListener(this);
            slider.setBackground(BACKGROUND);
            slider.setSize(new Dimension(80,14));
            slider.setPreferredSize(new Dimension(80,14));
            slider.setMajorTickSpacing(1);
            slider.setSnapToTicks(true);
            slider.setPaintTicks(false);
            
            this.label = new TreeLabel();
            this.label.setFont(font);
            this.label.setText(labelT);
            this.label.setBackground(BACKGROUND);
            
            this.add(iconLabel);
            this.add(slider);
            this.add(this.label);
           
//            this.add(weight);
            
            this.value = INIT_VALUE;
            
            this.isVisible = true;
            this.isAllowed = true;
            
            setFace();
            
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
			value = f;
			slider.setValue((int)(f*10));
			if(predicate!=null) predicate.weight = f ;
		}
		
		public void adjustValue() {
			value = (float)slider.getValue()/10;
			if(predicate!=null) predicate.weight = value ;
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
            this.setFace();      
            this.value=Math.min(slider.getValue()/FACTOR,1);
            calculateValues(this,value);
            this.repaint();
        }
        
        private void setFace() {
            float sliderValue = slider.getValue();
            //weight.setText("("+Float.toString(sliderValue/FACTOR)+")");
            if(predicate!=null)
            //weight.setText("(sum="+this.sum+",value="+this.value+",pred="+this.predicate.getUri()+",value="+this.predicate.weight+")");
            if (sliderValue==0) {
                label.setForeground(PASSIVE_FOREG);
                //weight.setForeground(PASSIVE_FOREG);
            } else if (sliderValue==10) {
                label.setFont(bold);
                //weight.setFont(bold);
            } else {
                label.setForeground(ACTIVE_FOREG);
                //weight.setForeground(ACTIVE_FOREG);
                label.setFont(font);
                //weight.setFont(font);
            } 
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
                    if (isSelected) {
                        g.setColor(Color.YELLOW);
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

        public void setSelected(boolean isSelected) {
            this.isSelected = isSelected;
            this.repaint();
        }

        public void setFocus(boolean hasFocus) {
            this.hasFocus = hasFocus;
        }
    }
}
