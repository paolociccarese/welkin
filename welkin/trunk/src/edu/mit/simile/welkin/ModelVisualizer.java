package edu.mit.simile.welkin;

import java.awt.Color;
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
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Iterator;

import javax.swing.JComponent;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * @author Stefano Mazzocchi <stefanom@mit.edu>
 * @author Paolo Ciccarese <paolo@hcklab.org>
 */
public class ModelVisualizer extends JComponent implements Runnable
{
    final static float BORDER = 3.0f;
    final static float BORDERs = BORDER * 2.0f;

    final float MIN_ALPHA = 1.0f;
    final float MAX_ALPHA = MIN_ALPHA + 80.0f;
    final float ALPHA_INC = 20.0f;
    final float ZOOM_FACTOR = 2.0f;

    final Color fixedColor = Color.green;
    final Color selectColor = Color.red;
    final Color edgeColor = new Color(150, 150, 150, 100);
    final Color edgeValueColor = new Color(50, 50, 50, 100);
    final Color nodeColor = Color.red;
    final Color timeColor = Color.black;
    final Color tooltipBorderColor = Color.black;
    final Color pickedBGColor = new Color(255, 255, 0, 100);
    final Color pickedFontColor = Color.black;
    final Color highlightBGColor = new Color(255, 255, 0, 150);
    final Color highlightFontColor = Color.black;
    final Color groupColor = new Color(0, 0, 0, 200);
    final Color groupFontColor = groupColor;
    final Color groupBGColor = new Color(255, 255, 255, 100);
    final Color groupBorderColor = Color.black;

    final Font bigFont = new Font("Verdana", Font.BOLD, 10);
    final Font smallFont = new Font("Verdana", Font.PLAIN, 8);
    final Font tinyFont = new Font("Verdana", Font.PLAIN, 9);

    public int delay = 50; // time (milliseconds)
    public float mass = 10.0f; // mass (kg)
    public float drag = 2.0f; // drag coefficient (kg / second)
    public float attraction = 1.0f; // force (kg * pixel / second^2) [/100]
    public float repulsion = 1.0f; // force (kg * pixel / second^2) [*100]
    
    float REPULSION_END = 40.0f; // distance (pixel)
    float REPULSION_ENDs = 2.0f * REPULSION_END;

    public boolean random = false;
    public boolean antialias = true;
    public boolean drawedges = true;
    public boolean drawnodes = true;
    public boolean timing = true;
    public boolean drawgroups = true;
    public boolean drawedgevalues = false;
    public boolean background = false;

    //    Node pick;
    Resource pick;

    boolean pickfixed;
    boolean zoom = false;
    float zoomX = 0.0f;
    float zoomY = 0.0f;
    float alpha = MIN_ALPHA;

    class MyMouseListener extends MouseAdapter
    {
        public void mousePressed(MouseEvent e)
        {
            if (e.getButton() == MouseEvent.BUTTON3 || e.isPopupTrigger() )
            {
                zoom = true;
                zoomX = (float) e.getX() - cx;
                zoomY = (float) e.getY() - cy;
            } else
            {
                float x = (float) e.getX() - cx;
                float y = (float) e.getY() - cy;
                double bestdist = Double.MAX_VALUE;
                Iterator i = model.getNodes().iterator();
                while (i.hasNext())
                {
                    Resource n = (Resource) i.next();
                    if (n != null)
                    {
                        float dx = model.getX(n) - x;
                        float dy = model.getY(n) - y;
                        float dist = dx * dx + dy * dy;
                        if (dist < bestdist)
                        {
                            pick = n;
                            bestdist = dist;
                        }
                    }
                }
                if (pick != null)
                {
                    pickfixed = model.getFixedNode(pick);
                    model.setFixedNode(pick, true);
                    model.setX(pick, x);
                    model.setY(pick, y);
                }
            }
            repaint();
        }

        public void mouseReleased(MouseEvent e)
        {
            if (zoom)
            {
                zoom = false;
            } else
            {
                if (pick != null)
                {
                    int x = e.getX() - (int) cx;
                    int y = e.getY() - (int) cy;
                    model.setX(pick, x);
                    model.setY(pick, y);
                    keepInsideCanvas(pick);
                    if (e.getClickCount() == 2)
                    {
                        model.setFixedNode(pick, !pickfixed);
                    } else
                    {
                        model.setFixedNode(pick, pickfixed);
                    }
                    pick = null;
                }
            }
            repaint();
        }
    }

