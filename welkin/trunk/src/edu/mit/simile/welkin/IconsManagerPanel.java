package edu.mit.simile.welkin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

public class IconsManagerPanel extends JPanel implements ActionListener {

    final Font edgeFont = new Font("Monospaced", Font.PLAIN, 12);
	
	ModelManager model;
	Welkin welkin;
	
	JButton defineButton;
	JButton clearButton;
	JTextField ruleField;
	JLabel iconLabel;
	
	ButtonGroup group;
	JCheckBox typeCheck;
	JCheckBox ruleCheck;
	
	JButton removeAllButton;
	JScrollPane scroll;
	
	JPanel tablePanel;
	ArrayList iconList;
	
	TablePanel tablePane;
	
    String iconsDirBase;
    
    int iconsCount = 0;
    
    TableElement bufferedElement;
	
    public IconsManagerPanel(ModelManager model, Welkin welkin) {
        this.model = model;
        this.welkin = welkin;
        this.setBackground(Color.WHITE);
        
        defineButton = new JButton("New");
        removeAllButton = new JButton("Remove All");
        removeAllButton.setActionCommand("RemoveAll");
        
        defineButton.addActionListener(this);
        removeAllButton.addActionListener(this); 
        
    	clearButton = new JButton("Clear Fields");
    	clearButton.setActionCommand("Clear");
    	clearButton.addActionListener(this);
        
        JPanel modifyButtons = new JPanel();
        modifyButtons.setLayout(new BoxLayout(modifyButtons, BoxLayout.X_AXIS));
        modifyButtons.add(Box.createHorizontalGlue());
        modifyButtons.add(clearButton);
        modifyButtons.add(removeAllButton);
        modifyButtons.add(Box.createHorizontalGlue());
        
        defineButton = new JButton("Add Element");
        defineButton.setActionCommand("Add");

    	ruleField = new JTextField();
    	iconLabel = new JLabel();
    	
    	iconLabel.addMouseListener(new MouseAdapter() {
    		public void mousePressed(MouseEvent e) {
	            try {
	                // Save File Manager
	                JFileChooser openWin;
	                if(iconsDirBase != null) openWin = new JFileChooser(iconsDirBase);
	                else openWin = new JFileChooser();

	                openWin.setFileFilter(new WFileFilter());

	                int returnVal = openWin.showOpenDialog(null);

	                if (returnVal == JFileChooser.APPROVE_OPTION) {
	                	ImageIcon icon = new ImageIcon(Toolkit.getDefaultToolkit().createImage(openWin.getSelectedFile().getAbsolutePath()));
	                	iconsDirBase = openWin.getSelectedFile().getParent();
	                	iconLabel.setIcon(icon);
	                	iconLabel.setText("");
	                }
	            } catch (Exception ex) {
	                ex.printStackTrace();
	            }
			}
		});
    	
    	defineButton.addActionListener(this);
    	
    	typeCheck = new JCheckBox("type");
    	ruleCheck = new JCheckBox("regexp");
    	
    	group = new ButtonGroup();
    	group.add(typeCheck);
    	group.add(ruleCheck);
        
        JPanel newButtons = new JPanel();
        newButtons.setLayout(new BoxLayout(newButtons, BoxLayout.X_AXIS));
        newButtons.add(Box.createHorizontalGlue());
        newButtons.add(ruleCheck);
        newButtons.add(typeCheck);
        newButtons.add(defineButton);
        newButtons.add(Box.createHorizontalGlue());
        
        JPanel fieldButtons = new JPanel();
        fieldButtons.setLayout(new BoxLayout(fieldButtons, BoxLayout.X_AXIS));
        fieldButtons.add(iconLabel);
        fieldButtons.add(ruleField);
        
        JPanel allButtons = new JPanel();
        allButtons.setLayout(new BorderLayout());
        allButtons.add(newButtons, "North");
        allButtons.add(fieldButtons, "Center");
        allButtons.add(modifyButtons, "South");
        
        resetForm();
        
        tablePane = new TablePanel();
        scroll = new JScrollPane(tablePane);
        
        this.setLayout(new BorderLayout());
        this.add(scroll, BorderLayout.CENTER);
        this.add(allButtons, BorderLayout.SOUTH);
    }
    
