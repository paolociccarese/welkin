/*
 * Created on 17-nov-2004
 */
package edu.mit.simile.welkin;

import org.apache.xerces.util.XMLChar;

import com.hp.hpl.jena.rdf.model.Resource;

/**
 * @author Paolo Ciccarese
 */
public class Util {
    public static String getNameSpace(Resource res) {
        return getNameSpace(res.getURI());
    }
    
    public static String getNameSpace (String uri) {
        char ch;
        int lg = uri.length();
        if (lg == 0) return "";
        int j=0;
        int i;
        for (i = lg - 1; i >= 1; i--) {
            ch = uri.charAt(i);
            boolean bol = XMLChar.isNCName(ch);
            if (!XMLChar.isNCName(ch))
                break;
        }
        
        return uri.substring(0 , i );       
    }
}
