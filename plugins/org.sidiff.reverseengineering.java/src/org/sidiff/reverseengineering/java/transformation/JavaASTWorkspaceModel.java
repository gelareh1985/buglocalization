package org.sidiff.reverseengineering.java.transformation;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.xmi.XMLResource;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * Model which contains all projects of the workspace.
 * 
 * @author Manuel Ohrndorf
 */
public abstract class JavaASTWorkspaceModel {

	/**
	 * The model representing a Java workspace.
	 */
	private XMLResource workspaceModel;
	
	/**
	 * Name of the workspace model.
	 */
	private String name;

	/**
	 * @param workspaceModel The model representing a Java workspace.
	 */
	@Inject
	public JavaASTWorkspaceModel(
			@Assisted XMLResource workspaceModel, 
			@Assisted String name) {
		this.workspaceModel = workspaceModel;
		this.name = name;
	}
	
	/**
	 * @param projectModel The model element representing the project's root.
	 */
	public abstract void addToWorkspace(EObject projectModel);

	/**
	 * @param position     The desired position in the AST.
	 * @param projectModel The model element representing the project's root.
	 */
	public abstract void addToWorkspace(int position, EObject projectModel);
	
	/**
	 * @param baseURI     The path to the main transformation folder.
	 * @param projectName The name of the project to be removed.
	 * @return Removed corresponding files.
	 */
	public abstract List<Path> removeFromWorkspace(URI baseURI, String projectName) throws IOException;

	/**
	 * @return The model representing a Java workspace.
	 */
	public XMLResource getWorkspaceModel() {
		return workspaceModel;
	}
	
	/**
	 * @return Name of the workspace model.
	 */
	public String getName() {
		return name;
	}
}