    public void clear() {
		iconList.clear();
		tablePane.repaint();
		scroll.revalidate();
    }

    public void clearModel() {
    	welkin.iconsCheckbox.setSelected(false);
    	welkin.visualizer.drawicons = false;
    	model.clearIcons();
    	welkin.iconsCheckbox.setSelected(true);
    	welkin.visualizer.drawicons = true;
    	welkin.visualizer.repaint();
    }
    
	public void actionPerformed(ActionEvent a) {
		if(a.getActionCommand().equals("Clear")) {
			resetForm();
		} else if(a.getActionCommand().equals("Add")) {
			if(ruleField.getText().trim().length()>0) {
				if (bufferedElement != null) {
		            for(Iterator it = iconList.iterator(); it.hasNext(); ) {
		            	TableElement te = (TableElement) it.next();
		            	if (te.id == bufferedElement.id) {
		            		te.icon = ((ImageIcon)iconLabel.getIcon()).getImage();
		            		te.rule = ruleField.getText().trim();
		            		updateModel(bufferedElement.type, bufferedElement.rule);
		            		bufferedElement = null;
		            		break;
		            	}
		            }
				} else {
					iconList.add(new TableElement(iconsCount++, typeCheck.isSelected(), 
							((ImageIcon)iconLabel.getIcon()).getImage(), 
							ruleField.getText().trim()));
				}
				updateModel();
				resetForm();
				welkin.visualizer.repaint();
			}
		} else if(a.getActionCommand().equals("RemoveAll")) {
			iconList.clear();
			clearModel();
		}
		
		tablePane.repaint();
		scroll.revalidate();
	}
	
	public Image getIconById(int id) {
        for(Iterator it = iconList.iterator(); it.hasNext(); ) {
        	TableElement te = (TableElement) it.next();
        	
        	if(te.id == id) return te.icon;
        }		
        return null;
	}
	
	private void updateModel() {
        for(Iterator it = iconList.iterator(); it.hasNext(); ) {
        	TableElement te = (TableElement) it.next();
        	
        	model.updateIcons(te.id, te.type, te.rule);
        }
	}
	
	private void updateModel(boolean type, String rule) {
        model.updateIcons(type, rule);
	}
	
