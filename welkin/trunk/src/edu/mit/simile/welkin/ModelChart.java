package edu.mit.simile.welkin;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
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

public abstract class ModelChart extends JComponent {

    private float highCut = 1.0f;
    private float lowCut = 0.0f;
    
    private String title;
    
    private ModelManager model;
    
    class MyMouseMotionListener extends MouseMotionAdapter {
        public void mouseDragged(MouseEvent e) {
        }
    }

    class Count {
        public int count = 1;
    }

    public ModelChart(ModelManager model, String title) {
        this.title = title;
        this.model = model;
        this.addMouseMotionListener(new MyMouseMotionListener());
    }

    public abstract int process(WResource node);
    
    public abstract float scale(float value);

    private Map distribution = new HashMap();
    private int maxCount = 0;
    private int maxValue = 0;

    void analyze(boolean rescale) {
        this.distribution.clear();
        
        if (rescale) {
            this.maxCount = 0;
            this.maxValue = 0;
        }

        for (Iterator it = model.cache.resources.iterator(); it.hasNext();) {
            WResource n = (WResource) it.next();
            if (!n.isVisible) continue;

            int value = process(n);
            Integer _value = new Integer(value);
            Count count = (Count) this.distribution.get(_value);
            if (count == null) {
                this.distribution.put(_value,new Count());
            } else {
                count.count++;
                if (count.count > maxCount) maxCount = count.count;
            }
            if (rescale && value > maxValue) maxValue = value;
        }
    }

    void clear() {
        reanalyze();
    }
    
    void reanalyze() {
        analyze(false);
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
    final static Color cutColor = new Color(150, 150, 150, 100);
    
    final static float SIDE = 10.0f;
    
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        AffineTransform t = g2.getTransform();

        FontMetrics gridFM = g2.getFontMetrics(gridFont);
        FontMetrics titleFM = g2.getFontMetrics(titleFont);
        float titleHeight = titleFM.getAscent() + titleFM.getDescent();

        float h = getWidth() * 0.65f - titleHeight;
        float w = getWidth() - 2.0f * SIDE;
        
        g2.setColor(titleColor);
        g2.setFont(titleFont);
        g2.drawString(this.title,0, titleFM.getAscent());
        
        g2.translate(0, titleHeight);
        g2.setColor(axisColor);
        g2.draw(new Rectangle2D.Float(0,0,w,h));

        g2.translate(SIDE, h);
        g2.setColor(barColor);
        g2.setPaint(barColor);
        for (Iterator it = this.distribution.keySet().iterator(); it.hasNext();) {
            Integer degree = (Integer) it.next();
            Count count = (Count) this.distribution.get(degree);
            if (count != null && degree.intValue() > 0) {
                float x = w * scale(degree.floatValue()) / scale(maxValue);
                float y = h * scale(count.count) / scale(maxCount);
                g2.draw(new Ellipse2D.Float(x-1.0f,-y-1.0f,2.0f,2.0f));
            }
        }

        g2.translate(SIDE, h);
        g2.setColor(cutColor);
        g2.setPaint(cutColor);
        float x = w + SIDE;
        float y = lowCut * h;
        g2.draw(new Rectangle2D.Float(0,0,x,y));
        y = highCut * h;
        g2.draw(new Rectangle2D.Float(0,h,x,y));
    }
    
    public Dimension getMinimumSize() {
        return new Dimension(100,50);
    }
}    

