/*
 * 
 */
package edu.mit.simile.welkin;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

import javax.swing.JComponent;

import edu.mit.simile.welkin.InfoCache.Node;

/**
 * 
 */
public class ModelCharter extends JComponent {

    final Font timeFont = new Font("Verdana", Font.PLAIN, 9);
    final Color timeColor = Color.BLACK;

    private boolean timing = true;

    private long analysisTime;
    private long drawingTime;

    private int h;
    private int w;
    
    private ModelWrapper model;
    
    class MyMouseListener extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON3 || e.isPopupTrigger()) {
            } else {
            }
            repaint();
        }

        public void mouseReleased(MouseEvent e) {
            repaint();
        }
    }

    class MyMouseMotionListener extends MouseMotionAdapter {
        public void mouseDragged(MouseEvent e) {
        }
    }

    public ModelCharter(ModelWrapper model) {
        this.addMouseMotionListener(new MyMouseMotionListener());
        this.addMouseListener(new MyMouseListener());
        this.model = model;
    }

    public void setGraph(ModelWrapper model) {
        this.model = model;
        analyze();
    }

    public ModelWrapper getGraph() {
        return this.model;
    }

    public void reshape(int x, int y, int w, int h) {
        super.reshape(x, y, w, h);
        this.w = w;
        this.h = h ;
    }

    void analyze() {
        long startTime = 0;

        if (timing) startTime = System.currentTimeMillis();

        for (Iterator it = model.cache.nodes.iterator(); it.hasNext();) {
            Node n = (Node) it.next();
            if (!n.isVisible) continue;

            // calculate something per each node

        }

        if (timing) analysisTime = System.currentTimeMillis() - startTime;

        repaint();
    }

    public Dimension getMinimumSize() {
        return new Dimension(100,100);
    }
    
    public void paintComponent(Graphics g) {
        long startTime = 0;

        if (timing) startTime = System.currentTimeMillis();

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2.setColor(Color.white);
        g2.fill(new Rectangle2D.Float(0.0f, 0.0f, w, h));
        
        AffineTransform t = g2.getTransform();

        // draw something

        if (timing) {
            drawingTime = System.currentTimeMillis() - startTime;
            g.setColor(timeColor);
            g.setFont(timeFont);
            g.drawString("calculation: " + analysisTime + " ms", 5, 15);
            g.drawString("drawing: " + drawingTime + " ms", 5, 25);
        }
    }
}    
