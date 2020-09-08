package org.sidiff.bug.localization.dataset.retrieval;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import org.eclipse.emf.common.util.URI;
import org.eclipse.modisco.infra.discovery.core.exception.DiscoveryException;
import org.sidiff.bug.localization.dataset.Activator;
import org.sidiff.bug.localization.dataset.history.model.Version;
import org.sidiff.bug.localization.dataset.model.DataSet;
import org.sidiff.bug.localization.dataset.retrieval.storage.SystemModelRepository;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModel;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModelFactory;
import org.sidiff.bug.localization.dataset.systemmodel.views.ViewDescriptions;
import org.sidiff.bug.localization.dataset.workspace.model.Project;

public class SystemModelRetrieval {
	
	private SystemModelRetrievalFactory factory;

	private Path codeRepositoryPath;
	
	private SystemModelRepository javaModelRepository;
	
	private SystemModelRepository systemModelRepository;
	
	public SystemModelRetrieval(SystemModelRetrievalFactory factory, Path codeRepositoryPath) {
		this.factory = factory;
		this.codeRepositoryPath = codeRepositoryPath;
	}

	public void retrieve() {
		
		// Storage:
		this.javaModelRepository = new SystemModelRepository(codeRepositoryPath, ViewDescriptions.JAVA_MODEL);
		
		DataSet dataset = javaModelRepository.getDataSet();
		List<Version> versions = dataset.getHistory().getVersions();
		
		this.systemModelRepository = new SystemModelRepository(codeRepositoryPath, ViewDescriptions.UML_CLASS_DIAGRAM, dataset);
		
		// Iterate from old to new versions:
		for (int i = versions.size(); i-- > 0;) {
			Version olderVersion = (versions.size() > i + 1) ? versions.get(i + 1) : null;
			Version version = versions.get(i);
			
			for (Project project : version.getWorkspace().getProjects()) {
				try {
					retrieveSystemModelVersion(version, project);
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
			systemModelRepository.commitVersion(version, olderVersion);
		}
	}

	private void retrieveSystemModelVersion(Version version, Project project) throws DiscoveryException, IOException {
		javaModelRepository.checkout(version);
		
		if (Activator.getLogger().isLoggable(Level.FINER)) {
			Activator.getLogger().log(Level.FINER, "System Model Discovery: " + project.getName());
		}
		
		// Discover the multi-view system model of the project version:
		SystemModel javaSystemModel = SystemModelFactory.eINSTANCE.createSystemModel(javaModelRepository.getSystemModelFile(project));
		SystemModel systemModel = SystemModelFactory.eINSTANCE.createSystemModel();
		factory.discover(systemModel, javaSystemModel);

		// Store system model in data set:
		Path systemModelFile = systemModelRepository.getSystemModelFile(project);
		systemModel.setURI(URI.createFileURI(systemModelFile.toString()));
		systemModel.saveAll(Collections.emptyMap());
		
		// Update data set path:
		project.setSystemModel(systemModelRepository.getDataSetPath().getParent().relativize(systemModelFile));
	}
	
	public void saveDataSet() {
		// Store and commit data set for Java model:
		try {
			systemModelRepository.saveDataSet(true);
		} catch (IOException e) {
			e.printStackTrace();
		}
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
