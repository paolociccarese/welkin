package edu.mit.simile.welkin;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.UIManager;

/**
 * @author Stefano Mazzocchi <stefanom at mit.edu>
 * @author Paolo Ciccarese <paolo at hcklab.org>
 */
public class Welkin extends JApplet implements ActionListener, ItemListener {

    public static final String NAME = "@name@";
	public static final String VERSION = "@version@";
	
	static final String ICON_PATH = "resources/icons/";
	static final String LOGO_SMALL_ICON = ICON_PATH + "logo-small.gif"; 
	static final String LOGO_ICON = ICON_PATH + "logo.gif"; 
	static final String START_ICON = ICON_PATH + "start.gif"; 
	static final String STOP_ICON = ICON_PATH + "stop.gif"; 
	static final String UNSELECTED_ICON = ICON_PATH + "file.gif"; 
	static final String SELECTED_ICON = ICON_PATH + "selected.gif"; 
    
	CheckTree tree;
    ModelVisualizer visualizer;
    ModelWrapper wrapper;

    boolean running = false;
    
	JLabel delayLabel = new JLabel("Delay(ms)");
	JLabel massLabel = new JLabel("Mass");
	JLabel dragLabel = new JLabel("Drag");
	JLabel attractionLabel = new JLabel("Attraction");
	JLabel repulsionLabel = new JLabel("Repulsion");
    
    JButton dataLoadButton;
    JButton dataClearButton;
    
    JButton controlButton;
    JButton circleButton;
    JButton scrambleButton;
    JButton shakeButton;
    JButton highlightButton;
    JButton clearButton;

    JCheckBox randomCheckbox;
    JCheckBox antialiasCheckbox;
    JCheckBox nodesCheckbox;
    JCheckBox edgesCheckbox;
    JCheckBox edgeValuesCheckbox;
    JCheckBox groupsCheckbox;
    JCheckBox timeCheckbox;
    JCheckBox backgroundCheckbox;
    
    JTextField delayField;
    JTextField massField;
    JTextField dragField;
    JTextField attractionField;
    JTextField repulsionField;
    JTextField highlightField;

	ImageIcon startIcon;
	ImageIcon stopIcon;
	ImageIcon logoIcon;

    // called by the applet sandbox
    public void init() {
    	initPanel();
    }
	            
