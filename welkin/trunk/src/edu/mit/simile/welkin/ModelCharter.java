package edu.mit.simile.welkin;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JComponent;

import edu.mit.simile.welkin.ModelCache.WResource;
import edu.mit.simile.welkin.ModelCache.WStatement;

public class ModelCharter extends JComponent {

    final static int LENGTH = 100;
    
    private boolean timing = true;

    private long analysisTime;
    private long drawingTime;

    private int h;
    private int w;
    
    private ModelManager model;
    
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

    ModelCharter(ModelManager model) {
        this.addMouseMotionListener(new MyMouseMotionListener());
        this.addMouseListener(new MyMouseListener());
        this.model = model;
    }

    void setGraph(ModelManager model) {
        this.model = model;
        analyze();
    }

    ModelManager getGraph() {
        return this.model;
    }

    public void reshape(int x, int y, int w, int h) {
        super.reshape(x, y, w, h);
        this.w = w;
        this.h = h ;
    }
    
    private Map inDegreeDistribution = new HashMap();
    private Map outDegreeDistribution = new HashMap();
    private Map clustCoeffDistribution = new HashMap();
    
    private int maxInDegree = 0;
    private int maxOutDegree = 0;
    
    class Count {
        public int count = 1;
    }
    
    /*
     * @returns the out-degree of the node. In RDF terms, the number of
     * non-literal objects connected to this node via some predicate.
     */
    private int outDegree(WResource node) {
        int counter = 0;
        for (Iterator it = node.linkedObjectNodes.iterator(); it.hasNext();) {
            Object o = it.next();
            if (o instanceof WStatement) {
                WResource n = ((WStatement) o).object;
                if (n.isVisible) counter++;
            }
        }
        return counter; 
    }

    /*
     * @returns the in-degree of the node. In RDF terms, the number of
     * subjects that connect to this node via some predicate.
     */
    private int inDegree(WResource node) {
        int counter = 0;
        for (Iterator it = node.linkedSubjectNodes.iterator(); it.hasNext();) {
            Object o = it.next();
            if (o instanceof WStatement) {
                WResource n = ((WStatement) o).subject;
                if (n.isVisible) counter++;
            }
        }
        return counter; 
    }
    
    /*
     * @returns the clustering coefficient of the node, basically the number
     * of the existing predicates between neighboring nodes (either objects or subjects)
     * normalized against the number of possible ones and returned as a percentage.
     */
    private int clustCoeff(WResource node) {
        int neighbors = 0;
        int neighborEdges = 0;
        for (Iterator it = node.linkedObjectNodes.iterator(); it.hasNext();) {
            WStatement e = (WStatement) it.next();
            if (e.object.isVisible) {
                neighbors++;
                for (Iterator it2 = e.object.linkedObjectNodes.iterator(); it2.hasNext();) {
                    WStatement e2 = (WStatement) it2.next();
                    if ((e2.object.isVisible) && (node.linkedSubjectNodes.contains(e2) || node.linkedObjectNodes.contains(e2))) {
                        neighborEdges++;
                    }
                }
                for (Iterator it2 = e.object.linkedSubjectNodes.iterator(); it2.hasNext();) {
                    WStatement e2 = (WStatement) it2.next();
                    if ((e2.object.isVisible) && (node.linkedSubjectNodes.contains(e2) || node.linkedObjectNodes.contains(e2))) {
                        neighborEdges++;
                    }
                }
            }
        }
        for (Iterator it = node.linkedSubjectNodes.iterator(); it.hasNext();) {
            WStatement e = (WStatement) it.next();
            if (e.subject.isVisible) {
                neighbors++;
                for (Iterator it2 = e.subject.linkedObjectNodes.iterator(); it2.hasNext();) {
                    WStatement e2 = (WStatement) it2.next();
                    if ((e2.object.isVisible) && (node.linkedSubjectNodes.contains(e2) || node.linkedObjectNodes.contains(e2))) {
                        neighborEdges++;
                    }
                }
                for (Iterator it2 = e.subject.linkedSubjectNodes.iterator(); it2.hasNext();) {
                    WStatement e2 = (WStatement) it2.next();
                    if ((e2.object.isVisible) && (node.linkedSubjectNodes.contains(e2) || node.linkedObjectNodes.contains(e2))) {
                        neighborEdges++;
                    }
                }
            }
        }
        if (neighbors > 0) {
            return (100 * neighborEdges) / (neighbors * neighbors);
        } else {
            return 0;
        }
    }
    
