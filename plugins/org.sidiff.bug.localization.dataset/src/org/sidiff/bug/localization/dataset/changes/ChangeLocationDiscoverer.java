package org.sidiff.bug.localization.dataset.changes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.modisco.java.AbstractTypeDeclaration;
import org.eclipse.modisco.java.discoverer.internal.io.java.binding.PendingElement;
import org.eclipse.modisco.kdm.source.extension.discovery.SourceVisitListener;
import org.sidiff.bug.localization.dataset.history.model.changes.FileChange;
import org.sidiff.bug.localization.dataset.history.model.changes.FileChange.FileChangeType;
import org.sidiff.bug.localization.dataset.history.model.changes.LineChange;
import org.sidiff.bug.localization.dataset.history.model.changes.LineChange.LineChangeType;
import org.sidiff.bug.localization.dataset.systemmodel.Change;
import org.sidiff.bug.localization.dataset.systemmodel.ChangeType;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModelFactory;

public class ChangeLocationDiscoverer implements SourceVisitListener {
	
	private static Map<LineChangeType, ChangeType> lineChangeMap = new HashMap<>();
	
	static {
		lineChangeMap.put(LineChangeType.DELETE, ChangeType.DELETE);
		lineChangeMap.put(LineChangeType.EMPTY, ChangeType.DELETE);
		lineChangeMap.put(LineChangeType.INSERT, ChangeType.ADD);
		lineChangeMap.put(LineChangeType.REPLACE, ChangeType.MODIFY);
	}
	
	private static Map<FileChangeType, ChangeType> fileChangeMap = new HashMap<>();
	
	static {
		fileChangeMap.put(FileChangeType.ADD, ChangeType.ADD);
		fileChangeMap.put(FileChangeType.COPY, ChangeType.ADD);
		fileChangeMap.put(FileChangeType.DELETE, ChangeType.DELETE);
		fileChangeMap.put(FileChangeType.MODIFY, ChangeType.MODIFY);
		fileChangeMap.put(FileChangeType.RENAME, ChangeType.MODIFY);
	}
	
	private List<Change> changes;
	
	private ChangeResolver changeResolver;

	public ChangeLocationDiscoverer(ChangeResolver changeResolver) {
		this.changes = new ArrayList<>();
		this.changeResolver = changeResolver;
	}

	@Override
	public void sourceRegionVisited(String filePath, int startOffset, int endOffset, int startLine, int endLine, EObject targetNode) {
		
		// FIXME WORKAROUND: Unwrap MoDisco PendingElement placeholder...
		if (targetNode instanceof PendingElement) {
			targetNode = ((PendingElement) targetNode).getClientNode();
		}
		
		if (targetNode instanceof AbstractTypeDeclaration) {
			FileChange fileChange = changeResolver.getFileChange(filePath);
			
			if (fileChange != null) {
				Change change = SystemModelFactory.eINSTANCE.createChange();
				change.setType(fileChangeMap.get(fileChange.getType()));
				change.setQuantification(calculateQuantification(fileChange));
				change.setLocation(targetNode);
				changes.add(change);
			}
		}
		
		// TODO: More fine grained change locations...
//		FileChange fileChange = changeResolver.getFileChange(filePath);
//		
//		if (fileChange != null) {
//			LineChange lineChange = changeResolver.getLineChange(fileChange, startLine);
//			
//			if (lineChange != null) {
//				Change change = SystemModelFactory.eINSTANCE.createChange();
//				change.setType(lineChangeMap.get(lineChange.getType()));
//				change.setQuantification(Math.min(
//						lineChange.getEndA() - lineChange.getBeginA(),
//						startLine - endLine) + 1);
//				change.setLocation(targetNode);
//				changes.add(change);
//			}
//		}
	}

	protected int calculateQuantification(FileChange fileChange) {
		int changeCount = 0;
		
		for (LineChange lineChange : fileChange.getLines()) {
			changeCount += (lineChange.getEndA() - lineChange.getBeginA()) + 1;
		}
		
		return (changeCount > 0) ? changeCount : 1;
	}

	public List<Change> getChanges() {
		return changes;
	}
	
	public void setChanges(List<Change> changes) {
		this.changes = changes;
	}
}
