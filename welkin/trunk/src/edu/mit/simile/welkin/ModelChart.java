package edu.mit.simile.welkin;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
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

    private int xLow;
    private int xHigh;
    private int yLow;
    private int yHigh;
    
    private float xHighCut = 1.0f;
    private float xLowCut = 0.0f;

    private float yHighCut = 1.0f;
    private float yLowCut = 0.0f;

    private int x_offset = 0;
    private int y_offset = 0;
    
    private String title;
    
    private ModelManager model;
    
    final static float DRAG_TOLERANCE = 10.0f;

    class MyMouseListener extends MouseAdapter {
        public void mouseReleased(MouseEvent e) {
            filter();
        }
    }
    
    class MyMouseMotionListener extends MouseMotionAdapter {
        long last = 0;
        JComponent parent;
        
        public MyMouseMotionListener(JComponent parent) {
            this.parent = parent;
        }
        
        public void mouseDragged(MouseEvent e) {
            float width = parent.getWidth() - x_offset;
            float height = parent.getHeight() - y_offset;
            float x = e.getX() - x_offset;
            float y = height - (e.getY() - y_offset);
            float w;
            float h;
            
            if (x < 0.0f) {
                w = 0.0f;
            } else if (x > width) {
                w = 1.0f;
            } else {
                w = x / width;
            }
            
            if (y < 0.0f) {
                h = 1.0f;
            } else if (y > height) {
                h = 0.0f;
            } else {
                h = y / height;
            }

            if (Math.abs(x - xHighCut * width) < DRAG_TOLERANCE) {
                xHighCut = w;
            } else if (Math.abs(x - xLowCut * width) < DRAG_TOLERANCE) {
                xLowCut = w;
            } else if (Math.abs(y - yHighCut * height) < DRAG_TOLERANCE) {
                yHighCut = h;
            } else if (Math.abs(y - yLowCut * height) < DRAG_TOLERANCE) {
                yLowCut = h;
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

    public ModelChart(ModelManager model, String title) {
        this.title = title;
        this.model = model;
        this.addMouseMotionListener(new MyMouseMotionListener(this));
        this.addMouseListener(new MyMouseListener());
    }

    public abstract int process(WResource node);
    
    public float scale(float value,float max,float scale) {
        return (float) (Math.log((double) value) / Math.log((double) max)) * scale;
    }

    public float unscale(float value,float max,float scale) {
        return (float) (max + Math.exp((double) (value/scale)));
    }
    
    private Map distributionByValue = new HashMap();
    private Map distributionByCount = new HashMap();

    private int maxValue = 100;
    private int maxCount = 100;

    void filter() {
        int newXLow = (int) (xLowCut * (float) maxValue);
        int newXHigh = (int) (xHighCut * (float) maxValue);
        int newYLow = (int) (yLowCut * (float) maxCount);
        int newYHigh = (int) (yHighCut * (float) maxCount);

        //processValueVisibility((int) unscale(xLow,maxValue), (int) unscale(newXLow,maxValue));
        //processValueVisibility((int) unscale(xHigh,maxValue), (int) unscale(newXHigh,maxValue));
        
        this.xLow = newXLow;
        this.xHigh = newXHigh;
        this.yLow = newYLow;
        this.yHigh = newYHigh;
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
        
        this.xLow = (int) this.xLowCut * maxValue;
        this.xHigh = (int) this.xHighCut * maxValue;
        this.yLow = (int) this.yLowCut * maxCount;
        this.yHigh = (int) this.yHighCut * maxCount;
    }

    void clear() {
        xHighCut = 1.0f;
        xLowCut = 0.0f;
        yHighCut = 1.0f;
        yLowCut = 0.0f;
        reanalyze();
    }
    
    void reanalyze() {
        analyze(false);
        repaint();
    }

    public void processCountVisibility(int previous, int current) {
        int start = (current > previous) ? current : previous;
        int end = (current > previous) ? previous : current;
        boolean show = (current > previous);
        for (int i = start; i < end; i++) {
            Integer v = new Integer(i);
            if (this.distributionByCount.containsKey(v)) {
                Count c = (Count) this.distributionByCount.get(v);
                if (show) {
                    c.show();
                } else {
                    c.hide();
                };
            }
        }
    }
    
    public void processValueVisibility(int previous, int current) {
        int start = (current > previous) ? current : previous;
        int end = (current > previous) ? previous : current;
        boolean show = (current > previous);
        for (int i = start; i < end; i++) {
            Integer v = new Integer(i);
            if (this.distributionByValue.containsKey(v)) {
                Count c = (Count) this.distributionByValue.get(v);
                if (show) {
                    c.show();
                } else {
                    c.hide();
                };
            }
        }
    }
    
    final static Font timeFont = new Font("Verdana", Font.PLAIN, 9);
    final static Font gridFont = new Font("Verdana", Font.PLAIN, 8);
    final static Font titleFont = new Font("Verdana", Font.PLAIN, 9);
    
    final static Color titleColor = Color.BLACK;
    final static Color timeColor = Color.BLACK;
    final static Color axisColor = new Color(0x80,0x80,0x80);
    final static Color backgroundColor = Color.WHITE;
    final static Color gridColor = new Color(150, 150, 150, 100);
    final static Color drawColor = Color.RED;
    final static Color xFilterColor = new Color(0, 128, 0, 100);
    final static Color xFilterBorderColor = new Color(0, 192, 0, 200);
    final static Color yFilterColor = new Color(0, 0, 128, 100);
    final static Color yFilterBorderColor = new Color(0, 0, 192, 200);

    final static int XTICKS = 20;
    final static int YTICKS = 10;
    
    final static float SIDE = 10.0f;
    
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        AffineTransform t = g2.getTransform();

        FontMetrics gridFM = g2.getFontMetrics(gridFont);
        FontMetrics titleFM = g2.getFontMetrics(titleFont);
        float titleHeight = titleFM.getAscent() + titleFM.getDescent();

        float h = getHeight() - 2.0f;
        float w = getWidth() - 2.0f;
        
        // paint title
        
        g2.setColor(titleColor);
        g2.setFont(titleFont);
        g2.drawString(this.title,0, titleFM.getAscent());

        // set the offset
        
        x_offset = 0;
        y_offset = (int) titleHeight;
        
        // paint chart canvas
        
        g2.translate(x_offset, y_offset);
        h -= y_offset;
        
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

        g2.translate(w,0.0f);
        
        // paint filters
        
        float wh = w * (1.0f - xHighCut);
        Shape xHighRect = new Rectangle2D.Float(-wh,-h,wh,h);
        float wl = w * xLowCut;
        Shape xLowRect = new Rectangle2D.Float(-w,-h,wl,h);
        float hh = h * (1.0f - yHighCut);
        Shape yHighRect = new Rectangle2D.Float(-w,-h,w,hh);
        float hl = h * yLowCut;
        Shape yLowRect = new Rectangle2D.Float(-w,-hl,w,hl);
        g2.setColor(xFilterColor);
        g2.fill(xHighRect);
        g2.fill(xLowRect);
        g2.setColor(xFilterBorderColor);
        g2.draw(xHighRect);
        g2.draw(xLowRect);
        g2.setColor(yFilterColor);
        g2.fill(yHighRect);
        g2.fill(yLowRect);
        g2.setColor(yFilterBorderColor);
        g2.draw(yHighRect);
        g2.draw(yLowRect);
    }

    public Dimension getMinimumSize() {
        return new Dimension(100,50);
    }
}    

