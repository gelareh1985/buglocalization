package org.sidiff.bug.localization.dataset.changes;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.sidiff.bug.localization.dataset.history.model.changes.FileChange;

public class ChangeResolver {

	private String projectName = "";
	
	private Map<String, FileChange> fileToChangeIndex;

	// TODO: More fine grained change locations...
//	private Map<FileChange, NavigableMap<Integer, LineChange>> lineToChangeIndex;
	
	public ChangeResolver(Collection<FileChange> fileChanges) {
		this.fileToChangeIndex = new HashMap<>();
//		this.lineToChangeIndex = new HashMap<>();
		
		for (FileChange fileChange : fileChanges) {
			fileToChangeIndex.put(fileChange.getLocation().toString().replace("\\", "/"), fileChange);

			// TODO: More fine grained change locations...
//			if (!fileChange.getLines().isEmpty()) {
//				NavigableMap<Integer, LineChange> lineChangesOfFile = new TreeMap<Integer, LineChange>();
//				lineToChangeIndex.put(fileChange, lineChangesOfFile);
//				
//				for (LineChange lineChanges : fileChange.getLines()) {
//					lineChangesOfFile.put(lineChanges.getBeginA(), lineChanges);
//				}
//			}
		}
	}
	
	public String getProjectName() {
		return projectName;
	}
	
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	
	public FileChange getFileChange(String path) {
		return fileToChangeIndex.get(projectName + path.replace("\\", "/"));
	}
	
	// TODO: More fine grained change locations...
//	public LineChange getLineChange(FileChange fileChange, int line) {
//		 NavigableMap<Integer, LineChange> lineChangesOfFile = lineToChangeIndex.get(fileChange);
//		 
//		 if (lineChangesOfFile != null) {
//			 Map.Entry<Integer, LineChange> entry = lineChangesOfFile.floorEntry(line);
//			 
//			 if ((entry != null) && (line <= entry.getValue().getEndA())) {
//				 return entry.getValue();
//			 }
//		 }
//		
//		return null;
//	}
}