    class MyMouseMotionListener extends MouseMotionAdapter
    {
        public void mouseDragged(MouseEvent e)
        {
            if (pick != null)
            {
                int x = e.getX() - (int) cx;
                int y = e.getY() - (int) cy;
                model.setX(pick, x);
                model.setY(pick, y);
                keepInsideCanvas(pick);
                repaint();
            } else if (zoom)
            {
                zoomX = (float) e.getX() - cx;
                zoomY = (float) e.getY() - cy;
                repaint();
            }
        }
    }

    ModelWrapper model;

    Thread relaxer;

    float cx;

    float cy;

    public ModelVisualizer(ModelWrapper model)
    {
        this.addMouseMotionListener(new MyMouseMotionListener());
        this.addMouseListener(new MyMouseListener());
        this.model = model;
    }

    public void setGraph(ModelWrapper model)
    {
        this.model = model;
    }

    public ModelWrapper getGraph()
    {
        return this.model;
    }

    public boolean isRunning()
    {
        return relaxer != null;
    }

    public void start()
    {
        relaxer = new Thread(this);
        relaxer.start();
    }

    public void stop()
    {
        relaxer = null;
    }

    public void reshape(int x, int y, int w, int h)
    {
        super.reshape(x, y, w, h);
        this.cx = w / 2.0f;
        this.cy = h / 2.0f;
    }

    public void run()
    {
        Thread thisThread = Thread.currentThread();
        while (relaxer == thisThread)
        {
            if (random && (Math.random() < 0.03))
            {
                Resource n = (Resource) model.getNodes().get(
                        (int) (Math.random() * model.getNodes().size()));
                if (!model.getFixedNode(n))
                {
                    model
                            .setX(n, (float) (model.getX(n) + (100 * Math
                                    .random() - 50)));
                    model
                            .setY(n, (float) (model.getY(n) + (100 * Math
                                    .random() - 50)));
                    keepInsideCanvas(n);
                }
            }
            simulate();
            try
            {
                Thread.sleep(delay);
            } catch (InterruptedException e)
            {
                break;
            }
        }
    }

    public void circle()
    {
        float r = Math.min(cx, cy) - 50.0f;
        float alpha = (float) (2.0d * Math.PI / model.getNodes().size());

        int j = 0;
        Iterator i = model.getNodes().iterator();
        while (i.hasNext())
        {
            Resource n = (Resource) i.next();
            model.setX(n, (float) (r * Math.sin(alpha * j)));
            model.setY(n, (float) (r * Math.cos(alpha * j++)));
        }
        repaint();
    }

    public void scramble()
    {
        Iterator i = model.getNodes().iterator();
        while (i.hasNext())
        {
            Resource n = (Resource) i.next();
            if (!model.getFixedNode(n))
            {
                model
                        .setX(n, (float) ((double) (cx - 50.0f) * (Math
                                .random() - 0.5d)));
                model
                        .setY(n, (float) ((double) (cy - 50.0f) * (Math
                                .random() - 0.5d)));
            }
        }
        repaint();
    }

    public void shake()
    {
        int j = 0;
        Iterator i = model.getNodes().iterator();
        while (i.hasNext())
        {
            Resource n = (Resource) i.next();
            if (!model.getFixedNode(n))
            {
                model.setX(n,
                        (float) (model.getX(n) + (80 * Math.random() - 40)));
                model.setY(n,
                        (float) (model.getY(n) + (80 * Math.random() - 40)));
                keepInsideCanvas(n);
            }
        }
        repaint();
    }

    void keepInsideCanvas(Resource n)
    {
        if (model.getX(n) < -cx)
        {
            model.setX(n, -cx);
        } else if (model.getX(n) > cx)
        {
            model.setX(n, cx);
        }
        if (model.getY(n) < -cy)
        {
            model.setY(n, cy);
        } else if (model.getY(n) > cy)
        {
            model.setY(n, cy);
        }
    }

    long simulationTime;

    long drawingTime;

    float attractive(float d, float weight)
    {
        return attraction * weight * d / 100.0f;
    }

