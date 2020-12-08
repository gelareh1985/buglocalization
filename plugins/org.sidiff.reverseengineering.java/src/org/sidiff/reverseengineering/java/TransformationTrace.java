package org.sidiff.reverseengineering.java;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.eclipse.core.resources.IResource;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jdt.core.dom.ASTNode;

public class TransformationTrace {
	
	private IResource javaResource;
	
	private List<EObject> rootModelElements;
	
	private Map<ASTNode, EObject> javaToModelTrace;
	
	private TreeMap<Integer, EObject> lineToModel;
	
	private Resource workspaceModel;
	
	private Resource libraryModel;
	
	private Resource projectModel;
	
	private Resource typeModel;

	/**
	 * @return The transformed Java source file.
	 */
	public IResource getJavaResource() {
		return javaResource;
	}
	
	public void setJavaResource(IResource javaResource) {
		this.javaResource = javaResource;
	}
	
	/**
	 * @return The corresponding workspace model containing all projects.
	 */
	public Resource getWorkspaceModel() {
		return workspaceModel;
	}
	
	public void setWorkspaceModel(Resource workspaceModel) {
		this.workspaceModel = workspaceModel;
	}
	
	/**
	 * @return The corresponding library model containing model elements not
	 *         contained in the (defined) workspace (scope).
	 */
	public Resource getLibraryModel() {
		return libraryModel;
	}
	
	public void setLibraryModel(Resource libraryModel) {
		this.libraryModel = libraryModel;
	}
	
	/**
	 * @return The model corresponding to the Java project.
	 */
	public Resource getProjectModel() {
		return projectModel;
	}
	
	public void setProjectModel(Resource projectModel) {
		this.projectModel = projectModel;
	}
	
	/**
	 * @return The model corresponding to the Java resource.
	 */
	public Resource getTypeModel() {
		return typeModel;
	}
	
	public void setTypeModel(Resource modelResource) {
		this.typeModel = modelResource;
	}

	/**
	 * @return All root model elements created by this transformation.
	 */
	public List<EObject> getRootModelElements() {
		return rootModelElements;
	}
	
	public void setRootModelElements(List<EObject> rootModelElements) {
		this.rootModelElements = rootModelElements;
	}

	/**
	 * @return Trace: Java AST node -> Model element
	 */
	public Map<ASTNode, EObject> getJavaToModelTrace() {
		return javaToModelTrace;
	}
	
	public void setJavaToModelTrace(Map<ASTNode, EObject> javaToModelTrace) {
		this.javaToModelTrace = javaToModelTrace;
	}
	
	/**
	 * @return Code Line -> Main Model Element
	 */
	public TreeMap<Integer, EObject> getLineToModel() {
		return lineToModel;
	}
	
	public void setLineToModel(TreeMap<Integer, EObject> lineToModel) {
		this.lineToModel = lineToModel;
	}
	
	/**
	 * @param lines Lines of code.
	 * @return The corresponding main model elements.
	 */
	public EObject getModelElementsByLine(Integer line) {
		Entry<Integer, EObject> lineMatch = lineToModel.ceilingEntry(line);

		if (lineMatch != null) {
			return lineMatch.getValue();
		} else {
			// assign space after last operation/declaration to the class
			return lineToModel.firstEntry().getValue();
		}
	}
}
