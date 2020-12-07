package org.sidiff.reverseengineering.java.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.eclipse.core.resources.IResource;
import org.eclipse.emf.ecore.EObject;

/**
 * Indexed storage for code lines to model element traces.
 * 
 * @author Manuel Ohrndorf
 */
public class CodeLinesToModelTrace {

	/**
	 * Workspace resource -> Line -> Model Element<br>
	 */
	private Map<IResource, TreeMap<Integer, EObject>> resourceToModel;
	
	public CodeLinesToModelTrace() {
		this.resourceToModel = new HashMap<>();
	}
	
	/**
	 * @param resource           A Java workspace resource.
	 * @param lineToModelElement The trace of lines to model elements.
	 */
	public void addModel(IResource resource, TreeMap<Integer, EObject> lineToModelElement) {
		resourceToModel.put(resource, lineToModelElement); // also overwrites old traces
	}
	
	/**
	 * @param resource           A Java workspace resource.
	 */
	public void removeModel(IResource resource) {
		resourceToModel.remove(resource);
	}
	
	/**
	 * @param resource A Java workspace resource.
	 * @param line     The source code line
	 * @return The closest match of the given line to the corresponding model element.
	 */
	public EObject getModelElement(IResource resource, Integer line) {
		TreeMap<Integer, EObject> lineToModelElement = resourceToModel.get(resource);
		
		if (lineToModelElement != null) {
			return getModelElementsByLine(line, lineToModelElement);
		}
		
		return null;
	}
	
	/**
	 * @param lines Lines of code.
	 * @return The corresponding main model elements.
	 */
	public EObject getModelElementsByLine(Integer line, TreeMap<Integer, EObject> lineToModelElement) {
		Entry<Integer, EObject> lineMatch = lineToModelElement.ceilingEntry(line);

		if (lineMatch != null) {
			return lineMatch.getValue();
		} else {
			// assign space after last operation/declaration to the class
			if (!lineToModelElement.isEmpty()) {
				return lineToModelElement.firstEntry().getValue();
			}
		}
		
		return null;
	}
	
}
