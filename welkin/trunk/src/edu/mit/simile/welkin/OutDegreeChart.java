package edu.mit.simile.welkin;

import java.util.Iterator;

import edu.mit.simile.welkin.ModelCache.WResource;
import edu.mit.simile.welkin.ModelCache.WStatement;

public class OutDegreeChart extends ModelChart {

    public OutDegreeChart(ModelManager model) {
        super(model, "OutDegree");
    }
    
    public int process(WResource node) {
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
    
    public float scale(float value) {
        return (float) Math.log((double) value);
    }
}    