    float repulsive(float d)
    {
        if (d < REPULSION_END)
        {
            float r = 100.0f * repulsion * (d - REPULSION_END)
                    / (d * (d - REPULSION_ENDs));
            return Math.min(r, 500.0f);
        } else
        {
            return 0.0f;
        }
    }

    void simulate()
    {
        long startTime = 0;

        if (timing)
            startTime = System.currentTimeMillis();

        Iterator i = model.getNodes().iterator();
        while (i.hasNext())
        {
            Resource n = (Resource) i.next();

            float xi = model.getX(n);
            float yi = model.getY(n);
            float vxi = model.getVX(n);
            float vyi = model.getVY(n);

            float fx = 0.0f;
            float fy = 0.0f;

            Iterator j = model.getNodes().iterator();
            while (j.hasNext())
            {
                Resource m = (Resource) j.next();

                float xj = model.getX(m);
                float yj = model.getY(m);

                // calculate euclidean distance
                float deltax = xi - xj;
                float deltay = yi - yj;
                float d2 = deltax * deltax + deltay * deltay;
                float d = (float) Math.sqrt(d2);
                if (d == 0)
                    d = 0.0001f; // avoid divide by zero

                Property edge = (Property) model.getConnection(n, m);

                float weight = (edge != null) ? 1.0f : 0.0f;

                // attractive force
                float af = attractive(d, weight);

                // repulsion force
                float rf = repulsive(d);

                // resulting force
                float f = rf - af;

                // apply the forces
                fx += (deltax / d) * f;
                fy += (deltay / d) * f;
            }

            // repulsion force from the borders
            fx += repulsive(xi + cx) - repulsive(cx - xi);
            fy += repulsive(yi + cy) - repulsive(cy - yi);

            // drag
            fx -= drag * vxi;
            fy -= drag * vyi;

            // update speed with acceleration
            vxi += fx / mass;
            vyi += fy / mass;

            // update location with speed
            xi += vxi;
            yi += vyi;

            // TODO Verify
            if (!model.getFixedNode(n))
            {
                model.setX(n, xi);
                model.setY(n, yi);
                model.setVX(n, vxi);
                model.setVY(n, vyi);
                keepInsideCanvas(n);
            }
        }

        if (timing)
            simulationTime = System.currentTimeMillis() - startTime;

        repaint();
    }

    public float zoom(float x, float y)
    {
        if (alpha > MIN_ALPHA)
        {
            x -= zoomX;
            y -= zoomY;
            return ZOOM_FACTOR
                    * (float) Math.exp(-((x * x) + (y * y)) / (alpha * alpha));
        } else
        {
            return 0.0f;
        }
    }

