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

import com.hp.hpl.jena.rdf.model.Resource;

import edu.mit.simile.welkin.InfoCache.CachedLiteral;
import edu.mit.simile.welkin.InfoCache.Edge;
import edu.mit.simile.welkin.InfoCache.Node;

/**
 * @author Stefano Mazzocchi <stefano@apache.org>
 * @author Paolo Ciccarese <paolo@hcklab.org>
 */
public class ModelVisualizer extends JComponent implements Runnable {
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

    ModelWrapper model;
    Node pick;

    boolean pickfixed;
    boolean zoom = false;

    float zoomX = 0.0f;
    float zoomY = 0.0f;
    float alpha = MIN_ALPHA;

    class MyMouseListener extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON3 || e.isPopupTrigger()) {
                zoom = true;
                zoomX = (float) e.getX() - cx;
                zoomY = (float) e.getY() - cy;
            } else {
                float x = (float) e.getX() - cx;
                float y = (float) e.getY() - cy;
                double bestdist = Double.MAX_VALUE;

                for (Iterator i = model.cache.nodes.iterator(); i.hasNext();) {
                    Node n = (Node) i.next();
                    if(!n.isVisible) continue;
                    if (n != null) {
                        float dx = n.x - x;
                        float dy = n.y - y;
                        float dist = dx * dx + dy * dy;
                        if (dist < bestdist) {
                            pick = n;
                            bestdist = dist;
                        }
                    }
                }

                if (pick != null) {
                    pickfixed = pick.fixed;
                    pick.fixed = true;
                    pick.x = x;
                    pick.y = y;
                }
            }
            repaint();
        }

        public void mouseReleased(MouseEvent e) {
            if (zoom) {
                zoom = false;
            } else {
                if (pick != null) {
                    int x = e.getX() - (int) cx;
                    int y = e.getY() - (int) cy;
                    pick.x = x;
                    pick.y = y;
                    keepInsideCanvas(pick);
                    if (e.getClickCount() == 2) {
                        pick.fixed = !pickfixed;
                    } else {
                        pick.fixed = pickfixed;
                    }
                    pick = null;
                }
            }
            repaint();
        }
    }

    class MyMouseMotionListener extends MouseMotionAdapter {
        public void mouseDragged(MouseEvent e) {
            if (pick != null) {
                int x = e.getX() - (int) cx;
                int y = e.getY() - (int) cy;
                pick.x = x;
                pick.y = y;
                keepInsideCanvas(pick);
                repaint();
            } else if (zoom) {
                zoomX = (float) e.getX() - cx;
                zoomY = (float) e.getY() - cy;
                repaint();
            }
        }
    }

    Thread relaxer;

    float cx;

    float cy;

    public ModelVisualizer(ModelWrapper model) {
        this.addMouseMotionListener(new MyMouseMotionListener());
        this.addMouseListener(new MyMouseListener());
        this.model = model;
    }

    public void setGraph(ModelWrapper model) {
        this.model = model;
    }

    public ModelWrapper getGraph() {
        return this.model;
    }

    public boolean isRunning() {
        return relaxer != null;
    }

    public void start() {
        relaxer = new Thread(this);
        relaxer.start();
    }

    public void stop() {
        relaxer = null;
    }

    public void reshape(int x, int y, int w, int h) {
        super.reshape(x, y, w, h);
        this.cx = w / 2.0f;
        this.cy = h / 2.0f;
    }

    public void run() {
        Thread thisThread = Thread.currentThread();
        while (relaxer == thisThread) {
            if (random && (Math.random() < 0.03)) {
                Resource n = (Resource) model.getNodes().get(
                        (int) (Math.random() * model.getNodes().size()));
                if (!model.getFixedNode(n)) {
                    float[] xy = model.getCoordinateXY(n);
                    pick.x = (float) (xy[0] + (100 * Math.random() - 50));
                    pick.y = (float) (xy[1] + (100 * Math.random() - 50));
                    keepInsideCanvas(n);
                }
            }
            simulate();
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public void circle() {
        float r = Math.min(cx, cy) - 50.0f;
        float alpha = (float) (2.0d * Math.PI / model.getNodes().size());

        int j = 0;
        for (Iterator it = model.cache.nodes.iterator(); it.hasNext();) {
            Node n = (Node) it.next();
            n.x = (float) (r * Math.sin(alpha * j));
            n.y = (float) (r * Math.cos(alpha * j++));
        }
        repaint();
    }

    public void scramble() {
        for (Iterator it = model.cache.nodes.iterator(); it.hasNext();) {
            Node n = (Node) it.next();
            if (!n.fixed) {
                n.x = (float) ((double) (cx - 50.0f) * (Math.random() - 0.5d));
                n.y = (float) ((double) (cy - 50.0f) * (Math.random() - 0.5d));
            }
        }
        repaint();
    }

    public void shake() {
        int j = 0;
        for (Iterator it = model.cache.nodes.iterator(); it.hasNext();) {
            Node n = (Node) it.next();
            if (!n.fixed) {
                n.x = (float) (n.x + (80 * Math.random() - 40));
                n.y = (float) (n.y + (80 * Math.random() - 40));
                keepInsideCanvas(n);
            }
        }
        repaint();
    }

    void keepInsideCanvas(Node n) {
        if (n.x < -cx) {
            n.x = -cx;
        } else if (n.x > cx) {
            n.x = cx;
        }
        if (n.y < -cy) {
            n.y = cy;
        } else if (n.y > cy) {
            n.y = cy;
        }
    }

    void keepInsideCanvas(Resource n) {
        float[] xy = model.getCoordinateXY(n);
        float tmpX = 0;
        float tmpY = 0;
        if (xy[0] < -cx) {
            tmpX = -cx;
        } else if (xy[0] > cx) {
            tmpX = cx;
        }
        if (xy[1] < -cy) {
            tmpY = cy;
        } else if (xy[1] > cy) {
            tmpY = cy;
        }
        model.setCoordinateXY(n, tmpX, tmpY);
    }

    long simulationTime;

    long drawingTime;

    float attractive(float d, float weight) {
        return attraction * weight * d / 100.0f;
    }

    float repulsive(float d) {
        if (d < REPULSION_END) {
            float r = 100.0f * repulsion * (d - REPULSION_END)
                    / (d * (d - REPULSION_ENDs));
            return Math.min(r, 500.0f);
        } else {
            return 0.0f;
        }
    }

    void simulate() {
        long startTime = 0;

        if (timing)
            startTime = System.currentTimeMillis();

        for (Iterator it = model.cache.nodes.iterator(); it.hasNext();) {
            Node n = (Node) it.next();
            
            if(!n.isVisible) continue;

            float xi = n.x;
            float yi = n.y;
            float vxi = n.vx;
            float vyi = n.vy;

            float fx = 0.0f;
            float fy = 0.0f;

            for (Iterator j = model.cache.nodes.iterator(); j.hasNext();) {
                Node m = (Node) j.next();
                
                if(!m.isVisible) continue;

                float xj = m.x;
                float yj = m.y;

                // calculate euclidean distance
                float deltax = xi - xj;
                float deltay = yi - yj;
                float d2 = deltax * deltax + deltay * deltay;
                float d = (float) Math.sqrt(d2);
                if (d == 0)
                    d = 0.0001f; // avoid divide by zero

//                float weight1 = (n.isObjectOf(m)) ? 1.0f : 0.0f;
                float weight = (model.cache.getEntries(n.hash,m.hash)!=null) ? 1.0f : 0.0f;
                
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
            if (!n.fixed) {
                n.x = xi;
                n.y = yi;
                n.vx = vxi;
                n.vy = vyi;
                keepInsideCanvas(n);
            }
        }

        if (timing)
            simulationTime = System.currentTimeMillis() - startTime;

        repaint();
    }

    public float zoom(float x, float y) {
        if (alpha > MIN_ALPHA) {
            x -= zoomX;
            y -= zoomY;
            return ZOOM_FACTOR
                    * (float) Math.exp(-((x * x) + (y * y)) / (alpha * alpha));
        } else {
            return 0.0f;
        }
    }

    public void paintComponent(Graphics g) {
        if (model.getNodes() == null)
            return;

        long startTime = 0;

        if (timing)
            startTime = System.currentTimeMillis();

        Graphics2D g2 = (Graphics2D) g;

        if (antialias) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }

        if (background) {
            g2.setColor(Color.white);
            g2.fill(new Rectangle2D.Float(0.0f, 0.0f, 2.0f * cx, 2.0f * cy));
        }

        if (drawedges) {
            g2.setFont(tinyFont);
            FontMetrics fm = g2.getFontMetrics(tinyFont);
            float ascent = fm.getAscent();

            for (Iterator nodes = model.cache.nodes.iterator(); nodes.hasNext();) {
                Node n1 = (Node) nodes.next();
                if(!n1.isVisible) continue;
                for (Iterator edges = n1.linkedObjectNodes.iterator(); edges
                        .hasNext();) {
                    Node n2 = ((Edge) edges.next()).object;
                    if(!n2.isVisible) continue;
                    float z1 = zoom(n1.x, n1.y);
                    float z2 = zoom(n2.x, n2.y);
                    float x1 = n1.x + (n1.x - zoomX) * z1 + cx;
                    float x2 = n2.x + (n2.x - zoomX) * z2 + cx;
                    float y1 = n1.y + (n1.y - zoomY) * z1 + cy;
                    float y2 = n2.y + (n2.y - zoomY) * z2 + cy;

                    g2.setColor(edgeColor);
                    g2.draw(new Line2D.Float(x1, y1, x2, y2));
                    if (drawedgevalues) {
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

        if (drawnodes) {
            for (Iterator i = model.cache.nodes.iterator(); i.hasNext();) {
                Node n = (Node) i.next();
                if(!n.isVisible) continue;
                float z = zoom(n.x, n.y);
                float x = n.x + (n.x - zoomX) * z + cx;
                float y = n.y + (n.y - zoomY) * z + cy;
                Shape nodeshape = new Rectangle2D.Float(x - 3.0f, y - 3.0f,
                        6.0f, 6.0f);
                if (n == pick) {
                    g2.setColor(selectColor);
                    g2.fill(nodeshape);
                } else {
                    g2.setColor(n.fixed ? fixedColor : nodeColor);
                    g2.draw(nodeshape);
                }
            }
        }

        AffineTransform t = g2.getTransform();

        for (Iterator it = model.cache.nodes.iterator(); it.hasNext();) {
            Node n = (Node) it.next();
            if(!n.isVisible) continue;
            Font font = ((n == pick) || zoom) ? bigFont : smallFont;
            g2.setFont(font);
            FontMetrics fm = g2.getFontMetrics(font);
            float width = fm.stringWidth(n.label) + BORDERs;
            float ascent = fm.getAscent();
            float descent = fm.getDescent();
            float height = ascent + descent + BORDERs;
            float halfHeight = height / 2.0f;

            if (zoom) {
                float z = zoom(n.x, n.y);
                if (z > ZOOM_FACTOR * 0.95f) {
                    float x = n.x + (n.x - zoomX) * z;
                    float y = n.y + (n.y - zoomY) * z;
                    float dx = x - zoomX;
                    float dy = y - zoomY;
                    double theta;
                    if (Math.abs(dx) < Math.abs(dy)) {
                        theta = Math.acos(dx / dy);
                        if (dy < 0.0f)
                            theta += Math.PI;
                    } else {
                        theta = Math.asin(dy / dx);
                        if (dx < 0.0f)
                            theta += Math.PI;
                    }
                    float d = (float) Math.sqrt((dx * dx) + (dy * dy));

                    if (dx < 0.0f) {
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
                    g2.drawString(n.label, d + BORDER, +BORDER);
                    g2.setTransform(t);
                }
            } else {
                float x = n.x;
                if ((x + width + 2 * BORDER) > cx) {
                    x -= width;
                }
                x += cx;

                float y = n.y;
                if ((y - 2 * BORDER) < -cy) {
                    y += height;
                }
                y += cy;

                if ((n == pick) || n.fixed /*
                                            * || (n.highlighted)
                                            */) {
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
                    g2.drawString(n.label, x + BORDER, y + BORDER);

                    if ((n == pick)) {
                        // count properties and max text length
                        int count = 0;
                        float max = width;
                        for (Iterator i = n.getLiterals(); i.hasNext();) {
                            CachedLiteral lit = (CachedLiteral) i.next();
                            fm = g2.getFontMetrics(smallFont);
                            float length = fm.stringWidth(lit.predicate
                                    .toString()
                                    + " -> " + lit.literal)
                                    + BORDERs;
                            if (length > max)
                                max = length;
                            count++;
                        }

                        if (count > 0) {
                            float startY = 0;
                            float rectHeigh = count * 12 + BORDER;
                            if ((y + rectHeigh) > cy)
                                startY = -(rectHeigh + height + 2);

                            // Draw properties rect
                            Shape rect = new Rectangle2D.Float(x, y + 21
                                    + startY - halfHeight, max, rectHeigh);
                            g2.setColor(Color.WHITE);
                            g2.fill(rect);
                            g2.setColor(Color.BLACK);
                            g2.draw(rect);
                            g2.setFont(smallFont);

                            int ddy = 18;
                            for (Iterator itt = n.getLiterals(); itt.hasNext();) {
                                CachedLiteral lit = (CachedLiteral) itt.next();
                                g2.drawString(lit.predicate.toString()
                                        + " -> " + lit.literal, x
                                        + BORDER, y + BORDER + ddy + startY);
                                ddy += 12;
                            }
                        }
                    }
                }
            }
        }

        if (timing) {
            drawingTime = System.currentTimeMillis() - startTime;
            g.setColor(timeColor);
            g.setFont(smallFont);
            g.drawString("calculation: " + simulationTime + " ms", 5, 15);
            g.drawString("drawing: " + drawingTime + " ms", 5, 25);
            g.drawString("nodes: " + model.getNodes().size(), 5, 35);
            //            g.drawString("edges: " + graph.edges, 5, 45);
        }

        if (zoom) {
            if (alpha < MAX_ALPHA) {
                alpha += ALPHA_INC;
                repaint();
            }
        } else {
            if (alpha > MIN_ALPHA) {
                alpha -= ALPHA_INC;
                repaint();
            }
        }
    }
}

