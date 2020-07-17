package org.sidiff.bug.localization.dataset.retrieval;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.logging.Level;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.modisco.infra.discovery.core.exception.DiscoveryException;
import org.sidiff.bug.localization.dataset.Activator;
import org.sidiff.bug.localization.dataset.history.model.Version;
import org.sidiff.bug.localization.dataset.model.DataSet;
import org.sidiff.bug.localization.dataset.retrieval.storage.SystemModelRepository;
import org.sidiff.bug.localization.dataset.systemmodel.discovery.JavaProject2MultiViewModelDiscoverer;
import org.sidiff.bug.localization.dataset.systemmodel.views.MultiViewSystemModel;
import org.sidiff.bug.localization.dataset.systemmodel.views.ViewDescriptions;
import org.sidiff.bug.localization.dataset.workspace.model.Project;

public class SystemModelRetrieval {

	private Path codeRepositoryPath;
	
	private SystemModelRepository javaModelRepository;
	
	private SystemModelRepository systemModelRepository;
	
	public SystemModelRetrieval(Path codeRepositoryPath) {
		this.codeRepositoryPath = codeRepositoryPath;
	}

	public void retrieve() {
		
		// Storage:
		this.javaModelRepository = new SystemModelRepository(codeRepositoryPath, ViewDescriptions.JAVA_MODEL);
		DataSet dataset = javaModelRepository.getDataSet();
		
		this.systemModelRepository = new SystemModelRepository(codeRepositoryPath, ViewDescriptions.UML_CLASS_DIAGRAM, dataset);
		Version previousVersion = null;
		
		for (Version version : dataset.getHistory().getVersions()) {
			for (Project project : version.getWorkspace().getProjects()) {
				try {
					retrieveSystemModelVersion(project);
				} catch (DiscoveryException e) {
					if (Activator.getLogger().isLoggable(Level.SEVERE)) {
						Activator.getLogger().log(Level.SEVERE, "Could not discover system model for '"
								+ project.getName() + "' version " + version.getIdentification());
					}
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			// Store Java AST model workspace as revision:
			previousVersion = systemModelRepository.commitVersion(version, previousVersion);
		}
		
		// Store data set for UML model:
		try {
			systemModelRepository.saveDataSet();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void retrieveSystemModelVersion(Project project) throws DiscoveryException, IOException {
		
		if (Activator.getLogger().isLoggable(Level.FINER)) {
			Activator.getLogger().log(Level.FINER, "System Model Discovery: " + project.getName());
		}
		
		// Discover the multi-view system model of the project version:
		MultiViewSystemModel multiViewSystemModel = new MultiViewSystemModel(javaModelRepository.getSystemModelFile(project));
		JavaProject2MultiViewModelDiscoverer multiViewModelDiscoverer = new JavaProject2MultiViewModelDiscoverer();
		
		Resource javaResource = multiViewSystemModel.getViewByKind(ViewDescriptions.JAVA_MODEL);
		multiViewModelDiscoverer.discoverUMLClassDiagram(multiViewSystemModel, javaResource, new NullProgressMonitor());
//		multiViewModelDiscoverer.discoverUMLOperationControlFlow(multiViewSystemModel, javaResource, new NullProgressMonitor()); // FIXME: discover UML Operation Control Flow
		
		// Remove java model:
		multiViewSystemModel.removeViewKind(ViewDescriptions.JAVA_MODEL);
		
		// Store system model in data set:
		Path systemModelFile = systemModelRepository.getSystemModelFile(project);
		multiViewSystemModel.setURI(URI.createFileURI(systemModelFile.toString()));
		multiViewSystemModel.saveAll(Collections.emptyMap());
		
		// Update data set path:
		project.setSystemModel(systemModelRepository.getDataSetPath().getParent().relativize(systemModelFile));
	}
	
	public Path getCodeRepositoryPath() {
		return codeRepositoryPath;
	}
	
	public Path getJavaModelRepositoryPath() {
		return javaModelRepository.getRepositoryPath();
	}
	
	public Path getSystemModelRepositoryPath() {
		return systemModelRepository.getRepositoryPath();
	}
}
