package org.sidiff.bug.localization.dataset.systemmodel.views;

public class ViewDescription {

	private String name;
	
	private String description;
	
	private String viewKind;

	public ViewDescription(String name, String description, String viewKind) {
		this.name = name;
		this.description = description;
		this.viewKind = viewKind;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getViewKind() {
		return viewKind;
	}
}
