package org.sidiff.bug.localization.dataset.model;

import org.sidiff.bug.localization.dataset.history.model.History;

public class DataSet {

	private String name;
	
	private String repositoryHost;
	
	private String repositoryPath;
	
	private String bugtrackerHost;
	
	private String bugtrackerProduct;
	
	private History history;
	
	public DataSet() {
	}
	
	public DataSet(String repositoryHost, String repositoryPath, String bugtrackerHost, String bugtrackerProduct) {
		this.repositoryHost = repositoryHost;
		this.repositoryPath = repositoryPath;
		this.bugtrackerHost = bugtrackerHost;
		this.bugtrackerProduct = bugtrackerProduct;
		
		// cut '.git' from last URL path segment:
		this.name = repositoryPath.substring(
				repositoryPath.lastIndexOf("/") + 1,
				repositoryPath.lastIndexOf("."));
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRepositoryHost() {
		return repositoryHost;
	}

	public void setRepositoryHost(String repositoryHost) {
		this.repositoryHost = repositoryHost;
	}

	public String getRepositoryPath() {
		return repositoryPath;
	}

	public void setRepositoryPath(String repositoryPath) {
		this.repositoryPath = repositoryPath;
	}

	public String getBugtrackerHost() {
		return bugtrackerHost;
	}

	public void setBugtrackerHost(String bugtrackerHost) {
		this.bugtrackerHost = bugtrackerHost;
	}

	public String getBugtrackerProduct() {
		return bugtrackerProduct;
	}

	public void setBugtrackerProduct(String bugtrackerProduct) {
		this.bugtrackerProduct = bugtrackerProduct;
	}

	public History getHistory() {
		return history;
	}

	public void setHistory(History history) {
		this.history = history;
	}

	@Override
	public String toString() {
		return "DataSet [name=" + name + ", repositoryHost=" + repositoryHost + ", repositoryPath=" + repositoryPath
				+ ", bugtrackerHost=" + bugtrackerHost + ", bugtrackerProduct=" + bugtrackerProduct + ", history="
				+ history + "]";
	}
}
