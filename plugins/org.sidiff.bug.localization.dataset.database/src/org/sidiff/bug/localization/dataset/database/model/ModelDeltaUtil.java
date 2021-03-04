package org.sidiff.bug.localization.dataset.database.model;

import java.util.Arrays;

import org.eclipse.emf.common.util.URI;

public class ModelDeltaUtil {

	public static URI getRelativeURI(URI baseURI, URI modelURI) {
		if (baseURI != null) {
			URI deresolvedURI = modelURI.deresolve(baseURI);
			String[] relativePath = Arrays.copyOfRange(deresolvedURI.segments(), 1, deresolvedURI.segmentCount());
			return URI.createURI("").appendSegments(relativePath).appendFragment(modelURI.fragment());
		}
		return modelURI;
	}
}
