package edu.mit.simile.welkin.resource;

import org.openrdf.model.URI;

public class PredicateUri extends UriWrapper {

	public float weight;
	
	public PredicateUri(URI uri, float weight) {
		super(uri);
		this.weight = weight;
	}
}
