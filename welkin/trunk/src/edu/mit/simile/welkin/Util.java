package edu.mit.simile.welkin;


public class Util {
    
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
    
    public static String getBase(String uri) {
    	String[] us= new String[3];
    	
    	try {
	    	int canceIndex = uri.lastIndexOf("#");
	    	if(canceIndex<0) {
	    		canceIndex = uri.lastIndexOf("/");
	    	}
	    	
	    	return uri.substring(0, canceIndex);
    	} catch(Exception exc) {
    		int canceIndex = uri.lastIndexOf(":");
    		
    		if(canceIndex>=0) return uri.substring(0, canceIndex);
    		return "";
    	}
    }
    
    public static String[] getBasisParts(String base) {
    	String[] us= new String[2];
    	
    	int slashIndex = base.indexOf('/',7);
    	if(slashIndex>=7) {
    		us[1] = base.substring(slashIndex);
    		us[0] = base.substring(0, slashIndex);
    	}
    	
    	return us;
    }
}
