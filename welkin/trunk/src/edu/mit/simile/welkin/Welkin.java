package edu.mit.simile.welkin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;
import java.net.URL;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.UIManager;

public class Welkin extends JApplet implements ActionListener, ItemListener {

    public static final String NAME = "Welkin";
    public static final String VERSION = "@version@";
    public static final String YEAR = "@year@";
	
    static final String ICON_PATH = "resources/icons/";
    static final String LOGO_SMALL_ICON = ICON_PATH + "logo-small.gif"; 
    static final String LOGO_ICON = ICON_PATH + "sombrero.png"; 
    static final String START_ICON = ICON_PATH + "start.gif"; 
    static final String STOP_ICON = ICON_PATH + "stop.gif"; 
    static final String UNSELECTED_ICON = ICON_PATH + "file.gif"; 
    static final String SELECTED_ICON = ICON_PATH + "selected.gif"; 
    
    static JFrame frame;
    
    CheckTree tree;
    ModelVisualizer visualizer;
    ModelWrapper wrapper;
    ModelCharter charter;
    
    boolean running = false;
    
    JLabel delayLabel = new JLabel("Delay(ms)");
    JLabel massLabel = new JLabel("Mass");
    JLabel dragLabel = new JLabel("Drag");
    JLabel attractionLabel = new JLabel("Attraction");
    JLabel repulsionLabel = new JLabel("Repulsion");
    
    JButton dataLoadButton;
    JButton dataClearButton;
    JButton aboutButton;

    JButton controlButton;
    JButton circleButton;
    JButton scrambleButton;
    JButton shakeButton;
    JButton highlightButton;
    JButton clearButton;
    
    JCheckBox antialiasCheckbox;
    JCheckBox nodesCheckbox;
    JCheckBox edgesCheckbox;
    JCheckBox arrowCheckbox;
    JCheckBox edgeValuesCheckbox;
    JCheckBox groupsCheckbox;
    JCheckBox timeCheckbox;
    JCheckBox backgroundCheckbox;
    JCheckBox highlightOnLabelCheckbox;
    
    JTextField delayField;
    JTextField massField;
    JTextField dragField;
    JTextField attractionField;
    JTextField repulsionField;
    JTextField highlightField;
    
    ImageIcon startIcon;
    ImageIcon stopIcon;
    ImageIcon logoIcon;
    
    JDialog about;
    
    class About extends JComponent {
        public void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.drawImage(createImageIcon(LOGO_ICON).getImage(),0,0,null);
            g2.setFont(new Font("Verdana", Font.BOLD, 30));
            g2.setColor(new Color(206,50,8));
            g2.drawString(NAME,18,291);
            g2.setFont(new Font("Verdana", Font.PLAIN, 12));
            g2.setColor(new Color(128,128,128));
            g2.drawString("A Graphical RDF Browser",18,325);
            g2.setFont(new Font("Verdana", Font.PLAIN, 12));
            g2.setColor(new Color(192,192,192));
            g2.drawString("Version " + VERSION,18,341);
        }
        
