package edu.mit.simile.welkin;

import org.apache.xerces.util.XMLChar;

public class Util {
    
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
        
        return uri.substring(0 , i+1);       
    }
}
