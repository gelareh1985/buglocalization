package org.sidiff.bug.localization.dataset.changes.model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileChange {

	/** General type of change a single file-level patch describes. */
	public enum FileChangeType {
		
		/** Add a new file to the project */
		ADD,

		/** Modify an existing file in the project (content and/or mode) */
		MODIFY,

		/** Delete an existing file from the project */
		DELETE,

		/** Rename an existing file to a new location */
		RENAME,

		/** Copy an existing file to a new location, keeping the original */
		COPY;
	}
	
	private FileChangeType type;
	
	private Path location;
	
	private List<LineChange> lines;

	public FileChange() {
	}
	
	public FileChange(FileChangeType fileChangeType, Path file) {
		this.type = fileChangeType;
		this.location = file;
	}

	public FileChangeType getType() {
		return type;
	}

	public void setType(FileChangeType fileChangeType) {
		this.type = fileChangeType;
	}

	public Path getLocation() {
		return location;
	}

	public void setLocation(Path file) {
		this.location = file;
	}

	public List<LineChange> getLines() {
		
		if (lines == null) {
			this.lines = new ArrayList<>();
		}
		
		return lines;
	}

	public void setLines(List<LineChange> changes) {
		this.lines = changes;
	}

	@Override
	public String toString() {
		return "Change [changeType=" + type 
				+ ", file=" + location 
				+ ", changes.size=" + ((lines != null) ? lines.size() : 0) + "]";
	}
}
