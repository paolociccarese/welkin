package edu.mit.simile.welkin;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;

import edu.mit.simile.welkin.InfoCache.Namespace;

public class NamespacesPanel extends JComponent {

    private static final int HTOP = 13;
    private static final int XTOP = 2;
    private static final int HROW = 18;
    
    final Font font = new Font("Verdana", Font.PLAIN, 11);
    
    Welkin welkin;
    Set namespaces = new HashSet();
    
    class NamespaceRow {
        int x,y;
        boolean on=false;
        Namespace ns;
        
        NamespaceRow(Namespace ns, int x, int y) {
            this.ns = ns;
            this.x = x;
            this.y = y;
        }
    }
    
    class MyMouseListener extends MouseAdapter implements ActionListener{
        
        JColorChooser jcc;
        Namespace namespace;
        
        public void mousePressed(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                for(Iterator i=namespaces.iterator();i.hasNext();) {
                    NamespaceRow nsr = (NamespaceRow) i.next();
                    if(e.getPoint().x>=nsr.x && e.getPoint().x<=nsr.x+8
                      && e.getPoint().y>=nsr.y-12 && e.getPoint().y<=nsr.y+4) {
                        namespace = nsr.ns;
                        jcc = new JColorChooser();
                        JDialog chooser = JColorChooser.createDialog((Component)Welkin.frame,"Pick the namespace color", true, jcc, this, this);
                        chooser.show();
                    }
                }
            } 
            repaint();
        }
        
        public void actionPerformed(ActionEvent evt) {
            if(evt.getActionCommand().equals("OK")) {
                namespace.color = jcc.getColor();
                welkin.notifyNamespaceColorChange();
            }
        }

        public void mouseReleased(MouseEvent e) {
            repaint();
        }
    }
    
    class MyMouseMotionListener extends MouseMotionAdapter {
        public void mouseMoved(MouseEvent e) {
            for(Iterator i=namespaces.iterator();i.hasNext();) {
                NamespaceRow nsr = (NamespaceRow) i.next();
                if(e.getPoint().x>=nsr.x && e.getPoint().x<=nsr.x+10
                  && e.getPoint().y>=nsr.y-12 && e.getPoint().y<=nsr.y+4) {
                    nsr.on=true;
                } else {
                    nsr.on=false;
                }
            }
            repaint(); 
        }
    }
    
    NamespacesPanel (Welkin welkin) {
        this.welkin = welkin;
        this.setLayout(null);
        
        this.addMouseListener(new MyMouseListener());
        this.addMouseMotionListener(new MyMouseMotionListener());
    }
    
    public void init() {
        namespaces = new HashSet();
        
        int shift=0;
        for(Iterator i=welkin.wrapper.cache.namespaces.iterator();i.hasNext();) {
            namespaces.add(new NamespaceRow((Namespace)i.next(),XTOP,HTOP+(HROW*shift++)));
        }
        
        this.revalidate();
        this.validate();
        this.repaint();
    }
    
    public void clear() {
        namespaces = new HashSet();
        
        this.revalidate();
        this.validate();
        this.repaint();       
    }
    
    public void paintComponent(Graphics g) {
        
        int maxWidth=0;

        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.white);
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics(font);
        
        g2.fill(new Rectangle2D.Float(0.0f, 0.0f, this.getWidth(), this.getHeight()));
        
        for(Iterator i=namespaces.iterator();i.hasNext();) {
            NamespaceRow ns = (NamespaceRow)(i.next());
            if (ns.on) g2.setColor(Color.red);
            else g2.setColor(Color.black);
            g2.drawString(ns.ns.name ,ns.x+11,ns.y);
            g2.setColor(ns.ns.color);
            Shape shape = new Rectangle(ns.x,ns.y-8,8,8);
            g2.fill(shape);
            
            maxWidth=fm.stringWidth(ns.ns.name)>maxWidth? fm.stringWidth(ns.ns.name):maxWidth;
        }
        
        if(maxWidth>0)
            this.setPreferredSize(new Dimension(maxWidth+20,HROW*namespaces.size()+4));
    }
}