	protected static ImageIcon createImageIcon(String path) {
		URL imgURL;
		try {
			imgURL = new URL(path);

			if (imgURL != null) {
				return new ImageIcon(imgURL);
			} else {
				System.err.println("Couldn't find image resource: " + path);
				return null;
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public void resetForm() {
		ruleField.setText("");
  	    URL url = IconsManagerPanel.class.getResource("resources/icons/help16.gif");
	    Image img = Toolkit.getDefaultToolkit().getImage(url);
    	iconLabel.setIcon(new ImageIcon(img));
    	bufferedElement = null;
    	typeCheck.setSelected(true);
	}
	
	public Image getImage(int id) {
        for(Iterator it = iconList.iterator(); it.hasNext(); ) {
        	TableElement te = (TableElement) it.next();

        	if(te.id == id) {
        		return te.icon;
        	}
        }	
        return null;
	}
	
    class WFileFilter extends FileFilter {

        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }

            String extension = getExtension(f);
            if (extension != null) {
                if (extension.equals("gif") ||
                    extension.equals("jpeg") ||
                    extension.equals("jpg")) {
                        return true;
                }
        	}

            return false;
        }

        // The description of this filter
        public String getDescription() {
            return "gif, jpeg or jpg icon file";
        }

        /*
         * Get the extension of a file.
         */
        public String getExtension(File f) {
            String ext = null;
            String s = f.getName();
            int i = s.lastIndexOf('.');

            if (i > 0 &&  i < s.length() - 1) {
                ext = s.substring(i+1).toLowerCase();
            }
            return ext;
        }
    }
    
    class TablePanel extends JPanel {
    	
    	Image remove;
    	int maximumTextWidth=140;
    	
    	public TablePanel () {
    		iconList = new ArrayList();
    		this.setBackground(Color.WHITE);
    		this.addMouseListener(new MyMouseListener());
      	    URL url = IconsManagerPanel.class.getResource("resources/icons/help16.gif");
    	    Image img = Toolkit.getDefaultToolkit().getImage(url);
    	    url = IconsManagerPanel.class.getResource("resources/icons/remove16.gif");
    	    remove = Toolkit.getDefaultToolkit().getImage(url);
    	    
    	    // TODO Remove
    		//iconList.add(new TableElement(iconsCount++, true, img, "type  Person"));
    		//iconList.add(new TableElement(iconsCount++, true, img, "rule  http://pippo/"));
    	}
    	
    	public void addItem(TableElement element) {
    		iconList.add(element);
    	}
    	
        public void paintComponent(Graphics g) {
        	super.paintComponent(g);
        	
            Graphics2D g2 = (Graphics2D) g;
            g2.setFont(edgeFont);
            FontMetrics fm = g2.getFontMetrics(edgeFont);
            
            int dy = 0;
            
            for(Iterator it = iconList.iterator(); it.hasNext(); ) {
            	TableElement te = (TableElement) it.next();
            	
            	maximumTextWidth = maximumTextWidth < fm.stringWidth(te.rule) 
            			? fm.stringWidth(te.rule) : maximumTextWidth;
            	this.setPreferredSize(
            			new Dimension(maximumTextWidth + te.icon.getWidth(this)+40, 30));

            	
            	te.x = 2 + 20;
            	te.y = dy + 2;
            	te.dx = 200;
            	te.dy = te.icon.getHeight(this)+4;
            	if(te.isSelected) {
            		g2.setColor(Color.LIGHT_GRAY);
            		g2.fillRect(20, dy + 2, maximumTextWidth + te.icon.getWidth(this)+14, te.icon.getHeight(this)+4);
            		g2.setColor(Color.GRAY);
            		g2.drawRect(20, dy + 2, maximumTextWidth + te.icon.getWidth(this)+14, te.icon.getHeight(this)+4);
            		g2.setColor(Color.BLACK);
            		g2.drawString(te.rule, 30 + te.icon.getWidth(this), dy + 8 + te.icon.getHeight(this)/2);
            	} else {
            		g2.setColor(Color.GRAY);
            		g2.drawRect(20, dy + 2, maximumTextWidth + te.icon.getWidth(this)+14, te.icon.getHeight(this)+4);
            		g2.setColor(Color.BLACK);
            		g2.drawString(te.rule, 28 + te.icon.getWidth(this), dy + 8 + te.icon.getHeight(this)/2);
            	}
            	
            	g2.drawImage(remove, 2, dy + 5, this);
            	g2.drawImage(te.icon, 23, dy + 5, this);
            	dy += te.icon.getHeight(this)+ 4 + 4;
            }
        }
        
        class MyMouseListener extends MouseAdapter {
            public void mousePressed(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();
                for(Iterator it = iconList.iterator(); it.hasNext(); ) {
                	TableElement te = (TableElement) it.next();

                	if(x > 5 && x< 21 && y > te.y && y< (te.y+te.dy)) {
                		iconList.remove(te);
                		updateModel(te.type, te.rule);
                		break;
                	}
                	
					if (x > te.x && x< (te.x+te.dx) && y > te.y && y< (te.y+te.dy)) {
						te.isSelected = true;
					} else {
						te.isSelected = false;
					}
                }
                
                repaint();
            }
            
            public void mouseReleased(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();
                if (e.getClickCount() == 2) {
                    for(Iterator it = iconList.iterator(); it.hasNext(); ) {
                    	TableElement te = (TableElement) it.next();
                    	
    					if (x > te.x && x< (te.x+te.dx) && y > te.y && y< (te.y+te.dy)) {
    						bufferedElement = new TableElement(te.id, te.type, te.icon, te.rule);
    						iconLabel.setIcon(new ImageIcon(te.icon));
    						ruleField.setText(te.rule);
    						if(te.type) typeCheck.setSelected(true);
    						else ruleCheck.setSelected(true);
    						//iconList.remove(te);
    						break;
    					}
                    }
                } 
                
                repaint();
            }
        }
    }
    
    class TableElement {
    	public int id;
    	public int x, y, dx, dy;
    	public boolean type;
    	public Image icon;
    	public String rule;
    	public boolean isSelected;
    	
    	public TableElement(int id, boolean type, Image icon, String rule) {
    		this.id = id;
    		this.icon = icon;
    		this.rule = rule;
    		this.type = type;
    		isSelected = false;
    	}
    }
}
