package org.sidiff.bug.localization.dataset.changes;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.modisco.java.Package;
import org.sidiff.bug.localization.dataset.Activator;
import org.sidiff.bug.localization.dataset.history.model.changes.FileChange;
import org.sidiff.bug.localization.dataset.history.model.changes.FileChange.FileChangeType;
import org.sidiff.bug.localization.dataset.history.model.changes.LineChange;
import org.sidiff.bug.localization.dataset.history.model.changes.LineChange.LineChangeType;
import org.sidiff.bug.localization.dataset.history.model.changes.util.FileChangeFilter;
import org.sidiff.bug.localization.dataset.systemmodel.Change;
import org.sidiff.bug.localization.dataset.systemmodel.ChangeType;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModelFactory;

public class ChangeLocationMatcher {
	
	public static final Map<LineChangeType, ChangeType> LINE_CHANGE_MAP = new HashMap<>();
	
	static {
		LINE_CHANGE_MAP.put(LineChangeType.DELETE, ChangeType.DELETE);
		LINE_CHANGE_MAP.put(LineChangeType.EMPTY, ChangeType.DELETE);
		LINE_CHANGE_MAP.put(LineChangeType.INSERT, ChangeType.ADD);
		LINE_CHANGE_MAP.put(LineChangeType.REPLACE, ChangeType.MODIFY);
	}
	
	public static final Map<FileChangeType, ChangeType> FILE_CHANGE_MAP = new HashMap<>();
	
	static {
		FILE_CHANGE_MAP.put(FileChangeType.ADD, ChangeType.ADD);
		FILE_CHANGE_MAP.put(FileChangeType.COPY, ChangeType.ADD);
		FILE_CHANGE_MAP.put(FileChangeType.DELETE, ChangeType.DELETE);
		FILE_CHANGE_MAP.put(FileChangeType.MODIFY, ChangeType.MODIFY);
		FILE_CHANGE_MAP.put(FileChangeType.RENAME, ChangeType.MODIFY);
	}

	private String projectName;
	
	private Map<String, List<ChangeLocationMatch>> fileToChangeIndex;
	
	private Map<FileChange, Package> addedFiles;
	
	public ChangeLocationMatcher(String projectName, Collection<FileChange> fileChanges, FileChangeFilter fileChangeFilter) {
		this.projectName = projectName;
		this.fileToChangeIndex = new HashMap<>();
		this.addedFiles = new HashMap<>();
		
		for (FileChange fileChange : fileChanges) {
			if (!fileChangeFilter.filter(fileChange)) {
				if (fileChange.getLocation().startsWith(projectName)) {
					if (fileChange.getType().equals(FileChangeType.ADD)) {
						addedFiles.put(fileChange, null);
					} else {
						String path = fileChange.getLocation().toString().replace("\\", "/");
						List<ChangeLocationMatch> changeLocationMatches = new ArrayList<>(fileChange.getLines().size());
						
						for (LineChange lineChange : fileChange.getLines()) {
							changeLocationMatches.add(new ChangeLocationMatch(lineChange));
						}
						
						fileToChangeIndex.put(path, changeLocationMatches);
					}
				}
			} else {
				if (Activator.getLogger().isLoggable(Level.INFO)) {
					Activator.getLogger().log(Level.INFO, "File Change Filtered: " + fileChange);
				}
			}
		}
	}

	public void match(String path, int startOffset, int endOffset, int startLine, int endLine, EObject location) {
		String matchingPath = projectName + path.replace("\\", "/");
		List<ChangeLocationMatch> changeLocationMatches = fileToChangeIndex.get(matchingPath);
		
		if (changeLocationMatches != null) {
			for (ChangeLocationMatch locationMatch : changeLocationMatches) {
				locationMatch.match(startOffset, endOffset, startLine, endLine, location);
			}
		}
		
		// Match added file to (closest) parent, existing package:
		if (location instanceof Package) {
			for (Entry<FileChange, Package> addedFile : addedFiles.entrySet()) {
				Path fileChangePath = addedFile.getKey().getLocation();
				Path packagePath = Paths.get(matchingPath).getParent();
				
				Package containerPackage = addedFile.getValue();
				
				if ((containerPackage == null) || (isParent(containerPackage, (Package) location))) {
					if (fileChangePath.startsWith(packagePath)) {
						addedFile.setValue((Package) location);
					}
				}
			}
		}
	}
	
	private boolean isParent(Package parent, Package child) {
		EObject container = child.eContainer();
		
		while (container != null) {
			if (container == parent) {
				return true;
			} else {
				container = container.eContainer();
			}
		}
		
		return false;
	}

	public List<Change> getChanges() {
		List<Change> changeLocations = new ArrayList<>();
		
		for (List<ChangeLocationMatch> matches : fileToChangeIndex.values()) {
			for (ChangeLocationMatch match : matches) {
				changeLocations.addAll(match.getChangeMatches());
			}
		}
		
		// Match added file to (closest) parent, existing package:
		for (Entry<FileChange, Package> addedFile : addedFiles.entrySet()) {
			FileChange fileChange = addedFile.getKey();
			Package containerPackage = addedFile.getValue();
			
			if (containerPackage != null) {
				Change change = SystemModelFactory.eINSTANCE.createChange();
				change.setType(FILE_CHANGE_MAP.get(fileChange.getType()));
				change.setLocation(containerPackage);
				
				if (!fileChange.getLines().isEmpty()) {
					assert (fileChange.getLines().size() == 1);
					change.setQuantification(fileChange.getLines().get(0).getEndB());
				} else {
					change.setQuantification(1);
				}
				
				changeLocations.add(change);
			} else {
				if (Activator.getLogger().isLoggable(Level.WARNING)) {
					Activator.getLogger().log(Level.WARNING, "Change location package not found for: " + fileChange);
				}
			}
		}
		
		return changeLocations;
	}
}
