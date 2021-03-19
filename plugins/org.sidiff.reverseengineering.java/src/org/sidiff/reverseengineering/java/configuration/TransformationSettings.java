package org.sidiff.reverseengineering.java.configuration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.sidiff.reverseengineering.java.util.EMFHelper;

public class TransformationSettings {

	private String name = "System";

	private URI baseURI;

	private String modelFileExtension = "xmi";

	private String defaultPackageName = "default";

	private boolean includeMethodBodies = true;

	private XMLResource workspaceModel;

	private XMLResource libraryModel;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public URI getBaseURI() {
		return baseURI;
	}

	public void setBaseURI(URI baseURI) {
		this.baseURI = baseURI;
	}

	public String getModelFileExtension() {
		return modelFileExtension;
	}

	public void setModelFileExtension(String modelFileExtension) {
		this.modelFileExtension = modelFileExtension;
	}

	/**
	 * @param astPath The path of the Java AST resource.
	 * @return The path of the corresponding model.
	 */
	public IPath getModelName(IPath javaFile) {
		return javaFile.removeFileExtension().addFileExtension(getModelFileExtension());
	}

	/**
	 * @param astPath The path of the Java AST resource.
	 * @return The path of the corresponding model.
	 */
	public String getModelElementName(IResource javaFile) {
		return javaFile.getFullPath().removeFileExtension().lastSegment().toString();
	}

	/**
	 * Maps a Java resource to a model resources. This works also for Java resources
	 * that do not exists anymore. The model resource must exist, otherwise returns
	 * <code>null</code>.
	 * 
	 * @param javaFile     The Java resource.
	 * @param modelBaseURI The model "workspace" path of the system model.
	 * @return The corresponding system model relative model path segments or
	 *         <code>null</code>.
	 */
	public String[] findModelPath(IResource javaFile, URI modelBaseURI) {
		Path modelPath = Path.of(modelBaseURI.devicePath(), javaFile.getProject().getName());

		if (Files.exists(modelPath)) {
			String[] javaPathSegments = javaFile.getProjectRelativePath().segments();
			javaPathSegments[javaPathSegments.length - 1] = getModelName(javaFile.getProjectRelativePath())
					.lastSegment();
			LinkedList<String> modelPathSegments = new LinkedList<>();

			for (String javaFolder : javaPathSegments) {
				Path modelFolder = modelPath.resolve(javaFolder);

				if (Files.exists(modelFolder)) {
					modelPathSegments.add(javaFolder);
					modelPath = modelFolder;
				}
			}

			// Model file found?
			if (modelPathSegments.getLast() == javaPathSegments[javaPathSegments.length - 1]) {
				modelPathSegments.removeLast();
				String[] packages = modelPathSegments.toArray(new String[0]);
				return getModelPath(javaFile.getProject().getName(), packages, javaFile); // for consistency
			} else {
				return null;
			}
		}

		return null;
	}

	/**
	 * @param projectName The containing project.
	 * @param javaElement The Java resource.
	 * @return The corresponding system model relative model path segments.
	 */
	public String[] getModelPath(String projectName, IJavaElement javaElement) {
		String[] packages = getPackageName(javaElement).split("\\.");
		return getModelPath(projectName, packages, javaElement.getResource());
	}

	/**
	 * @param projectName  The containing project.
	 * @param packages     The Java resource package segments.
	 * @param javaResource The Java resource.
	 * @return The corresponding system model relative model path segments.
	 */
	public String[] getModelPath(String projectName, String[] packages, IResource javaResource) {
		String[] modelPath = new String[packages.length + 2];
		modelPath[0] = projectName;

		for (int i = 0; i < packages.length; i++) {
			modelPath[i + 1] = packages[i];
		}

		modelPath[modelPath.length - 1] = getModelName(javaResource.getFullPath()).lastSegment();
		return modelPath;
	}

	protected String getPackageName(IJavaElement javaElement) {
		while ((javaElement != null) && !(javaElement instanceof IPackageFragment)) {
			javaElement = javaElement.getParent();
		}

		if (javaElement != null) {
			return ((IPackageFragment) javaElement).getElementName();
		} else {
			return defaultPackageName;
		}
	}

	public boolean isIncludeMethodBodies() {
		return includeMethodBodies;
	}

	public void setIncludeMethodBodies(boolean includeMethodBodies) {
		this.includeMethodBodies = includeMethodBodies;
	}

	public XMLResource getWorkspaceModel() {

		if (workspaceModel == null) {
			ResourceSet resourcesSet = (libraryModel != null) ? libraryModel.getResourceSet() : new ResourceSetImpl();
			URI modelURI = baseURI.appendSegment(getName()).appendFileExtension(modelFileExtension);
			this.workspaceModel = EMFHelper.initializeResource(resourcesSet, modelURI);
		}

		return workspaceModel;
	}

	public void setWorkspaceModel(XMLResource workspaceModel) {
		this.workspaceModel = workspaceModel;
	}

	public XMLResource getLibraryModel() {

		if (libraryModel == null) {
			ResourceSet resourcesSet = (workspaceModel != null) ? workspaceModel.getResourceSet()
					: new ResourceSetImpl();
			URI modelURI = baseURI.appendSegment(getName() + ".library").appendFileExtension(modelFileExtension);
			this.libraryModel = EMFHelper.initializeResource(resourcesSet, modelURI);
		}

		return libraryModel;
	}

	public void setLibraryModel(XMLResource libraryModel) {
		this.libraryModel = libraryModel;
	}

	public String getDefaultPackageName() {
		return defaultPackageName;
	}

	public void setDefaultPackageName(String defaultPackageName) {
		this.defaultPackageName = defaultPackageName;
	}
}
