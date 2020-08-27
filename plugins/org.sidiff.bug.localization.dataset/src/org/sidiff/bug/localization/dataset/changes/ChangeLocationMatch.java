package org.sidiff.bug.localization.dataset.changes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import org.eclipse.emf.ecore.EObject;
import org.sidiff.bug.localization.dataset.Activator;
import org.sidiff.bug.localization.dataset.history.model.changes.LineChange;
import org.sidiff.bug.localization.dataset.systemmodel.Change;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModelFactory;

public class ChangeLocationMatch {
	
	private EObject matchingCandidate;
	
	private int matchingCandidateDistance = Integer.MAX_VALUE;
	
	private List<EObject> matchingLocations;
	
	private LineChange codeChange;

	public ChangeLocationMatch(LineChange codeChange) {
		this.codeChange = codeChange;
		this.matchingLocations = new ArrayList<>(1);
	}
	
	public void match(int startOffset, int endOffset, int startLine, int endLine, EObject location) {
		if (!matchLine(location, startLine)) {
			matchLine(location, endLine);
		}
	}

	private boolean matchLine(EObject location, int line) {
		
		if (location == null) {
			return false;
		}
		
		// Is in range?
		if ((line >= codeChange.getBeginA()) && (line <= codeChange.getEndA())) {
			matchingLocations.add(location);
			return true;
		
		// find closest location:
		} else if (matchingLocations.isEmpty()) {
			int distanceAbove = codeChange.getBeginA() - line;
			
			if ((distanceAbove > 0) && (distanceAbove < matchingCandidateDistance)) {
				this.matchingCandidateDistance = distanceAbove;
				this.matchingCandidate = location;
			} else {
				int distanceBelow = line - codeChange.getEndA();
				
				if ((distanceBelow > 0) && (distanceBelow < matchingCandidateDistance)) {
					this.matchingCandidateDistance = distanceBelow;
					this.matchingCandidate = location;
				}
			}
		}
		
		return false;
	}
	
	public List<EObject> getLocationMatches() {
		if (!matchingLocations.isEmpty()) {
			return matchingLocations; // found exact matches
		} else {
			if (matchingCandidate != null) {
				if (Activator.getLogger().isLoggable(Level.FINE)) {
					Activator.getLogger().log(Level.FINE, "No exact location match found - closest match: " + matchingCandidate);
				}
				return Collections.singletonList(matchingCandidate); // closest match
			} else {
				return Collections.emptyList(); // no match
			}
		}
	}
	
	public List<Change> getChangeMatches() {
		List<Change> matchingChanges = new ArrayList<>();
		
		for (EObject location : getLocationMatches()) {
			Change change = SystemModelFactory.eINSTANCE.createChange();
			change.setType(ChangeLocationMatcher.LINE_CHANGE_MAP.get(codeChange.getType()));
			change.setLocation(location);
			change.setQuantification(1);
			matchingChanges.add(change);
		}
		
		return matchingChanges;
	}

}
