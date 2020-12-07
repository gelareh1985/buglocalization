package org.sidiff.bug.localization.dataset.changes;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.modisco.java.discoverer.internal.io.java.binding.PendingElement;
import org.eclipse.modisco.java.emf.JavaPackage;
import org.eclipse.modisco.kdm.source.extension.discovery.SourceVisitListener;
import org.sidiff.bug.localization.dataset.systemmodel.Change;
import org.sidiff.bug.localization.dataset.systemmodel.ChangeType;

@SuppressWarnings("restriction")
public class ChangeLocationDiscoverer implements SourceVisitListener {
	
	private static Set<EClass> CHANGE_LOCATION_TYPES = new HashSet<>();
	
	static {
		CHANGE_LOCATION_TYPES.add(JavaPackage.eINSTANCE.getPackage());
		CHANGE_LOCATION_TYPES.add(JavaPackage.eINSTANCE.getClassDeclaration());
		CHANGE_LOCATION_TYPES.add(JavaPackage.eINSTANCE.getInterfaceDeclaration());
		CHANGE_LOCATION_TYPES.add(JavaPackage.eINSTANCE.getEnumDeclaration());
		CHANGE_LOCATION_TYPES.add(JavaPackage.eINSTANCE.getFieldDeclaration());
		CHANGE_LOCATION_TYPES.add(JavaPackage.eINSTANCE.getMethodDeclaration());
		CHANGE_LOCATION_TYPES.add(JavaPackage.eINSTANCE.getConstructorDeclaration());
	}
	
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

	public Collection<Change> getChanges() {
		return assignChanges(changeLocationMatcher.getChanges(), CHANGE_LOCATION_TYPES);
	}
	
	protected Collection<Change> assignChanges(List<Change> changes, Set<EClass> changeLocationTypes) {
		
		// Combine changes of child AST elements to parent container:
		for (Change change : changes) {
			EObject location = findLocation(changeLocationTypes, change); 
			change.setLocation(location);
		}
		
		// Combine changes with the same location:
		Map<EObject, Change> changeLocations = new HashMap<>();
		
		for (Change change : changes) {
			Change existingChange = changeLocations.get(change.getLocation());
			
			if (existingChange == null) {
				changeLocations.put(change.getLocation(), change);
			} else {
				existingChange.setQuantification(existingChange.getQuantification() + change.getQuantification());
				
				// e.g. delete != add or modify != delete ...
				if (existingChange.getType() != change.getType()) {
					existingChange.setType(ChangeType.MODIFY);
				}
			}
		}
		
		return changeLocations.values();
	}

	private EObject findLocation(Set<EClass> changeLocationTypes, Change change) {
		if (changeLocationTypes.contains(change.eClass())) {
			return change.getLocation();
		} else {
			// find parent matching container:
			EObject location = change.getLocation();
			EObject container = location.eContainer();
			
			while (container != null) {
				if (changeLocationTypes.contains(container.eClass())) {
					return container;
				} else {
					location = container;
					container = container.eContainer();
				}
			}
			return location;
		}
	}

}
