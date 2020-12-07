package org.sidiff.reverseengineering.java.transformation.uml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.PackageableElement;
import org.eclipse.uml2.uml.UMLFactory;
import org.sidiff.reverseengineering.java.transformation.JavaASTWorkspaceModel;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * UML model which contains all projects of the workspace.
 * 
 * @author Manuel Ohrndorf
 */
public class JavaASTWorkspaceModelUML extends JavaASTWorkspaceModel {

	private UMLFactory umlFactory = UMLFactory.eINSTANCE;
	
	/**
	 * The root element of the workspace model.
	 */
	private Model workspaceModelRoot;
	
	/**
	 * @see {@link JavaASTWorkspaceModel#JavaASTWorkspaceModel(XMLResource, String)}
	 */
	@Inject
	public JavaASTWorkspaceModelUML(
			@Assisted XMLResource workspaceModel, 
			@Assisted String name) {
		super(workspaceModel, name);
		
		if (workspaceModel.getContents().isEmpty()) {
			this.workspaceModelRoot = umlFactory.createModel();
			this.workspaceModelRoot.setName(name);
			getWorkspaceModel().getContents().add(workspaceModelRoot);
			getWorkspaceModel().setID(workspaceModelRoot, "workspace::" + name);
		} else {
			this.workspaceModelRoot = (Model) workspaceModel.getContents().get(0);
		}
	}
	
	@Override
	public void addToWorkspace(EObject projectModel) {
		if (projectModel instanceof PackageableElement) {
			workspaceModelRoot.getPackagedElements().add((PackageableElement) projectModel);
		}
	}

	@Override
	public void addToWorkspace(int position, EObject projectModel) {
		if (projectModel instanceof PackageableElement) {
			if (!workspaceModelRoot.getPackagedElements().contains(projectModel)) {
				workspaceModelRoot.getPackagedElements().add(position, (PackageableElement) projectModel);
			}
		}
	}

	@Override
	public List<Path> removeFromWorkspace(URI baseURI, String projectName) throws IOException {
		PackageableElement toBeRemovedProject = workspaceModelRoot.getPackagedElement(projectName);
		
		if (toBeRemovedProject != null) {
			Resource projectModelResource = toBeRemovedProject.eResource();
			workspaceModelRoot.getPackagedElements().remove(toBeRemovedProject);
			
			// Remove project from file system:
			Path projectModelPath = Paths.get(projectModelResource.getURI().resolve(baseURI).toFileString());
			
			if (projectModelPath.getParent().getFileName().toString().equals(projectName)) {
				List<Path> removedFiles = new ArrayList<>();
				deleteDirectory(projectModelPath.getParent(), removedFiles);
				return removedFiles;
			}
		}
		
		return Collections.emptyList();
	}
		
	private void deleteDirectory(Path path, List<Path> removed) {
		try {
			if (path != null) {
				if (Files.isDirectory(path)) {
					try (Stream<Path> paths = Files.list(path)) {
						for (Iterator<Path> iterator = paths.iterator(); iterator.hasNext();) {
							Path pathToBeRemoved = iterator.next();
							deleteDirectory(pathToBeRemoved, removed);
						}
					}
				}
				try {
					Files.delete(path);
					removed.add(Paths.get(path.toString()));
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