    void analyze() {
        long startTime = 0;

        if (timing) startTime = System.currentTimeMillis();

        this.inDegreeDistribution.clear();
        this.outDegreeDistribution.clear();
        this.clustCoeffDistribution.clear();
        
        this.maxInDegree = 0;
        this.maxOutDegree = 0;
        
        for (Iterator it = model.cache.resources.iterator(); it.hasNext();) {
            WResource n = (WResource) it.next();
            if (!n.isVisible) continue;

            Integer inDegree = new Integer(inDegree(n));
            Count inCount = (Count) inDegreeDistribution.get(inDegree);
            if (inCount == null) {
                inDegreeDistribution.put(inDegree,new Count());
            } else {
                inCount.count++;
                if (inCount.count > maxInDegree) maxInDegree = inCount.count;
            }

            Integer outDegree = new Integer(outDegree(n));
            Count outCount = (Count) outDegreeDistribution.get(outDegree);
            if (outCount == null) {
                outDegreeDistribution.put(outDegree,new Count());
            } else {
                outCount.count++;
                if (outCount.count > maxOutDegree) maxOutDegree = outCount.count;
            }

            Integer clustCoeff = new Integer(clustCoeff(n));
            Count clustCoeffCount = (Count) clustCoeffDistribution.get(clustCoeff);
            if (clustCoeffCount == null) {
                clustCoeffDistribution.put(clustCoeff,new Count());
            } else {
                clustCoeffCount.count++;
            }
        }

        if (timing) analysisTime = System.currentTimeMillis() - startTime;

        repaint();
    }

    void clear() {
        this.inDegreeDistribution.clear();
        this.outDegreeDistribution.clear();
        this.clustCoeffDistribution.clear();
        repaint();
    }
    
    final static Font timeFont = new Font("Verdana", Font.PLAIN, 9);
    final static Font gridFont = new Font("Verdana", Font.PLAIN, 8);
    final static Font titleFont = new Font("Verdana", Font.PLAIN, 9);
    
    final static Color titleColor = Color.BLACK;
    final static Color timeColor = Color.BLACK;
    final static Color axisColor = new Color(0x80,0x80,0x80);
    final static Color gridColor = new Color(0xc0,0xc0,0xc0);
    final static Color barColor = Color.RED;
    final static Color dotColor = Color.RED;
    
    final static float xBORDER = 10.0f;
    final static float xBORDERs = 2.0f * xBORDER;
    final static float yBORDER = 10.0f;
    final static float yBORDERs = 2.0f * yBORDER;
    final static float WIDTH = 100.0f;
    final static float HEIGHT = 50.0f;
    final static float STEPS = 5.0f;
    