    public void initPanel(){

		wrapper = new ModelWrapper();
        visualizer = new ModelVisualizer(wrapper);
        tree = new CheckTree(this);

		dataClearButton = new JButton("Clear");
		dataLoadButton = new JButton("Load");
		
		dataClearButton.addActionListener(this);
		dataLoadButton.addActionListener(this);
		
		JPanel dataButtons = new JPanel();
		dataButtons.setLayout(new BorderLayout());
		dataButtons.add(dataClearButton, BorderLayout.WEST);
		dataButtons.add(dataLoadButton, BorderLayout.EAST);
		
		JPanel dataControls = new JPanel();
		dataControls.setLayout(new BorderLayout());
		dataControls.add(dataButtons, BorderLayout.SOUTH);

		logoIcon = createImageIcon(LOGO_ICON);

		JLabel logoLabel = new JLabel();
		logoLabel.setIcon(logoIcon);

		JLabel nameLabel = new JLabel();        
		nameLabel.setText(NAME + " " + VERSION);

		JPanel about = new JPanel();
		about.setLayout(new BoxLayout(about, BoxLayout.X_AXIS));
		about.add(nameLabel);
		about.add(Box.createRigidArea(new Dimension(5,0)));
		about.add(logoLabel);

		JScrollPane scroll = new JScrollPane(tree);
		
		JPanel dataPane = new JPanel();
		dataPane.setLayout(new BorderLayout());
		dataPane.add(about, BorderLayout.NORTH);
		dataPane.add(scroll, BorderLayout.CENTER);
		dataPane.add(dataControls, BorderLayout.SOUTH);
		dataPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		
		startIcon = createImageIcon(START_ICON);
		stopIcon = createImageIcon(STOP_ICON);

		if ((startIcon != null) && (stopIcon != null)) {
			controlButton = new JButton("Start", startIcon);
			controlButton.setVerticalTextPosition(AbstractButton.CENTER);
			controlButton.setHorizontalTextPosition(AbstractButton.TRAILING);
		} else {
			controlButton = new JButton("Start");
		}
		
		circleButton = new JButton("Circle");
		scrambleButton = new JButton("Scramble");
		shakeButton = new JButton("Shake");
		highlightButton = new JButton("Highlight");
		clearButton = new JButton("Clear");
		
        randomCheckbox = new JCheckBox("Random Jumps",visualizer.random);
        antialiasCheckbox = new JCheckBox("Antialias",visualizer.antialias);
        nodesCheckbox = new JCheckBox("Nodes",visualizer.drawnodes);
        edgesCheckbox = new JCheckBox("Edges",visualizer.drawedges);
        edgeValuesCheckbox = new JCheckBox("Edge Values",visualizer.drawedgevalues);
        groupsCheckbox = new JCheckBox("Groups",visualizer.drawgroups);
        timeCheckbox = new JCheckBox("Timing",visualizer.timing);
		backgroundCheckbox = new JCheckBox("Background",visualizer.background);
		    
        delayField = new JTextField(Integer.toString(visualizer.delay),4);
        massField = new JTextField(Float.toString(visualizer.mass),4);
        dragField = new JTextField(Float.toString(visualizer.drag),4);
        attractionField = new JTextField(Float.toString(visualizer.attraction),4);
        repulsionField = new JTextField(Float.toString(visualizer.repulsion),4);
        highlightField = new JTextField("http",15);
    
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
        
        randomCheckbox.addItemListener(this);
        antialiasCheckbox.addItemListener(this);
        nodesCheckbox.addItemListener(this);
        edgesCheckbox.addItemListener(this);
		edgeValuesCheckbox.addItemListener(this);
		groupsCheckbox.addItemListener(this);
        timeCheckbox.addItemListener(this);
		backgroundCheckbox.addItemListener(this);

        JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.X_AXIS));
        controls.add(controlButton);
		controls.add(Box.createHorizontalGlue());
        controls.add(circleButton);
        controls.add(scrambleButton);
        controls.add(shakeButton);
		controls.add(Box.createHorizontalGlue());
        controls.add(randomCheckbox);
		controls.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        JPanel highlight = new JPanel();
		highlight.setLayout(new BoxLayout(highlight, BoxLayout.X_AXIS));
        highlight.add(highlightField);
		highlight.add(Box.createRigidArea(new Dimension(5,0)));
        highlight.add(highlightButton);
		highlight.add(Box.createRigidArea(new Dimension(5,0)));
        highlight.add(clearButton);
		highlight.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        JPanel parameters = new JPanel();
		parameters.setLayout(new BoxLayout(parameters, BoxLayout.X_AXIS));
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
		parameters.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        
		JPanel drawing = new JPanel();
		drawing.setLayout(new BoxLayout(drawing, BoxLayout.X_AXIS));
		drawing.add(antialiasCheckbox);
		drawing.add(nodesCheckbox);
		drawing.add(edgesCheckbox);
		drawing.add(edgeValuesCheckbox);
		drawing.add(groupsCheckbox);
		drawing.add(timeCheckbox);
		drawing.add(backgroundCheckbox);
		drawing.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		        
		JTabbedPane tools = new JTabbedPane(JTabbedPane.TOP);
        tools.addTab("Controls",controls);
		tools.addTab("Drawing", drawing);
		tools.addTab("Highlight",highlight);
		tools.addTab("Parameters",parameters);
		
		JPanel graphPane = new JPanel();
		graphPane.setLayout(new BorderLayout());
		graphPane.add(visualizer, BorderLayout.CENTER);
		graphPane.add(tools, BorderLayout.SOUTH);

		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,dataPane,graphPane);

		this.getContentPane().add(split);
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
        if (source == randomCheckbox) {
            visualizer.random = selected;
        } else if (source == antialiasCheckbox) {
            visualizer.antialias = selected;
        } else if (source == nodesCheckbox) {
            visualizer.drawnodes = selected;
        } else if (source == edgesCheckbox) {
            visualizer.drawedges = selected;
		} else if (source == edgeValuesCheckbox) {
			visualizer.drawedgevalues = selected;
		} else if (source == groupsCheckbox) {
			visualizer.drawgroups = selected;
        } else if (source == timeCheckbox) {
            visualizer.timing = selected;
		} else if (source == backgroundCheckbox) {
			visualizer.background = selected;
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
//		  TOREMOVE - Exception
			try
            {
                boolean res=wrapper.importModel();
                if(res) tree.buildTree();
            } catch (FileNotFoundException e1)
            {
                e1.printStackTrace();
            }
		} else if (source == dataClearButton) {
		    wrapper.clear();
		    tree.clear();
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
            visualizer.getGraph().highlightNode(highlightField.getText(),true);
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
//        if (args.length == 0) {
//            System.out.println("Usage: " + Welkin.class + " dataURI");
//            System.exit(1);
//        }
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) { }

		Welkin welkin = new Welkin();
		welkin.init();

        JFrame frame = new JFrame(NAME);
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
        frame.setVisible(true);
        frame.show();
    }    
}
