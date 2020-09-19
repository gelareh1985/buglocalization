package org.sidiff.bug.localization.dataset.retrieval;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.modisco.infra.discovery.core.exception.DiscoveryException;
import org.sidiff.bug.localization.dataset.changes.util.FileChangeFilter;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModel;
import org.sidiff.bug.localization.dataset.systemmodel.View;
import org.sidiff.bug.localization.dataset.systemmodel.discovery.JavaModel2UMLClassSystemModel;
import org.sidiff.bug.localization.dataset.systemmodel.discovery.JavaModel2UMLFlowSystemModel;

public class SystemModelRetrievalProvider {

	/**
	 * The discovery transformations from the Java model to the system model.
	 */
	private SystemModelDiscoverer[] systemModelDiscoverer;
	
	/**
	 * Files to be tested for changes, whether a model needs to be recalculated.
	 */
	private FileChangeFilter fileChangeFilter;
	
	/**
	 * Cyclic save of the data set.
	 */
	private int intermediateSave = -1;
	
	@FunctionalInterface
	public interface SystemModelDiscoverer {
		void discover(SystemModel systemModel, SystemModel javaSystemModel) throws DiscoveryException;
	}
	
	public SystemModelRetrievalProvider(
			SystemModelDiscoverer[] systemModelDiscoverer,
			FileChangeFilter fileChangeFilter) {
		this.systemModelDiscoverer = systemModelDiscoverer;
		this.fileChangeFilter = fileChangeFilter;
	}
	
	public SystemModelRetrievalProvider() {
		SystemModelDiscoverer umlClasses = (SystemModel systemModel, SystemModel javaSystemModel) -> {
			JavaModel2UMLClassSystemModel java2UmlClass = new JavaModel2UMLClassSystemModel();
			SystemModel umlSystemModel = java2UmlClass.discover(javaSystemModel, new NullProgressMonitor());
			moveViews(umlSystemModel, systemModel);
		};
		
		// FIXME: discover UML Operation Control Flow
		@SuppressWarnings("unused")
		SystemModelDiscoverer umlOperationControlFlow = (SystemModel systemModel, SystemModel javaSystemModel) -> {
			JavaModel2UMLFlowSystemModel java2UmlClass = new JavaModel2UMLFlowSystemModel();
			SystemModel umlSystemModel = java2UmlClass.discover(javaSystemModel, new NullProgressMonitor());
			moveViews(umlSystemModel, systemModel);
		};
		
		this.systemModelDiscoverer = new SystemModelDiscoverer[] {umlClasses};
		this.fileChangeFilter = (fileChange) -> !fileChange.getLocation().toString().endsWith(".java");
		this.intermediateSave = 200;
	}

	private void moveViews(SystemModel source, SystemModel target) {
		for (View sourceView : source.getViews().toArray(new View[0])) {
			target.eResource().getResourceSet().getResources().add(sourceView.eResource());
			target.getViews().add(sourceView);
		}
	}
	
	public void setSystemModelDiscoverer(SystemModelDiscoverer[] systemModelDiscoverer) {
		this.systemModelDiscoverer = systemModelDiscoverer;
	}
	
	public SystemModelDiscoverer[] getSystemModelDiscoverer() {
		return systemModelDiscoverer;
	}
	
	public FileChangeFilter getFileChangeFilter() {
		return fileChangeFilter;
	}

	public void setFileChangeFilter(FileChangeFilter fileChangeFilter) {
		this.fileChangeFilter = fileChangeFilter;
	}

	public int getIntermediateSave() {
		return intermediateSave;
	}

	public void setIntermediateSave(int intermediateSave) {
		this.intermediateSave = intermediateSave;
	}
}
