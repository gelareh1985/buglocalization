package org.sidiff.bug.localization.dataset.changes;

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.modisco.java.discoverer.internal.io.java.binding.PendingElement;
import org.eclipse.modisco.kdm.source.extension.discovery.SourceVisitListener;
import org.sidiff.bug.localization.dataset.systemmodel.Change;

@SuppressWarnings("restriction")
public class ChangeLocationDiscoverer implements SourceVisitListener {
	
	private ChangeLocationMatcher changeLocationMatcher;

	public ChangeLocationDiscoverer(ChangeLocationMatcher changeLocationMatcher) {
		this.changeLocationMatcher = changeLocationMatcher;
	}

	@Override
	public void sourceRegionVisited(String filePath, int startOffset, int endOffset, int startLine, int endLine, EObject targetNode) {
		
		// FIXME WORKAROUND: Unwrap MoDisco PendingElement placeholder...
		if (targetNode instanceof PendingElement) {
			targetNode = ((PendingElement) targetNode).getClientNode();
		}
		
		changeLocationMatcher.match(filePath, startOffset, endOffset, startLine, endLine, targetNode);
	}

	public List<Change> getChanges() {
		return changeLocationMatcher.getChanges();
	}
}
