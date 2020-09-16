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
import org.sidiff.bug.localization.dataset.history.util.HistoryUtil;
import org.sidiff.bug.localization.dataset.model.DataSet;
import org.sidiff.bug.localization.dataset.retrieval.SystemModelRetrievalProvider.SystemModelDiscoverer;
import org.sidiff.bug.localization.dataset.retrieval.storage.SystemModelRepository;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModel;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModelFactory;
import org.sidiff.bug.localization.dataset.systemmodel.views.ViewDescriptions;
import org.sidiff.bug.localization.dataset.workspace.model.Project;

public class SystemModelRetrieval {
	
	private SystemModelRetrievalProvider provider;

	private Path codeRepositoryPath;
	
	private SystemModelRepository javaModelRepository;
	
	private SystemModelRepository systemModelRepository;
	
	public SystemModelRetrieval(SystemModelRetrievalProvider provider, Path codeRepositoryPath) {
		this.provider = provider;
		this.codeRepositoryPath = codeRepositoryPath;
	}

	public void retrieve() {
		
		// Storage:
		this.javaModelRepository = new SystemModelRepository(codeRepositoryPath, ViewDescriptions.JAVA_MODEL);
		
		DataSet dataset = javaModelRepository.getDataSet();
		List<Version> versions = dataset.getHistory().getVersions();
		
		this.systemModelRepository = new SystemModelRepository(codeRepositoryPath, ViewDescriptions.UML_CLASS_DIAGRAM, dataset);
		
		try {
			// Iterate from old to new versions:
			for (int i = versions.size(); i-- > 0;) {
				Version olderVersion = (versions.size() > i + 1) ? versions.get(i + 1) : null;
				Version version = versions.get(i);
				
				// Clean up older version:
				systemModelRepository.removeMissingProjects(olderVersion, version);
				
				// Load newer version:
				javaModelRepository.checkout(version);
				
				// Discover projects:
				for (Project project : version.getWorkspace().getProjects()) {
					try {
						retrieveSystemModelVersion(olderVersion, version, project);
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
				
				// Store system model workspace as revision:
				systemModelRepository.commitVersion(version, olderVersion);
				
				// Intermediate save:
				if ((provider.getIntermediateSave() > 0) && ((i + 1) % provider.getIntermediateSave()) == 0) {
					saveDataSet();
				}
				
				if (Activator.getLogger().isLoggable(Level.FINE)) {
					Activator.getLogger().log(Level.FINE, "Discovered system model version " + (versions.size() - i) + " of " + versions.size() + " versions");
				}
			}
		} finally {
			javaModelRepository.resetRepository();
		}
	}

	private void retrieveSystemModelVersion(Version olderVersion, Version version, Project project) throws DiscoveryException, IOException {
		
		if (Activator.getLogger().isLoggable(Level.FINER)) {
			Activator.getLogger().log(Level.FINER, "System Model Discovery: " + project.getName());
		}
		
		Path systemModelFile = systemModelRepository.getSystemModelFile(project);
		
		// OPTIMIZATION: Recalculate changed projects only (and initial versions).
		if (HistoryUtil.hasChanges(project, olderVersion, version, provider.getFileChangeFilter())) {
			
			// Discover the multi-view system model of the project version:
			SystemModel javaSystemModel = SystemModelFactory.eINSTANCE.createSystemModel(javaModelRepository.getSystemModelFile(project));
			SystemModel systemModel = SystemModelFactory.eINSTANCE.createSystemModel();
			systemModel.setName(project.getName());
			
			// START:
			discover(systemModel, javaSystemModel);
			
			// Store system model in data set:
			systemModel.setURI(URI.createFileURI(systemModelFile.toString()));
			systemModel.saveAll(Collections.emptyMap());
		}
		
		// Update data set path:
		project.setSystemModel(systemModelRepository.getDataSetPath().getParent().relativize(systemModelFile));
	}
	
	private void discover(SystemModel systemModel, SystemModel javaSystemModel) throws DiscoveryException {
		for (SystemModelDiscoverer systemModelDiscovery : provider.getSystemModelDiscoverer()) {
			try {
				systemModelDiscovery.discover(systemModel, javaSystemModel);
			} catch (Throwable e) {
				e.printStackTrace();
				
				if (Activator.getLogger().isLoggable(Level.SEVERE)) {
					Activator.getLogger().log(Level.SEVERE, "Could not discover system model: " + javaSystemModel.eResource().getURI());
				}
			}
		}
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