        public Dimension getPreferredSize() {
            return new Dimension(300, 370);
        }        
    }

    class AboutMouseAdapter extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            about.setVisible(false);
        }
    }

    void about() {
        if (about == null) {
            about = new JDialog(frame, "About " + NAME + "...", false);
            about.setSize(300,400);
            MouseAdapter ma = new AboutMouseAdapter();
            about.addMouseListener(ma);
            JButton okButton = new JButton("Ok");
            JPanel bottom = new JPanel();
            okButton.addMouseListener(ma);
            bottom.setLayout(new BorderLayout());
            bottom.add(okButton,BorderLayout.EAST);
            bottom.setBorder(BorderFactory.createEmptyBorder(3,25,3,25));
            About aboutPane = new About();
            Container panel = about.getContentPane();
            panel.setLayout(new BorderLayout());
            panel.setBackground(Color.white);
            panel.add(aboutPane, BorderLayout.NORTH);
            panel.add(bottom, BorderLayout.SOUTH);
        }

        // NOTE: these lines center the about dialog in the
        // current window. Some older Swing versions have
        // a bug in getLocationOnScreen() and they may not
        // make this behave properly.
        Point p = frame.getLocationOnScreen();
        Dimension d1 = frame.getSize();
        about.pack();
        Dimension d2 = about.getSize();
        about.setLocation(
            p.x + (d1.width - d2.width) / 2,
            p.y + (d1.height - d2.height) / 2
        );
        about.setVisible(true);
    }
        
    // called by the applet sandbox
    public void init() {
    	    initPanel();
    }
	            
    public void initPanel(){

        wrapper = new ModelWrapper();
        visualizer = new ModelVisualizer(wrapper);
        charter = new ModelCharter(wrapper);

        tree = new CheckTree(this);

        JScrollPane scrollingTree = new JScrollPane(tree);
        charter.setBorder(scrollingTree.getBorder());
        visualizer.setBorder(scrollingTree.getBorder());

        dataClearButton = new JButton("Clear");
        dataLoadButton = new JButton("Load");
        
        dataClearButton.addActionListener(this);
        dataLoadButton.addActionListener(this);
        
        JPanel dataButtons = new JPanel();
        dataButtons.setLayout(new BoxLayout(dataButtons, BoxLayout.X_AXIS));
        dataButtons.add(Box.createHorizontalGlue());
        dataButtons.add(dataLoadButton);
        dataButtons.add(dataClearButton);
        dataButtons.add(Box.createHorizontalGlue());
		
        JPanel dataControls = new JPanel();
        dataControls.setLayout(new BorderLayout());
        dataControls.add(dataButtons, BorderLayout.SOUTH);
        
        startIcon = createImageIcon(START_ICON);
        stopIcon = createImageIcon(STOP_ICON);

        if ((startIcon != null) && (stopIcon != null)) {
            controlButton = new JButton("Start", startIcon);
            controlButton.setVerticalTextPosition(AbstractButton.CENTER);
            controlButton.setHorizontalTextPosition(AbstractButton.TRAILING);
        } else {
            controlButton = new JButton("Start");
        }

        aboutButton = new JButton("About");
        aboutButton.addActionListener(this);
        
        JPanel dataPane = new JPanel();
        dataPane.setLayout(new BorderLayout());
        dataPane.add(dataButtons, BorderLayout.WEST);
        dataPane.add(aboutButton, BorderLayout.EAST);
		
        circleButton = new JButton("Circle");
        scrambleButton = new JButton("Scramble");
        shakeButton = new JButton("Shake");
        highlightButton = new JButton("Highlight");
        clearButton = new JButton("Clear");
        
        antialiasCheckbox = new JCheckBox("Antialias",visualizer.antialias);
        nodesCheckbox = new JCheckBox("Nodes",visualizer.drawnodes);
        edgesCheckbox = new JCheckBox("Edges",visualizer.drawedges);
        arrowCheckbox = new JCheckBox("Arrows",visualizer.drawarrows);
        edgeValuesCheckbox = new JCheckBox("Edge Values",visualizer.drawedgevalues);
        timeCheckbox = new JCheckBox("Timing",visualizer.timing);
        backgroundCheckbox = new JCheckBox("Background",visualizer.background);
        highlightOnLabelCheckbox = new JCheckBox("Label",visualizer.highlightOnLabel);
            
        delayField = new JTextField(Integer.toString(visualizer.delay),4);
        massField = new JTextField(Float.toString(visualizer.mass),4);
        dragField = new JTextField(Float.toString(visualizer.drag),4);
        attractionField = new JTextField(Float.toString(visualizer.attraction),4);
        repulsionField = new JTextField(Float.toString(visualizer.repulsion),4);
        highlightField = new JTextField("",15);
    
        delayField.addActionListener(this);
        massField.addActionListener(this);
        dragField.addActionListener(this);
        attractionField.addActionListener(this);
        repulsionField.addActionListener(this);
        highlightField.addActionListener(this);
        controlButton.addActionListener(this);
        circleButton.addActionListener(this);
        scrambleButton.addActionListener(this);
        shakeButton.addActionListener(this);
        highlightButton.addActionListener(this);
        clearButton.addActionListener(this);
        
        antialiasCheckbox.addItemListener(this);
        nodesCheckbox.addItemListener(this);
        edgesCheckbox.addItemListener(this);
        arrowCheckbox.addItemListener(this);
        edgeValuesCheckbox.addItemListener(this);
        timeCheckbox.addItemListener(this);
        backgroundCheckbox.addItemListener(this);
        highlightOnLabelCheckbox.addItemListener(this);
        
        JPanel highlight = new JPanel();
        highlight.setLayout(new BoxLayout(highlight, BoxLayout.X_AXIS));
        highlight.add(Box.createHorizontalGlue());
        highlight.add(highlightField);
        highlight.add(Box.createRigidArea(new Dimension(5,0)));
        highlight.add(highlightOnLabelCheckbox);
        highlight.add(Box.createRigidArea(new Dimension(5,0)));
        highlight.add(highlightButton);
        highlight.add(Box.createRigidArea(new Dimension(5,0)));
        highlight.add(clearButton);
        highlight.add(Box.createHorizontalGlue());
        highlight.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));

        JPanel parameters = new JPanel();
		parameters.setLayout(new BoxLayout(parameters, BoxLayout.X_AXIS));
		parameters.add(Box.createHorizontalGlue());
		parameters.add(delayLabel);
		parameters.add(Box.createRigidArea(new Dimension(5,0)));
		parameters.add(delayField);
		parameters.add(Box.createRigidArea(new Dimension(30,0)));
		parameters.add(massLabel);
		parameters.add(Box.createRigidArea(new Dimension(5,0)));
		parameters.add(massField);
		parameters.add(Box.createRigidArea(new Dimension(30,0)));
		parameters.add(dragLabel);
		parameters.add(Box.createRigidArea(new Dimension(5,0)));
		parameters.add(dragField);
		parameters.add(Box.createRigidArea(new Dimension(30,0)));
		parameters.add(attractionLabel);
		parameters.add(Box.createRigidArea(new Dimension(5,0)));
		parameters.add(attractionField);
		parameters.add(Box.createRigidArea(new Dimension(30,0)));
		parameters.add(repulsionLabel);
		parameters.add(Box.createRigidArea(new Dimension(5,0)));
		parameters.add(repulsionField);
		parameters.add(Box.createHorizontalGlue());
		parameters.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
        
		JPanel drawing = new JPanel();
		drawing.setLayout(new BoxLayout(drawing, BoxLayout.X_AXIS));
        drawing.add(controlButton);
        drawing.add(Box.createHorizontalGlue());
        drawing.add(circleButton);
        drawing.add(scrambleButton);
        drawing.add(shakeButton);
		drawing.add(Box.createHorizontalGlue());
		drawing.add(nodesCheckbox);
		drawing.add(edgesCheckbox);
		drawing.add(arrowCheckbox);
        //drawing.add(edgeValuesCheckbox);
        drawing.add(Box.createHorizontalGlue());
		//drawing.add(timeCheckbox);
        drawing.add(antialiasCheckbox);
		drawing.add(backgroundCheckbox);
		drawing.add(Box.createHorizontalGlue());
		drawing.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
		        
		JTabbedPane toolsPane = new JTabbedPane(JTabbedPane.TOP);
		//toolsPane.addTab("Controls",controls);
		toolsPane.addTab("Drawing", drawing);
		toolsPane.addTab("Highlight",highlight);
		toolsPane.addTab("Parameters",parameters);

        JSplitPane chartPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,charter, visualizer);
        chartPane.setOneTouchExpandable(true);
        chartPane.setResizeWeight(0.01);
        chartPane.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
        
        JSplitPane visualizerPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,chartPane,toolsPane);
        visualizerPane.setOneTouchExpandable(true);
        visualizerPane.setResizeWeight(1.0);
        visualizerPane.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));

        JSplitPane bodyPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,scrollingTree,visualizerPane);
        bodyPane.setOneTouchExpandable(true);
        bodyPane.setResizeWeight(0.25);
        bodyPane.setDividerLocation(30);        
        bodyPane.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
        
        JPanel pane = new JPanel();
        pane.setLayout(new BorderLayout());
        pane.add(dataPane, BorderLayout.NORTH);
        pane.add(bodyPane, BorderLayout.CENTER);
        pane.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));

        this.getContentPane().add(pane);
    }
	
    public void start() {
		super.start();
		if (running) internalStart();
    }
    
    public void stop() {
		super.stop();
        if (running) internalStop();
    }

	public void internalStart() {
		running = true;
		visualizer.start();
		controlButton.setText("Stop");
		if (stopIcon != null) controlButton.setIcon(stopIcon);
	}

	public void internalStop() {
		running = false;
		visualizer.stop();
		controlButton.setText("Start");
		if (startIcon != null) controlButton.setIcon(startIcon);
	}

    public String getAppletInfo() {
        return NAME;
    }    

    public void notifyTreeChange() {
        wrapper.cache.verifyVisualized(tree);
        visualizer.repaint();
    }
    
    public void itemStateChanged(ItemEvent e) {
        Object source = e.getItemSelectable();
        boolean selected = (e.getStateChange() == ItemEvent.SELECTED);
        if (source == antialiasCheckbox) {
            visualizer.antialias = selected;
        } else if (source == nodesCheckbox) {
            visualizer.drawnodes = selected;
        } else if (source == edgesCheckbox) {
            visualizer.drawedges = selected;
        } else if (source == arrowCheckbox) {
            visualizer.drawarrows = selected;
		} else if (source == edgeValuesCheckbox) {
			visualizer.drawedgevalues = selected;
        } else if (source == timeCheckbox) {
            visualizer.timing = selected;
		} else if (source == backgroundCheckbox) {
			visualizer.background = selected;
		} else if (source == highlightOnLabelCheckbox) {
			visualizer.highlightOnLabel = selected;
        }
        visualizer.repaint();
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == controlButton) {
            if (visualizer.isRunning()) {
            	    internalStop();
            } else {
            	    internalStart();
            }
        } else if (source == circleButton) {
            visualizer.circle();
        } else if (source == scrambleButton) {
            visualizer.scramble();
        } else if (source == shakeButton) {
            visualizer.shake();
		} else if (source == dataLoadButton) {
            try {
                boolean res = wrapper.importModel();
                if (res) {
                    tree.buildTree();
                    charter.analyze();
                }
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
		} else if (source == dataClearButton) {
            wrapper.clear();
            tree.clear();
            charter.clear();
        } else if (source == aboutButton) {
            about();
        } else if (source == delayField) {
            visualizer.delay = Integer.parseInt(delayField.getText());
        } else if (source == massField) {
            visualizer.mass = Float.parseFloat(massField.getText());
        } else if (source == dragField) {
            visualizer.drag = Float.parseFloat(dragField.getText());
        } else if (source == attractionField) {
            visualizer.attraction = Float.parseFloat(attractionField.getText());
        } else if (source == repulsionField) {
            visualizer.repulsion = Float.parseFloat(repulsionField.getText());
        } else if ((source == highlightField) || (source == highlightButton)) {
            visualizer.getGraph().highlightNode(highlightField.getText(),true,visualizer.highlightOnLabel);
        } else if (source == clearButton) {
            visualizer.getGraph().clearHighlights();
        }
		visualizer.repaint();
    }
	
	protected static ImageIcon createImageIcon(String path) {
		URL imgURL = Welkin.class.getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL);
		} else {
			System.err.println("Couldn't find image resource: " + path);
			return null;
		}
	}
	
	public static void error(String msg) {
		System.err.println(msg);
	}

	public static void log(String msg) {
		System.out.println(msg);
	}
	    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) { }

		Welkin welkin = new Welkin();
		welkin.init();

        frame = new JFrame(NAME);
        URL logo = Welkin.class.getResource(LOGO_SMALL_ICON);
        if (logo != null) {
        	    frame.setIconImage(Toolkit.getDefaultToolkit().createImage(logo));
        } else {
        	    System.err.println("Couldn't find image resource: " + LOGO_ICON);
        }

        frame.getContentPane().add(welkin, BorderLayout.CENTER);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        frame.setSize(800, 600);
        frame.pack();
        frame.setVisible(true);
        frame.show();
    }    
}
