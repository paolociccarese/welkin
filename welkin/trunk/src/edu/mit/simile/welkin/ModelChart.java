package edu.mit.simile.welkin;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JComponent;

import edu.mit.simile.welkin.ModelCache.WResource;

public abstract class ModelChart extends JComponent {

    private int lowValue;
    private int highValue;
    private int lowCount;
    private int highCount;
    
    private ModelManager model;
    
    final static float DRAG_TOLERANCE = 10.0f;

    private int westDelta;
    private int eastDelta;
    private int northDelta;
    private int southDelta;
    
    class MyMouseListener extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            westDelta = highValue - x;
            eastDelta = x - lowValue;
            northDelta = highCount - y;
            southDelta = y - lowCount;
        }
        public void mouseReleased(MouseEvent e) {
            filter();
        }
    }
    
    class MyMouseMotionListener extends MouseMotionAdapter {
        JComponent parent;
        
        public MyMouseMotionListener(JComponent parent) {
            this.parent = parent;
        }
        
        public void mouseMoved(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            
            if (Math.abs(x - highValue) < DRAG_TOLERANCE) {
                this.parent.setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
            } else if (Math.abs(x - lowValue) < DRAG_TOLERANCE) {
                this.parent.setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
            } else if (Math.abs(y - highCount) < DRAG_TOLERANCE) {
                this.parent.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
            } else if (Math.abs(y - lowCount) < DRAG_TOLERANCE) {
                this.parent.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
            } else {
                this.parent.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            }
        }
        
        public void mouseDragged(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            
            if (Math.abs(x - highValue) < DRAG_TOLERANCE) {
                highValue = x;
            } else if (Math.abs(x - lowValue) < DRAG_TOLERANCE) {
                lowValue = x;
            } else if (Math.abs(y - highCount) < DRAG_TOLERANCE) {
                highCount = y;
            } else if (Math.abs(y - lowCount) < DRAG_TOLERANCE) {
                lowCount = y;
            } else {
                highValue = x + westDelta;
                lowValue = x - eastDelta;
                highCount = y + northDelta;
                lowCount = y - northDelta;
            }
            
            parent.repaint();
        }
    }

    class Count extends ArrayList {
        boolean visible = true;
        
        public Count(Object o) {
            this.add(o);
        }
        
        public void hide() {
            for (Iterator i = this.iterator(); i.hasNext();) {
                WResource r = (WResource) i.next();
                r.hide();
            }
            this.visible = false;
        }
        
        public void show() {
            for (Iterator i = this.iterator(); i.hasNext();) {
                WResource r = (WResource) i.next();
                r.show();
            }
            this.visible = false;
        }
    }

    public ModelChart(ModelManager model) {
        this.model = model;
        this.addMouseMotionListener(new MyMouseMotionListener(this));
        this.addMouseListener(new MyMouseListener());
    }

    public abstract int process(WResource node);
    
    public float scale(float value,float max,float scale) {
        return (float) (Math.log((double) value) / Math.log((double) max)) * scale;
    }

    public float unscale(float value,float max,float scale) {
        return (float) (Math.exp((double) (value/scale) * Math.log((double) max)));
    }
    
    private Map distributionByValue = new HashMap();
    private Map distributionByCount = new HashMap();

    private int maxValue = 100;
    private int maxCount = 100;

    void filter() {
        float w = (float) getWidth() - 1.0f;
        float h = (float) getHeight() - 1.0f;
        
        float hv = unscale(highValue,maxValue,w);
        float lv = unscale(lowValue,maxValue,w);
        float hc = unscale(h - lowCount,maxCount,h);
        float lc = unscale(h - highCount,maxCount,h);
        
        processValueVisibility(lv,hv);
        processCountVisibility(lc,hc);
    }

    void analyze(boolean rescale) {
        this.distributionByValue.clear();
        
        if (rescale) {
            this.maxCount = 0;
            this.maxValue = 0;
        }

        for (Iterator it = model.cache.resources.iterator(); it.hasNext();) {
            WResource n = (WResource) it.next();
            if (!n.isVisible) continue;

            int value = process(n);
            Integer _value = new Integer(value);
            Count count = (Count) this.distributionByValue.get(_value);
            if (count == null) {
                count = new Count(n);
                this.distributionByValue.put(_value,count);
            } else {
                count.add(n);
                Integer v = new Integer(count.size());
                if (count.size() > maxCount) maxCount = count.size();
            }
            if (rescale && value > maxValue) maxValue = value;
        }
        
        this.lowValue = 0;
        this.highValue = getWidth() - 1;
        this.lowCount = 0;
        this.highCount = getHeight() - 1;
    }

    void clear() {
        reanalyze();
    }
    
    void reanalyze() {
        analyze(false);
        repaint();
    }

    public void processCountVisibility(float low, float high) {
        System.out.println("count: " + low + " " + high);
        for (Iterator i = this.distributionByCount.keySet().iterator(); i.hasNext();) {
            Count c = (Count) this.distributionByCount.get(i.next());
            float size = (float) c.size();
            if (size < low || size > high) {
                c.hide();
            } else {
                c.show();
            }
        }
    }
    
    public void processValueVisibility(float low, float high) {
        System.out.println("value: " + low + " " + high);
        for (Iterator i = this.distributionByValue.keySet().iterator(); i.hasNext();) {
            Count c = (Count) this.distributionByValue.get(i.next());
            float size = (float) c.size();
            if (size < low || size > high) {
                c.hide();
            } else {
                c.show();
            }
        }
    }
    
    final static Color titleColor = Color.BLACK;
    final static Color axisColor = new Color(0x80,0x80,0x80);
    final static Color backgroundColor = Color.WHITE;
    final static Color gridColor = new Color(150, 150, 150, 100);
    final static Color drawColor = Color.RED;
    final static Color xFilterColor = new Color(0, 128, 0, 100);
    final static Color xFilterBorderColor = new Color(0, 192, 0, 200);
    final static Color yFilterColor = new Color(0, 0, 128, 100);
    final static Color yFilterBorderColor = new Color(0, 0, 192, 200);

    final static int XTICKS = 20;
    final static int YTICKS = 20;
    
    final static float SIDE = 10.0f;
    
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        AffineTransform t = g2.getTransform();

        float h = getHeight() - 1.0f;
        float w = getWidth() - 1.0f;
                
        Shape chartRect = new Rectangle2D.Float(0,0,w,h);
        g2.setColor(backgroundColor);
        g2.fill(chartRect);
        g2.setColor(axisColor);
        g2.draw(chartRect);

        g2.translate(0.0f, h);

        // paint grid
        
        int x_inc = maxValue / XTICKS;
        int y_inc = maxCount / YTICKS;

        g2.setColor(gridColor);
        for (int i = 1; i < XTICKS; i++) {
            float x = scale(i * x_inc,maxValue,w);
            g2.draw(new Line2D.Float(x,0.0f,x,-h));
        }
        for (int i = 1; i < YTICKS; i++) {
            float y = scale(i * y_inc,maxCount,h);
            g2.draw(new Line2D.Float(0.0f,-y,w,-y));
        }
 
        // paint chart
        
        g2.setColor(drawColor);
        for (Iterator it = this.distributionByValue.keySet().iterator(); it.hasNext();) {
            Integer degree = (Integer) it.next();
            Count count = (Count) this.distributionByValue.get(degree);
            if (count != null && degree.intValue() > 0) {
                float x = scale(degree.floatValue(),maxValue,w);
                float y = scale(count.size(),maxCount,h);
                g2.draw(new Ellipse2D.Float(x-1.0f,-y-1.0f,2.0f,2.0f));
            }
        }

        // paint filters
        
        float hv = highValue;
        Shape westRect = new Rectangle2D.Float(hv,-h,w - hv,h);
        float lv = lowValue;
        Shape eastRect = new Rectangle2D.Float(0,-h,lv,h);
        float hc = highCount;
        Shape northRect = new Rectangle2D.Float(0,hc - h,w,h - hc);
        float lc = lowCount;
        Shape southRect = new Rectangle2D.Float(0,-h,w,lc);
        g2.setColor(xFilterColor);
        g2.fill(westRect);
        g2.fill(eastRect);
        g2.setColor(xFilterBorderColor);
        g2.draw(westRect);
        g2.draw(eastRect);
        g2.setColor(yFilterColor);
        g2.fill(northRect);
        g2.fill(southRect);
        g2.setColor(yFilterBorderColor);
        g2.draw(northRect);
        g2.draw(southRect);
    }

    public Dimension getMinimumSize() {
        return new Dimension(100,50);
    }
}    

