package org.sidiff.bug.localization.dataset.model;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.sidiff.bug.localization.dataset.history.model.History;

public class DataSet {

	private String name;
	
	private String repositoryHost;
	
	private String repositoryPath;
	
	private String bugtrackerHost;
	
	private List<String> bugtrackerProducts;
	
	private String timestamp;
	
	private History history;
	
	public DataSet() {
	}
	
	public DataSet(String repositoryHost, String repositoryPath, String bugtrackerHost, List<String> bugtrackerProduct) {
		this.repositoryHost = repositoryHost;
		this.repositoryPath = repositoryPath;
		this.bugtrackerHost = bugtrackerHost;
		this.bugtrackerProducts = bugtrackerProduct;
		
		// cut '.git' from last URL path segment:
		this.name = repositoryPath.substring(
				repositoryPath.lastIndexOf("/") + 1,
				repositoryPath.lastIndexOf("."));
	}
	
	public String createTimestamp() {
		DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneId.systemDefault());
		this.timestamp = timeFormat.format(Instant.now());
		return timestamp;
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

	public List<String> getBugtrackerProducts() {
		return bugtrackerProducts;
	}

	public void setBugtrackerProduct(List<String> bugtrackerProduct) {
		this.bugtrackerProducts = bugtrackerProduct;
	}
	
	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
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
				+ ", bugtrackerHost=" + bugtrackerHost + ", bugtrackerProduct=" + bugtrackerProducts + ", timestamp="
				+ timestamp + ", history=" + history + "]";
	}

}