    public void paintComponent(Graphics g)
    {
        if (model == null || model.getNodes() == null)
            return;

        long startTime = 0;

        if (timing)
            startTime = System.currentTimeMillis();

        Graphics2D g2 = (Graphics2D) g;

        if (antialias)
        {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }

        if (background)
        {
            g2.setColor(Color.white);
            g2.fill(new Rectangle2D.Float(0.0f, 0.0f, 2.0f * cx, 2.0f * cy));
        }

        if (drawedges)
        {
            g2.setFont(tinyFont);
            FontMetrics fm = g2.getFontMetrics(tinyFont);
            float ascent = fm.getAscent();

            Iterator nodes = model.getNodes().iterator();
            while (nodes.hasNext())
            {
                Resource n1 = (Resource) nodes.next();
                Iterator edges = model.getConnectionTo(n1).iterator();
                while (edges.hasNext())
                {
                    Resource n2 = (Resource) edges.next();
                    if ((n1 != null) && (n2 != null))
                    {
                        float xn1 = model.getX(n1);
                        float xn2 = model.getX(n2);
                        float yn1 = model.getY(n1);
                        float yn2 = model.getY(n2);
                        
                        float z1 = zoom(xn1, yn1);
                        float z2 = zoom(xn2, model.getY(n2));
                        float x1 = xn1 + (xn1 - zoomX)
                                * z1 + cx;
                        float x2 = xn2 + (xn2 - zoomX)
                                * z2 + cx;
                        float y1 = yn1 + (yn1 - zoomY)
                                * z1 + cy;
                        float y2 = yn2 + (yn2 - zoomY)
                                * z2 + cy;

                        g2.setColor(edgeColor);
                        g2.draw(new Line2D.Float(x1, y1, x2, y2));
                        if (drawedgevalues)
                        {
                            float x = (x2 + x1) / 2.0f;
                            float y = (y2 + y1) / 2.0f + ascent;
                            String value = "1.0";
                            //                            String value = String.valueOf(((Edge) n1.edgesTo
                            //                                    .get(n2)).value);
                            g2.setColor(edgeValueColor);
                            g2.drawString(value, x, y);
                        }
                    }
                }
            }
        }

        if (drawnodes)
        {
            Iterator i = model.getNodes().iterator();
            while (i.hasNext())
            {
                Resource n = (Resource) i.next();
                if (n != null)
                {
                    float xn = model.getX(n);
                    float yn = model.getY(n);
                    float z = zoom(xn, yn);
                    float x = xn + (xn - zoomX) * z + cx;
                    float y = yn + (yn - zoomY) * z + cy;
                    Shape nodeshape = new Rectangle2D.Float(x - 3.0f, y - 3.0f,
                            6.0f, 6.0f);
                    if (n == pick)
                    {
                        g2.setColor(selectColor);
                        g2.fill(nodeshape);
                    } else
                    {
                        g2.setColor(model.getFixedNode(n) ? fixedColor
                                : nodeColor);
                        g2.draw(nodeshape);
                    }
                }
            }
        }

        //        if (drawgroups)
        //        {
        //            g2.setFont(smallFont);
        //            FontMetrics fm = g2.getFontMetrics(smallFont);
        //            float ascent = fm.getAscent();
        //            float descent = fm.getDescent();
        //            float height = ascent + descent + 2 * BORDER;
        //
        //            Iterator i = graph.groups.values().iterator();
        //            while (i.hasNext())
        //            {
        //                Group group = (Group) i.next();
        //                Iterator j = group.nodes.iterator();
        //                float minX = Float.MAX_VALUE;
        //                float minY = Float.MAX_VALUE;
        //                float maxX = -Float.MAX_VALUE;
        //                float maxY = -Float.MAX_VALUE;
        //                while (j.hasNext())
        //                {
        //                    Resource n = (Resource) j.next();
        //// if (n.x < minX)
        //// minX = n.x;
        //// if (n.x > maxX)
        //// maxX = n.x;
        //// if (n.y < minY)
        //// minY = n.y;
        //// if (n.y > maxY)
        //// maxY = n.y;
        //                    
        //                    if (model.getX(n) < minX)
        //                        minX = model.getX(n);
        //                    if (model.getX(n) > maxX)
        //                        maxX = model.getX(n);
        //                    if (model.getY(n) < minY)
        //                        minY = model.getY(n);
        //                    if (model.getY(n) > maxY)
        //                        maxY = model.getY(n);
        //                }
        //                float dx = maxX - minX + 5.0f;
        //                float dy = maxY - minY + 5.0f;
        //                float width = fm.stringWidth(group.name) + BORDERs;
        //                g2.setColor(groupColor);
        //                g2.draw(new Ellipse2D.Float(minX + cx, minY + cy, dx, dy));
        //                float sx = (dx - width) / 2.0f + minX + cx;
        //                float sy = (dy - height) / 2.0f + minY + cy;
        //                g2.setColor(groupFontColor);
        //                g2.drawString(group.name, sx + BORDER, sy + ascent + BORDER);
        //            }
        //        }

        AffineTransform t = g2.getTransform();

        Iterator i = model.getNodes().iterator();
        while (i.hasNext())
        {
            Resource n = (Resource) i.next();
            if ((n != null) && (n.getURI() != null))
            {
                Font font = ((n == pick) || zoom) ? bigFont : smallFont;
                g2.setFont(font);
                FontMetrics fm = g2.getFontMetrics(font);
                float width = fm.stringWidth(n.getURI()) + BORDERs;
                float ascent = fm.getAscent();
                float descent = fm.getDescent();
                float height = ascent + descent + BORDERs;
                float halfHeight = height / 2.0f;

                if (zoom)
                {
                    float z = zoom(model.getX(n), model.getY(n));
                    if (z > ZOOM_FACTOR * 0.95f)
                    {
                        float xn = model.getX(n);
                        float yn = model.getY(n);
                        float x = xn + (xn - zoomX) * z;
                        float y = yn + (yn - zoomY) * z;
                        float dx = x - zoomX;
                        float dy = y - zoomY;
                        double theta;
                        if (Math.abs(dx) < Math.abs(dy))
                        {
                            theta = Math.acos(dx / dy);
                            if (dy < 0.0f)
                                theta += Math.PI;
                        } else
                        {
                            theta = Math.asin(dy / dx);
                            if (dx < 0.0f)
                                theta += Math.PI;
                        }
                        float d = (float) Math.sqrt((dx * dx) + (dy * dy));

                        if (dx < 0.0f)
                        {
                            theta -= Math.PI;
                            d = -d - width;
                        }

                        g2.translate(zoomX + cx, zoomY + cy);
                        g2.rotate(theta);

                        Shape rectangle = new RoundRectangle2D.Float(d,
                                -halfHeight, width, height, height, height);
                        g2.setColor(highlightBGColor);
                        g2.fill(rectangle);
                        g2.setColor(tooltipBorderColor);
                        g2.draw(rectangle);
                        g2.setColor(highlightFontColor);
                        g2.drawString(n.getURI(), d + BORDER, +BORDER);
                        g2.setTransform(t);
                    }
                } else
                {
                    float x = model.getX(n);
                    if ((x + width + 2 * BORDER) > cx)
                    {
                        x -= width;
                    }
                    x += cx;

                    float y = model.getY(n);
                    if ((y - 2 * BORDER) < -cy)
                    {
                        y += height;
                    }
                    y += cy;

                    if ((n == pick) || model.getFixedNode(n) /*
                                                              * ||
                                                              * (n.highlighted)
                                                              */)
                    {
                        Shape rectangle = new RoundRectangle2D.Float(x, y
                                - halfHeight, width, height, height, height);
                        Color bgColor = (n == pick) ? pickedBGColor
                                : highlightBGColor;
                        Color fontColor = (n == pick) ? pickedFontColor
                                : highlightFontColor;
                        g2.setColor(bgColor);
                        g2.fill(rectangle);
                        g2.setColor(tooltipBorderColor);
                        g2.draw(rectangle);
                        g2.setColor(fontColor);
                        g2.drawString(n.getURI(), x + BORDER, y + BORDER);
                        
                        if ((n == pick))
                        {
                            // count properties and max text length
                            int count = 0;
                            float max = width;
                            for (Iterator it = model.getLiterals(n); it
                                    .hasNext();)
                            {
                                Statement no = (Statement) it.next();
                                fm = g2.getFontMetrics(smallFont);
                                float length = fm.stringWidth(no.getPredicate()
                                        .toString()
                                        + " -> " + no.getObject())
                                        + BORDERs;
                                if (length > max)
                                    max = length;
                                count++;
                            }
                            
                            float startY=0;
                            float rectHeigh=count * 12 + BORDER;
                            if((y+rectHeigh)>cy)
                                startY=-(rectHeigh+height+2);

                            // Draw properties rect
                            Shape rect = new Rectangle2D.Float(x, y + 21 +startY
                                    - halfHeight, max, rectHeigh);
                            g2.setColor(Color.WHITE);
                            g2.fill(rect);
                            g2.setColor(Color.BLACK);
                            g2.draw(rect);
                            g2.setFont(smallFont);

                            int ddy = 18;
                            for (Iterator it = model.getLiterals(n); it
                                    .hasNext();)
                            {
                                Statement no = (Statement) it.next();
                                g2.drawString(no.getPredicate().toString()
                                        + " -> " + no.getObject().toString(), x
                                        + BORDER, y + BORDER + ddy+ startY);
                                ddy += 12;
                            }
                        }
                    }
                }
            }
        }

        if (timing)
        {
            drawingTime = System.currentTimeMillis() - startTime;
            g.setColor(timeColor);
            g.setFont(smallFont);
            g.drawString("calculation: " + simulationTime + " ms", 5, 15);
            g.drawString("drawing: " + drawingTime + " ms", 5, 25);
            g.drawString("nodes: " + model.getNodes().size(), 5, 35);
            //            g.drawString("edges: " + graph.edges, 5, 45);
        }

        if (zoom)
        {
            if (alpha < MAX_ALPHA)
            {
                alpha += ALPHA_INC;
                repaint();
            }
        } else
        {
            if (alpha > MIN_ALPHA)
            {
                alpha -= ALPHA_INC;
                repaint();
            }
        }
    }
}