    public void paintComponent(Graphics g) {
        long startTime = 0;

        if (timing) startTime = System.currentTimeMillis();

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2.setColor(Color.white);
        g2.fill(new Rectangle2D.Float(0.0f, 0.0f, w, h));
        
        AffineTransform t = g2.getTransform();

        FontMetrics gridFM = g2.getFontMetrics(gridFont);
        FontMetrics titleFM = g2.getFontMetrics(titleFont);
        
        float titleHeight = titleFM.getAscent() + titleFM.getDescent();
        g2.translate(xBORDER, yBORDER + HEIGHT + titleHeight);
        g2.setColor(titleColor);
        g2.setFont(titleFont);
        g2.drawString("InDegree",0,-HEIGHT - titleFM.getDescent() - 2);
        g2.setColor(axisColor);
        g2.draw(new Rectangle2D.Float(0,-HEIGHT,WIDTH,HEIGHT));
        g2.setColor(barColor);
        g2.setPaint(barColor);
        for (Iterator it = inDegreeDistribution.keySet().iterator(); it.hasNext();) {
            Integer degree = (Integer) it.next();
            Count count = (Count) inDegreeDistribution.get(degree);
            if (count != null && degree.intValue() > 0) {
                float x = WIDTH * (float) (Math.log(degree.doubleValue())/Math.log((float) model.cache.resources.size()));
                float y = HEIGHT * (float) (Math.log(count.count)/Math.log(maxInDegree));
                g2.draw(new Ellipse2D.Float(x-1.0f,-y-1.0f,2.0f,2.0f));
            }
        }

        g2.translate(0, yBORDER + HEIGHT + titleHeight);
        g2.setColor(titleColor);
        g2.setFont(titleFont);
        g2.drawString("OutDegree",0,-HEIGHT - titleFM.getDescent() - 2);
        g2.setColor(axisColor);
        g2.draw(new Rectangle2D.Float(0,-HEIGHT,WIDTH,HEIGHT));
        g2.setColor(barColor);
        g2.setPaint(barColor);
        for (Iterator it = outDegreeDistribution.keySet().iterator(); it.hasNext();) {
            Integer degree = (Integer) it.next();
            Count count = (Count) inDegreeDistribution.get(degree);
            if (count != null && degree.intValue() > 0) {
                float x = WIDTH * (float) (Math.log(degree.doubleValue())/Math.log((float) model.cache.resources.size()));
                float y = HEIGHT * (float) (Math.log(count.count)/Math.log(maxOutDegree));
                g2.draw(new Ellipse2D.Float(x-1.0f,-y-1.0f,2.0f,2.0f));
            }
        }
        
        g2.translate(0, yBORDER + HEIGHT + titleHeight);
        g2.setColor(titleColor);
        g2.setFont(titleFont);
        g2.drawString("Clustering Coefficient",0,-HEIGHT - titleFM.getDescent() - 2);
        g2.setColor(gridColor);
        g2.setFont(gridFont);
        float width = WIDTH - gridFM.stringWidth("100") - 3.0f;
        float halfHeight = (gridFM.getAscent() + gridFM.getDescent()) / 2.0f;
        for (int i = 0; i <= STEPS; i++) {
            float y = i * HEIGHT / STEPS;
            g2.draw(new Line2D.Float(0.0f,-y,width,-y));
            g2.drawString(String.valueOf(100 * i / (int) STEPS), width + 3.0f, -y + halfHeight - 1);
        }
        g2.setColor(axisColor);
        g2.draw(new Line2D.Float(0,0,width,0));
        g2.setColor(dotColor);
        g2.setPaint(dotColor);
        float tick = width / (float) LENGTH;
        for (float f = 0.0f; f < LENGTH; f += 1.0f) {
            Count clustCoeffCount = (Count) clustCoeffDistribution.get(new Integer((int) f));
            if (clustCoeffCount != null) {
                float w = HEIGHT * (float) clustCoeffCount.count / (float) model.cache.resources.size();
                g2.draw(new Rectangle2D.Float(f * tick, -w, tick, w));
            }
        }

        g2.setTransform(t);

        if (timing) {
            g2.translate(5, h - 5);
            drawingTime = System.currentTimeMillis() - startTime;
            g.setColor(timeColor);
            g.setFont(timeFont);
            FontMetrics fm2 = g2.getFontMetrics(timeFont);
            int height = (int) (fm2.getAscent() + fm2.getDescent());
            g.drawString("drawing: " + drawingTime + " ms", 0, -height);
            g.drawString("calculation: " + analysisTime + " ms", 0, 0);
            g2.setTransform(t);
        }
    }
    
    public Dimension getMinimumSize() {
        float x = WIDTH + xBORDERs;
        float y = (HEIGHT + yBORDERs) * 3;
        if (timing) y += 50;
        return new Dimension((int) x, (int) y);
    }
}    

