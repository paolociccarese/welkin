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
    
    public static String[] getParts(String uri) {
    	String[] us= new String[3];
    	
    	int canceIndex = uri.lastIndexOf("#");
    	if(canceIndex>=0) us[2] = uri.substring(canceIndex);
    	else 
    	{
    		canceIndex = uri.lastIndexOf("/");
    		if(canceIndex>3) {
    			us[2] = uri.substring(canceIndex);
    		} else {
	    		String[] uris = new String[1];
				uris[0] = uri;
				return uris;
    		}
    	}
    	
    	int slashIndex = uri.indexOf('/',7);
    	if(slashIndex>=7) {
    		us[1] = uri.substring(slashIndex, canceIndex);
    		us[0] = uri.substring(0, slashIndex);
    	}
    	
    	return us;
    }
}
