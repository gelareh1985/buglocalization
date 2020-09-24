package org.sidiff.bug.localization.dataset.retrieval;

import org.sidiff.bug.localization.dataset.changes.util.FileChangeFilter;
import org.sidiff.bug.localization.dataset.retrieval.discoverer.UMLClassSystemModelDiscoverer;
import org.sidiff.bug.localization.dataset.retrieval.util.SystemModelDiscoverer;

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
	
	public SystemModelRetrievalProvider(
			SystemModelDiscoverer[] systemModelDiscoverer,
			FileChangeFilter fileChangeFilter) {
		this.systemModelDiscoverer = systemModelDiscoverer;
		this.fileChangeFilter = fileChangeFilter;
	}
	
	public SystemModelRetrievalProvider() {
		SystemModelDiscoverer umlClasses = new UMLClassSystemModelDiscoverer();
		
		// FIXME: discover UML Operation Control Flow
//		SystemModelDiscoverer umlOperationControlFlow = new UMLFlowSystemModelDiscoverer();
		
		this.systemModelDiscoverer = new SystemModelDiscoverer[] {umlClasses};
		this.fileChangeFilter = (fileChange) -> !fileChange.getLocation().toString().endsWith(".java");
		this.intermediateSave = 200;
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
