package org.sidiff.bug.localization.dataset.modelview.model;

import java.nio.file.Path;

public class SystemModel {

	private String name;
	
	private String description;
	
	private Path model;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Path getModel() {
		return model;
	}

	public void setModel(Path model) {
		this.model = model;
	}
	
}
